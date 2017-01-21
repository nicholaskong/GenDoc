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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

public class CellRangeRefMarker extends AbstractCellRefMarker implements ICellRefMarker {
	protected final String attrPath; 
	protected final String parentCountAttrPath; 
	private Map<String, List<CellMark>[]> markRanges;
	
	public CellRangeRefMarker(String mark, String xpath, String attrPath) {
		super(mark,xpath);
		this.attrPath = attrPath;
		this.parentCountAttrPath = null;
	}

	public CellRangeRefMarker(String mark, String relationType, String xpath, String attrPath) {
		super(mark,relationType,xpath);
		this.attrPath = attrPath;
		this.parentCountAttrPath = null;
	}

	public CellRangeRefMarker(String mark, String relationType, String xpath, String attrPath, String parentCountAttrPath) {
		super(mark,relationType,xpath);
		this.attrPath = attrPath;
		this.parentCountAttrPath = parentCountAttrPath;
	}

	@Override
	public List<CellMark> getAppliedMarks(XLSXParser xlsxParser) {
		List<CellMark> marks = super.getAppliedMarks(xlsxParser);
		markRanges = calculateMarkRanges(marks);
		return marks;
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, List<CellMark>[]> calculateMarkRanges(List<CellMark> marks) {
		int maxKind = 2;
		for (CellMark m : marks) {
			int kind = Integer.valueOf(m.kind);
			maxKind = Math.max(maxKind, kind);
		}
		
		Map<String, List<CellMark>[]> ranges = new LinkedHashMap<String, List<CellMark>[]>();		
		for (CellMark m : marks) {
			List<CellMark>[] markRange = ranges.get(m.path);
			if (markRange == null) {
				markRange = new List[maxKind];
				for (int i=0; i<maxKind; i++)
					markRange[i] = new ArrayList<CellMark>();
				ranges.put(m.path, markRange);
			}
			
			int kind = Integer.valueOf(m.kind);
			markRange[kind].add(m);
		}
		return ranges;
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
				String references = attrXpath.evaluate(el);
				String[] items = references.split("\\s+");
				int nCount = 0;
				for (String ref : items) {
					if (!ref.contains(":"))
						ref = ref+":"+ref;
					String elXPath = XPathXlsxUtils.getNodeXPath(el);
			
					String[] parts = ref.split(":");
					marks.add(createMark(xlsxParser, new CellRef(parts[nCount+0]), relationType, elXPath, nCount));
					marks.add(createMark(xlsxParser, new CellRef(parts[nCount+1]), relationType, elXPath, nCount+1));
					nCount+= 2;
				}
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
		
		// Find the init / close mark sets and use the one with less cardinality
		// to clone the target and apply the range
		CellMark first = targets.get(0);
		List<CellMark>[] marks = markRanges.get(first.path);
		if (marks == null)
			return;
		markRanges.remove(first.path);
		for (int r=0; r<marks.length; r+=2) {
		int nCount = 0;
			try {
				XPathExpression attrXpath = XPathXlsxUtils.compile(attrPath);
				Node nodeRef = null;
				int nMarks = Math.min(marks[r+0].size(), marks[r+1].size());
				for (int i=0; i<nMarks; i++) {
					Element firstEl = getTargetElement(xlsxParser, first);
					if (firstEl == null)
						return;
					Element el = firstEl;
					if (i != 0) {
						el = (Element)firstEl.cloneNode(true);
						firstEl.getParentNode().insertBefore(el,nodeRef);
						nCount++;
					} else {
						nodeRef = firstEl.getNextSibling();
					}
					
					Node n = (Node)attrXpath.evaluate(el, XPathConstants.NODE);
					if (n != null)
						n.setNodeValue(marks[r+0].get(i).cell.getRef()+":"+marks[r+1].get(i).cell.getRef());
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
}
