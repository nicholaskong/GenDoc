/*****************************************************************************
 * Copyright (c) 2008 Atos Origin.
 * 
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * 	Tristan Faure (Atos Origin) tristan.faure@atosorigin.com -
 * 		Initial API and implementation
 *  Antonio Campesino (Ericsson) antonio.campesinorobles@ericsson.com
 *  	Add isAncestor check.
 * 
 *****************************************************************************/
package org.eclipse.gendoc.document.parser.documents.helper;

import org.w3c.dom.Node;

/**
 * The Class XMLHelper. operations for xml object
 * 
 * @author tristan.faure@atosorigin.com
 */
public final class XMLHelper
{
    /**
     * Instantiates a new xML helper.
     */
    private XMLHelper()
    {
    }

    /**
     * Gets the tag value without namespace.
     * 
     * @param n the n
     * @return the tag value without namespace
     */
    public static String getTagValueWithoutNamespace(Node n)
    {
        if (n != null)
        {
            String tag = n.getNodeName();
            return tag.substring(tag.indexOf(":") + 1, tag.length());
        }
        return null;
    }

    /**
     * get the node following the node in parameter
     * 
     * @param n the n
     * @return the node
     */
    public static Node next(Node n)
    {
        if (n == null)
        {
            return null;
        }
        Node currentNode = n;
        if (currentNode.getChildNodes().getLength() > 0)
        {
            currentNode = currentNode.getFirstChild();
        }
        else
        {
            if (currentNode.getNextSibling() != null)
            {
                currentNode = currentNode.getNextSibling();
            }
            else
            {
                Node oldCurrentNode = currentNode;
                currentNode = currentNode.getParentNode().getNextSibling();
                while (oldCurrentNode != null && currentNode == null)
                {
                    oldCurrentNode = oldCurrentNode.getParentNode();
                    if (oldCurrentNode != null)
                    {
                        currentNode = oldCurrentNode.getNextSibling();
                    }
                }
            }
        }
        return currentNode;
    }
    
    /**
     * Get the sibling in next context
     * @param current
     * @return the sibling
     */
    public static Node getSibling(Node current)
    {
        Node sibling = current.getNextSibling();
        Node tmpParent = current ;
        while (tmpParent != null && sibling == null)
        {
            tmpParent = tmpParent.getParentNode();
            if (tmpParent != null)
            {
                sibling = tmpParent.getNextSibling();
            }
        }
        return sibling;
    }

    /**
     * Check if a node in the parent hierarchy has the name given in parameter
     * @param node
     * @param string
     * @return true if the node exists
     */
    public static boolean containsInHierarchy(Node node, String string)
    {
        Node parent = node ;
        if (node != null)
        {
            do 
            {
                parent = parent.getParentNode();
            }
            while (parent != null && !parent.getNodeName().equals(string));
        }
        return parent != null ;
    }
    
    public static boolean isAncestor(Node node, Node ancestor) {
        while (node != null) {
        	if (node == ancestor)
        		return true;
        	node = node.getParentNode();
        }
        return false;    	
    }
}
