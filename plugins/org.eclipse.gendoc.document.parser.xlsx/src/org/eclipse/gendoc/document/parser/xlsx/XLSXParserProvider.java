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
package org.eclipse.gendoc.document.parser.xlsx;

import java.io.File;

import org.eclipse.gendoc.document.parser.documents.Document.CONFIGURATION;
import org.eclipse.gendoc.document.parser.documents.IXMLParserProvider;
import org.eclipse.gendoc.document.parser.documents.XMLParser;

public class XLSXParserProvider implements IXMLParserProvider {
	public XLSXParserProvider(XLSXDocument doc){
		this.document = doc;
	}
	
	@Override
	public XMLParser createParser(File f, CONFIGURATION conf) {
		return new XLSXParser(f, conf, document);
	}

	private XLSXDocument document;
}
