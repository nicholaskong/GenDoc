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
 * Tristan FAURE (Atos) tristan.faure@atos.net - Initial API and Implementation
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.exceptions;

import org.eclipse.gendoc.m2t.IFragmentableM2TProcessor;
import org.eclipse.gendoc.m2t.IM2TProcessor;
import org.eclipse.gendoc.services.exception.GenDocException;

public class FragmentUnavailableException extends GenDocException
{

    public FragmentUnavailableException(Class<? extends IM2TProcessor> aClass)
    {
        super("fragments can not be used " + aClass.getName() + " can not be cast to " + IFragmentableM2TProcessor.class);
        setUIMessage("Your configuration does not allow you to use fragment tags");
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
