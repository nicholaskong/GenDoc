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
package org.eclipse.gendoc.documents.metadata;

import java.util.List;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.services.IService;
/**
 * The Interface IDocumentMetadataService provides methods to 
 * access the metadata information in a {@link Document}. 
 */
public interface IDocumentMetadataService extends IService {
	
	/**
	 * Gets the metadata properties.
	 *
	 * @param doc a {@link Document} with the the metadata properties.
	 * @return a {@link List} containing the name of the metadata 
	 *         properties. 
	 */
	public List<String> getMetadataProperties(Document doc);
	
	/**
	 * Gets the string representation of the value of a metadata property 
	 * in the document
	 *
	 * @param doc a {@link Document} with the the metadata properties.
	 * @param propertyId a {@link String} identifying metadata property to access
	 * @return the metadata value
	 */
	public String getMetadataValue(Document doc, String propertyId);
	
	/**
	 * Sets the metadata value.
	 *
	 * @param doc a {@link Document} with the the metadata properties.
	 * @param propertyId a {@link String} identifying metadata property to 
	 *                   set the value.
	 * @param value the string representation of the value of the metadata property.
	 */
	public void setMetadataValue(Document doc, String propertyId, String value);
	
	/**
	 * Save the metadata in the document
	 * 
	 * @param doc a {@link Document} with the the metadata properties.
	 */
	public void saveMetadata(Document doc);
}
