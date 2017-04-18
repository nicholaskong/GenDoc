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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.xlsx.CellRef;
import org.eclipse.gendoc.document.parser.xlsx.XLSXParser;
import org.eclipse.gendoc.document.parser.xlsx.helper.XPathXlsxUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CellAnchorMarker extends AbstractCellRefMarker implements ICellRefMarker {
	private boolean isRange = false;
	public CellAnchorMarker(String mark, String relationType, String xpath, boolean isRange) {
		super(relationType,xpath);
		this.isRange = isRange;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CellMark> getMarksToApply(XLSXParser xlsxParser) {
		Document doc = xlsxParser.getDocument();
		if (relationType != null) {
			try {
				XMLParser parser = xlsxParser.loadImplicitPartDocument(relationType);
				if (parser == null) 
					return Collections.EMPTY_LIST;
				doc = parser.getDocument();
			} catch (IOException e) {
				return Collections.EMPTY_LIST;
			}
		}
		
		try {
			List<CellMark> marks = new ArrayList<CellMark>();
			NodeList nl = (NodeList)XPathXlsxUtils.evaluateNodes(doc,xpath);
			for (int i=0; i<nl.getLength(); i++) {
				Element el = (Element)nl.item(0);				
				CellRef ref = new CellRef(Integer.valueOf(XPathXlsxUtils.evaluateText(el,"xdr:from/xdr:row/text()")),											  
										  Integer.valueOf(XPathXlsxUtils.evaluateText(el,"xdr:from/xdr:col/text()")));
				String elXPath = XPathXlsxUtils.getNodeXPath(el);
				marks.add(createMark(xlsxParser, ref, relationType, elXPath, 0));					

				if (isRange) {
					ref = new CellRef(Integer.valueOf(XPathXlsxUtils.evaluateText(el,"xdr:to/xdr:row/text()")),											  
							  		  Integer.valueOf(XPathXlsxUtils.evaluateText(el,"xdr:to/xdr:col/text()")));
					marks.add(createMark(xlsxParser, ref, relationType, elXPath, 0));					
				}				
			}
			return marks;
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}		
	}

	@Override
	public void layoutCellReference(XLSXParser xlsxParser, CellRef source, List<CellMark> targets) {
	}
}
