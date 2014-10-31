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
 *  Anne Haugommard (Atos Origin) anne.haugommard@atosorigin.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.services.docx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.document.parser.documents.docx.DocxDocument;
import org.eclipse.gendoc.document.parser.documents.docx.DocxNamespaceContext;
import org.eclipse.gendoc.documents.IAdditionalResourceService;
import org.eclipse.gendoc.documents.IMimeHtmlService;
import org.eclipse.gendoc.documents.XMLDocumentService;
import org.eclipse.gendoc.services.exception.DocumentServiceException;
import org.eclipse.gendoc.services.exception.InvalidContentException;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Specific service for DOCX document management
 */
public class DOCXDocumentService extends XMLDocumentService
{
    /** Service for additional resources */
    IAdditionalResourceService additionalResourceService;
    IMimeHtmlService mimehtmlservice;

    private final String TAG_TABLE = "w:tbl";

	private String serviceId;
	
	public DOCXDocumentService()
    {
        super();
        additionalResourceService = new DOCXAdditionalResourceService();
        mimehtmlservice = new DOCXMimeHtmlService();
    }

    public DOCXDocumentService(Document document)
    {
        super(document);
        additionalResourceService = new DOCXAdditionalResourceService();
        mimehtmlservice = new DOCXMimeHtmlService();
    }

    public String getListLabel()
    {
    	return null;
    }

    public boolean isList(String label)
    {
        return false;
    }

    public boolean isListItem(String label)
    {
        return false;
    }

    public boolean isPara(String label)
    {
        return "w:p".equals(label) || "w:bookmarkEnd".equals(label);
    }

    public boolean isTable(String label)
    {
        return "w:tbl".equals(label);
    }

    public boolean isRow(String label)
    {
        return "w:tr".equals(label);
    }

    public String getTextStyle()
    {
        return "w:t";
    }

    public String[] getTextTagLabels()
    {
        return new String[] {"w:p", "w:tbl"};
    }

    public String getNamingSpaceURL()
    {
    	// Add name space for validating Office 2010 embedded images
        return "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";
    }

    public NamespaceContext getNameSpaceContext()
    {
        return new DocxNamespaceContext();
    }

    public void saveDocument(Document document, String path) throws DocumentServiceException
    {
        if (!(document instanceof DocxDocument))
        {
            throw new DocumentServiceException("Document is not a valid DOCX document.");
        }

        insertDocumentInFile((DocxDocument) document);
        ((DocxDocument) document).zipToLocation(path);
        
    }

    public IAdditionalResourceService getAdditionalResourceService()
    {
        return additionalResourceService;
    }

    public IMimeHtmlService getMimeHtmlService()
    {
        return mimehtmlservice;
    }

    /**
     * @throws TransformerFactoryConfigurationError
     */
    private void insertDocumentInFile(DocxDocument document)
    {
        try
        {
            // back to the beginning
            document.jumpToStart();
            do
            {
                DOMSource domSource = new DOMSource(document.getXMLParser().getDocument());
                StreamResult fluxDestination = new StreamResult(new File(document.getUnzipLocationDocumentFile().getAbsolutePath() + "/word/" + document.getXMLParser().getXmlFile().getName()));
                TransformerFactory fabrique = TransformerFactory.newInstance();

                Transformer transformationIdentite = fabrique.newTransformer();
                // DO NOT INDENT => it causes extra spaces 
                // transformationIdentite.setOutputProperty(OutputKeys.INDENT, "yes");
                transformationIdentite.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                transformationIdentite.transform(domSource, fluxDestination);
            }
            while (document.jumpToNextFile());
        }
        catch (TransformerConfigurationException e1)
        {
            e1.printStackTrace();
        }
        catch (TransformerException e2)
        {
            e2.printStackTrace();
        }
    }

