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
package org.eclipse.gendoc.services.xlsx;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.xlsx.XLSXDocument;
import org.eclipse.gendoc.document.parser.xlsx.XLSXParser;
import org.eclipse.gendoc.document.parser.xlsx.helper.XLSXHelper;
import org.eclipse.gendoc.document.parser.xlsx.helper.XPathXlsxUtils;
import org.eclipse.gendoc.documents.AbstractImageService;
import org.eclipse.gendoc.documents.IAdditionalResourceService;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.AdditionalResourceException;
import org.eclipse.gendoc.tags.ITag;
import org.w3c.dom.Element;

//TODO: Provide a common ooxml abstract implementation of XLSXImageService
public class XLSXImageService extends AbstractImageService
{
	@Override
	public String manageImage(ITag tag, String imageId, String filePath, boolean keepH, boolean keepW, boolean maxH, boolean maxW) throws AdditionalResourceException {
		IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
		XLSXDocument document = (XLSXDocument)documentService.getDocument();
		XMLParser parser = document.getXMLParser();
		if (!(parser instanceof XLSXParser))
			return "";

		IAdditionalResourceService additionalResourceService = documentService.getAdditionalResourceService();
		String imageFileName = additionalResourceService.getImageRelativePath(imageId);
		int index = imageFileName.lastIndexOf("/");
		if (index != -1)
			imageFileName = imageFileName.substring(index+1);
		XLSXParser xlsxParser = (XLSXParser)parser;
		try {
			String drawingId = XPathXlsxUtils.evaluateText(xlsxParser.getDocument(), "/:worksheet/:drawing/@r:id");			
			XMLParser drawingParser = null;
			if (drawingId.isEmpty()) {
				// Create a drawingPart
				drawingParser = createDrawingPart(document, xlsxParser);
			} else {
				drawingParser = xlsxParser.loadExplicitPartDocument(XLSXHelper.DRAWING_RELATIONSHIP, drawingId);
			}
			
			int nextId = XPathXlsxUtils.evaluateMax(drawingParser.getDocument(), "//@id")+1;
			if (nextId < 0)
				nextId = 1;
			
			Element wsDrEl = drawingParser.getDocument().getDocumentElement();		
			Element anchor = (Element)XPathXlsxUtils.parserXmlFragment(
					"<xdr:twoCellAnchor xmlns:xdr=\""+XLSXHelper.DRAWING_NAMESPACE+"\" "
	 				  + "xmlns:a=\""+XLSXHelper.DRAWING_ML_NAMESPACE+"\" "
	 				  + "editAs=\"oneCell\">"
					  + "<xdr:from>"
						  + "<xdr:col>0</xdr:col>"
						  + "<xdr:colOff>0</xdr:colOff>"
						  + "<xdr:row>0</xdr:row>"
						  + "<xdr:rowOff>0</xdr:rowOff>"
					  + "</xdr:from>"
					  + "<xdr:to> "
					  	+ "<xdr:col>0</xdr:col>"
					  	+ "<xdr:colOff>0</xdr:colOff>"
					  	+ "<xdr:row>0</xdr:row>"
					  	+ "<xdr:rowOff>0</xdr:rowOff>"
					  + "</xdr:to>"
					  + "<xdr:pic>"
					  	+ "<xdr:nvPicPr>"
					  		+ "<xdr:cNvPr id=\""+nextId+"\" name=\""+imageFileName+"\"/>"
					  		+ "<xdr:cNvPicPr/>"
				  		+ "</xdr:nvPicPr>"
					  	+ "<xdr:blipFill>"
					  		+ "<a:blip xmlns:r=\""+XLSXHelper.RELATIONSHIPS_NAMESPACE+"\" "
					  			+ "r:embed=\""+imageId+"\"/>"
					  		+ "<a:stretch>"
					  			+ "<a:fillRect/>"
					  		+ "</a:stretch>"
				  		+ "</xdr:blipFill>"
				  		+ "<xdr:spPr>"
				  			+ "<a:prstGeom prst=\"rect\">"
				  				+ "<a:avLst />"
				  			+ "</a:prstGeom>"
			  			+ "</xdr:spPr>"
					  + "</xdr:pic>"
					  + "<xdr:clientData/>"
				  + "</xdr:twoCellAnchor>");
			
		anchor = (Element)wsDrEl.appendChild(wsDrEl.getOwnerDocument().importNode(anchor,true));
			
			String nodeXpath = XPathXlsxUtils.getNodeXPath(anchor);
			
			String str = String.format("&lt;gendoc:mark id=\"image\" "
					+ "xlpart=\"%s\" "
					+ "path=\"%s\" "
					+ "kind=\"0\" "
					+ "imageId=\"%s\" keepH=\"%b\" keepW=\"%b\" maxH=\"%b\" maxW=\"%b\"/&gt;", 
					XLSXHelper.DRAWING_RELATIONSHIP,
					nodeXpath,
					imageId, keepH, keepW, maxH, maxW);
			return str;
		} catch (Exception e) {
			throw new AdditionalResourceException(e.getMessage() == null ? "" : e.getMessage(),e); 
		}
	}
	
	private XMLParser createDrawingPart(XLSXDocument doc, XLSXParser worksheetPart) throws IOException {
		String target = doc.getNextDocumentName("/xl/drawings/drawing*.xml");

		XMLParser parser = doc.createSubdocument(target, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
		"<xdr:wsDr xmlns:xdr=\""+XLSXHelper.DRAWING_NAMESPACE+"\" " +
		"xmlns:a=\""+XLSXHelper.DRAWING_ML_NAMESPACE+"\">"+
		"</xdr:wsDr>");
		
		// Add an explicit relation to the worksheet part
		String drawingId = worksheetPart.addRelationship(XLSXHelper.DRAWING_RELATIONSHIP,target);
		// Link the drawing relation with the worksheet
		try {
			Element wsEl = (Element)XPathXlsxUtils.evaluateNode(worksheetPart.getDocument(),"/:worksheet");
			Element refEl = (Element)XPathXlsxUtils.evaluateFirstOf(wsEl, ".", 
					"legacyDrawing","legacyDrawingHF","picture","oleObjects",
					"controls","webPublishItems","tableParts","extLst");
			Element drawingEl = (Element)wsEl.insertBefore(wsEl.getOwnerDocument().createElementNS(
					XLSXHelper.WORKSHEET_NAMESPACE, "drawing"), refEl);
			drawingEl.setAttributeNS(XLSXHelper.RELATIONSHIPS_NAMESPACE, "r:id", drawingId);
			
			// Add Overwrite to the [Content_types].xml			
			XMLParser ctParser = doc.getSubdocument("[Content_Types].xml");
			Element el = (Element)ctParser.getDocument().getDocumentElement().appendChild(
					ctParser.getDocument().createElementNS(XLSXHelper.CONTENT_TYPES_NAMESPACE, "ct:Override"));
			el.setAttribute("PartName", target);
			el.setAttribute("ContentType", XLSXHelper.DRAWING_CONTENT_TYPE);
			return parser;
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}
	}	
}
