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

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gendoc.m2t.model.Argument;
import org.eclipse.gendoc.m2t.model.Fragment;
import org.eclipse.gendoc.script.acceleo.ServicesExtension;
import org.eclipse.gendoc.services.exception.GenDocException;

/**
 * A script generator specific to fragments
 * 
 * @author tfaure
 * s
 */
public class FragmentScriptGenerator extends ScriptGenerator
{

    private final Fragment f;

    public FragmentScriptGenerator(List<Pattern> patterns, Fragment f)
    {
        super(patterns);
        this.f = f;
    }

    @Override
    protected void addImports(EObject element, StringBuilder resultScript) throws GenDocException
    {
        for (String bundle : getImportedBundlesForScript())
        {
            addImport(element, resultScript, bundle);
        }
        for (String fragmentImported : f.getImportedFragments())
        {
            super.addImport(element, resultScript, fragmentImported);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.script.acceleo.impl.ScriptGenerator#addModuleImport()
     * 
     * @override
     */
    protected boolean addModuleImport()
    {
        // a fragment can import other fragments
        return true;
    }

    @Override
    protected void appendModuleName(StringBuilder resultScript)
    {
        // the name of the module is the name of the fragment
        resultScript.append(f.getName());
    }

    @Override
    protected String getTemplateName()
    {
        return f.getName();
    }

    @Override
    protected List<String> getImportedBundlesForScript()
    {
    	List<String> importedBundles =  new LinkedList<String>(f.getImportedBundles());
        for (String bundleName : ServicesExtension.getInstance().getBundlesImportedByDefault())
        {
            if (!importedBundles.contains(bundleName))
            {
                importedBundles.add(bundleName);
            }
        }
        return importedBundles;
    }

    @Override
    protected boolean generatesFile()
    {
        // no temporary file is needed
        return false;
    }

    @Override
    protected String getTemplateArguments(EObject element)
    {
        StringBuffer result = new StringBuffer();
        boolean flag = false;
        for (Argument a : f.getArguments())
        {
            if (flag)
            {
                result.append(", ");
            }
            result.append(a.getName()).append(" : ").append(a.getType());
            flag = true;
        }
        return result.toString();
    }

    @Override
    protected String getTemplateVisibility()
    {
        // no matter ScriptGenerator is modified this template must be public
        return "public";
    }

}
