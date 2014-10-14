/*****************************************************************************
 * Copyright (c) 2011 Atos Origin.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Papa Malick WADE (Atos Origin) papa-malick.wade@atos.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.gendoc.tags.hyperlink.impl;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IBookmarkService;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.AbstractPrePostTagHandler;
import org.eclipse.gendoc.tags.handlers.Activator;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

/**
 * Handler for &lt;alias&gt; tags.
 * 
 * @author Papa Malick Wade
 * 
 */

public class AliasTagHandler extends AbstractPrePostTagHandler
{

    protected String runAttributes(ITag tag, String value) throws GenDocException
    {
        IBookmarkService bookmarkRegistry = GendocServices.getDefault().getService(IBookmarkService.class);
        ILogger logger = GendocServices.getDefault().getService(ILogger.class);
        IGendocDiagnostician diagnostician = GendocServices.getDefault().getService(IGendocDiagnostician.class);

        try
        {
            super.runAttributes(tag, value);

            String source = tag.getAttributes().get(RegisteredTags.ALIAS_SOURCE);
            String target = tag.getAttributes().get(RegisteredTags.ALIAS_TARGET);

            if (!bookmarkRegistry.getBookmarkRegistry().keySet().contains(source))
            {
                bookmarkRegistry.put(source, target);
            }
            else
            {
                String message = "The name of source :" + source + " is found in many alias tag. it must be unique throughout the document";
                diagnostician.addDiagnostic(new BasicDiagnostic(Diagnostic.ERROR, Activator.PLUGIN_ID, 0, message, null));
                logger.log(message, ILogger.DEBUG);
            }

        }
        catch (GenDocException e)
        {
            throw e;
        }
        return value;
    }

}
