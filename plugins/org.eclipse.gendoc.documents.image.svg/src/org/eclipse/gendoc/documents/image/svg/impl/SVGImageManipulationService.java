/*****************************************************************************
 * Copyright (c) 2016 Atos Origin.
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
package org.eclipse.gendoc.documents.image.svg.impl;

import java.io.IOException;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.gendoc.documents.IImageManipulationService;
import org.eclipse.gendoc.documents.ImageDimension;
import org.eclipse.gendoc.services.exception.AdditionalResourceException;
import org.w3c.dom.svg.SVGDocument;

public class SVGImageManipulationService implements IImageManipulationService {

	private static int NB_PIXEL_PER_INCH = 96 ;
	private static int NB_PIXEL_PER_CM = 38 ;
	
	public ImageDimension getImageDimension(String imagePath) throws IOException,
			AdditionalResourceException {
		String uri = "file:/" + imagePath ;
		if (uri == null || !uri.toLowerCase().endsWith("svg")){
			throw new AdditionalResourceException("Cannot get dimension of image '"+uri+"'.");
		}
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		try {
			SVGDocument doc = f.createSVGDocument(uri);
			String width = doc.getDocumentElement().getAttributeNS(null,"width");
			String height = doc.getDocumentElement().getAttributeNS(null,"height");
			
			ImageDimension result = new ImageDimension();
			result.setWidth(100);
			result.setHeight(100);
			try {
				if (height.endsWith("in")){
					height = height.replace("in", "");
					double dbHeight = Double.valueOf(height);
					result.setHeight(dbHeight*NB_PIXEL_PER_INCH);
				} else if (height.endsWith("cm")){
					height = height.replace("cm", "");
					double dbHeight = Double.valueOf(height);
					result.setHeight(dbHeight*NB_PIXEL_PER_CM);
				} else{
					result.setHeight(Double.valueOf(height));
				}
				if (width.endsWith("in")){
					width = width.replace("in", "");
					double dbWidth = Double.valueOf(width);
					result.setWidth(dbWidth*NB_PIXEL_PER_INCH);
				} else if (width.endsWith("cm")){
					width = width.replace("cm", "");
					double dbWidth = Double.valueOf(width);
					result.setWidth(dbWidth*NB_PIXEL_PER_CM);
				} else{
					result.setWidth(Double.valueOf(width));
				}
				
			}
			catch (NumberFormatException e){
				
			}
			return result;
			
		} catch (IOException e) {
			throw new AdditionalResourceException("Cannot load image '"+uri+"'.", e);
		}
	}
	
	public String renameExtension(String extension) {
		return extension;
	}

	public void transform(IPath path) {
		//nothing to transform here
	}

}
