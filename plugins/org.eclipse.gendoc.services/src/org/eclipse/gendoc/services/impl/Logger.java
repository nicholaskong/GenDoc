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
 * Caroline Bourdeu d'Aguerre (Atos Origin) caroline.bourdeudaguerre@atosorigin.com - Initial API and implementation
 * 
 *****************************************************************************/

package org.eclipse.gendoc.services.impl;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.GendocServiceActivator;
import org.eclipse.gendoc.services.ILogger;

/**
 * The default implementation of ILogger.
 * 
 * @see org.eclipse.gendoc.services.ILogger
 */
public class Logger extends AbstractService implements ILogger
{

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.services.ILogger#log(String, int)
     */
    public void log(String message, int severity)
    {
        if ((severity == ILogger.DEBUG) && !Platform.inDebugMode())
        {
            return;
        }
        if (severity == ILogger.DEBUG)
        {
            severity = Status.INFO;
        }
        GendocServiceActivator.log(message, severity);
    }

}
