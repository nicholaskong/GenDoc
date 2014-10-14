/*****************************************************************************
 * Copyright (c) 2011 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Vincent Hemery (Atos Origin) - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.bundle.acceleo.papyrus.service;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.ModelNotFoundException;
import org.eclipse.gendoc.tags.handlers.IEMFModelLoaderService;
import org.eclipse.gendoc.tags.handlers.impl.context.ContextService;
import org.eclipse.gendoc.tags.handlers.impl.context.EMFModelLoaderService;
import org.eclipse.papyrus.infra.core.resource.sasheditor.DiModel;

/**
 * This context service ensures a correct resource set is used for Papyrus
 * 
 * @author vhemery
 */
public class PapyrusContextService extends ContextService
{

    /**
     * Set model path and check if it is a Papyrus model
     */
    @Override
    public void setModel(String modelPath) throws ModelNotFoundException
    {
        if (isPapyrusModel(modelPath))
        {
        	EMFModelLoaderService loader = GendocServices.getDefault().getService(IEMFModelLoaderService.class);
        	// prevent the multi creation of Resource Set.
        	if (!(loader instanceof PapyrusModelLoaderService)){
        		EMFModelLoaderService emfService = new PapyrusModelLoaderService(getDiURI(modelPath));
        		emfService.setServiceId("EMFModelLoaderService");//$NON-NLS-1$
        		GendocServices.getDefault().setService(IEMFModelLoaderService.class, emfService);
        	}
        }
        super.setModel(modelPath);
    }

    /**
     * Check if model path corresponds to a Papyrus model
     * 
     * @param modelPath model path to check
     * @return true if path for Papyrus model
     */
    protected boolean isPapyrusModel(String modelPath)
    {
        
        return exists(getDiURI(modelPath));
    }

	public URI getModelURI(String modelPath) {
		URI modelUri = null;
    	try {
    		modelUri = URI.createURI(modelPath);
    	}
        catch (IllegalArgumentException e){
        }
		if (!isValid(modelUri))
		{
			modelUri = URI.createFileURI(modelPath);
		}
		return modelUri;
	}
	
	private URI getDiURI(String modelPath){
		URI diUri = getModelURI(modelPath).trimFileExtension().appendFileExtension(DiModel.MODEL_FILE_EXTENSION);//$NON-NLS-1$
		return diUri;
	}

    /**
     * Returns <b>true</b> only if the URI represents a file and if this file exists.
     * 
     * @param uri uri of file to check existence
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    private boolean exists(URI uri)
    {
        if (uri.isFile())
        {
            return new File(uri.toFileString()).exists();
        }
        else if (EMFPlugin.IS_RESOURCES_BUNDLE_AVAILABLE && uri.isPlatformResource())
        {
            return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(uri.toPlatformString(true))).exists();
        }
        else
        {
            return false;
        }
    }
}