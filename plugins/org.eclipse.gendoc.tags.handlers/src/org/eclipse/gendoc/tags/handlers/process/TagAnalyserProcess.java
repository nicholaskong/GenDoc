/*****************************************************************************
 * Copyright (c) 2011 Atos
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Caroline Bourdeu d'Aguerre (Atos Origin) caroline.bourdeudaguerre@atosorigin.com - Initial API and implementation
 * Papa Malick WADE (Atos Origin) papa-malick.wade@atosorigin.com - development on the id
 * Tristan FAURE (Atos) tristan.faure@atos.net - refactoring 
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.process;

import java.util.LinkedList;
import org.eclipse.gendoc.tags.handlers.process.TagAnalyserFileBuffer;
import org.eclipse.gendoc.tags.handlers.process.buffers.ITagAnalyserBuffer;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.process.AbstractStepProcess;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.IncompleteTagException;
import org.eclipse.gendoc.services.exception.InvalidContentException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.ITagExtensionService;
import org.eclipse.gendoc.tags.ITagHandler;
import org.eclipse.gendoc.tags.handlers.ITagHandlerService;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;
import org.eclipse.gendoc.tags.parsers.ITagParserService;
import org.w3c.dom.Node;

public abstract class TagAnalyserProcess extends AbstractStepProcess {

    /**
     * List of top level tags
     */
    protected List<String> topLevelTagNames;
    
    /**
     * Full list of all available tag names (more than just top level tags)
     */
    private List<String> allTagNames;

    protected List<Pattern> tagNamePatterns;

    /**
     * Constructs a new GendocManager
     */
    public TagAnalyserProcess()
    {
        super();
        this.init();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.AbstractProcess#step(org.eclipse.gendoc.document.parser.documents.Document)
     */
    @Override
    protected void step(Document document) throws GenDocException
    {
        IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
        ITagParserService tagParserService = GendocServices.getDefault().getService(ITagParserService.class);

        // List of document nodes that will be replaced
        List<Node> nodesList = new LinkedList<Node>();

        // Got to the next paragraph that contains a tag
        boolean next = this.goToNextTextWithTag(document);
        if (!next)
        {
            return;
        }
        Node currentNode = document.getXMLParser().getCurrentNode();

        // Clean the tags - remove all document tags inside ALL  the gendoc tags 
        currentNode = documentService.cleanTags(currentNode, this.allTagNames);

        document.getXMLParser().setCurrentNode(currentNode);

        nodesList.add(currentNode);

        StringBuffer bufferedText = new StringBuffer(documentService.cleanTagContent(documentService.asText(currentNode), this.topLevelTagNames));

        // Split the string into tags
        List<ITag> tags = tagParserService.parse(null, bufferedText.toString(), this.topLevelTagNames);

        // while last tag is not closed (IncompleteTag)
        while (next && !(tags.get(tags.size() - 1)).isComplete())
        {
            // Get next paragraph
            next = this.goToNextText(document, bufferedText, nodesList);

            // Get current node
            currentNode = document.getXMLParser().getCurrentNode();

            // Clean the tags - remove all document tags inside ALL the gendoc tags (not only top level tags)
            currentNode= documentService.cleanTags(currentNode, this.allTagNames);
            document.getXMLParser().setCurrentNode(currentNode);
            nodesList.add(currentNode);

            // Concatenate paragraphs
            bufferedText.append(documentService.cleanTagContent(documentService.asText(currentNode), this.topLevelTagNames));
            tags = tagParserService.parse(null, bufferedText.toString(), this.topLevelTagNames);
        }

        // If the last tag is incomplete
        if (!tags.get(tags.size() - 1).isComplete())
        {
            ITag tag = tags.get(tags.size() - 1);
            
          
            //retrieve the tag
            String tagIdFound = tag.getAttributes().get(RegisteredTags.ID);
            
            if (null == tagIdFound)
            {
                throw new IncompleteTagException(tag.getName());   
            }
            else
            {
                throw new IncompleteTagException(tag.getName(),tagIdFound);  
            }
            
           
        }

        if (tags.size() > 0)
        {
            executeAndInjectTags(document, documentService, nodesList, currentNode, tags);
        }

        this.worked(1);
    }

    protected void executeAndInjectTags(Document document, IDocumentService documentService, List<Node> nodesList, Node currentNode, List<ITag> tags) throws GenDocException, InvalidContentException
    {
        // Execute the tags
        StringBuffer finalText = executeTags(tags);
        // execute tags returns null if the tag has been executed otherwise the xml remains the same
        if (finalText != null)
        {
            // inject them in XML
            injectTagsExecuted(document, documentService, nodesList, currentNode, finalText);
        }
    }

    protected void injectTagsExecuted(Document document, IDocumentService documentService, List<Node> nodesList, Node currentNode, StringBuffer finalText) throws InvalidContentException
    {
    	ITagAnalyserBuffer buffer = GendocServices.getDefault().getService(ITagAnalyserBuffer.class);
        // Inject the text in nodes
    	buffer.bufferize (document, currentNode, finalText, nodesList);
    }

    @Override
    protected void postRun() {
    	super.postRun();
    	ITagAnalyserBuffer buffer = GendocServices.getDefault().getService(ITagAnalyserBuffer.class);
    	buffer.flush();
    }
    
    /**
     * Check if one of the tag is present in the given string
     * 
     * @param text
     * @return true is the text contains one the tag
     */
    protected boolean containsTag(String text)
    {
        boolean match = false;
        for (Pattern pattern : this.tagNamePatterns)
        {
            if (pattern.matcher(text).matches())
            {
                match = true;
                break;
            }
        }
        return match;
    }

    /**
     * Runs the handler for each tag.
     * 
     * @param tags the tags to execute
     * @return the replacement text or null if the tag is 
     * @throws GenDocException
     */
    protected StringBuffer executeTags(List<ITag> tags) throws GenDocException
    {
        boolean result = true ;
        ITagHandlerService tagHandlerService = GendocServices.getDefault().getService(ITagHandlerService.class);
        StringBuffer finalText = new StringBuffer("");
        for (ITag tag : tags)
        {
            result &= executeOneTag(tagHandlerService, finalText, tag);
            
        }
        if (!result)
        {
            return null ;
        }
        return finalText;
    }

    /**
     * @param tagHandlerService
     * @param finalText
     * @param tag
     * @return true if the tag has been processed. false means content is unchanged
     * @throws GenDocException
     */
    protected boolean executeOneTag(ITagHandlerService tagHandlerService, StringBuffer finalText, ITag tag) throws GenDocException
    {
        ITagHandler tagHandler = tagHandlerService.getHandlerFor(tag);
        if (tagHandler != null)
        {
            finalText.append(tagHandler.run(tag));
        }
        else
        {
            finalText.append(tag.getRawText());
        }
        return true ;
    }

    /**
     * Run a next on the given document until the current node own text or table
     * 
     * @param document
     * @param nodesToRemove list of nodes that contain tag or tag content and that will be removed in final document
     * @return the return of the next method (false of the document is at the end)
     * @throws InvalidContentException
     */
    protected boolean goToNextText(Document document, StringBuffer buffer, List<Node> nodesToRemove) throws InvalidContentException
    {
        IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);

        this.worked(1);
        boolean next = document.next();
        Object extratedObject = document.get(Document.PROPERTY.text);

        Node currentNode = document.getXMLParser().getCurrentNode();
        while (next && this.isTextKO(nodesToRemove, extratedObject, currentNode))
        {
            this.worked(1);
            next = document.next();
            if (next)
            {
                currentNode = document.getXMLParser().getCurrentNode();
                extratedObject = document.get(Document.PROPERTY.text);
                if ("".equals(extratedObject) && documentService.isPara(currentNode.getNodeName()) && !this.isAlreadyProcessed(nodesToRemove, currentNode))
                {
                    buffer.append(documentService.asText(currentNode));
                    nodesToRemove.add(currentNode);
                }
            }

        }

        return next;
    }

    /**
     * Run a next on the given document until the current node own valid text (para containing "<" and ">")
     * 
     * @param document
     * @return the return of the next method (false of the document is at the end)
     */
    protected boolean goToNextTextWithTag(Document document)
    {
        boolean next = true;
        Object extratedObject = document.get(Document.PROPERTY.text);
        while (next && !this.isValidText(document, extratedObject))
        {
            this.worked(1);
            next = document.next();
            if (next)
            {
                extratedObject = document.get(Document.PROPERTY.text);
            }
        }
        return next;
    }

    /**
     * Initialize all the needed fields
     */
    protected void init()
    {
        ITagExtensionService tagExtensionService = GendocServices.getDefault().getService(ITagExtensionService.class);
        // Initialize tag names
        this.topLevelTagNames = tagExtensionService.getTopLevelTagNames();
        this.allTagNames = tagExtensionService.getAllTagNames();
        this.tagNamePatterns = new LinkedList<Pattern>();
        for (String label : this.topLevelTagNames)
        {
            this.tagNamePatterns.add(Pattern.compile(".*<" + label + ".*>.*"));
            this.tagNamePatterns.add(Pattern.compile(".*</" + label + ".*>.*"));
        }
    }

    /**
     * All nodes that have already been processed are present in the node list. If the current node or one of its parent
     * hierarchy is present in the node list, it is already process
     * 
     * @param nodesList
     * @param node
     * @return true if the node (or one of its parents) has been processed, otherwise false
     */
    protected boolean isAlreadyProcessed(List<Node> nodesList, Node node)
    {
        boolean isAlreadyProcessed = false;
        Node parent = node;
        while ((parent != null) && !isAlreadyProcessed)
        {
            if (nodesList.contains(parent))
            {
                isAlreadyProcessed = true;
            }
            parent = parent.getParentNode();
        }
        return isAlreadyProcessed;
    }

    /**
     * Returns true if it is a text valid => the node has not been processed => the text id different from "" or it must
     * be a list or a table
     * 
     * @param nodesToRemove
     * @param extratedObject
     * @param currentNode
     * @return
     */
    protected boolean isTextKO(List<Node> nodesToRemove, Object extratedObject, Node currentNode)
    {
        IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
        boolean ko = false;
        boolean isAlreadyProcessed = this.isAlreadyProcessed(nodesToRemove, currentNode);
        if (isAlreadyProcessed)
        {
            ko = true;
        }
        else if ("".equals(extratedObject) && !documentService.isList(currentNode.getNodeName()) && !documentService.isTable(currentNode.getNodeName()))
        {
            ko = true;
        }
        return ko;
    }

    /**
     * Check if the text is valid. A text is valid it is not null if it is a instance of string and if it contains one
     * of the tag to match
     * 
     * @param extratedObject
     * @param extratedObject
     * @return true if it is valid
     */
    protected boolean isValidText(Document document, Object extratedObject)
    {
        boolean isValid = false;
        if (!"".equals(extratedObject) && this.containsTag((String) extratedObject))
        {
            isValid = true;
        }
        return isValid;
    }

}
