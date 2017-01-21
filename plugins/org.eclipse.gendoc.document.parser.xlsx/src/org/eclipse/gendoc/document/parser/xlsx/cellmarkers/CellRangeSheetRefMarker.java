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
package org.eclipse.gendoc.document.parser.xlsx.cellmarkers;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.xlsx.helper.XPathXlsxUtils;
import org.w3c.dom.NodeList;

public class CellRangeSheetRefMarker extends CellRangeRefMarker {
	protected final String sheetPath; 
	
	public CellRangeSheetRefMarker(String mark, String xpath, String attrPath, String sheetPath) {
		super(mark,xpath,attrPath, null);
		this.sheetPath = sheetPath;
	}

	public CellRangeSheetRefMarker(String mark, String relationType, String xpath, String attrPath, String sheetPath) {
		super(mark,relationType,xpath,attrPath, null);
		this.sheetPath = sheetPath;
	}

	public CellRangeSheetRefMarker(String mark, String relationType, String xpath, String attrPath, String sheetPath, String parentCountAttrPath) {
		super(mark,relationType,xpath,attrPath, parentCountAttrPath);
		this.sheetPath = sheetPath;
	}

	@Override
	protected NodeList evaluateNodes(XMLParser xlsxParser) throws XPathExpressionException {
		String name = xlsxParser.getXmlFile().getName();
		return (NodeList)XPathXlsxUtils.evaluateNodes(xlsxParser.getDocument(),xpath+"["+attrPath+" and "+sheetPath+"='"+name+"']");
	}	
}
