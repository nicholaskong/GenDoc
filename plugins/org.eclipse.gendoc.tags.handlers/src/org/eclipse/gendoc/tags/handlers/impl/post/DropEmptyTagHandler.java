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
package org.eclipse.gendoc.tags.handlers.impl.post;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.AbstractPrePostTagHandler;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;
import org.eclipse.gendoc.tags.parsers.TagParserConfig;

/**
 * Handler for &lt;dropEmpty&gt; tags.
 * 
 * @author Kris Robertson
 */
public class DropEmptyTagHandler extends AbstractPrePostTagHandler
{

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.AbstractTagHandler#run(org.eclipse.gendoc.tags.ITag)
     */
    @Override
    public String doRun(ITag tag) throws GenDocException
    {
        String value = super.doRun(tag);

        List<Pattern> patterns = new ArrayList<Pattern>(1);
        patterns.add(Pattern.compile("(\\[[^\\[\\]]*\\])|(\\[/[^\\[\\]]*\\])"));

        IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
        String cleanValue = documentService.cleanContent("[" + value + "/]", patterns);
        if ("[/]".equals(cleanValue))
        {
            return TagParserConfig.INF + RegisteredTags.DROP + TagParserConfig.SLASH + TagParserConfig.SUP;
        }

        return value;
    }

}
