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

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.gendoc.document.parser.documents.Document.CONFIGURATION;
import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.documents.helper.UnitsHelper;
import org.eclipse.gendoc.document.parser.documents.helper.UnitsHelper.Unit;
import org.eclipse.gendoc.document.parser.xlsx.cellmarkers.BreakRefMarker;
import org.eclipse.gendoc.document.parser.xlsx.cellmarkers.CellAnchorMarker;
import org.eclipse.gendoc.document.parser.xlsx.cellmarkers.CellMark;
import org.eclipse.gendoc.document.parser.xlsx.cellmarkers.CellRangeRefMarker;
import org.eclipse.gendoc.document.parser.xlsx.cellmarkers.CellRangeSheetRefMarker;
import org.eclipse.gendoc.document.parser.xlsx.cellmarkers.CellRefMarker;
import org.eclipse.gendoc.document.parser.xlsx.cellmarkers.FirstCellRefMarker;
import org.eclipse.gendoc.document.parser.xlsx.cellmarkers.ICellMarker;
import org.eclipse.gendoc.document.parser.xlsx.cellmarkers.ICellRefMarker;
import org.eclipse.gendoc.document.parser.xlsx.cellmarkers.ImageRefMarker;
import org.eclipse.gendoc.document.parser.xlsx.cellmarkers.MinCellRefMarker;
import org.eclipse.gendoc.document.parser.xlsx.cellmarkers.UnionCellRangeRefMarker;
import org.eclipse.gendoc.document.parser.xlsx.helper.XLSXHelper;
import org.eclipse.gendoc.document.parser.xlsx.helper.XPathXlsxUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//TODO: Handel tr@r (§3.15.4); not supported due to be a woorkbook reference
//TODO: Handle hyperlinks 
//TODO: Handle Pivot tables (§3.10.x.x) (ST_Ref: location@ref (§3.10.1.49); pivotArea@offset (§3.3.1.66); 
//                                               pivotTableDefinition (§3.10.1.73))
//TODO: Query Tables (§3.10.x.x) properly
//TODO: Tables (§3.5.x.x)

public class XLSXParser extends XMLParser {
	private XLSXDocument xlsxDocument;
	private LinkedHashMap<Integer, RowReflowInfo> rowReflowInfos = null;

	private XMLParser relationships;
	private int relationshipsNextId;
	private HashMap<String, XMLParser> partsDocuments;
	
