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

import java.util.LinkedList;
import java.util.regex.Pattern;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.document.parser.documents.Document.PROPERTY;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.process.AbstractStepProcess;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;
import org.w3c.dom.Node;

public class DropTagProcess extends AbstractStepProcess
{
	protected static final Pattern DROP_TAG_PATTERN = Pattern.compile("<\\s*" + RegisteredTags.DROP + "\\s*/\\s*>", Pattern.MULTILINE);
	
	protected IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
	
	protected LinkedList<Node> nodesToRemove;

	@Override
	protected void doRun() throws GenDocException {
		nodesToRemove = new LinkedList<Node>();

		super.doRun();

		for (Node n : nodesToRemove) {
			Node parent = n.getParentNode();
			if (parent != null) {
				parent.removeChild(n);
			}
		}
	}

	@Override
	protected void step(Document document) throws GenDocException
	{
		Node currentNode = document.getXMLParser().getCurrentNode();
		if (documentService.isPara(currentNode.getNodeName())) {
			String text = (String)document.get(PROPERTY.text);
			if (text != null && DROP_TAG_PATTERN.matcher(text).find()) {
				nodesToRemove.add(currentNode);
			}
		}
	}

}
