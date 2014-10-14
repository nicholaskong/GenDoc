/*****************************************************************************
 * Copyright (c) 2012 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Anne Haugommard (Atos) anne.haugommard@atos.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.gendoc.documents;

import java.util.Set;

import org.eclipse.gendoc.services.IService;

/**
 * Service used for additional resources
 */
public interface IMimeHtmlService extends IService {

	/**
	 * Convert a classic html to a mime html usable by mht file.
	 * 
	 * @param sourceHtml
	 * @return finalHtml
	 */
	public String convertToMimeHtml(String sourceHtml);

	/**
	 * Get extentions find in html to add them to content type file.
	 * 
	 * @return extenstionSet
	 */
	public Set<String> getFileExtensions();

	/**
	 * Get begin of mime html file
	 * 
	 * @return
	 */
	public String getBeginPart();

	/**
	 * Get end of mime html file
	 * 
	 * @return
	 */
	public String getEndPart();
}
