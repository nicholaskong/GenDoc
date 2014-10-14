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
 * Tristan FAURE - ATOS ORIGIN INTEGRATION - tristan.faure@atosorigin.com
 * 
 *****************************************************************************/
package org.eclipse.gendoc.documents;

import org.eclipse.gendoc.services.exception.GenDocException;

public class SourceException extends GenDocException
{

	private static final long serialVersionUID = -3335628796296509595L;

	public SourceException(String message)
    {
        super(message);
    }

}
