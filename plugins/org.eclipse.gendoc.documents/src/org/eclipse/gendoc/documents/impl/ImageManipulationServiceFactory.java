/*****************************************************************************
 * Copyright (c) 2015 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jean-François Rolland (ATOS) - Initial API and implementation
 * Tristan Faure (ATOS)
 *
 *****************************************************************************/

package org.eclipse.gendoc.documents.impl;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gendoc.documents.IImageManipulationService;
import org.eclipse.gendoc.documents.IImageManipulationServiceFactory;
import org.eclipse.gendoc.services.AbstractService;

public class ImageManipulationServiceFactory extends AbstractService implements IImageManipulationServiceFactory{


	private CommonImageManipulationService commonImageInfoService  = null;
	private HashMap<String, IImageManipulationService> imageInfoServiceMap = new HashMap<String, IImageManipulationService>();

	
	public ImageManipulationServiceFactory(){
		super();
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.gendoc.imageTypes");
		for (IConfigurationElement element : elements)
		{
			String extensionAttribute = element.getAttribute("extension");
			IImageManipulationService service;
			try {
				service = (IImageManipulationService)element.createExecutableExtension("imageManipulationService");
				imageInfoServiceMap.put(extensionAttribute, service);
				
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	public IImageManipulationService getService(String ext){
		IImageManipulationService service = imageInfoServiceMap.get(ext);
		if (service != null){
			return service;
		} else {
			if (commonImageInfoService == null){
				commonImageInfoService = new CommonImageManipulationService();
			}
			return commonImageInfoService;
		}
	}
}