	private final ICellMarker[] markers = new ICellMarker[] {
		// ST_RefCell refs
		new CellRefMarker("cellSmartTags", "/:worksheet/:smartTags/:cellSmartTags", "@r"),
		new CellRefMarker("cellWatch", "/:worksheet/:cellWatches/:cellWatch", "@r"),
		new MinCellRefMarker("customSheetView", "/:worksheet/:customSheetViews/:customSheetView", "@topLeftCell"),
		new MinCellRefMarker("customSheetView/pane", "/:worksheet/:customSheetViews/:customSheetView/:pane", "@topLeftCell"),
		new CellRefMarker("inputCells", "/:worksheet/:scenarios/:scenario/:inputCells", "@r"),
		new MinCellRefMarker("sheetView/pane", "/:worksheet/:sheetViews/:sheetView/:pane", "@topLeftCell"),
		new MinCellRefMarker("sheetView", "/:worksheet/:sheetViews/:sheetView", "@topLeftCell"),
		new FirstCellRefMarker("customSheetView/selection", "/:worksheet/:customSheetViews/:customSheetView/:selection", "@activeCell"),
		new FirstCellRefMarker("sheetView/selection", "/:worksheet/:sheetViews/:sheetView/:selection", "@activeCell"),
		//new CellRefMarker("tr", "/:volTypes/:volType/:main/:tp/:tr", "@r"), // A woorkbook relation.
		new CellRefMarker("singleXmlCell", XLSXHelper.SINGLE_CELL_TABLE_RELATIONSHIP, 
				"/:singleXmlCells/:singleXmlCell", "@r"),		
		
		// ST_Ref refs
		new UnionCellRangeRefMarker("worksheet/autoFilter", "/:worksheet/:autoFilter", "@ref"),
		new CellRefMarker("comment", XLSXHelper.COMMENTS_RELATIONSHIP, "/:comments/:commentList/:comment", "@ref"),
		new CellRangeSheetRefMarker("dataRef", null, "/:worksheet/:dataConsolidate/:dataRefs/:dataRef", "@ref", "@sheet", "../@count"),
		new CellRefMarker("hyperlinks", "/:worksheet/:hyperlinks/:hyperlink", "@ref"),
		new CellRangeRefMarker("mergeCell", null, "/:worksheet/:mergeCells/:mergeCell", "@ref", "../@count"),
		new UnionCellRangeRefMarker("syncRef", "/:worksheet/:sheetPr", "@syncRef"),
		new CellRangeRefMarker("worksheet/sortCondition", "/:worksheet/:sortState/:sortCondition", "@syncRef"),
		new CellRangeRefMarker("worksheet/autoFilter/sortCondition", "/:worksheet/:autoFilter/:sortState/:sortCondition", "@ref"),
		new UnionCellRangeRefMarker("worksheet/sortState", "/:worksheet/:sortState", "@syncRef"),
		new UnionCellRangeRefMarker("worksheet/sortState", "/:worksheet/:autoFilter/:sortState", "@ref"),
		new CellRangeRefMarker("webPublishItems", null, "/:worksheet/:webPublishItems/:webPublishItem", "@sourceRef", "../@count"),
		
		new CellAnchorMarker("oneCellAnchor", 
				"http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing", 
				"/xdr:wsDr/xdr:oneCellAnchor",
				false),
		new CellAnchorMarker(
				"twoCellAnchor",
				"http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing", 
				"/xdr:wsDr/xdr:twoCellAnchor",
				true),
		
		// TODO: scenarios@sqref (§3.3.1.74); 		
		// ST_Sqref refs
		new UnionCellRangeRefMarker("customSheetView/selection/sqref", "/:worksheet/:customSheetViews/:customSheetView/:selection", "@sqref"),
		new UnionCellRangeRefMarker("sheetView/selection/sqref", "/:worksheet/:sheetViews/:sheetView/:selection", "@sqref"),
		// We should actually merge the ranges instead of clone them...
		new CellRangeRefMarker("conditionalFormatting", "/:worksheet/:conditionalFormatting", "@sqref"),
		new CellRangeRefMarker("dataValidation", null, "/:worksheet/:dataValidations/:dataValidation", "@sqref", "../@count"),
		new CellRangeRefMarker("ignoredError", "/:worksheet/:ignoredErrors/:ignoredError", "@sqref"),
		new CellRangeRefMarker("protectedRange", "/:worksheet/:protectedRanges/:protectedRange", "@sqref"),
		
		// TODO: scenarios@sqref (§3.3.1.74); 		
		// ST_Sqref refs
		new UnionCellRangeRefMarker("customSheetView/selection", "/:worksheet/:customSheetViews/:customSheetView/:selection", "@sqref"),
		new UnionCellRangeRefMarker("sheetView/selection", "/:worksheet/:sheetViews/:sheetView/:selection", "@sqref"),
		// We should actually merge the ranges instead of clone them...
		new CellRangeRefMarker("conditionalFormatting", "/:worksheet/:conditionalFormatting", "@sqref"),
		new CellRangeRefMarker("dataValidation", null, "/:worksheet/:dataValidations/:dataValidation", "@sqref", "../@count"),
		new CellRangeRefMarker("ignoredError", "/:worksheet/:ignoredErrors/:ignoredError", "@sqref"),
		new CellRangeRefMarker("protectedRange", "/:worksheet/:protectedRanges/:protectedRange", "@sqref"),
		
		// Breaks
		new BreakRefMarker("rowBreaks", "//:worksheet/:rowBreaks/:brk", true),
		new BreakRefMarker("rowBreaks", "//:worksheet/:colBreaks/:brk", false),
		new ImageRefMarker()
	};
	
	private static class RowReflowInfo {
		public RowReflowInfo(int row) {
			this.row = row;
		}
		
		int row;
		int prevRowsGap;
		List<Integer> reflowedRows;
		LinkedHashMap<CellRef, CellReflowInfo> cellReflowInfos;
	}
	
	private static class CellReflowInfo {
		public CellReflowInfo(CellRef ref) {
			this.ref = ref;
		}
		
		CellRef ref;
		int prevCellGap;		
		List<CellRef> reflowedCells;
	}
	
