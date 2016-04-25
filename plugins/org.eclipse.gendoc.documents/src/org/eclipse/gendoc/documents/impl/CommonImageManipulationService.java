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

import java.io.IOException;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageContext;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageSessionContext;
import org.eclipse.core.runtime.IPath;
import org.eclipse.gendoc.documents.IImageManipulationService;
import org.eclipse.gendoc.documents.ImageDimension;
import org.eclipse.gendoc.services.exception.AdditionalResourceException;

public class CommonImageManipulationService implements IImageManipulationService {

	public ImageDimension getImageDimension(String imagePath) throws IOException, AdditionalResourceException {
		ImageDimension initialSize = new ImageDimension();
		// READ image and extract size
		ImageManager imageManager = new ImageManager(new DefaultImageContext());
		ImageSessionContext sessionContext = new DefaultImageSessionContext(
				imageManager.getImageContext(), null);

		ImageInfo info;
		try {
			info = imageManager.getImageInfo(imagePath, sessionContext);
			initialSize.setWidth(info.getSize().getWidthPx());
			initialSize.setHeight(info.getSize().getHeightPx());
		} catch (ImageException e) {
			e.printStackTrace();
		}
		return initialSize;
	}

	public String renameExtension(String extension) {
		return extension;
	}

	public void transform(IPath path) {
		//nothing to transform here 
	}

}
