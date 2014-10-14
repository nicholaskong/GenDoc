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
package org.eclipse.gendoc.tags.handlers.exceptions;

import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

public class IncorrectTagException extends GenDocException
{
    private static final long serialVersionUID = 1L;
    
    public IncorrectTagException(ITag tag, String message)
    {
        super(message);
        StringBuffer buffer = new StringBuffer("Error in tag ").append(tag.getName());
        String id = tag.getAttributes().get(RegisteredTags.ID);
        if (id != null)
        {
            buffer.append(" with id : ").append(id);
        }
        else
        {
            buffer.append(" : ");
        }
        buffer.append(message);
        setUIMessage(buffer.toString());
    }

    

}
