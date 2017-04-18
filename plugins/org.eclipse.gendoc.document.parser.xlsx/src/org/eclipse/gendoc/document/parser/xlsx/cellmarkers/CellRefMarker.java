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

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.xlsx.CellRef;
import org.eclipse.gendoc.document.parser.xlsx.XLSXParser;
import org.eclipse.gendoc.document.parser.xlsx.helper.XPathXlsxUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CellRefMarker extends AbstractCellRefMarker implements ICellRefMarker {
	protected final String attrPath; 
	protected final String parentCountAttrPath; 
	
	public CellRefMarker(String mark, String xpath, String attrPath) {
		super(mark,xpath);
		this.attrPath = attrPath;
		this.parentCountAttrPath = null;	
	}

	public CellRefMarker(String mark, String relationType, String xpath, String attrPath) {
		super(mark,xpath);
		this.attrPath = attrPath;
		this.parentCountAttrPath = null;
	}

	public CellRefMarker(String mark, String relationType, String xpath, String attrPath, String parentCountAttrPath) {
		super(mark,relationType,xpath);
		this.attrPath = attrPath;
		this.parentCountAttrPath = parentCountAttrPath;
	}

	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CellMark> getMarksToApply(XLSXParser xlsxParser) {
		XMLParser parser = xlsxParser;
		if (relationType != null) {
			try {
				parser = xlsxParser.loadImplicitPartDocument(relationType);
				if (parser == null) 
					return Collections.EMPTY_LIST;
			} catch (IOException e) {
				return Collections.EMPTY_LIST;
			}
		}
		try {
			List<CellMark> marks = new ArrayList<CellMark>();
			NodeList nl = (NodeList)evaluateNodes(parser);
			XPathExpression attrXpath = XPathXlsxUtils.compile(attrPath);
			for (int i=0; i<nl.getLength(); i++) {
				Element el = (Element)nl.item(0); 
				String ref = attrXpath.evaluate(el);
				String elXPath = XPathXlsxUtils.getNodeXPath(el);
				marks.add(createMark(xlsxParser, new CellRef(ref), relationType, elXPath, 0));					
			}
			return marks;
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected NodeList evaluateNodes(XMLParser xlsxParser) throws XPathExpressionException {
		return (NodeList)XPathXlsxUtils.evaluateNodes(xlsxParser.getDocument(),xpath+"["+attrPath+"]");
	}

	@Override
	public void layoutCellReference(XLSXParser xlsxParser, CellRef source, List<CellMark> targets) {
		if (targets == null || targets.isEmpty())
			return;
		
		XMLParser parser = xlsxParser;
		if (relationType != null) {
			try {
				parser = xlsxParser.loadImplicitPartDocument(relationType);
				if (parser == null) 
					return;
			} catch (IOException e) {
				return;
			}
		}
		
		int nCount = 0;
		CellMark first = targets.get(0);
		try {
			Element firstEl = getTargetElement(xlsxParser, first);
			if (firstEl == null)
				return;
			Node refNode = firstEl.getNextSibling();
			XPathExpression attrXpath = XPathXlsxUtils.compile(attrPath);
			for (CellMark target : targets) {
				Element el = firstEl;
				if (first != target) {
					el = (Element)firstEl.cloneNode(true);
					firstEl.getParentNode().insertBefore(el,refNode);
					nCount++;
				}				
				
				Node n = (Node)attrXpath.evaluate(el, XPathConstants.NODE);
				if (n != null)
					n.setNodeValue(target.cell.toString());
			}	
			
			if (parentCountAttrPath != null) {
				Node n = XPathXlsxUtils.evaluateNode(parser.getDocument(), first.path+"/"+parentCountAttrPath);
				nCount += Integer.valueOf(n.getNodeValue());
				n.setNodeValue(String.valueOf(nCount));
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		
	}
}
