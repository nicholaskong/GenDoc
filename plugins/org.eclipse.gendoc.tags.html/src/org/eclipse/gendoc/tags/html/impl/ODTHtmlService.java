/*****************************************************************************
 * Copyright (c) 2010 Atos Origin.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Kris Robertson (Atos Origin) kris.robertson@atosorigin.com - Initial API and implementation
 * Anass RADOUANI (AtoS) anass.radouani@atos.net - format and RTF
 * Anne Haugommard (Atos) anne.haugommard@atos.net - update JTidy version
 * 
 *****************************************************************************/
package org.eclipse.gendoc.tags.html.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.tags.html.Activator;
import org.eclipse.gendoc.tags.html.IHtmlService;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

/**
 * The odt html service is used to include html in odt documents by running an
 * xsl transform.
 * 
 * @author Kris Robertson
 */
public class ODTHtmlService extends AbstractService implements IHtmlService {

	/** Name of the file containing the document content */
	private static final String content = "content.xml";

	protected String SEPARATOR = "/";

	private String format = "HTML";

	public String convert(String value) {
		String output = "";
		
		if ("RTF".equalsIgnoreCase(format)) {
			try {
				value = rtfToHtml(new StringReader(value));
				format = "HTML";
			} catch (IOException e) {
			    ILogger logger = GendocServices.getDefault().getService(
                        ILogger.class);
			    logger.log("Error transforming RTF to HTML.", Status.ERROR);
			}
            catch (BadLocationException e)
            {
                ILogger logger = GendocServices.getDefault().getService(
                        ILogger.class);
                logger.log("Error writing RTF to HTML.", Status.ERROR);
            }
		}
		
		if ("HTML".equalsIgnoreCase(format)) {
			try {
				Tidy tidy = new Tidy();
				configureTidy(tidy);
				InputStream is = new ByteArrayInputStream(value.getBytes());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				parse(tidy, is, baos);
				value = baos.toString();
				is.close();
				baos.close();
				
				value = "<root>" + value + "</root>";
				StringWriter outputWriter = new StringWriter();
				StreamSource xslSource = new StreamSource(FileLocator.openStream(
						Platform.getBundle(Activator.PLUGIN_ID), new Path(
								"resources/html2odt.xsl"), false));
				StreamSource inputSource = new StreamSource(new StringReader(value));
				StreamResult outputResult = new StreamResult(outputWriter);
				Transformer transformer = TransformerFactory.newInstance()
						.newTransformer(xslSource);
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
						"yes");
				transformer.transform(inputSource, outputResult);
				output = outputWriter.toString();
				output = "&lt;drop/&gt;</text:h></text:p>" + output;
			} catch (IOException e) {
				ILogger logger = GendocServices.getDefault().getService(
						ILogger.class);
				logger.log("Unable to open XSL file for HTML transformation.",
						Status.ERROR);
			} catch (TransformerConfigurationException e) {
				ILogger logger = GendocServices.getDefault().getService(
						ILogger.class);
				logger.log("Error in HTML transformer configuration.", Status.ERROR);
			} catch (TransformerFactoryConfigurationError e) {
				ILogger logger = GendocServices.getDefault().getService(
						ILogger.class);
				logger.log("Error in HTML transformer factory configuration.",
						Status.ERROR);
			} catch (TransformerException e) {
				ILogger logger = GendocServices.getDefault().getService(
						ILogger.class);
				logger.log("Error transforming HTML.", Status.ERROR);
			}
		} 
		return output;
	}
	
    public static String rtfToHtml(Reader reader) throws IOException, BadLocationException {
        JEditorPane p = new JEditorPane();
        p.setContentType("text/rtf");
        EditorKit kitRtf = p.getEditorKitForContentType("text/rtf");
        kitRtf.read(reader, p.getDocument(), 0);
        kitRtf = null;
        EditorKit kitHtml = p.getEditorKitForContentType("text/html");
        Writer writer = new StringWriter();
        kitHtml.write(writer, p.getDocument(), 0, p.getDocument().getLength());
        return writer.toString();
    }
	
	public void parse(Tidy tidy, InputStream inputStream, OutputStream outputStream) {
		StringWriter stringWriter = new StringWriter();
		tidy.setErrout(new PrintWriter(stringWriter));
		tidy.parse(inputStream, outputStream);
		ILogger logger = GendocServices.getDefault().getService(ILogger.class);
		logger.log(stringWriter.getBuffer().toString(), ILogger.DEBUG);
	}

	public void configureTidy(Tidy tidy) {
		// info for debug or not
		tidy.setShowWarnings(true);
		tidy.setMakeClean(true);
		tidy.setQuiet(false);
		// these attributes are mandatories for html not well formed with
		// proprietary code (like microsoft) less destructive
		// than tidy.setMS2000(true)
//		tidy.setDropProprietaryAttributes(true);
		// older attributes
		tidy.setEncloseBlockText(true);
		tidy.setXmlOut(true);
		tidy.setLogicalEmphasis(true);
		// mandatory as the html is cleaned there is still errors
//		tidy.setForceOutput(true);
//		tidy.setPrintBodyOnly(true);
	}

	public void setVersion(String version) {
	}

	public void setInTable(String inTable) {
	}

	public void setIncludePic(String inTable) {
	}

	public void addAdditionalStyles(Document document) {

		// FIXME Huge hack to have borders in ODT Html tables
		String TABLEBORDERSTYLE = "TableWithBorder";

		try {
			document.jumpToStart();
			while (!content.equals(document.getXMLParser().getXmlFile()
					.getName())) {
				document.jumpToNextFile();
			}
			org.w3c.dom.Document documentNode = (org.w3c.dom.Document) document
					.getXMLParser().getDocument();

			Node stylesNode = getNodeFromXPath(documentNode,
					"//office:automatic-styles");

			String STYLE_NAMESPACE = "xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\"";
			Element newStyle = documentNode.createElementNS(STYLE_NAMESPACE,
					"style:style");
			newStyle.setAttributeNS(STYLE_NAMESPACE, "style:name",
					TABLEBORDERSTYLE);
			newStyle.setAttributeNS(STYLE_NAMESPACE, "style:family",
					"table-cell");
			Element styleProps = documentNode.createElementNS(STYLE_NAMESPACE,
					"style:table-cell-properties");
			String FO_NAMESPACE = "xmlns:fo=\"urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0\"";
			styleProps.setAttributeNS(FO_NAMESPACE, "fo:border-bottom",
					"0.002cm solid #000000");
			styleProps.setAttributeNS(FO_NAMESPACE, "fo:border-left",
					"0.002cm solid #000000");
			styleProps.setAttributeNS(FO_NAMESPACE, "fo:border-right",
					"0.002cm solid #000000");
			styleProps.setAttributeNS(FO_NAMESPACE, "fo:border-top",
					"0.002cm solid #000000");
			newStyle.appendChild(styleProps);
			stylesNode.appendChild(newStyle);

		} catch (DOMException e) {
			e.printStackTrace();
		}

	}

	private Node getNodeFromXPath(Node start, String expression) {
		IDocumentService documentService = GendocServices.getDefault()
				.getService(IDocumentService.class);
		ILogger logger = GendocServices.getDefault().getService(ILogger.class);
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			xpath.setNamespaceContext(documentService.getNameSpaceContext());
			XPathExpression expr = xpath.compile(expression);
			Object result = expr.evaluate(start, XPathConstants.NODE);
			if (result != null && result instanceof Node) {
				return (Node) result;
			}
		} catch (XPathExpressionException e) {
			logger.log("Invalid XPath expression : " + expression + "\n"
					+ e.getStackTrace().toString(), IStatus.ERROR);
		}
		return null;
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
