/*****************************************************************************
 * Copyright (c) 2011 Atos.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Tristan FAURE (Atos) tristan.faure@atos.net - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.process;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.process.AbstractProcess;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;


/**
 * A Process registering all the variables declared to the input document
 * @author tfaure
 *
 */
public class RegisterVariableProcess extends AbstractProcess
{

	@Override
    protected void doRun() throws GenDocException
    {
        // get the document service, and the current document used
        // with the document the file is get and the path variable names can be added to the configuration service
        IDocumentService docService = GendocServices.getDefault().getService(IDocumentService.class);
        if (docService != null)
        {
            URI documentURI;
            try
            {
                documentURI = docService.getDocument().getDocumentURL().toURI();
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                URI workspaceURI = root.getLocationURI();
                URI relative = workspaceURI.relativize(documentURI);
                IFile file = root.getFile(new Path(relative.getPath()));
                IConfigurationService configService = GendocServices.getDefault().getService(IConfigurationService.class);
                IPathVariableManager manager = file.getPathVariableManager();
                String[] keys = manager.getPathVariableNames();
                if (keys != null)
                {
                    for (String s : keys)
                    {
                        URI resolveURI = manager.resolveURI(manager.getURIValue(s));
                    	configService.addParameter(s, resolveURI.getPath());
                    }
                }
            }
            catch (URISyntaxException e)
            {
                e.printStackTrace();
                ILogger logger = GendocServices.getDefault().getService(ILogger.class);
                logger.log(e.getMessage(), IStatus.WARNING);
            }
            catch (IllegalArgumentException e) {
            	e.printStackTrace();
                ILogger logger = GendocServices.getDefault().getService(ILogger.class);
                logger.log(e.getMessage(), IStatus.WARNING);
       		}
        }
    }

    @Override
    protected int getTotalWork()
    {
        return 1;
    }

}
