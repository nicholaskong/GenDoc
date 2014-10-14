/*******************************************************************************
 * Copyright (c) 2014 Atos 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anne Haugommard (Atos) anne.haugommard@atos.net - initial API and implementation
 *******************************************************************************/
package org.eclipse.gendoc.tags.hyperlink.impl;

import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.AbstractPrePostTagHandler;

public class BookmarksTagHandler extends AbstractPrePostTagHandler
{

    public ITag preRun(ITag tag) throws GenDocException
    {
        return tag;
    }

}
