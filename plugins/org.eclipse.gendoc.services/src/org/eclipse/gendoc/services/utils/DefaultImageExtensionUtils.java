/*****************************************************************************
 * Copyright (c) 2013 AtoS.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philippe ROLAND (AtoS) philippe.roland@atos.net - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.services.utils;

/**
 * Utility class used to determine the default image format to use
 */
public class DefaultImageExtensionUtils
{
    protected static String PNG_DIAGRAM_EXTENSION = "png";

    protected static String JPG_DIAGRAM_EXTENSION = "jpg";

    protected static String OS = System.getProperty("os.name").toLowerCase();

    protected static boolean isUnix()
    {
        return (OS.indexOf("nux") >= 0 || OS.indexOf("nix") >= 0 || OS.indexOf("aix") > 0);
    }

    /**
     * Get the file extension to use. Returns JPG for Linux systems and PNG everywhere else
     * 
     * @param runnable the runnable
     * @return the file extension to use
     */
    public static String getDefaultImageExtension()
    {
        if (isUnix())
        {
            return JPG_DIAGRAM_EXTENSION;
        }
        else
        {
            return PNG_DIAGRAM_EXTENSION;
        }
    }
}
