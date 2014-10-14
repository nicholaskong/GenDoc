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

import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.gendoc.tags.handlers.impl.context.EMFModelLoaderService;
import org.eclipse.papyrus.infra.core.Activator;
import org.eclipse.papyrus.infra.core.resource.AbstractBaseModel;
import org.eclipse.papyrus.infra.core.resource.IModel;
import org.eclipse.papyrus.infra.core.resource.ModelIdentifiers;
import org.eclipse.papyrus.infra.core.resource.ModelMultiException;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.resource.sasheditor.SashModel;
import org.eclipse.papyrus.infra.core.services.ExtensionServicesRegistry;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationModel;
import org.eclipse.papyrus.uml.tools.model.UmlModel;


/**
 * This model loader service handles correctly Papyrus model sets
 * 
 * @author vhemery
 */
public class PapyrusModelLoaderService extends EMFModelLoaderService
{
	
	URI modelURI;
	
	
	public PapyrusModelLoaderService ( URI modelUri){
        this.modelURI = modelUri;
	}
	

    /**
     * Construct a correct Papyrus model set
     */
    @Override
    protected ResourceSet constructResourceSet()
    {
        if (resourceSet == null)
        {
            try {
            	resourceSet = getPapyrusModelSet(modelURI);
            	
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        if( resourceSet == null){
        	resourceSet = super.constructResourceSet();
        }
        return resourceSet;
    }


	/**
     * Load the resource correctly and return it
     * 
     * @param uri resource URI
     * @return loaded resource
     */
    protected Resource getResource(URI uri)
    {
        ResourceSet resourceSet = getResourceSet();
        if (resourceSet.getResource(uri, false) != null)
        {
            // already loaded
            return resourceSet.getResource(uri, false);
        }
        if (resourceSet instanceof ModelSet)
        {
            ModelSet modelSet = (ModelSet) resourceSet;
            String ext = uri.fileExtension();

            String fileName = uri.toFileString();
            IFile file = null;
            // get IFile to load in model set
            if (fileName != null)
            {
                file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));
            }
            else if (uri.toString().startsWith("platform:/resource")) { //$NON-NLS-1$
                String path = uri.toString().substring("platform:/resource".length()); //$NON-NLS-1$
                IResource workspaceResource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
                if (workspaceResource instanceof IFile)
                {
                    file = (IFile) workspaceResource;
                }
            }
            if (file != null)
            {
                // try loading the model
                String modelId = getModelId(ext);
                if (modelId != null)
                {
                    try
                    {
                    	modelSet = getPapyrusModelSet(uri);
                		
                		// After model loading, get model
                        IModel model = modelSet.getModel(modelId);
                        return ((AbstractBaseModel) model).getResource();
                    } catch (ServiceException e) 
                    {
                    	Activator.log.error(e.getMessage(), e);
					}
                }
            }
        }
        // could not load it correctly, use default way.
        return super.getResource(uri);
    }

	private ModelSet getPapyrusModelSet(URI modelUri) throws ServiceException  {
		ModelSet modelSet = null;
		
		// Start services registry and ModelSet service.
		// Use service registry to get a new model set
		ServicesRegistry servicesRegistry = new ExtensionServicesRegistry(
				Activator.PLUGIN_ID);
		try{
			servicesRegistry.startServices(Collections.singletonList(ModelSet.class
				.getName()));
			modelSet = servicesRegistry.getService(ModelSet.class);
			if (modelSet != null && modelUri != null) {
				// load models
				modelSet.loadModels(modelUri);
			}
			// start remaining services
			servicesRegistry.startRegistry();
		
        } catch (ModelMultiException e) {
			try {
				// with the ModelMultiException it is still possible to open the
				// editors that's why the service registry is still started
				servicesRegistry.startRegistry();
				
			} catch (ServiceException e1) {
				//Do nothing
			}
		} catch (ServiceException e) {
			//Do nothing
		}
		return modelSet;
	}

    /**
     * Get the model identifiers which shall be imported with this id
     * 
     * @param modelId model identifier requested
     * @return list of model identifiers to load at the same time
     */
    protected ModelIdentifiers getModelIdsToImport(String modelId)
    {
        if (SashModel.MODEL_ID.equals(modelId) || NotationModel.MODEL_ID.equals(modelId) || UmlModel.MODEL_ID.equals(modelId))
        {
            return new ModelIdentifiers(NotationModel.MODEL_ID, UmlModel.MODEL_ID);
        }
        // else other model added later
        return new ModelIdentifiers(modelId);
    }

    /**
     * Get the model resource id corresponding to a file extension
     * 
     * @param ext file extension
     * @return model id in model set
     */
    protected String getModelId(String ext)
    {
        if (SashModel.MODEL_FILE_EXTENSION.equals(ext))
        {
            return SashModel.MODEL_ID;
        }
        else if (NotationModel.NOTATION_FILE_EXTENSION.equals(ext))
        {
            return NotationModel.MODEL_ID;
        }
        else if (UmlModel.UML_FILE_EXTENSION.equals(ext))
        {
            return UmlModel.MODEL_ID;
        }
        else
        {
            return "org.eclipse.papyrus.resource.additional";
        }
    }
    

}