	public XLSXParser(File f, CONFIGURATION idForDocument, XLSXDocument doc) {
		super(f, idForDocument);
		this.xlsxDocument = doc;
		parse();
	}
	
	public XLSXDocument getXLSXDocument() {
		return this.xlsxDocument;
	}
	
	public String getRelationshipsPath() {
		return getPartParentRelativePath()+"/"+"_rels"+"/"+getXmlFile().getName()+".rels";
	}
	
	public String getPartParentRelativePath() {
		try {
			return getXmlFile().getParentFile().getAbsolutePath().replace(
					getXLSXDocument().getUnzipLocationDocumentFile().getCanonicalPath(),"").
					replace(File.separatorChar, '/');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getPartRelativePath() {
		try {
			return getXmlFile().getAbsolutePath().replace(
					getXLSXDocument().getUnzipLocationDocumentFile().getCanonicalPath(),"").
					replace(File.separatorChar, '/');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void parse() {
		String relpath = getPartRelativePath().replace(getXmlFile().getName(),"")+
				"_rels/"+getXmlFile().getName()+".rels" ;			
		relationships = getXLSXDocument().getSubdocument(relpath);
		if (relationships != null) {
			relationshipsNextId = getNextRelationId(); 
		}

		partsDocuments= new HashMap<String, XMLParser>();

		try {
			inlineCellStrings();
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}
		
		for (ICellMarker marker : markers) {
			marker.setMarks(this);
		}
		
		calculateGaps();
	}
	
	public XMLParser loadExplicitPartDocument(String rel, String relId) throws IOException {
		return loadPartDocument(relId+":"+rel,"//rel:Relationship[@Type='"+rel+
				"' and @Id='"+relId+"']/@Target");		
	}
	
	public XMLParser loadImplicitPartDocument(String rel) throws IOException {
		return loadPartDocument(rel,"//rel:Relationship[@Type='"+rel+"']/@Target");		
	}
	
	private XMLParser loadPartDocument(String key, String xpath) throws IOException {
		XMLParser parser = partsDocuments.get(key);
		if (parser == null && relationships != null) {
			try {
				NodeList nl = XPathXlsxUtils.evaluateNodes(relationships.getDocument(),xpath);
				if (nl.getLength() == 0)
					return null;
				String target = ((Attr)nl.item(0)).getValue();
				File f = new File(getXLSXDocument().getUnzipLocationDocumentFile(), 
						 target.replace("/", File.separator));
				if (!target.startsWith("/")) {
					f = new File(getXmlFile().getParentFile(),target).getCanonicalFile();
				}
				String relpath = f.getAbsolutePath().replace(
						getXLSXDocument().getUnzipLocationDocumentFile().getCanonicalPath(),"");
				parser = getXLSXDocument().getSubdocument(relpath);
				if (parser != null)
					partsDocuments.put(key,parser);
			} catch (XPathExpressionException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return parser;
	}

	/**
	 * Add an explicit relationship of the provided type to the provided target. A new relationship
	 * is generated and returned to be used to reference new relationship.
	 * 
	 * @param relationshipType the type of the relationship.
	 * @param target the target of the relation
	 * @return
	 * @throws IOException 
	 */
	public String addRelationship(String relationshipType, String target) throws IOException {
		try {
			if (relationships == null) {
				createRelationshipPart();
			}
		
		
			Element relationshipsEl = (Element)XPathXlsxUtils.evaluateNode(relationships.getDocument(),"/rel:Relationships"); 
			Element rel = (Element)relationshipsEl.appendChild(relationships.getDocument().createElementNS(
					XLSXHelper.PACKAGE_RELATIONSHIPS_NAMESPACE, "rel:Relationship"));
			String id = "rId"+relationshipsNextId; 
			rel.setAttribute("Id",id);
			rel.setAttribute("Type",relationshipType);
			rel.setAttribute("Target",target.replace("/xl/","../"));
			relationshipsNextId++;
			return id;
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		} 
	}
	
	private void createRelationshipPart() throws IOException {
		relationships = getXLSXDocument().createSubdocument(getRelationshipsPath(), 
				"<Relationships xmlns=\""+XLSXHelper.PACKAGE_RELATIONSHIPS_NAMESPACE+"\"/>");
		relationshipsNextId += 1;
	}

	public int getNextRelationId() {
		try {
			List<String> values = XPathXlsxUtils.evaluateValues(relationships.getDocument(), 
				"/rel:Relationships/rel:Relationship/@Id[starts-with(.,'rId')]");
			
			int nextId = 1;
			for (int i=0; i<values.size(); i++) {
				String last = values.get(i).substring(3);
				try {
					nextId = Math.max(nextId, Integer.valueOf(last));
				} catch (NumberFormatException e) {}
			}
			return nextId+1;
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e); 
		}
	}
	
	private void inlineCellStrings() throws XPathExpressionException {
		NodeList nl = (NodeList)XPathXlsxUtils.evaluateNodes(getDocument(), "//:c[./@t='s']");
		for (int i=0; i<nl.getLength(); i++) {
			Element cell = (Element)nl.item(i);
			cell.setAttribute("t", "inlineStr");
			NodeList valueNodes = cell.getElementsByTagName("v");
			Element value = (Element)valueNodes.item(0);
			int ref = Integer.valueOf(value.getTextContent());
			Element newElement = (Element)cell.insertBefore(getDocument().createElementNS(XLSXHelper.WORKSHEET_NAMESPACE, "is"), value);
			cell.removeChild(value);
			
			NodeList cellValueNodes = xlsxDocument.getSharedStringNodes(ref);
			for (int j=0; j<cellValueNodes.getLength(); j++) {
				newElement.appendChild(newElement.getOwnerDocument().importNode(cellValueNodes.item(j), true));				
			}
		}
	}
	
	private void calculateGaps() {
		rowReflowInfos = new LinkedHashMap<Integer, RowReflowInfo>();
    	    	
		int rowIndex = -1;
    	NodeList rows = null;;
		try {
			rows = XPathXlsxUtils.evaluateNodes(getDocument(), "//:row");
		} catch (XPathExpressionException e) {
			return; // Can not happen
		}
		
		for (int i=0; i<rows.getLength(); i++) {
			Element row = (Element)rows.item(i);
			int newRowIndex = Integer.valueOf(row.getAttribute("r"))-1;
			RowReflowInfo rrInfo = new RowReflowInfo(newRowIndex);
			rrInfo.prevRowsGap = newRowIndex-rowIndex;
			rowReflowInfos.put(newRowIndex, rrInfo);
			rowIndex = newRowIndex;
			
			NodeList columns = row.getElementsByTagName("c");
			int colIndex = -1;
			rrInfo.cellReflowInfos  = new LinkedHashMap<CellRef, CellReflowInfo>();

			for (int j=0; j<columns.getLength(); j++) {
				Element column = (Element)columns.item(j);
				CellRef ref = new CellRef(column.getAttribute("r"));
				int newColIndex = ref.getCol();
				CellReflowInfo crInfo = new CellReflowInfo(ref);
				crInfo.prevCellGap = newColIndex-colIndex;
				rrInfo.cellReflowInfos.put(ref, crInfo);
				colIndex = newColIndex;
			}
		}    	
	}
	
	public void layoutCells() {
		int minRow = Integer.MAX_VALUE;
		int minCol = Integer.MAX_VALUE;
		int maxCol = 0;
		
		int curRow = 0;
    	NodeList rows = getDocument().getElementsByTagName("row");
		for (int i=0; i<rows.getLength(); i++) {
			Element row = (Element)rows.item(i);
			int rowIndex = Integer.valueOf(row.getAttribute("r"))-1;
			int rowDiff = getPreviousRowGap(rowIndex);
			curRow += rowDiff;

			NodeList columns = row.getElementsByTagName("c");
			int curCol = 0;
			for (int j=0; j<columns.getLength(); j++) {
				Element column = (Element)columns.item(j);
				CellRef ref = new CellRef(column.getAttribute("r"));
				int colDiff = getPreviousCellGap(ref);				
				curCol += colDiff;
				maxCol = Math.max(maxCol, curCol-1);
				CellRef newRef = new CellRef(curRow-1, curCol-1);  
				column.setAttribute("r", newRef.getRef());
				mapCells(ref, newRef);
			}

			row.setAttribute("r", String.valueOf(curRow));
			row.removeAttribute("spans");

			if (row.getTextContent().contains("<drop/>"))
				curRow--;			
		} 		
		
		if (minRow == Integer.MAX_VALUE)
			minRow = 0;
		if (minCol == Integer.MAX_VALUE)
			minCol = 0;
		
		try {
			XPathXlsxUtils.evaluateNode(getDocument(), "/:worksheet/:dimension/@ref").setNodeValue(
					new CellRef(minRow,minCol).getRef()+":"+ new CellRef(curRow,maxCol).getRef());
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		
		// Review how to get the mapping... correctly
		for (ICellMarker marker : markers) {	
			List<CellMark> marks = marker.getAppliedMarks(this);
			if (marker instanceof ICellRefMarker) {
				Map<CellRef,Map<String,Set<CellMark>>> markMap = getMarksByCellRef(marks);
				LinkedHashSet<CellRef> sourceCells = new LinkedHashSet<CellRef>();
				for (Map<String,Set<CellMark>> m1 : markMap.values()) {
					for (Set<CellMark> m2 : m1.values()) {
						for (CellMark m : m2) {
							sourceCells.add(m.src);					
						}
					}
				}
	
				for (CellRef source : sourceCells) {
					RowReflowInfo rrInfo = rowReflowInfos.get(source.getRow());
					CellReflowInfo crInfo = rrInfo.cellReflowInfos.get(source);
					Map<String,List<CellMark>> marksByPath = new HashMap<String,List<CellMark>>();
					for (CellRef r : crInfo.reflowedCells) {
						Map<String,Set<CellMark>> tmpMap = markMap.get(r);
						if (tmpMap == null)
							continue;
						for (Map.Entry<String,Set<CellMark>> e : tmpMap.entrySet()) {
							List<CellMark> l = marksByPath.get(e.getKey());
							if (l == null) {
								l = new ArrayList<CellMark>();
								marksByPath.put(e.getKey(),l);
							}
							l.addAll(e.getValue());
						}
					}
					
					for (List<CellMark> ms : marksByPath.values()) {
						((ICellRefMarker)marker).layoutCellReference(this, source, ms);
					}
				}
			} else {
				marker.layoutCells(this, marks);
			}
			marker.cleanup(this);
		}
	}
	
	public void handleDropCellReferences() {		
		try {
			HashSet<String> cellRefs = new HashSet<String>(XPathXlsxUtils.evaluateValues(getDocument(), "//:row/:c/@r"));
			for (ICellMarker marker : markers) {
				if (!(marker instanceof ICellRefMarker))
					continue;
				
				ICellRefMarker cellMarker = (ICellRefMarker)marker;
				cellMarker.verifyCellReference(this, cellRefs);
			}
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private Map<CellRef,Map<String,Set<CellMark>>> getMarksByCellRef(List<CellMark> marks) {
		Map<CellRef,Map<String,Set<CellMark>>> map = new LinkedHashMap<CellRef,Map<String,Set<CellMark>>>();
		for (CellMark m : marks) {
			Map<String,Set<CellMark>> cmLMap = map.get(m.cell);
			if (cmLMap == null) {
				cmLMap = new LinkedHashMap<String,Set<CellMark>>();
				map.put(m.cell, cmLMap);
			}
			Set<CellMark> cmList = cmLMap.get(m.path);
			if (cmList == null) {
				cmList = new LinkedHashSet<CellMark>();
				cmLMap.put(m.path, cmList);
			}
			cmList.add(m);
		}
		return map;
	}
	
	private void mapCells(CellRef origRef, CellRef newRef) {
		RowReflowInfo rowReflowInfo = rowReflowInfos.get(origRef.getRow());
		if (rowReflowInfo.reflowedRows == null) {
			rowReflowInfo.reflowedRows = new ArrayList<Integer>();
		}
		rowReflowInfo.reflowedRows.add(newRef.getCol());

		CellReflowInfo cellReflowInfo = rowReflowInfo.cellReflowInfos.get(origRef);
		if (cellReflowInfo.reflowedCells == null) {
			cellReflowInfo.reflowedCells = new ArrayList<CellRef>();
		}
		cellReflowInfo.reflowedCells.add(newRef);		
	}

	
	private int getPreviousCellGap(CellRef ref) {
		RowReflowInfo rrInfo =  rowReflowInfos.get(ref.getRow());
		CellReflowInfo crInfo = rrInfo.cellReflowInfos.get(ref);
		return crInfo.prevCellGap;
	}

	private int getPreviousRowGap(int rowIndex) {
		RowReflowInfo rrInfo = rowReflowInfos.get(rowIndex);
		return rrInfo.prevRowsGap;
/*		int gap = 0;
		for (Map.Entry<Integer, RowReflowInfo> e : rowReflowInfos.entrySet()) {
			RowReflowInfo rrInfo = e.getValue();
			boolean removed = rrInfo.reflowedRows == null ||rrInfo.reflowedRows.isEmpty();
			if (e.getKey() >= rowIndex) {
				if (e.getKey() == rowIndex)
					gap += rrInfo.prevRowsGap;
				break;
			}
			gap = (removed) ? (gap + rrInfo.prevRowsGap-1) : 0;				
		}
		return gap;
*/
	}
	
	// TODO: Calculate the cell Width also... 
	public int calculateColumnWidth(int nColumn) {
		try {
			Element colEl = (Element)XPathXlsxUtils.evaluateNode(getDocument(),
					"/:worksheet/:cols/:col[@min <= "+nColumn+" and @max >= "+nColumn+"]");

			double width = 0.0;
			int styleIndex = 0;
			if (colEl != null) {
				styleIndex = colEl.hasAttribute("style") ? Integer.valueOf(colEl.getAttribute("style")) : 0;
				width = colEl.hasAttribute("width") ? Double.valueOf(colEl.getAttribute("width")) : 8.0;
			} else {
				width = XPathXlsxUtils.evaluateNumber(getDocument(),
						"/:worksheet/:sheetFormatPr/@defaultColWidth",0.0d);
				if (width == 0.0) {
					width = XPathXlsxUtils.evaluateNumber(getDocument(),
							"/:worksheet/:sheetFormatPr/@baseColWidth",8.0d);
					width += 5;
				}
			}
			
			XMLParser stylePart = getXLSXDocument().getSubdocument(XLSXHelper.STYLES_PATH+"styles.xml");
			int fontIndex = XPathXlsxUtils.evaluateNumber(stylePart.getDocument(), 
					"/:styleSheet/:cellStyleXfs/:xf["+(styleIndex+1)+"]/@fontId",0);
			Element fontEl = (Element)XPathXlsxUtils.evaluateNode(stylePart.getDocument(), 
					"/:styleSheet/:fonts/:font["+(fontIndex+1)+"]");
			String fontName = XPathXlsxUtils.evaluateText(fontEl, "./:name/@val");
			boolean bold = Boolean.valueOf(XPathXlsxUtils.evaluateText(fontEl, "./:b/@val"));
			boolean italic = Boolean.valueOf(XPathXlsxUtils.evaluateText(fontEl, "./:i/@val"));
			float sz = XPathXlsxUtils.evaluateNumber(fontEl, "./:sz/@val", 11.0f);
			
			Font f = new Font(fontName, (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0) , (int)sz).
					deriveFont(sz);
			double max = -1;
			GlyphVector gv = f.createGlyphVector(new FontRenderContext(null,true,true), "0123456789");
			for (int i=0; i<=9; i++) {
				GlyphMetrics gm = gv.getGlyphMetrics(i);
				Rectangle2D bb = gm.getBounds2D();
				max = Math.max(max,bb.getWidth());
			}
			
			return (int)(((256.0d * width + (double)((int)(128.0d/max)))/256.0d)*max);			
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}		
	}

	public int calculateRowHeight(int nRow) {
		try {
			double sheetFormatEl = XPathXlsxUtils.evaluateNumber(getDocument(),
					"/:worksheet/:sheetFormatPr/@defaultRowHeight",0.0d);
			int defHeight = (int)UnitsHelper.convertToPixels(Unit.POINT, sheetFormatEl, 96);
			
			Element rowEl = (Element)XPathXlsxUtils.evaluateNode(getDocument(),
					"/:worksheet/:sheetData/:row[@r="+nRow+"]");
			if (rowEl.hasAttribute("ht")) {
				return (int)UnitsHelper.convertToPixels(Unit.POINT, Double.valueOf(rowEl.getAttribute("ht")),96);
			}
			return defHeight;
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}		
	}
}