    /**
     * @param currentNode subtree in which the clean is done.
     * @param tagLabels list of known tag labels
     * @param baseNode Node on which to start
     * @return
     * @throws InvalidContentException 
     */
    protected Node cleanTags(Node currentNode, List<String> tagLabels, Node baseNode) throws InvalidContentException
    {
        if (baseNode == null)
        {
            return null;
        }

        // 2. Check that this node contains the start of a valid tag label
        StringBuffer newNodeContent = new StringBuffer(extractNodeTextValue(baseNode));
        while (baseNode != null && !containsOneOf(tagLabels, newNodeContent.toString()))

        {
            baseNode = findNodeWithStartTag(baseNode, currentNode);
            if (baseNode != null)
            {
                newNodeContent = new StringBuffer(extractNodeTextValue(baseNode));
            }
        }
        if (baseNode == null)
        {
            return null;
        }
        // 3. Base node is found AND matches a valid tag => Check tag closure

        boolean isCompleteTag = containsFullTags(newNodeContent.toString(), tagLabels);

        // 4. If tag not closed :
        if (!isCompleteTag)
        {
            // Find all nodes matching the base node label
            NodeList followingNodes = getNextNodes(baseNode, baseNode.getNodeName());
            List<Node> nodesToRemove = new ArrayList<Node>();

            if (followingNodes != null)
            {
                // Append text values of all these nodes until tag closure is found
                for (int i = 0; i < followingNodes.getLength(); i++)
                {
                    String textValue = extractNodeTextValue(followingNodes.item(i));
                    newNodeContent.append(textValue);
                    Node nodeToRemove = getBestAscendantUntil(currentNode, followingNodes.item(i));
                    if (!nodesToRemove.contains(nodeToRemove))
                    {
                        nodesToRemove.add(nodeToRemove);
                    }
                    if (containsFullTags(newNodeContent.toString(), tagLabels))
                    {
                        isCompleteTag = true;
                        break;
                    }

                }

                // Remove all nodes that are not useful anymore from initial current Node
                for (Node nodeToRemove : nodesToRemove)
                {
                    if (nodeToRemove != null && currentNode.equals(nodeToRemove.getParentNode()))
                    {
                        currentNode.removeChild(nodeToRemove);
                    }
                }
            }
        }
        // Replace content of base node with the text stored in "textContent" variable
        String[] separated = asText(baseNode).split(XML_TAG_START + "|" + XML_TAG_END);
        if (separated != null && separated.length > 1)
        {
            newNodeContent.insert(0, XML_TAG_START + separated[1] + XML_TAG_END);
            newNodeContent.append(XML_TAG_START + separated[separated.length - 1] + XML_TAG_END);
        }
        else
        {
            newNodeContent.append(asText(baseNode));
        }
        // Replace invalid characters
        String nodeContent = cleanXMLContent(newNodeContent.toString());

        // Replace base node by the value of the buffer
        Node result = injectNode(baseNode, nodeContent);
        baseNode.getParentNode().removeChild(baseNode);
        return result;

    }
    
    /** 
     * No similar tags in docx => always return false
	 * @see org.eclipse.gendoc.services.XMLDocumentService#areSimilarTags(java.lang.String, java.lang.String)
	 */
	@Override
	protected boolean areSimilarTags(String tagName1, String tagName2) {
		return false;
	}
    
    protected String containsSimilarTag (Stack<String> tagStack, String tagName)
    {
        return null;
    }
    
    public String getTableLabel()
    {
        return TAG_TABLE;
    }

	public String getServiceId ()
	{
		return this.serviceId;
	}

	public void setServiceId (String serviceId)
	{
		this.serviceId = serviceId;
	}
	
	protected static Pattern NOBR_REPLACE_PATTERN = Pattern.compile("(?:&lt;\\s*" + RegisteredTags.NOBR + "\\s*/\\s*&gt;)(?:.*?)(?:</w:t>)(?:.*?)(?:<w:t[^>]*>)", Pattern.DOTALL | Pattern.MULTILINE);

    public Pattern getNobrReplacePattern()
    {
        return NOBR_REPLACE_PATTERN;
    }

    public String getListId(Node n)
    {
        return null;
    }

    public String getContinueList(Node currentNode, String idList) throws InvalidContentException
    {
        return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.documents.IDocumentService#format(java.lang.String)
	 */
	public String format(String input) {
		
		String formatted=  
				input
				// handle carriage return
				.replace(CARRIAGE_RETURN,"</w:t></w:r></w:p><w:p><w:r><w:t>")
				.replace("\r", "</w:t></w:r></w:p><w:p><w:r><w:t>")
				
				//handle line feed
				.replace(LINE_FEED, "</w:t><w:br/><w:t>")
				.replace("\n","</w:t><w:br/><w:t>")
				
				//handle tabulation
				.replace(TABULATION,"</w:t><w:tab/><w:t>")
				.replace("\t","</w:t><w:tab/><w:t>");
		return formatted;
	}

}
