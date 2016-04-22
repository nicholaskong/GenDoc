/*****************************************************************************
 * Copyright (c) 2010 Atos Origin.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Anne Haugommard (Atos Origin) anne.haugommard@atosorigin.com - Initial API and implementation
 * Papa Malick WADE (Atos Origin) papa-malick.wade@atosorigin.com - add catching error with id
 *****************************************************************************/

package org.eclipse.gendoc.services.docx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.gendoc.documents.AbstractImageService;
import org.eclipse.gendoc.documents.ImageDimension;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.services.exception.AdditionalResourceException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

/**
 *
 */
public class DOCXImageService extends AbstractImageService
{

	public void clear ()
	{
	}

	public String manageImage (ITag tag, String imageId, String imagePath, boolean keepH, boolean keepW, boolean maxH, boolean maxW) throws AdditionalResourceException
	{

		String toInsert = "<v:imagedata xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\"" + imageId + "\" o:title=\"\" />";
		
		// Manage the format for Office 2010
		// A new specific tag exists for Office 2010 embedded images
		// for one image, there are two definitions : one for Office 2007 and one for Office 2010
		String toInsertDraw = "<wpc:bg><a:blipFill><a:blip r:embed=\"" + imageId + "\"/><a:stretch><a:fillRect/></a:stretch></a:blipFill></wpc:bg>";

		StringBuffer newTagContent = new StringBuffer(tag.getRawText());
		try{
			Pattern p = Pattern.compile("<v:shape [^<>]*>");
			Matcher m = p.matcher(newTagContent);
			if (m.find())
			{
				int index = m.end();
				newTagContent = newTagContent.insert(index, toInsert);
			}
			
			newTagContent = this.changeImageSize(newTagContent, imagePath, keepW, keepH, maxH, maxW);

			// Insert images for Office 2010 format
			Pattern pDraw = Pattern.compile("<w:drawing>");
			Matcher mDraw = pDraw.matcher(newTagContent);
			if (mDraw.find())
			{
				// determines if there are wpc:bg
				if (newTagContent.toString().contains("(<wpc:bg/>)")){
					newTagContent = new StringBuffer(newTagContent.toString().replaceAll("(<wpc:bg/>)", toInsertDraw));
				}
				else {
					// in some cases the wordprocessor or the user fills some properties of the wpc tag and the document
					// does not contain <wpc:bg/>
					Pattern openWpcTag = Pattern.compile("<wpc:bg>.*</wpc:bg>", Pattern.MULTILINE | Pattern.DOTALL);
					Matcher matcherOpenWpcTag = openWpcTag.matcher(newTagContent.toString());
					if (matcherOpenWpcTag.find()){
						newTagContent = new StringBuffer(matcherOpenWpcTag.replaceAll(toInsertDraw));
					}
					else {
						ILogger logger = GendocServices.getDefault().getService(ILogger.class);
						String tagIdDocx = tag.getAttributes().get(RegisteredTags.ID);
						logger.log("The execution of tag with id '"+ tagIdDocx +"' is maybe erroneous : problems with insertion\n\tcould not replace <wpc:bg>\n" + newTagContent.toString(), IStatus.WARNING);
					}
				}
			}
			
			// Manage size for Office 2010 embedded image definition
			newTagContent = this.changeDrawingSize(newTagContent, imagePath, keepW, keepH, maxH, maxW);
			
		}
		catch (AdditionalResourceException e)
		{
			IGendocDiagnostician diagnostician = (IGendocDiagnostician)GendocServices.getDefault().getService(IGendocDiagnostician.class);
			String tagIdDocx = tag.getAttributes().get(RegisteredTags.ID);

			if (null == tagIdDocx)
			{
				throw e; 
			}
			else
			{ 
				diagnostician.addDiagnostic(Diagnostic.WARNING, "The execution of tag with id '"+tagIdDocx+"' failed : Image cannot not be resized", null);
			}

		}
		return newTagContent.toString().replaceFirst("editas=\"canvas\"", "");
	}

