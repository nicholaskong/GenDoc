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
 * Kris Robertson (Atos Origin) kris.robertson@atosorigin.com - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.gendoc.document.parser.Activator;
import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.documents.IDocumentManager;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.process.AbstractProcess;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.services.exception.DocumentServiceException;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;

/**
 * Saves the generated document.
 * 
 * @author Kris Robertson
 */
public class SaveProcess extends AbstractProcess
{

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.process.AbstractProcess#doRun()
     */
    @Override
    protected void doRun() throws GenDocException
    {
        final IDocumentManager documentManager = GendocServices.getDefault().getService(IDocumentManager.class);
        Document document = documentManager.getDocTemplate();

        try
        {
            final IConfigurationService configurationService = GendocServices.getDefault().getService(IConfigurationService.class);
            final IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
            documentService.saveDocument(document, configurationService.getOutput());
        }
        catch (DocumentServiceException e)
        {
            final IGendocDiagnostician diagnostician = GendocServices.getDefault().getService(IGendocDiagnostician.class);
            diagnostician.addDiagnostic(new BasicDiagnostic(Diagnostic.WARNING, Activator.PLUGIN_ID, 0, "Error during document save: " + e.getMessage(), new Object[] {document}));

            final ILogger logger = GendocServices.getDefault().getService(ILogger.class);
            logger.log("Error during document save", ILogger.DEBUG);
        }
        catch (Exception t)
        {
    		IGendocDiagnostician diag = GendocServices.getDefault().getService(IGendocDiagnostician.class);
    		diag.addDiagnostic(IStatus.WARNING, "Error during document save : " + t.getMessage(), new Object[] {document});
        }

        this.worked(1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.process.AbstractProcess#getTotalWork()
     */
    @Override
    protected int getTotalWork()
    {
        return 1;
    }

}
