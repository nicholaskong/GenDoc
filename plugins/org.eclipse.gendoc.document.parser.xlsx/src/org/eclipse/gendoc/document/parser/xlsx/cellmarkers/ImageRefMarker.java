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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.documents.helper.UnitsHelper;
import org.eclipse.gendoc.document.parser.xlsx.CellRef;
import org.eclipse.gendoc.document.parser.xlsx.XLSXParser;
import org.eclipse.gendoc.document.parser.xlsx.helper.XLSXHelper;
import org.eclipse.gendoc.document.parser.xlsx.helper.XPathXlsxUtils;
import org.eclipse.gendoc.documents.IAdditionalResourceService;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.documents.IImageService;
import org.eclipse.gendoc.documents.ImageDimension;
import org.eclipse.gendoc.services.GendocServices;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImageRefMarker extends AbstractCellMarker {
	private static final class ImageCellMark extends CellMark {
		public ImageCellMark(CellRef srcRef, CellRef cellRef, String id, String xlPart, String path, String kind) {
			super(srcRef, cellRef, id, xlPart, path, kind);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((imageId == null) ? 0 : imageId.hashCode());
			result = prime * result + (keepH ? 1231 : 1237);
			result = prime * result + (keepW ? 1231 : 1237);
			result = prime * result + (maxH ? 1231 : 1237);
			result = prime * result + (maxW ? 1231 : 1237);
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ImageCellMark other = (ImageCellMark) obj;
			if (imageId == null) {
				if (other.imageId != null)
					return false;
			} else if (!imageId.equals(other.imageId))
				return false;
			if (keepH != other.keepH)
				return false;
			if (keepW != other.keepW)
				return false;
			if (maxH != other.maxH)
				return false;
			if (maxW != other.maxW)
				return false;
			return true;
		}

		private String imageId;
		private boolean keepH;
		private boolean keepW;
		private boolean maxH;
		private boolean maxW;		
	}
	
	public ImageRefMarker() {
		super("image", XLSXHelper.DRAWING_RELATIONSHIP, "/xdr:wsDr/xdr:twoCellAnchor");
	}

	@Override
	protected CellMark getCellMark(Element markEl) {
		Element cellEl = (Element)markEl.getParentNode();
		ImageCellMark cm = new ImageCellMark(
							new CellRef(cellEl.getAttribute("r")),
							new CellRef(cellEl.getAttribute("r")),
							markEl.getAttribute("id"),
							markEl.getAttribute("xlpart"),
							markEl.getAttribute("path").replace("['['/]","[").replace("[']'/]","]"),
							markEl.getAttribute("kind"));
		cm.imageId = markEl.getAttribute("imageId");
		cm.keepW = Boolean.parseBoolean(markEl.getAttribute("keepW"));
		cm.keepH = Boolean.parseBoolean(markEl.getAttribute("keepH"));
		cm.maxH = Boolean.parseBoolean(markEl.getAttribute("maxH"));
		cm.maxW = Boolean.parseBoolean(markEl.getAttribute("maxW"));
		return cm;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CellMark> getMarksToApply(XLSXParser xlsxParser) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public void setMarks(XLSXParser parser) {
	}

	@Override
	public List<CellMark> getAppliedMarks(XLSXParser parser) {
		List<CellMark> res = new ArrayList<CellMark>();
		try {
			NodeList refs = XPathXlsxUtils.evaluateNodes(parser.getDocument(),"//:row/:c[contains(.//:t/text(),'<gendoc:mark id=\"image\"')]");
			for (int i=0; i<refs.getLength(); i++) {
				Element markEl = (Element)refs.item(i);				
				String content = markEl.getTextContent();
				int is = content.indexOf("<gendoc:mark id=\"image\"");
				int ie = content.indexOf("/>",is);
				if (ie <=0)
					continue;
				ie += 2;
				
				while (true) {
					Node n = markEl.getFirstChild();
					if (n == null)
						break;
					markEl.removeChild(n);
				}
				
				Element el = (Element)XPathXlsxUtils.parserXmlFragment(content.substring(is, ie));
				el =(Element)markEl.appendChild(markEl.getOwnerDocument().importNode(el,true));
				CellMark m = getCellMark(el);
				res.add(m);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage(),e);
		}
		return res;
	}

	@Override
	public void layoutCells(XLSXParser parser, List<CellMark> targets) {
		if (targets == null || targets.size() == 0)
			return;
		
        IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
        IAdditionalResourceService additionalResourceService = documentService.getAdditionalResourceService();
        IImageService imageService = additionalResourceService.getImageService();
		
		try {
			XMLParser xlDrawing = parser.loadImplicitPartDocument(XLSXHelper.DRAWING_RELATIONSHIP);		
			for (CellMark m : targets) {
				ImageCellMark mark = (ImageCellMark)m;
				CellRef target = mark.cell;
			
				ImageDimension imDim = imageService.resizeImage(
						parser.getXLSXDocument().getUnzipLocationDocumentFile()+File.separator+
							additionalResourceService.getImageRelativePath(mark.imageId).replace('/', File.separatorChar), 
						// TODO: Calculate the cell Width instead, to take in account, merged cells 
						parser.calculateColumnWidth(target.getCol()+1),
						parser.calculateRowHeight(target.getRow()+1), 
						mark.keepH, mark.keepW, mark.maxH, mark.maxW);
				
				// TODO: Query the current DPI.
				long widthInEmu =  (long)UnitsHelper.convertFromPixels(imDim.getWidth(),96,UnitsHelper.Unit.EMU);
				long heightInEmu = (long)UnitsHelper.convertFromPixels(imDim.getHeight(),96,UnitsHelper.Unit.EMU);
				
				Element anchorEl = (Element)XPathXlsxUtils.evaluateNode(xlDrawing.getDocument(), mark.path);
				 
				Element fromCol = (Element)XPathXlsxUtils.evaluateNode(anchorEl, "./xdr:from/xdr:col");
				fromCol.setTextContent(String.valueOf(target.getCol()));
				Element fromRow = (Element)XPathXlsxUtils.evaluateNode(anchorEl, "./xdr:from/xdr:row");	
				fromRow.setTextContent(String.valueOf(target.getRow()));
				
				Element toCol = (Element)XPathXlsxUtils.evaluateNode(anchorEl, "./xdr:to/xdr:col");
				toCol.setTextContent(String.valueOf(target.getCol()));
				Element toColOff = (Element)XPathXlsxUtils.evaluateNode(anchorEl, "./xdr:to/xdr:colOff");
				toColOff.setTextContent(String.valueOf(widthInEmu));
				Element toRow = (Element)XPathXlsxUtils.evaluateNode(anchorEl, "./xdr:to/xdr:row");	
				toRow.setTextContent(String.valueOf(target.getRow()));
				Element toRowOff = (Element)XPathXlsxUtils.evaluateNode(anchorEl, "./xdr:to/xdr:rowOff");	
				toRowOff.setTextContent(String.valueOf(heightInEmu));				
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}			
	}
}
