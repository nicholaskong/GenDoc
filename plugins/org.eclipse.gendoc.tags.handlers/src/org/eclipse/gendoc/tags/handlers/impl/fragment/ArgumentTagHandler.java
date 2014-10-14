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
package org.eclipse.gendoc.tags.handlers.impl.fragment;

import org.eclipse.gendoc.m2t.IFragmentService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.AbstractPrePostTagHandler;
import org.eclipse.gendoc.tags.handlers.exceptions.IncorrectTagException;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

/**
 * The argument tag handler manages the arguments in a query
 * @author tfaure
 *
 */
public class ArgumentTagHandler extends AbstractPrePostTagHandler
{

    @Override
    protected String runAttributes(ITag tag, String value) throws GenDocException
    {
        super.runAttributes(tag, value);
        IFragmentService fragmentService = GendocServices.getDefault().getService(IFragmentService.class);
        String name = tag.getParent().getAttributes().get(RegisteredTags.FRAGMENT_NAME);
        String argumentName = tag.getAttributes().get(RegisteredTags.FRAGMENT_ARGUMENT_NAME);
        if (argumentName == null || argumentName.length() == 0)
        {
            throw new IncorrectTagException(tag, "name is mandatory");
        }
        String argumentType = tag.getAttributes().get(RegisteredTags.FRAGMENT_ARGUMENT_TYPE);
        if (argumentType == null || argumentType.length() == 0)
        {
            throw new IncorrectTagException(tag, "type is mandatory");
        }
        fragmentService.addArgument(name, argumentName, argumentType);
        return "";
    }


}
