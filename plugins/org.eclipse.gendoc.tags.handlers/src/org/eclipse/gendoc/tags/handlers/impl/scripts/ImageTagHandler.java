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
 * Kris Robertson (Atos Origin) kris.robertson@atosorigin.com - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.impl.scripts;

import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.documents.IImageService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.AbstractPrePostTagHandler;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

/**
 * Handler for &lt;image&gt; tags.
 * 
 * @author Kris Robertson
 */
public class ImageTagHandler extends AbstractPrePostTagHandler
{
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.AbstractTagHandler#run(org.eclipse.gendoc.tags.ITag)
     */
    @Override
    public String doRun(ITag tag) throws GenDocException
    {
        String result = super.doRun(tag);
        if ((tag != null) && (tag.getAttributes() != null))
        {

            boolean keepH = Boolean.parseBoolean(tag.getAttributes().get(RegisteredTags.IMAGE_KEEP_H));
            boolean keepW = Boolean.parseBoolean(tag.getAttributes().get(RegisteredTags.IMAGE_KEEP_W));
            boolean maxH = Boolean.parseBoolean(tag.getAttributes().get(RegisteredTags.IMAGE_MAX_H));
            boolean maxW = Boolean.parseBoolean(tag.getAttributes().get(RegisteredTags.IMAGE_MAX_W));

            IConfigurationService configService = GendocServices.getDefault().getService(IConfigurationService.class);

            String filePath = configService.replaceParameters(tag.getAttributes().get(RegisteredTags.IMAGE_FILE_PATH));
            String object = tag.getAttributes().get(RegisteredTags.IMAGE_OBJECT);

            IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
            IImageService imageService = documentService.getAdditionalResourceService().getImageService();

            String imageId = "";
            if (filePath != null)
            {
                imageId = imageService.getImageId(filePath);
            }
            else if (object != null)
            {
                imageId = object;
                filePath = imageService.getFilePath(imageId);
            }

            result = imageService.manageImage(tag, imageId, filePath, keepH, keepW, maxH, maxW);

            result = result.replaceAll("(&lt;image[^&]*&gt;)|(&lt;/image[^&]*&gt;)", "");
        }
        return result;
    }

}
