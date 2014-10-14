/*****************************************************************************
 * Copyright (c) 2009 ATOS ORIGIN INTEGRATION.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Tristan FAURE (ATOS ORIGIN INTEGRATION) tristan.faure@atosorigin.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.document.parser.documents.helper;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.gendoc.document.parser.documents.Unzipper;
import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.documents.Document.CONFIGURATION;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * The Class OfficeHelper.
 */
public final class OfficeHelper
{

	/**
	 * Fill collection.
	 * 
	 * @param unzipper the unzipper
	 * @param headers2 the headers2
	 * @param relationshipsHeader the relationships header
	 * @param idForDocument the id for document
	 */
	public static void fillCollection(Unzipper unzipper, Collection<XMLParser> headers2, String relationshipsHeader, CONFIGURATION idForDocument, String relsFile)
	{
		fillCollection(unzipper, headers2, relationshipsHeader, idForDocument,relsFile, null);
	}

	/**
	 * Retrieve the sub document file with the id given by parsing the 
	 * <tt>document.xml.rels</tt> XML file 
	 * 
	 * @param unzipper The unzipper containing all unzipped file of docx.
	 * @param relationshipsHeader The relationship header
	 * @param idForDocument the id for document
	 * @param relsFile The relationship XML file.
	 * @param id The id of sub document.
	 * 
	 * @return The document file name corresponding to the relationship identifier.
	 */
	public static String retrieveRelationshipSubDocument(Unzipper unzipper, String relationshipsHeader, String relsFile, String id) {
		if (relationshipsHeader != null && unzipper != null)
		{
			File docRels = unzipper.getFile(relsFile);
			if (docRels != null)
			{
				XMLParser documentRels = new XMLParser(docRels);
				do 
				{
					if ("Relationship".equals(documentRels.getCurrentNode().getNodeName()))
					{
						NamedNodeMap attributes = documentRels.getCurrentNode().getAttributes();
						Node item = attributes.getNamedItem("Type");
						if (item != null && relationshipsHeader.equals(item.getTextContent()))
						{
							Node target = attributes.getNamedItem("Target");
							Node idXml = attributes.getNamedItem("Id");
							Node external = attributes.getNamedItem("TargetMode");
							if (target != null && idXml != null && external != null && idXml.getNodeValue().equals(id) && external.getNodeValue().equalsIgnoreCase("external"))
							{
								return target.getTextContent();
							}
						}
					}
				}
				while (documentRels.next());
			}
		}
		return null;
	}

	/**
	 * Fill collection.
	 * 
	 * @param unzipper the unzipper
	 * @param headers2 the headers2
	 * @param relationshipsHeader the relationships header
	 * @param idForDocument the id for document
	 * @param toSearch the to search
	 */
	public static void fillCollection(Unzipper unzipper, Collection<XMLParser> headers2, String relationshipsHeader, CONFIGURATION idForDocument, String relsFile, String toSearch)
	{
		if (headers2 == null)
		{
			headers2 = new LinkedList<XMLParser>();
		}
		if (relationshipsHeader != null)
		{
			File docRels = unzipper.getFile(relsFile);
			XMLParser documentRels = new XMLParser(docRels);
			do 
			{
				if ("Relationship".equals(documentRels.getCurrentNode().getNodeName()))
				{
					NamedNodeMap attributes = documentRels.getCurrentNode().getAttributes();
					Node item = attributes.getNamedItem("Type");
					if (item != null && relationshipsHeader.equals(item.getTextContent()))
					{
						Node target = attributes.getNamedItem("Target");
						Node id = attributes.getNamedItem("Id");
						if (target != null && id != null)
						{
							boolean ok = toSearch == null || (toSearch != null && toSearch.equals(id.getTextContent()));
							if (ok)
							{
								File f = unzipper.getFile(target.getTextContent());
								if (f != null && f.exists())
								{
									headers2.add(new XMLParser(f,idForDocument));
									if (toSearch != null)
									{
										break ;
									}
								}
							}
						}
					}
				}
			}
			while (documentRels.next());
		}
	}

	private OfficeHelper ()
	{
		// DO NOTHING
	}

}
