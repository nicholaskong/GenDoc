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

public class BreakRefMarker extends AbstractCellRefMarker implements ICellRefMarker {
	protected final boolean isRowBreak; 
	
	public BreakRefMarker(String mark, String xpath, boolean isRowBreak) {
		super(mark,xpath);
		this.isRowBreak = isRowBreak;
	}

	public BreakRefMarker(String mark, String relationType, String xpath, boolean isRowBreak) {
		super(mark,relationType,xpath);
		this.isRowBreak = isRowBreak;
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
		List<CellMark> marks = new ArrayList<CellMark>();
		try {			
			NodeList nl = (NodeList)XPathXlsxUtils.evaluateNodes(doc,xpath);
			for (int i=0; i<nl.getLength(); i++) {
				Element el = (Element)nl.item(0); 
				int id = Integer.valueOf(el.getAttribute("id"));
				int min = 1;
				if (el.hasAttribute("min"))
					min = Integer.valueOf(el.getAttribute("min"));
				
				CellRef ref = isRowBreak ? new CellRef(id-1, min-1) : new CellRef(min-1, id-1);
				String elXPath = XPathXlsxUtils.getNodeXPath(el);
				marks.add(createMark(xlsxParser, ref, relationType, elXPath, 0));
			}
			return marks;
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void layoutCellReference(XLSXParser xlsxParser, CellRef source, List<CellMark> targets) {
		if (targets == null || targets.isEmpty())
			return;
		
		Document doc = xlsxParser.getDocument();
		if (relationType != null) {
			try {
				XMLParser parser = xlsxParser.loadImplicitPartDocument(relationType);
				if (parser == null) 
					return;
				doc = parser.getDocument();
			} catch (IOException e) {
				return;
			}
		}
		
		CellMark first = targets.get(0);
		Element breakEl = null;
		try {
			breakEl = (Element)XPathXlsxUtils.evaluateNode(doc,first.path);
		} catch (XPathExpressionException e) {
			new IllegalArgumentException(e.getMessage(),e);
		}
		
		Element breaksEl = (Element)breakEl.getParentNode();
		int origMin = getAttributeValue(breakEl, "min", Integer.MIN_VALUE);
		int origMax = getAttributeValue(breakEl, "max", Integer.MIN_VALUE);
		boolean isMan = getAttributeValue(breakEl, "man", 0) == 1;
		
		int manCount = getAttributeValue(breaksEl, "manualBreakCount", 0);
		int count = getAttributeValue(breaksEl, "count", 0);
		for (CellMark target : targets) {
			Element el = breakEl;
			if (first != target) {
				el = (Element)breakEl.cloneNode(true);
				breaksEl.appendChild(el);
				if (isMan)
					manCount++;
				count++;
			}
			el.setAttribute("id", String.valueOf(isRowBreak ? target.cell.getRow()+1 : target.cell.getCol()+1));

			if (origMin != Integer.MIN_VALUE) { 
				el.setAttribute("min", String.valueOf(isRowBreak ? target.cell.getCol()+1 : target.cell.getRow()+1));				
			}
			
			if (origMax != Integer.MIN_VALUE) {
				int reflow = isRowBreak ? target.cell.getCol()+1 : target.cell.getRow()+1;
				reflow -= isRowBreak ? source.getCol()+1 : source.getRow()+1;
				el.setAttribute("max", String.valueOf(origMax + reflow));				
			}
		}
		if (count > 0)
			breaksEl.setAttribute("count", String.valueOf(count));
		if (manCount > 0)
			breaksEl.setAttribute("manualBreakCount", String.valueOf(manCount));
	}

	private int getAttributeValue(Element el, String attr, int def) {
		return el.hasAttribute(attr) ? Integer.valueOf(el.getAttribute(attr)) : def;
	}
}
