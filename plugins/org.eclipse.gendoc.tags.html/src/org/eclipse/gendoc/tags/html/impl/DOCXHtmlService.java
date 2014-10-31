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
 * Anass RADOUANI (AtoS) anass.radouani@atos.net - RTF
 * Anne Haugommard (Atos) anne.haugommard@atos.net - update JTidy version
 * 
 *****************************************************************************/
package org.eclipse.gendoc.tags.html.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.services.IRegistryService;
import org.eclipse.gendoc.services.docx.DOCXAdditionalResourceService;
import org.eclipse.gendoc.services.docx.DOCXDocumentService;
import org.eclipse.gendoc.services.docx.DOCXMimeHtmlService;
import org.eclipse.gendoc.services.utils.DeleteFileRunnable;
import org.eclipse.gendoc.tags.html.Activator;
import org.eclipse.gendoc.tags.html.IHtmlService;
import org.w3c.tidy.Tidy;

/**
 * The docx html service is used to include html in docx documents as external
 * chunks (for word 2007) or convert it in docx tags by running an xsl transform
 * (for word 2003).
 * 
 * @author Kris Robertson
 */
public class DOCXHtmlService extends AbstractService implements IHtmlService {

	private List<String> tempFiles = new LinkedList<String>();

	private boolean use2003Compatibility = false;
	private boolean isInTable = false;
	private boolean includePic = false;
	private String format = "HTML";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gendoc.services.IService#clear()
	 */
	@Override
	public void clear() {
		for (String path : this.tempFiles) {
			File file = new File(path);
			file.delete();
		}
		this.tempFiles.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gendoc.html.IHtmlService#convert(java.lang.String)
	 */
	public String convert(String content) {
		
		if (content.length() > 0) {
			// clean DOCX
			content = cleanDocxTagsFromHtml(content);
			// Fix cleaning tag problem
			content = content.replace("richText&gt;", "").replaceAll("<cr>","<br>").replaceAll("<CR>","<BR>").replaceAll("<N>", "");
			if (content.length() > 0) {
				if ("HTML".equalsIgnoreCase(format) && use2003Compatibility) {
					return generateDOCXTagsFromHtml(content);
				} else if ("HTML".equalsIgnoreCase(format) || "RTF".equalsIgnoreCase(format)) {
					String output = "";
					DOCXDocumentService documentService = GendocServices
							.getDefault().getService(IDocumentService.class);
					DOCXAdditionalResourceService additonalResourceService = (DOCXAdditionalResourceService) documentService
							.getAdditionalResourceService();
	
					// Hack for RichText tag inside tables
					final String filePath = this.createFile(content);
					IRegistryService registry = GendocServices.getDefault()
							.getService(IRegistryService.class);
					registry.addCleaner(new DeleteFileRunnable(new File(filePath)));
					if (filePath != null) {
						String id = additonalResourceService.includeFile(filePath);
						output += "&lt;drop/&gt;";
						if (isInTable) {
							output += "</w:t></w:r>";
						}
						output += "</w:p><w:altChunk xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\""
								+ id + "\" />";
						if (isInTable) {
							output += "<w:p><w:r><w:t>";
						}
						return output;
					}
				}
			}
		} 
		return "";
	}

	private String generateDOCXTagsFromHtml(String html) {
		String output = "";
		try {
			Tidy tidy = new Tidy();
			configureTidy(tidy);
			InputStream is = new ByteArrayInputStream(html.getBytes());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			parse(tidy, is, baos);
			is.close();
			String cleanedXhtml = baos.toString();
			baos.close();
			cleanedXhtml = cleanedXhtml.replace("\r", "");
			cleanedXhtml = cleanedXhtml.replace("\n", "");

			cleanedXhtml = "<root>" + cleanedXhtml + "</root>";
			StringWriter outputWriter = new StringWriter();
			StreamSource xslSource = new StreamSource(FileLocator.openStream(
					Platform.getBundle(Activator.PLUGIN_ID), new Path(
							"resources/html2docx.xsl"), false));
			StreamSource inputSource = new StreamSource(new StringReader(
					cleanedXhtml));
			StreamResult outputResult = new StreamResult(outputWriter);
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer(xslSource);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
			transformer.transform(inputSource, outputResult);
			output = outputWriter.toString();
			output = "&lt;drop/&gt;</w:t></w:r></w:p>" + output;
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
		return output;
	}

	/**
	 * Creates a file to be included in the document
	 * 
	 * @param content
	 *            the content to include in the file
	 * @return the file path or null if the file could not be created
	 */
	private String createFile(String content) {
		String path = Activator.getDefault().getStateLocation().toOSString()
				+ File.separator + EcoreUtil.generateUUID();
		if ("HTML".equalsIgnoreCase(format)) {
			if (includePic) {
				path = path + ".mht";
			} else {
				path = path + ".xhtml";
			}
		} else if("RTF".equalsIgnoreCase(format)) {
			path = path + ".rtf";
		}
		try {
			FileWriter writer = new FileWriter(path);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if ("HTML".equalsIgnoreCase(format)) {
				Tidy tidy = new Tidy();
				InputStream is = new ByteArrayInputStream(content.getBytes());
				configureTidy(tidy);
				if (includePic) {
					DOCXDocumentService documentService = GendocServices
							.getDefault().getService(IDocumentService.class);
					DOCXAdditionalResourceService additonalResourceService = (DOCXAdditionalResourceService) documentService
							.getAdditionalResourceService();
					DOCXMimeHtmlService mimeHtmlService = (DOCXMimeHtmlService) documentService
							.getMimeHtmlService();
					parse(tidy, is, baos);
					String tidiedHtml = baos.toString();
					baos.close();
					String mHtml = mimeHtmlService
							.convertToMimeHtml(tidiedHtml);
					Set<String> fileExt = mimeHtmlService.getFileExtensions();
					additonalResourceService.includeExtensions(fileExt);
					String tidiedHtml2 = mimeHtmlService.getBeginPart() + mHtml
							+ mimeHtmlService.getEndPart();
					writer.write(tidiedHtml2);
					writer.flush();
				} else {
					parse(tidy, is, baos);
					writer.write(baos.toString());
					writer.flush();
					baos.close();
				}
			} else if ("RTF".equalsIgnoreCase(format)) {
				writer.write(content);
				writer.flush();
			}
			this.tempFiles.add(path);
			writer.close();
		} catch (IOException e) {
			ILogger logger = GendocServices.getDefault().getService(
					ILogger.class);
			logger.log("Unable to create html file.", Status.ERROR);
			return null;
		}
		return path;
	}

	private void parse(Tidy tidy, InputStream inputStream, OutputStream outputStream) throws IOException {
		StringWriter stringWriter = new StringWriter();
		tidy.setErrout(new PrintWriter(stringWriter));
		tidy.parse(inputStream, outputStream);
		ILogger logger = GendocServices.getDefault().getService(ILogger.class);
		logger.log(stringWriter.getBuffer().toString(), ILogger.DEBUG);
	}

	public void configureTidy(Tidy tidy) {
		if (!includePic) {
			// output type
			tidy.setXHTML(true);
		}
		// info for debug or not
		tidy.setShowWarnings(true);
		tidy.setMakeClean(true);
		tidy.setQuiet(false);
		// these attributes are mandatories for html not well formed with
		// proprietary code (like microsoft) less destructive
		// than tidy.setMS2000(true)
		// older attributes
		tidy.setEncloseBlockText(true);
		tidy.setXmlOut(true);
		// mandatory as the html is cleaned there is still errors
//		tidy.setForceOutput(true);
	}

	public void setVersion(String version) {
		if (version != null && version.equals("msw2003")) {
			use2003Compatibility = true;
		}
	}

	public void addAdditionalStyles(Document document) {
		// Do nothing (In docx, styles from HTML are managed automatically.
	}

	public void setInTable(String inTable) {
		if (inTable != null && inTable.equals("true")) {
			isInTable = true;
		}
	}

	public void setIncludePic(String inPic) {
		if (inPic != null && inPic.equals("true")) {
			includePic = true;
		}
	}

	private String cleanDocxTagsFromHtml(String htmlContent) {
		return htmlContent.replaceAll("<w:[^<]*>","").replaceAll("</w:[^<]*>", "").replaceAll("<o:[^<]*>","").replaceAll("</o:[^<]*>", "");
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
