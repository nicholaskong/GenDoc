/*****************************************************************************
 * Copyright (c) 2011 Atos
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Tristan FAURE (Atos) tristan.faure@atos.net - refactoring 
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.process;

import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.ITagHandler;
import org.eclipse.gendoc.tags.handlers.ITagHandlerService;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

public class FragmentAnalyserProcess extends TagAnalyserProcess
{
    @Override
    protected boolean executeOneTag(ITagHandlerService tagHandlerService, StringBuffer finalText, ITag tag) throws GenDocException
    { 
        ITagHandler tagHandler = tagHandlerService.getHandlerFor(tag);
        if (RegisteredTags.FRAGMENT.equals(tag.getName()) || tagHandler == null)
        {
            super.executeOneTag(tagHandlerService, finalText, tag);
            return true ;
        }
        return false ;
    }

}
