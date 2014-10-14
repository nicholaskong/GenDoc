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
package org.eclipse.gendoc.m2t.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.gendoc.m2t.IFragmentService;
import org.eclipse.gendoc.m2t.IncorrectFragmentException;
import org.eclipse.gendoc.m2t.model.Argument;
import org.eclipse.gendoc.m2t.model.Fragment;
import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.exception.GenDocException;

public class FragmentService extends AbstractService implements IFragmentService
{

    private Map<String, Fragment> fragments = new HashMap<String, Fragment>();

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.m2t.IFragmentService#addFragment(java.lang.String)
     */
    public void addFragment(String name) throws GenDocException
    {
        if (fragments.containsKey(name))
        {
            throw new IncorrectFragmentException(name);
        }
        Fragment f = new Fragment(name);
        fragments.put(name, f);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.m2t.IFragmentService#addArgument(java.lang.String, java.lang.String, java.lang.String)
     */
    public void addArgument(String fragmentName, String argumentName, String argumentType) throws GenDocException
    {
        Fragment f = fragments.get(fragmentName);
        if (f != null)
        {
            Argument a = new Argument(argumentName, argumentType);
            if (f.containsArgument(a.getName()))
            {
                throw new GenDocException("Fragment " + fragmentName + " already contains argument named " + argumentName)
                {
                    private static final long serialVersionUID = 1L;
                };
            }
            f.addArgument(a);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.m2t.IFragmentService#addFragmentScriptContent(java .lang.String, java.lang.String)
     */
    public void addFragmentScriptContent(String fragmentName, String scriptValue)
    {
        Fragment f = fragments.get(fragmentName);
        if (f != null)
        {
            f.setScriptValue(scriptValue);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.m2t.IFragmentService#getAllFragments()
     */
    public List<Fragment> getAllFragments()
    {
        return new LinkedList<Fragment>(fragments.values());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.m2t.IFragmentService#addBundleToFragment(java.lang .String, java.lang.String)
     */
    public void addBundleToFragment(String fragmentName, String bundle)
    {
        Fragment f = fragments.get(fragmentName);
        if (f != null)
        {
            f.addImportedBundle(bundle);
        }
    }

    public void addFragmentImportToFragment(String fragmentName, String fragmentImported)
    {
        Fragment f = fragments.get(fragmentName);
        if (f != null)
        {
            f.addImportedFragment(fragmentImported);
        }

    }

}
