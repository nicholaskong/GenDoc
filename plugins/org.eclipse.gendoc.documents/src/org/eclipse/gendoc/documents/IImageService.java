/*****************************************************************************
 * Copyright (c) 2014 Atos.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Anne Haugommard (Atos) anne.haugommard@atos.net - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.documents;

import org.eclipse.gendoc.services.IService;
import org.eclipse.gendoc.services.exception.AdditionalResourceException;
import org.eclipse.gendoc.tags.ITag;

public interface IImageService extends IService
{

	String getFilePath (String imageId) throws AdditionalResourceException;

	String getImageId (String filePath);

	String manageImage (ITag tag, String imageId, String filePath, boolean keepH, boolean keepW, boolean maxH, boolean maxW) throws AdditionalResourceException;

}
