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
import org.eclipse.gendoc.services.exception.AdditionalResourceException;
import org.eclipse.gendoc.services.exception.GenDocException;

/**
 * Adds additional resources to the generated document.
 * 
 * @author Kris Robertson
 */
public class AdditionalResourceProcess extends AbstractProcess
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
            final IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
            documentService.getAdditionalResourceService().addAdditionalResourcesToDocument();
        }
        catch (AdditionalResourceException e)
        {
            final IGendocDiagnostician diagnostician = GendocServices.getDefault().getService(IGendocDiagnostician.class);
            diagnostician.addDiagnostic(new BasicDiagnostic(Diagnostic.WARNING, Activator.PLUGIN_ID, 0, e.getUIMessage(), new Object[] {document}));

            final ILogger logger = GendocServices.getDefault().getService(ILogger.class);
            logger.log("Error during additional resources addition", ILogger.DEBUG);
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
