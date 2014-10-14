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
package org.eclipse.gendoc.m2t;

import java.util.List;

import org.eclipse.gendoc.m2t.model.Fragment;
import org.eclipse.gendoc.services.IService;
import org.eclipse.gendoc.services.exception.GenDocException;

/**
 * A service able to store data for fragments
 * 
 * @author tfaure
 * 
 */
public interface IFragmentService extends IService
{

    /**
     * Add a fragment for script reuse
     * 
     * @param name, the name of the script
     * @throws a GenDocException if a fragment with the same name has been registered
     */
    void addFragment(String name) throws GenDocException;

    /**
     * Add an argument to an already existing tag the fragment must be created previously
     * 
     * @param fragmentName, the name of the fragment
     * @param argumentName, the name of the argument
     * @param argumentType, the type of the argument. It is the task of {@link IFragmentableM2TProcessor} to understand
     *        the type
     */
    void addArgument(String fragmentName, String argumentName, String argumentType) throws GenDocException;

    /**
     * Add the content of the script to a fragment
     * 
     * @param fragmentName, the name of the fragment
     * @param scriptValue, the content of the script
     */
    void addFragmentScriptContent(String fragmentName, String scriptValue);

    /**
     * Returns all the fragments registered in the document template
     * 
     * @return a list of Fragments
     */
    List<Fragment> getAllFragments();

    /**
     * Add an imported bundle to the fragment
     * 
     * @param fragmentName
     * @param bundle
     */
    void addBundleToFragment(String fragmentName, String bundle);

    /**
     * Add an imported bundle to the fragment
     * 
     * @param fragmentName
     * @param bundle
     */
    void addFragmentImportToFragment(String fragmentName, String fragmentImported);

}
