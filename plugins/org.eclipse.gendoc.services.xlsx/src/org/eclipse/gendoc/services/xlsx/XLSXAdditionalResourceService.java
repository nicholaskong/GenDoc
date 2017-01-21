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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.xlsx.XLSXDocument;
import org.eclipse.gendoc.document.parser.xlsx.helper.XLSXHelper;
import org.eclipse.gendoc.document.parser.xlsx.helper.XPathXlsxUtils;
import org.eclipse.gendoc.documents.AdditionalResourceService;
import org.eclipse.gendoc.documents.FileRunnable;
import org.eclipse.gendoc.documents.IAdditionalResourceService;
import org.eclipse.gendoc.documents.IDocumentManager;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.documents.IImageService;
import org.eclipse.gendoc.documents.MimeTypes;
import org.eclipse.gendoc.documents.ResourceRunnable;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.services.exception.AdditionalResourceException;
import org.eclipse.gendoc.services.utils.DefaultImageExtensionUtils;
import org.w3c.dom.Element;

// TODO: Provide a common ooxml abstract implementation of IAdditionalResourceService
// TODO: The image service should add the relation to images itself  
public class XLSXAdditionalResourceService extends AdditionalResourceService implements IAdditionalResourceService {
	private IImageService imageService; 
	private List<Element> relationshipsToAdd;
	protected Map<String, String> externalChunkMap;

	public XLSXAdditionalResourceService() {
		super();
		relationshipsToAdd = new ArrayList<Element>();
		externalChunkMap = new LinkedHashMap<String, String>();
		imageService = new XLSXImageService();
	}

	@Override
	public IImageService getImageService() {
		return imageService;
	}

	@Override
	public String includeFile(String filePath) {
		String id = generateUniqueId();

		externalChunkMap.put(id, filePath);

		// Add file extension to extensions list
		fileExtensions.add(Path.fromOSString(filePath).getFileExtension());

		return id;
	}

	@Override
	public void includeExtensions(Set<String> fileExt) {
		fileExtensions.addAll(fileExt);
	}

	@Override
	public void addAdditionalResourcesToDocument() throws AdditionalResourceException {

		String mediaLoc = getResourceFolder();
		IDocumentManager docManager = GendocServices.getDefault().getService(IDocumentManager.class);		
		XLSXDocument xlsxDocument = (XLSXDocument)docManager.getDocTemplate();

		XLSXDocumentService documentService = GendocServices.getDefault()
				.getService(IDocumentService.class);
		File unzipLoc = ((XLSXDocument) documentService.getDocument())
				.getUnzipLocationDocumentFile();

		// Get directory "media" and add it if not present
		File mediaDir = new File(mediaLoc);
		if (!mediaDir.exists()) {
			mediaDir.mkdir();
		}
		// Add images resources to document
		addImagesResourcesToDocument(mediaLoc);

		// Add external file resources to document
		addExternalResourcesToDocument(mediaLoc);

		// Add mappings between images and files
		//String relsLocation = addRelationShips(unzipLoc);
		String relsLocation = SEPARATOR + "xl" + SEPARATOR + "drawings" + SEPARATOR + "_rels" + SEPARATOR;

		// Add mapping files for each document XML (document.xml, headers,
		// footers)
		addRelationShipFiles(xlsxDocument, relsLocation);

		// Modify file with content types [Content_Types].xml to add unknown
		// file extensions
		modifyContentTypes(xlsxDocument);

	}

	public String addRunnableResourceToDocument(String mediaLoc,
			String diagramKey) throws AdditionalResourceException {
		if (runnableMap.get(diagramKey) == null) {
			throw new AdditionalResourceException("Image with id '"
					+ diagramKey + "' cannot be found.");
		}
		// Get runnableResource

		ResourceRunnable runnable = runnableMap.get(diagramKey);

		String extension = getFileExtensionFromRunnable(runnable);

		// Run the resource
		runnable.run(diagramKey, getResourceFolder());

		// add relationship in drawing*.xml.rels
		relationshipsToAdd.add(newImageRelationship(diagramKey, diagramKey + "."+ extension));
		fileExtensions.add(extension);
		return getResourceFolder() + SEPARATOR + diagramKey + "." + extension;

	}

	@Override
	protected String getRelativeResourceFolder() {
		return "xl" + SEPARATOR + "media";
	}

	private void addImagesResourcesToDocument(String mediaLoc)
			throws AdditionalResourceException {

		// For each image
		for (String imageKey : imagesMap.keySet()) {
			// add image as file in the media directory
			File imageFile = new File(imagesMap.get(imageKey));

			// Extract and normalize file name
			String imageLink = imageKey
					+ "."
					+ Path.fromOSString(imageFile.getAbsolutePath())
							.getFileExtension();
			try {
				copyImage(imageFile, mediaLoc + SEPARATOR + imageLink);
			} catch (IOException e) {
				throw new AdditionalResourceException("File '"
						+ imageFile.getAbsolutePath() + "'cannot be copied.", e);
			}
			// add relationship in document.xml.rels
			relationshipsToAdd.add(newImageRelationship(imageKey, imageLink));

			if (!imageFile.exists()) {
				throw new AdditionalResourceException(
						"An image cannot be generated and has been replaced by a red cross. Cause: No image found at location:"
								+ imageFile.getAbsolutePath());
			}
		}
	}
	
