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
 *
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gendoc.services.IService;
import org.eclipse.gendoc.services.exception.ElementNotFoundException;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.ModelNotFoundException;

public interface IContextService extends IService
{



    /**
     * Set the model path
     * @param model the model path
     * @throws ModelNotFoundException when model is not found
     */
    void setModel(String model) throws GenDocException;
    
    /**
     * Getter for modelPath
     * @return the model path from the model given in setModel(..) method.
     */
    String getModelPath();
    
	/**
	 * Get model from referenced IEMFModelLoaderService
	 * @return EObject root of the model
	 * @throws ModelNotFoundException if model cannot be found
	 */
	EObject getModel() throws ModelNotFoundException;

	 /**
	  * Set element path to 
	 * @param element the path of the element
	 */
	void setElementPath(String element);
	
    /**
     * @return
     * @throws ModelNotFoundException
     * @throws ElementNotFoundException
     */
    EObject getElement() throws ModelNotFoundException, ElementNotFoundException;

    /**
     * Optional method to set the feature label to use in elementPath
     * default value : "name" 
     * @param featureLabel the feature label
     */
    void setFeatureLabel(String featureLabel);
    
    /**
     * Set the attribute 'searchMetamodels' (default value if not set : false).
     * @param searchMetamodels set to 'true' if multiple metamodels should be searched for
     */
    void setSearchMetamodels(boolean searchMetamodels);

    /**
     * The bundles parameters are given as "bundleName1;bundleName2"
     */
    void setImportedBundles(String attributesBundles);
    
    /**
     * Get the list of bundles to import
     * @return the list of bundle identifiers
     */
    List<String> getImportedBundles();

    /**
     * Getter for searchMetamodels attribute	
     * @return true if multiple meta-models should be searched for. False otherwise.
     */
    boolean isSearchMetamodels();
    
}
