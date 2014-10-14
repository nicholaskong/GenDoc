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
 *  Caroline Bourdeu d'Aguerre (Atos Origin) caroline.bourdeudaguerre@atosorigin.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.documents;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.namespace.NamespaceContext;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.services.IService;
import org.eclipse.gendoc.services.exception.DocumentServiceException;
import org.eclipse.gendoc.services.exception.InvalidContentException;
import org.w3c.dom.Node;

/**
 * Document service interface
 */
public interface IDocumentService extends IService
{

	/**
     * Inject node(s) corresponding to newNodeContent text just before nodeToBeReplaced in the XML tree
     * 
     * @param nodeToBeReplaced the node to be replaced
     * @param newNodeContent the text content of the future node
     * @return the first node added.
     */
    Node injectNode(Node nodeToBeReplaced, String newNodeContent) throws InvalidContentException;

    Node asNode(String text) throws InvalidContentException;

    String asText(Node nodeBegin) throws InvalidContentException;
    
    String addNamingSpaces(String nodes);
    
    String addNamingSpaces(String nodes, String label);

    /**
     * Clean tags inside a node
     * 
     * @param currentNode
     * @param tagLabels
     * @return
     * @throws InvalidContentException 
     */
    Node cleanTags(Node currentNode, List<String> tagLabels) throws InvalidContentException;

    /**
     * Clean text content . Only parts of the text matching patternsToClean are cleaned
     * 
     * @param text text content to clean
     * @param patternsToClean pattern to check and to clean
     * @return cleaned text
     */
    String cleanContent(String text, List<Pattern> patternsToClean);

    void saveDocument(Document document, String path) throws DocumentServiceException;

    Document getDocument();

    void setDocument(Document document);

    String cleanTextTag(String textTag);

    String cleanTagContent(String text, List<String> labels);

    boolean isList(String label);

	boolean isListItem(String label);
    
    boolean isPara(String label);

    boolean isTable(String label);
    
    boolean isRow(String label);
    
    String getListLabel();
    
    String getListId(Node n);
    
    String getContinueList(Node currentNode, String idList) throws InvalidContentException;
    
    String getTableLabel();
    
    IAdditionalResourceService getAdditionalResourceService();

    /**
     * Remove the file f from disk. If f is a directory, remove all sub files
     * @param f
     */
    void clean(File f);

    /**
     * Get the pattern that will be used to replace a multi paragraphs string with nobr
     * to a single paragraph without nobr
     * 
     */
    Pattern getNobrReplacePattern();

    
    /**
     * Get the namespace context for the document
     * @return a namespace context
     */
    NamespaceContext getNameSpaceContext();
}
