/*****************************************************************************
 * Copyright (c) 2014 Atos.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Anne Haugommard (Atos) anne.haugommard@atos.net - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.documents;

import java.io.File;
import java.net.URL;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.services.IService;

public interface IDocumentManager extends IService
{

	Document getDocument (File templateDoc);
	
	Document getDocument (URL url);

	void setDocTemplate (Document document);

	Document getDocTemplate ();

}
