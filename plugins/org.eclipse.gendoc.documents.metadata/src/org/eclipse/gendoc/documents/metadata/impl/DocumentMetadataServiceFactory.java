/*****************************************************************************
 * (c) Copyright 2016 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.documents.metadata.impl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.documents.metadata.IDocumentMetadataService;
import org.eclipse.gendoc.services.GendocServices;

/**
 * A factory for creating DocumentMetadataService objects.
 */
public class DocumentMetadataServiceFactory implements IExecutableExtensionFactory, IExecutableExtension {

	/**
	 * Instantiates a new document metadata service factory.
	 */
	public DocumentMetadataServiceFactory() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	}

	/**
	 * It returns a {@link IDocumentMetadataService} for the current IDocumentService.<br/>
	 * 
	 * @return a @{IDocumentMetadataService} if the current {@link IDocumentService} is not 
	 *         implementing @{IDocumentMetadataService}, null otherwise.
	 *         
	 */
	@Override
	public Object create() throws CoreException {
		IDocumentService service = null;
		try {
			service = ((IDocumentService)GendocServices.getDefault().getService(IDocumentService.class));
			if (service instanceof IDocumentMetadataService)
				return service;
						
			return null;
		} catch (Exception e) {
			throw new IllegalStateException("Document service cannot be initialized.", e);
		}
	}

}
