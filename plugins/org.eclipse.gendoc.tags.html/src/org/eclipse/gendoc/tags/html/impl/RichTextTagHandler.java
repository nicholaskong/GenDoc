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
 * Anass RADOUANI (AtoS) anass.radouani@atos.net - filePath
 * 
 *****************************************************************************/
package org.eclipse.gendoc.tags.html.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.runtime.Status;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.InvalidTemplateParameterException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.AbstractPrePostTagHandler;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;
import org.eclipse.gendoc.tags.html.IHtmlService;

/**
 * Handler for &lt;richText&gt; tags.
 * 
 * @author Kris Robertson
 */
public class RichTextTagHandler extends AbstractPrePostTagHandler
{

	private static final String DEFAULT_RICH_TEXT = "HTML" ;
	
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.AbstractTagHandler#run(org.eclipse.gendoc.tags.ITag)
     */
    @Override
    public String doRun(ITag tag) throws GenDocException
    {
        String value = super.doRun(tag);
        IHtmlService richTextService = GendocServices.getDefault().getService(IHtmlService.class);
        
        if (value == null || value.isEmpty()) {
            Map<String, String> attributes = tag.getAttributes();
            if (attributes != null) {
                String filePath = attributes.get("filePath");
                if (filePath != null && !filePath.isEmpty()) {
                    try {
                        value = getFileContent(filePath);
                    }
                    catch (FileNotFoundException e) {
                        ILogger logger = GendocServices.getDefault().getService(ILogger.class);
                        logger.log("Cannot find RichText file at : " + filePath, Status.ERROR);
                    }
                }
            }
        }
        value = richTextService.convert(value);
        return value;
    }

    private String getFileContent(String filePath) throws InvalidTemplateParameterException, FileNotFoundException
    {
        IConfigurationService configService = GendocServices.getDefault().getService(IConfigurationService.class);
        filePath = configService.replaceParameters(filePath);

        File file = new File(filePath.replaceAll("\\\\", "/"));
        String content = new Scanner(file).useDelimiter("\\Z").next();

        return content.trim();
    }

    @Override
    protected String runAttributes(ITag tag, String value) throws GenDocException
    {
        IHtmlService richTextService = GendocServices.getDefault().getService(IHtmlService.class);

        Map<String, String> attributes = tag.getAttributes();
        if (attributes != null)
        {
        	String format = attributes.get("format");
        	if (format != null){
        		format = format.toUpperCase();
        	}
        	// USE DEFAULT FORMAT
        	else {
        		format = DEFAULT_RICH_TEXT ;
        	}
        	String includePic = attributes.get("includePic");
        	if (value == null || value.isEmpty()) {
				String filePath = attributes.get("filePath");
				if (filePath != null) {
					if (format == null || format.isEmpty()) {
						if (filePath.endsWith(".rtf")) {
							format = "RTF";
						} else if (filePath.endsWith(".html") || filePath.endsWith(".xhtml") || filePath.endsWith(".mhl")) {
							format = "HTML";
						}
					}
					if (includePic == null || includePic.isEmpty()) {
						if (filePath.endsWith(".mhl")) {
							includePic = "true";
						}
					}
				}
			}
        	
			richTextService.setFormat(format);
            richTextService.setVersion(attributes.get("version"));
            richTextService.setInTable(attributes.get("insideTable"));
			richTextService.setIncludePic(includePic);
        }
        
        return value;
    }
}
