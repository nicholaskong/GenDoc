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
 * Anass RADOUANI (AtoS) anass.radouani@atos.net - format
 * 
 *****************************************************************************/
package org.eclipse.gendoc.tags.html;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.services.IService;

/**
 * The HTML service converts html into document specific text.
 * 
 * @author Kris Robertson
 */
public interface IHtmlService extends IService
{

    /**
     * Converts html to document specific text
     * 
     * @param html the html to convert
     * @return the converted text
     */
    String convert(String html);

    /**
     * Specify a compatibility version for the generated doc (usefull for word 2003 vs 2007)
     * 
     * @param version the compatibility version of the generated doc, can be null or empty (use default in this case).
     */
    void setVersion(String version);
    
    /**
     * Specify that the richText tag is used inside a table
     * 
     * @param inTable must be used with parameter &lt;richText inTable='true'&gt;...&lt;/richText&gt;").
     */
    void setInTable(String inTable);
    
    /**
     * Specify that the richText tag includePic
     * 
     * @param includePic must be used with parameter &lt;richText includePic='true' &gt;...&lt;/richText&gt;").
     */
    void setIncludePic(String includePic);
    
    /**
     * Specify the richText tag language
     * 
     * @param format HTML or RTF 
     */
    void setFormat(String format);

    /**
     * Add eventual additional styles (due to HTML import) to the final document (during AdditionalStylesProcess)
     */
    void addAdditionalStyles(Document document);

}
