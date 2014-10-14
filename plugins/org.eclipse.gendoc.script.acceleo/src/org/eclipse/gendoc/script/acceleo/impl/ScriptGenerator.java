/*****************************************************************************
 * Copyright (c) 2011 Atos
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Tristan FAURE (Atos) tristan.faure@atos.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.gendoc.script.acceleo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gendoc.script.acceleo.Generator;
import org.eclipse.gendoc.script.acceleo.IFileAndMMRegistry;
import org.eclipse.gendoc.script.acceleo.ServicesExtension;
import org.eclipse.gendoc.script.services.IModuleManagerService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.handlers.IContextService;

/**
 * A class able to generate the script for a given eobject
 * 
 * @author tfaure
 * 
 */
public class ScriptGenerator
{

    private final List<Pattern> patterns;

    public ScriptGenerator(List<Pattern> patterns)
    {
        this.patterns = patterns;
    }

    public String formatScript(EObject element, String script) throws GenDocException
    {
        IContextService contextService = GendocServices.getDefault().getService(IContextService.class);

        StringBuilder resultScript = new StringBuilder();
        insertModule(element, contextService, resultScript);

        final Pattern pattern = getQueryPattern();
        List<String> queries = extractQueries(script, pattern);
        String scriptWithoutQueries = cleanFromQueries(script, pattern);

        addImports(element, resultScript);

        for (String query : queries)
        {
            resultScript.append(query + "\n");
        }
        addTemplate(element, resultScript, scriptWithoutQueries);
        return resultScript.toString();
    }

    protected void addTemplate(EObject element, StringBuilder resultScript, String scriptWithoutQueries)
    {
        resultScript.append("[template ").append(getTemplateVisibility()).append(" ").append(getTemplateName()).append(" (").append(getTemplateArguments(element)).append(")]\n");
        if (generatesFile())
        {
            resultScript.append("[file ('tempFile',false )]\n");
        }
        resultScript.append(cleanInternalScriptContent(scriptWithoutQueries) + "\n");
        if (generatesFile())
        {
            resultScript.append("[/file] \n");
        }
        resultScript.append("[/template]\n");
    }

    /**
     * Determines if [file/] section has to be generated
     * 
     * @return
     */
    protected boolean generatesFile()
    {
        return true;
    }

    protected String getTemplateArguments(EObject element)
    {
        return "internalVar : " + element.eClass().getName();
    }

    protected String getTemplateName()
    {
        return Generator.templateNames;
    }

    protected String getTemplateVisibility()
    {
        return "public";
    }

    protected void addImports(EObject element, StringBuilder resultScript) throws GenDocException
    {
        for (String bundle : getImportedBundlesForScript())
        {
            addImport(element, resultScript, bundle);
        }
        if (addModuleImport())
        {
            IModuleManagerService moduleManager = GendocServices.getDefault().getService(IModuleManagerService.class);
            List<String> modules = moduleManager.getImportedModules(element);
            if (modules != null)
            {
                for (String s : modules)
                {
                    addImport(element, resultScript, s);
                }
            }

        }
    }

    /**
     * Determine if [import/] has to be generated
     * 
     * @return
     */
    protected boolean addModuleImport()
    {
        return true;
    }

    private String cleanInternalScriptContent(String script)
    {
        StringBuffer scriptContent = new StringBuffer();
        Pattern pattern = patterns.get(0);
        Matcher matcher = pattern.matcher(script);
        int start;
        int end;
        int index = 0;
        while (matcher.find())
        {
            start = matcher.start();
            end = matcher.end();
            scriptContent.append(script.substring(index, start));
            String content = script.substring(start, end);
            if (content.contains("&lt;") || content.contains("&gt;"))
            {
                content = content.replaceAll("&lt;", "<");
                content = content.replaceAll("&gt;", ">");
            }
            scriptContent.append(content);
            index = end;
        }
        scriptContent.append(script.substring(index));
        return scriptContent.toString();
    }

    private String cleanFromQueries(String script, Pattern pattern)
    {
        Matcher matcher = pattern.matcher(script);
        return matcher.replaceAll("");
    }

    private Pattern getQueryPattern()
    {
        String regex = Pattern.quote("[") + "query[^]]*" + Pattern.quote("/]");
        return Pattern.compile(regex);
    }

    protected void appendModuleName(StringBuilder resultScript)
    {
        resultScript.append(Generator.templateNames);
    }

    protected void addImport(EObject element, StringBuilder resultScript, String bundle)
    {
        resultScript.append("[import ").append(getBundleNameForImport(element, bundle)).append(" /]\n");
    }

    protected String getBundleNameForImport(EObject eobject, String bundleName)
    {
        return bundleName;
    }

    private List<String> extractQueries(String script, Pattern pattern)
    {
        List<String> queries = new ArrayList<String>();

        Matcher matcher = pattern.matcher(script);
        while (!matcher.hitEnd())
        {
            if (matcher.find())
            {
                String next = matcher.group();
                queries.add(next);
            }
        }
        return queries;
    }

    /**
     * Add all declared importedBundles AND all bundles that are defined as 'importedByDefault'
     * 
     * @return
     */
    protected List<String> getImportedBundlesForScript()
    {
        IContextService context = GendocServices.getDefault().getService(IContextService.class);
        List<String> importedBundles = context.getImportedBundles();
        for (String bundleName : ServicesExtension.getInstance().getBundlesImportedByDefault())
        {
            if (!importedBundles.contains(bundleName))
            {
                importedBundles.add(bundleName);
            }
        }
        return importedBundles;
    }

    protected void insertModule(EObject element, IContextService contextService, StringBuilder resultScript)
    {
        List<String> metaModels = ServicesExtension.getInstance().getDependentMetamodels();
        resultScript.append("[module ");
        appendModuleName(resultScript);
        resultScript.append(" ('");
        addMetaModelsToModule(element, contextService, resultScript, metaModels);
        resultScript.append("')/]\n");
    }

    protected void addMetaModelsToModule(EObject element, IContextService contextService, StringBuilder resultScript, List<String> metaModels)
    {
        if (contextService.isSearchMetamodels())
        {
            // Add metamodels associated to the file containing the element
            IFileAndMMRegistry registry = GendocServices.getDefault().getService(IFileAndMMRegistry.class);
            Collection<String> metamodelsFromElement = registry.getMetamodels(element);
            for (String metaModel : metamodelsFromElement)
            {
                if (!metaModels.contains(metaModel))
                {
                    metaModels.add(metaModel);
                }
            }
        }
        else
        {
            metaModels.add(element.eClass().getEPackage().getNsURI());
        }

        if (metaModels.isEmpty())
        {
            resultScript.append(element.eClass().getEPackage().getNsURI() + "')/]\n");
        }
        else
        {
            Iterator<String> metaModelsIt = metaModels.iterator();
            resultScript.append(metaModelsIt.next());
            while (metaModelsIt.hasNext())
            {
                resultScript.append("', '" + metaModelsIt.next());
            }
        }
    }

}
