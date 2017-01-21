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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.xlsx.CellRef;
import org.eclipse.gendoc.document.parser.xlsx.XLSXParser;
import org.eclipse.gendoc.document.parser.xlsx.helper.XLSXHelper;
import org.eclipse.gendoc.document.parser.xlsx.helper.XPathXlsxUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractCellMarker implements ICellMarker {
	protected final String mark;
	protected final String relationType;
	protected final String xpath;
	
	public AbstractCellMarker(String mark, String xpath) {
		this(mark, null,xpath);
	}
	
	public AbstractCellMarker(String mark, String relationType, String xpath) {
		super();
		this.mark = mark;
		this.relationType = relationType;
		this.xpath = xpath;
	}

	@Override
	public String getId() {
		return mark;
	}

	@Override
	public List<CellMark> getAppliedMarks(XLSXParser xlsxParser) {		
		Set<CellMark> res = new HashSet<CellMark>();
		try {
			NodeList refs = XPathXlsxUtils.evaluateNodes(xlsxParser.getDocument(),"//:row/:c/gendoc:mark[@id='"+this.mark+"']");
			for (int i=0; i<refs.getLength(); i++) {
				Element markEl = (Element)refs.item(i);				
				CellMark m = getCellMark(markEl);
				res.add(m);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage(),e);
		}
		List<CellMark> l = new ArrayList<CellMark>(res);
		Collections.sort(l);
		return l;
	}

	protected CellMark getCellMark(Element markEl) {
		Element cellEl = (Element)markEl.getParentNode();
		return new CellMark(new CellRef(markEl.getAttribute("r")),
							new CellRef(cellEl.getAttribute("r")), 
							markEl.getAttribute("id"),
							markEl.getAttribute("xlpart"),
							markEl.getAttribute("path").replace("['['/]","[").replace("[']'/]","]"),
							markEl.getAttribute("kind"));
	}
	
	protected CellMark createMark(XLSXParser xlsxParser, CellRef ref, String xlPart, String nodeXPath, int kind) throws XPathExpressionException {
		return new CellMark(ref, ref, mark, xlPart, nodeXPath, String.valueOf(kind));
	}
	
	protected void markCell(XLSXParser xlsxParser, CellMark mark) throws XPathExpressionException {		
		Element cellEl = getCell(xlsxParser, mark.cell, true);
		Element m = cellEl.getOwnerDocument().createElementNS(XLSXHelper.GENDOC_NS,"gendoc:mark");
		m.setAttribute("r", mark.cell.getRef());
		m.setAttribute("id", mark.id);
		m.setAttribute("xlpart", mark.xlPart);
		m.setAttribute("path", mark.path.replaceAll("(\\[|\\])", "['$1'/]"));
		m.setAttribute("kind", mark.kind);
		if (cellEl.getFirstChild() != null) {
			cellEl.insertBefore(m,cellEl.getFirstChild());
			m = (Element)m.cloneNode(false);
		}
		cellEl.appendChild(m);
	}

	protected Element getCell(XLSXParser xlsxParser, CellRef ref, boolean create) throws XPathExpressionException {
		Element cellEl = (Element)XPathXlsxUtils.evaluateNode(xlsxParser.getDocument(),"//:row/:c[@r='"+ref.getRef()+"']");
		if (cellEl == null && create) {
			// Insert Row and / or Cell
			Element rowEl = (Element)XPathXlsxUtils.evaluateNode(xlsxParser.getDocument(),"//:row[@r >= "+(ref.getRow()+1)+"]");
			if (rowEl == null) {
				Element sheetDataEl = (Element)XPathXlsxUtils.evaluateNode(xlsxParser.getDocument(),"//:sheetData");
				rowEl = (Element)sheetDataEl.insertBefore(sheetDataEl.getOwnerDocument().createElementNS(XLSXHelper.WORKSHEET_NAMESPACE,"row"), rowEl);
				rowEl.setAttribute("r", String.valueOf(ref.getRow()+1));								
			} else {
				int rowIndex = Integer.valueOf(rowEl.getAttribute("r"))-1; 
				if ( rowIndex > ref.getRow()) {
					rowEl = (Element)rowEl.getParentNode().insertBefore(xlsxParser.getDocument().createElementNS(XLSXHelper.WORKSHEET_NAMESPACE,"row"), rowEl);
					rowEl.setAttribute("r", String.valueOf(ref.getRow()+1));				
				}
			}
			Element refEl = (Element)XPathXlsxUtils.evaluateNode(rowEl,"/:c[@r > "+ref.getRef()+"]");
			cellEl = (Element)rowEl.insertBefore(
					rowEl.getOwnerDocument().createElementNS(XLSXHelper.WORKSHEET_NAMESPACE, "c"),
					refEl);
			cellEl.setAttribute("r", ref.getRef());
		}
		return cellEl;
	}
	

	@Override
	public void cleanup(XLSXParser parser) {
		try {
			unmarkCells(parser);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	protected void unmarkCells(XLSXParser xlsxParser) throws XPathExpressionException {
		NodeList nl = XPathXlsxUtils.evaluateNodes(xlsxParser.getDocument(),"//:row/:c/gendoc:mark[@id='"+mark+"']");
		for (int i=0; i<nl.getLength(); i++) {
			Node n = nl.item(i);
			n.getParentNode().removeChild(n);
		}
	}
	
	public Element getTargetElement(XLSXParser xlsxParser, CellMark mark) throws IOException, XPathExpressionException {
		String part = mark.xlPart;
		String path = mark.path;
		Document doc = xlsxParser.getDocument();
		if (!part.isEmpty()) {
			XMLParser xlPart = xlsxParser.loadImplicitPartDocument(part);
			if (xlPart == null)
				return null;
			doc = xlPart.getDocument();
		}
		
		return (Element)XPathXlsxUtils.evaluateNode(xlsxParser.getDocument(), path); 
	}
}
