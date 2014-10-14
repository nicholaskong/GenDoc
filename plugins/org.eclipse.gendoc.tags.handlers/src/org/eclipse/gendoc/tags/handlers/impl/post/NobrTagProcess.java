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
 * 
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.impl.post;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.document.parser.documents.Document.PROPERTY;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.process.AbstractStepProcess;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;
import org.w3c.dom.Node;

public class NobrTagProcess extends AbstractStepProcess
{

	protected static final Pattern NOBR_TAG_PATTERN = Pattern.compile("<\\s*" + RegisteredTags.NOBR + "\\s*/\\s*>", Pattern.MULTILINE);

	IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
	Pattern replacePattern = documentService.getNobrReplacePattern();

	List<Node> currentMerging = null;
	List<List<Node>> nodeSetToMerge = null;

	@Override
	protected void doRun() throws GenDocException {
		currentMerging = null;
		nodeSetToMerge = new ArrayList<List<Node>>();

		super.doRun();

		for (List<Node> nodesToMerge : nodeSetToMerge) {
			StringBuffer mergedText = new StringBuffer();

			for (Node node : nodesToMerge) {
				mergedText.append(documentService.asText(node));
			}

			Matcher matcher = replacePattern.matcher(mergedText);
			StringBuffer replacedText = new StringBuffer();
			while (matcher.find()) {
				matcher.appendReplacement(replacedText, "");
			}
			matcher.appendTail(replacedText);

			documentService.injectNode(nodesToMerge.get(0), replacedText.toString());
			for (Node node : nodesToMerge) {
				Node parent = node.getParentNode();
				if (parent != null) {
					parent.removeChild(node);
				}
			}
		}
	}

	@Override
	protected void step(Document document) throws GenDocException
	{
		Node currentNode = document.getXMLParser().getCurrentNode();

		if (documentService.isPara(currentNode.getNodeName())) {
			String text = (String)document.get(PROPERTY.text);

			if (text != null && NOBR_TAG_PATTERN.matcher(text).find()) {
				if (currentMerging == null) {
					currentMerging = new ArrayList<Node>();
				}
				currentMerging.add(currentNode);
			} else {
				if (currentMerging != null) {
					currentMerging.add(currentNode);
					nodeSetToMerge.add(currentMerging);
					currentMerging = null;
				}
			}
		}


	}
}
