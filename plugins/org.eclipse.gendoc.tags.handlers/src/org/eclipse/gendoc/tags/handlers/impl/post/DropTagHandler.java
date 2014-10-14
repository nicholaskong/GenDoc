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

import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.AbstractPrePostTagHandler;
import org.eclipse.gendoc.tags.parsers.TagParserConfig;

/**
 * Handler for &lt;drop&gt; tags.
 * 
 * @author Kris Robertson
 */
public class DropTagHandler extends AbstractPrePostTagHandler
{

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.AbstractTagHandler#run(org.eclipse.gendoc.tags.ITag)
     */
    @Override
    public String doRun(ITag tag) throws GenDocException
    {
        return TagParserConfig.INF + this.getTagName() + TagParserConfig.SLASH + TagParserConfig.SUP;
    }

}
