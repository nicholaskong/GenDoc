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
 * Alexia Allanic (Atos Origin) alexia.allanic@atosorigin.com - Initial API and implementation
 * Mohamed Ali Bach Tobji (Atos) mohamed-ali.bachtobji@atos.net - fix bug #501477 : replace parameters in attribute filePath  
 * 
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.impl.scripts;

import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.InvalidTemplateParameterException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.AbstractPrePostTagHandler;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

/**
 * Handler for &lt;include&gt; tags.
 * 
 * @author Alexia Allanic
 */
public class IncludeTagHandler extends AbstractPrePostTagHandler {

	
	@Override
	public String doRun(ITag tag) throws GenDocException {
		String result = super.doRun(tag);
		if ((tag != null) && (tag.getAttributes() != null)) {
			IConfigurationService configService = GendocServices.getDefault().getService(IConfigurationService.class);
			String filePath = configService.replaceParameters(tag.getAttributes().get(RegisteredTags.INCLUDE_FILE_PATH));
			IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
			String id = documentService.getAdditionalResourceService().includeFile(filePath);
			String output = "&lt;drop/&gt;</w:p><w:altChunk xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\"" + id + "\" />";
            return output;
		}
		return result;
	}
		
    
}
