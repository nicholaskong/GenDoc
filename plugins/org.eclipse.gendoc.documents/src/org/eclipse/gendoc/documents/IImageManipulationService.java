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

package org.eclipse.gendoc.documents;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.gendoc.services.exception.AdditionalResourceException;

public interface IImageManipulationService {

	/**
	 * Returns image dimension of given image
	 * @param uri
	 * @return
	 * @throws IOException
	 * @throws AdditionalResourceException
	 */
	public ImageDimension getImageDimension(String uri) throws IOException, AdditionalResourceException;
	
	/**
	 * Rename an image with a given extension to another extension
	 * @param extension
	 * @return
	 */
	public String renameExtension(String extension);
	
	/**
	 * Transform the given image if needed
	 * @param path
	 */
	public void transform(IPath path) ;
	
}