	// TODO: Check if external docs can be inserted.
	private void addExternalResourcesToDocument(String mediaLoc) throws AdditionalResourceException {
/*		for (String key : externalChunkMap.keySet()) {
			// add external file as file in the media directory
			File file = new File(externalChunkMap.get(key));
			// Extract file
			String externalLink = key
					+ "."
					+ Path.fromOSString(file.getAbsolutePath())
							.getFileExtension();// normalizeString(getFileName(externalChunkMap.get(key)));
			try {
				copyFile(file, mediaLoc + SEPARATOR + externalLink);
			} catch (IOException e) {
				throw new AdditionalResourceException("File '"
						+ file.getAbsolutePath() + "'cannot be copied.", e);
			}
			// add relationship in document.xml.rels
			relationShipsToAdd.append(newExternalChunkRelationship(key,
					externalLink));
			if (!file.exists()) {
				throw new AdditionalResourceException(
						"An external file cannot be generated. Cause: No file found at location:"
								+ file.getAbsolutePath());
			}
		}*/
	}

	private void addRelationShipFiles(XLSXDocument xlsxDocument, String relsLocation)
			throws AdditionalResourceException {
		
		if (relationshipsToAdd == null) 
			return;
		
		ILogger logger = GendocServices.getDefault().getService(ILogger.class);

		// Get Word directory
		File wordDir = new File(xlsxDocument.getUnzipLocationDocumentFile()+
				File.separator+relsLocation+File.separator+"..");
		if (!wordDir.isDirectory()) {
			return;
		}

		String[] fileNames = wordDir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				name = name.toLowerCase();
				return name.startsWith("drawing") && name.endsWith(".xml");
			}
		});
		try {
			// Add new relationships to all headers and footers
			for (String fileName : fileNames) {
				String relpath = relsLocation+ fileName + ".rels";
				
				XMLParser relParser = xlsxDocument.getSubdocument(relpath);				
				if (relParser == null) {
					relParser = xlsxDocument.createSubdocument(relpath, "<?xml version=\"1.0\" encoding=\"windows-1250\"?> "
							+ "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"/>");
				}

				try {
					for (Element relToAdd : relationshipsToAdd) {
							if (XPathXlsxUtils.evaluateNode(relParser.getDocument(), "/rel:Relationships/rel:Relationship[@Id='"+
									relToAdd.getAttribute("Id")+"']") != null) 									
								continue;
							relParser.getDocument().getDocumentElement().appendChild(
									relParser.getDocument().importNode(relToAdd, true));
					}
				} catch (XPathExpressionException e) {
					throw new IllegalArgumentException(e);
				}
			}

		} catch (IOException e) {
			logger.log(
					"Mapping files for headers and footers are not copied properly. Some images in headers and "
					+ "footers can be missing.",
					IStatus.INFO);
		}
	}

	private void modifyContentTypes(XLSXDocument xlsxDocument)
			throws AdditionalResourceException {
			
		XMLParser ctParser = xlsxDocument.getSubdocument("/[Content_Types].xml");
		try {
			for (String extension : fileExtensions) {
				if (XPathXlsxUtils.evaluateNode(ctParser.getDocument(), 
						"/ct:Types/ct:Default[@Extension='"+extension+"']") != null)
					continue;
				Element extEl = ctParser.getDocument().createElementNS(
						XLSXHelper.CONTENT_TYPES_NAMESPACE, "Default");
				extEl.setAttribute("ContentType", MimeTypes.getMimeTypefromExtension(extension));
				extEl.setAttribute("Extension", extension);
				ctParser.getDocument().getDocumentElement().insertBefore(
						extEl, ctParser.getDocument().getDocumentElement().getFirstChild());
			}
		} catch (Exception e) {
			throw new AdditionalResourceException(e);
		}		
	}

	private Element newImageRelationship(String imageKey, String imageLink) {
		try {
			return (Element)XPathXlsxUtils.parserXmlFragment("<rel:Relationship Id=\""
					+ imageKey
					+ "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"../media/"
					+ imageLink + "\"/>");
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}


	private String newExternalChunkRelationship(String key, String target) {
		return "<Relationship Id=\""
				+ key
				+ "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/aFChunk\" Target=\"../media/"
				+ target + "\"/>";
	}
	
	private String getFileExtensionFromRunnable(ResourceRunnable runnable) {
		if (runnable instanceof FileRunnable) {
			return ((FileRunnable) runnable).getFileExtension();
		}
		return DefaultImageExtensionUtils.getDefaultImageExtension();
	}
}
