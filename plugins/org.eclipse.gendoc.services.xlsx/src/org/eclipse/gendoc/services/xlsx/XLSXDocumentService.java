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
import java.io.StringWriter;
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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.document.parser.documents.Document.CONFIGURATION;
import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.xlsx.XLSXDocument;
import org.eclipse.gendoc.document.parser.xlsx.XLSXNamespaceContext;
import org.eclipse.gendoc.document.parser.xlsx.XLSXParser;
import org.eclipse.gendoc.documents.IAdditionalResourceService;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.documents.XMLDocumentService;
import org.eclipse.gendoc.services.exception.DocumentServiceException;
import org.eclipse.gendoc.services.exception.InvalidContentException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Provide a common ooxml abstract implementation of XMLDocumentService
// TODO: Some tags need to delegate to services associated to the tag (such as IHTMLService) which need to delegate to 
// an extension point and to a default implementation of the service for cases where the document type does not support
// the function provided by the tag / service:
// - IAdditionalResourceService
// - IHtmlService (RichTextTag)

public class XLSXDocumentService extends XMLDocumentService implements IDocumentService {
	private static final boolean DEBUG = System.getProperty("debug") != null; 
	private String serviceId;
	private XLSXAdditionalResourceService additionalResourceService = new XLSXAdditionalResourceService();

	public XLSXDocumentService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getServiceId ()
	{
		return this.serviceId;
	}

	@Override
	public void setServiceId (String serviceId)
	{
		this.serviceId = serviceId;
	}
	

	@Override
	public void saveDocument(Document doc, String path) throws DocumentServiceException {
        if (!(doc instanceof XLSXDocument)) {
            throw new DocumentServiceException("Document is not a valid DOCX document.");
        }

        XLSXDocument document = (XLSXDocument)doc;
        try
        {
            // back to the beginning
            document.jumpToStart();
            do
            {
                DOMSource domSource = new DOMSource(document.getXMLParser().getDocument());
                if (document.getXMLParser().getKind() == CONFIGURATION.content) {
                	if (document.getXMLParser() instanceof XLSXParser) {
	                	XLSXParser xlsxParser = (XLSXParser)document.getXMLParser();
	                	xlsxParser.handleDropCellReferences();
                	}
                	
	                StreamResult fluxDestination = new StreamResult(new File(
	                		document.getUnzipLocationDocumentFile().getAbsolutePath() + 
	                		"/xl/worksheets/" + document.getXMLParser().getXmlFile().getName()));
	                TransformerFactory transformBuilder = TransformerFactory.newInstance();
	
	                Transformer transformer = transformBuilder.newTransformer();
	                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	                transformer.setOutputProperty(OutputKeys.INDENT, "no");
	                transformer.transform(domSource, fluxDestination);
	                
	                if (DEBUG) {
	                	StringWriter swr = new StringWriter();	                		
	                	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		                transformer.transform(domSource, new StreamResult(swr));
		                swr.flush();
		                Activator.getDefault().getLog().log(new Status(
		                		IStatus.INFO, Activator.PLUGIN_ID,
		                		"/xl/worksheets/" + document.getXMLParser().getXmlFile().getName()+":\n"+
		                		swr.toString()));
	                }
                }                
            }
            while (document.jumpToNextFile());
                        
            for (XMLParser p : document.getSubdocuments()) {
                DOMSource domSource = new DOMSource(p.getDocument());
                StreamResult fluxDestination = new StreamResult(p.getXmlFile());
                TransformerFactory transformBuilder = TransformerFactory.newInstance();

                Transformer transformer = transformBuilder.newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "no");
                transformer.transform(domSource, fluxDestination);
                if (DEBUG) {
                	StringWriter swr = new StringWriter();	                		
                	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	                transformer.transform(domSource, new StreamResult(swr));
	                swr.flush();
	                Activator.getDefault().getLog().log(new Status(
	                		IStatus.INFO, Activator.PLUGIN_ID,
	                		p.getXmlFile().getAbsolutePath().replace(
	                				document.getUnzipLocationDocumentFile().getAbsolutePath(),"")+":\n"+
	                		swr.toString()));
                }
            }

            ((XLSXDocument) document).zipToLocation(path);
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

	@Override
	public String getListLabel() {
		return null;
	}

	@Override
	public boolean isList(String label) {
		return false;
	}

	@Override
	public String getListId(Node n) {
		return null;
	}

	@Override
	public String getContinueList(Node currentNode, String idList) throws InvalidContentException {
		return null;
	}

	@Override
	public boolean isListItem(String label) {
		return false;
	}

	@Override
	public String getTableLabel() {
		return null;
	}

	@Override
	public boolean isTable(String label) {
		return false;
	}

	@Override
	public boolean isRow(String label) {
		return false;
	}

	
	// TODO: The nobr tag can not use pattern matching as the xlsx format use <t> tags with 
	// xml:space="preserve" or by default preserve blanks.
	// A mergeParagraphs(...) function may be needed on the DocumentService interface => IDocumentService2
	@Override
    public Pattern getNobrReplacePattern()
    {
		return null;
    }

	@Override
	public String format(String input) {
		// The <t> tags preserve blanks. 
		return input;
	}

	@Override
	public NamespaceContext getNameSpaceContext() {
		return new XLSXNamespaceContext();
	}

	@Override
	public String getNamingSpaceURL() {
        return "xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";
	}

	@Override
	public String getTextStyle() {
		return "t";
	}

	@Override
	public boolean isPara(String label) {
		return label.equals("row");
	}

	@Override
	// It is not needed as XLSX use default namespaces
	public String[] getTextTagLabels() {
		return null;
	}

	@Override
	// TODO: Needed for insert images.
	public IAdditionalResourceService getAdditionalResourceService() {
		return additionalResourceService;
	}

	@Override
	protected Node cleanTags(Node currentNode, List<String> tagLabels, Node baseNode) throws InvalidContentException {
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

	@Override
	protected boolean areSimilarTags(String tagName1, String tagName2) {
		return false;
	}

	@Override
	protected String containsSimilarTag(Stack<String> tagStack, String tagName) {
		return null;
	}

}
