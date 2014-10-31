/*****************************************************************************
 * Copyright (c) 2010 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Caroline Bourdeu d'Aguerre (Atos Origin) caroline.bourdeudaguerre@atosorigin.com - Initial API and implementation
 *  Tristan Faure (Atos) tristan.faure@atos.net - refactoring
 *  Anne Haugommard (Atos) anne.haugommard@atos.net - special character handling
 *
 *****************************************************************************/
package org.eclipse.gendoc.script.acceleo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.gendoc.m2t.IFragmentableM2TProcessor;
import org.eclipse.gendoc.m2t.model.Fragment;
import org.eclipse.gendoc.script.acceleo.impl.FragmentScriptGenerator;
import org.eclipse.gendoc.script.acceleo.impl.ScriptGenerator;
import org.eclipse.gendoc.script.services.IModuleManagerService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IProgressMonitorService;
import org.eclipse.gendoc.services.exception.ElementNotFoundException;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.GenerationException;
import org.eclipse.gendoc.services.exception.ModelNotFoundException;
import org.eclipse.gendoc.services.exception.ParsingException;

/**
 * The Class AcceleoM2TProcess.
 * 
 * @author cbourdeu
 */
public class AcceleoM2TProcess implements IFragmentableM2TProcessor
{
    /**
     * Execute the given script and return the generated text.
     * 
     * @param script to execute as a String
     * @param context the context
     * 
     * @return the generated text as a String
     * 
     * @throws ElementNotFoundException the element not found exception
     * @throws ParsingException the parsing exception
     * @throws ModelNotFoundException the model not found exception
     * @throws GenerationException Other exception thrown during generation
     */
    public String runScript(String script, EObject element) throws ModelNotFoundException, ElementNotFoundException, ParsingException, GenerationException
    {
        return doRunScript(script, element, new ScriptGenerator(getScriptPatterns()));
    }

    protected String doRunScript(String script, EObject element, ScriptGenerator scriptGenerator) throws GenerationException
    {
        String result = "";
        String formattedScript = "";
        try
        {
        	formattedScript = scriptGenerator.formatScript(element, script);
            result = generate(element, formattedScript);
        }
        catch (IOException e)
        {
            throw new GenerationException("Error during generation of the following script : \n" + script, e);
        }
        catch (Exception e)
        {
            if (e instanceof IndexOutOfBoundsException && "index: 0, size: 1".equals(e.getMessage()))
            {
                throw new GenerationException("Error while trying to access index 0 of list.\n TIP : In Acceleo, index start at 1 and not 0.");
            }
            throw new GenerationException("Error during generation of Gendoc script :" + e.getMessage(), e);
        }

        return result;
    }


    protected String generate(EObject element, String formattedScript) throws IOException, ParsingException, GenerationException
    {
        IModuleManagerService service = GendocServices.getDefault().getService(IModuleManagerService.class);
        Generator generator = new Generator(element, formattedScript, service.getModuleURIs(element));
        IProgressMonitorService progressMonitor = GendocServices.getDefault().getService(IProgressMonitorService.class);
        String result = generator.doGenerate(progressMonitor.getDelegatingMonitor());
        // Unload
        unload(generator.getResourceSet());
        return result;
    }

    private void unload(ResourceSet resourceSet)
    {
        for (Resource r : resourceSet.getResources())
        {
            try
            {
                // the Gendocbundle:// uris shall not be unloaded
                if (!r.getURI().scheme().equals(Messages.GendocResourceFactory_BUNDLE_URI_SCHEME))
                {
                    r.unload();
                }
            }
            catch (Exception e)
            {
                // Sometimes the unload throw an exception
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.handlers.IM2TProcess#getAllAvailableBundles()
     */
    public List<String> getAllAvailableBundles()
    {
        return ServicesExtension.getInstance().getServices();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.handlers.IM2TProcess#getScriptPatterns()
     */
    public List<Pattern> getScriptPatterns()
    {
        // A pattern for elements matching : [XXXX] or [/XXXX] or [XXXX/]
        Pattern braquets = Pattern.compile("(\\[[^\\[\\]]*\\])|(\\[/[^\\[\\]]*\\])");
        List<Pattern> patterns = new ArrayList<Pattern>(1);
        patterns.add(braquets);
        return patterns;
    }

    public String runFragmentScript(EObject element, Fragment fragment) throws GenDocException
    {
        FragmentScriptGenerator generator = new FragmentScriptGenerator(getScriptPatterns(), fragment);
        return generator.formatScript(element, fragment.getScriptValue());
    }

    public void clear()
    {
        // do nothing
    }

}
