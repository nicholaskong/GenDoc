/*****************************************************************************
 * (c) Copyright 2016 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.services.xlsx;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.document.parser.xlsx.XLSXDocument;
import org.eclipse.gendoc.document.parser.xlsx.XLSXParser;
import org.eclipse.gendoc.documents.IDocumentManager;
import org.eclipse.gendoc.process.AbstractStepProcess;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;

public class XLSXReorderCellProcess extends AbstractStepProcess {
	
	@Override
	protected void doRun() throws GenDocException {
        final IDocumentManager documentManager = GendocServices.getDefault().getService(IDocumentManager.class);
        Document document = documentManager.getDocTemplate();
        if (!(document instanceof XLSXDocument))
        	return;
        
    	super.doRun();
	}

	@Override
	protected void step(Document document) throws GenDocException {
		do {
			if (document.getXMLParser() instanceof XLSXParser) {
				XLSXParser parser = (XLSXParser)document.getXMLParser();
				parser.layoutCells();
				document.getXMLParser().setCurrentNode(null);
			}
		} while (document.jumpToNextFile());
	}
}