	private StringBuffer changeImageSize (StringBuffer tagContent, String imagePath, boolean keepW, boolean keepH, boolean maxH, boolean maxW) throws AdditionalResourceException
	{
		double width = 0;
		double height = 0;
		Pattern p = Pattern.compile("<v:group [^>]*>");
		Matcher m = p.matcher(tagContent);
		int widthStart = -1;
		int widthEnd = -1;
		int heightStart = -1;
		int heightEnd = -1;
		if (m.find())
		{
			int start = m.start();
			String subString = m.group();
			p = Pattern.compile("width:[^;]*;");
			m = p.matcher(subString);
			if (m.find())
			{
				widthStart = start + m.start();
				widthEnd = start + m.end();
			}
			p = Pattern.compile("height:[^;]*;");
			m = p.matcher(subString);
			if (m.find())
			{
				heightStart = start + m.start();
				heightEnd = start + m.end();
			}
		}
		if (widthStart != -1)
		{
			String widthString = tagContent.substring(widthStart, widthEnd).replaceAll("(width:|;)", "");
			if (widthString.contains("pt"))
			{
				width = this.pointsToPixels(Double.parseDouble(widthString.replace("pt", "")));
			}
			else if (widthString.contains("in"))
			{
				width = this.inchesToPixels(Double.parseDouble(widthString.replace("in", "")));
			}
			else
			{
				width = Double.parseDouble(widthString);
			}
		}
		if (heightStart != -1)
		{
			String heightString = tagContent.substring(heightStart, heightEnd).replaceAll("(height:|;)", "");
			if (heightString.contains("pt"))
			{
				height = this.pointsToPixels(Double.parseDouble(heightString.replace("pt", "")));
			}
			else if (heightString.contains("in"))
			{
				height = this.inchesToPixels(Double.parseDouble(heightString.replace("in", "")));
			}
			else
			{
				height = Double.parseDouble(heightString);
			}
		}

		ImageDimension d = resizeImage(imagePath, width, height, keepH, keepW, maxH, maxW);
		if (heightStart != -1)
		{
			tagContent = tagContent.replace(heightStart, heightEnd, "height:" + d.getHeight() + ";");
		}
		if (widthStart != -1)
		{
			tagContent = tagContent.replace(widthStart, widthEnd, "width:" + d.getWidth() + ";");
		}
		return tagContent;

	}

	private double pointsToPixels (double dim)
	{
		return dim * 96 / 72;
	}
	
	/**
	 * For Office 2010, the dimension is in EMUs
	 * @param dim the value in EMUs
	 * @return the value in pixels
	 */
	private double emuToPixels (double dim)
	{
		return dim / (20 * 635);
	}

	/**
	 * For Office 2010, the dimension is in EMUs
	 * @param dim the value in pixels
	 * @return the value in EMUs
	 */
	private int pixelsToEmu (double dim)
	{
		return new Double(dim * 20 * 635).intValue();
	}

	private StringBuffer changeDrawingSize (StringBuffer tagContent, String imagePath, boolean keepW, boolean keepH, boolean maxH, boolean maxW) throws AdditionalResourceException
	{
		double width = 0;
		double height = 0;
		Pattern p = Pattern.compile("<wp:extent [^>]*>");
		Matcher m = p.matcher(tagContent);
		int widthStart = -1;
		int widthEnd = -1;
		int heightStart = -1;
		int heightEnd = -1;
		if (m.find())
		{
			int start = m.start();
			String subString = m.group();
			p = Pattern.compile("cx=\"[^\"]*\"");
			m = p.matcher(subString);
			if (m.find())
			{
				widthStart = start + m.start();
				widthEnd = start + m.end();
			}
			p = Pattern.compile("cy=\"[^\"]*\"");
			m = p.matcher(subString);
			if (m.find())
			{
				heightStart = start + m.start();
				heightEnd = start + m.end();
			}
		}
		if (widthStart != -1)
		{
			String widthString = tagContent.substring(widthStart + 4, widthEnd - 1);
			width = emuToPixels(Double.parseDouble(widthString));
			
		}
		if (heightStart != -1)
		{
			String heightString = tagContent.substring(heightStart + 4, heightEnd - 1);
			height = emuToPixels(Double.parseDouble(heightString));
		}

		ImageDimension d = resizeImage(imagePath, width, height, keepH, keepW, maxH, maxW);
		
		
		if (heightStart != -1)
		{
			tagContent = tagContent.replace(heightStart, heightEnd, "cy=\"" + pixelsToEmu(d.getHeight()) + "\"");
		}
		if (widthStart != -1)
		{
			tagContent = tagContent.replace(widthStart, widthEnd, "cx=\"" + pixelsToEmu(d.getWidth()) + "\"");
		}
		return tagContent;

	}
}
