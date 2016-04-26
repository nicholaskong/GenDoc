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

import java.awt.Toolkit;
import java.io.IOException;

import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.AdditionalResourceException;

public abstract class AbstractImageService extends AbstractService implements IImageService
{

	public String getFilePath (String imageId) throws AdditionalResourceException
	{
		IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
		IAdditionalResourceService additionalResourceService = documentService.getAdditionalResourceService();
		String filePath = additionalResourceService.addRunnableResourceToDocument(additionalResourceService.getResourceFolder(), imageId);
		return filePath;
	}

	public String getImageId (String filePath)
	{
		IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
		String imageId = documentService.getAdditionalResourceService().addImage(filePath.replaceAll("\\\\", "/"));
		return imageId;
	}

	protected double cmToPixels (double dim)
	{
		return dim * (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54);
	}

	protected double inchesToPixels (double dim)
	{
		return 2.54 * this.cmToPixels(dim);
	}

	protected double pixelToCm (double dim)
	{
		return dim * 2.54 / (Toolkit.getDefaultToolkit().getScreenResolution());
	}

	protected ImageDimension pixelToCm (ImageDimension dim)
	{
		ImageDimension dim2 = new ImageDimension();
		dim2.setWidth(this.pixelToCm(dim.getWidth()));
		dim2.setHeight(this.pixelToCm(dim.getHeight()));
		return dim2;
	}
	
	  /* (non-Javadoc)
     * @see org.eclipse.gendoc.services.IAdditionalResourceService#resizeImage(java.lang.String, double, double, boolean, boolean)
     */
    protected ImageDimension resizeImage(String imagePath, double frameWidth, double frameHeight, boolean keepH, boolean keepW, boolean maxH, boolean maxW) throws AdditionalResourceException
    {

        // By default, return frame dimension
        ImageDimension result = new ImageDimension();
        result.setWidth(frameWidth);
        result.setHeight(frameHeight);
        if (imagePath == null)
        {
            return result; 
        }

        try
        {
        	IImageManipulationServiceFactory imageManipulationServiceFactory =(IImageManipulationServiceFactory)GendocServices.getDefault().getService(IImageManipulationServiceFactory.class);
        	IImageManipulationService imageInfoService = imageManipulationServiceFactory.getService(getExtension(imagePath)) ;
            ImageDimension initialSize = imageInfoService.getImageDimension(imagePath);
            double imageHeight = initialSize.getHeight();
            double imageWidth = initialSize.getWidth();

            if (keepH && keepW)
            {
                // Do nothing => image is adapted to frame
            }
            else
            {
                if (!keepH && keepW)
                {
                    result.setWidth(frameWidth);
                    result.setHeight((imageHeight * frameWidth) / imageWidth);
                }
                else if (!keepW && keepH)
                {
                    result.setWidth((frameHeight * imageWidth) / imageHeight);
                    result.setHeight(frameHeight);
                }
                else if (!keepW && !keepH)
                {
                    result.setWidth(imageWidth);
                    result.setHeight(imageHeight);
                }
            }

            if (maxW && (result.getWidth() >= frameWidth))
            {
                result.setWidth(frameWidth);
                result.setHeight((imageHeight * frameWidth) / imageWidth);
            }
            if (maxH && (result.getHeight() >= frameHeight))
            {
                result.setWidth((frameHeight * imageWidth) / imageHeight);
                result.setHeight(frameHeight);
            }
        }
        catch (IOException e)
        {
        	throw new AdditionalResourceException("Error when reading image file.", e);
        }

        return result;
    }

    private String getExtension(String imagePath) {
    	String extension = "";

    	int i = imagePath.lastIndexOf('.');
    	if (i >= 0) {
    	    extension = imagePath.substring(i+1);
    	}
    	return extension;
    }

}
