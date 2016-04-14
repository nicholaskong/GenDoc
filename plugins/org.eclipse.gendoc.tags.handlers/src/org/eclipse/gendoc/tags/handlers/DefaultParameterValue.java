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
 *  Tristan FAURE (Atos Origin) tristan.faure@atosorigin.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.tags.handlers.impl.config.IDeferredParameterValue;
import org.eclipse.gendoc.tags.handlers.impl.config.IDeferredValue;

/**
 * A Parameter Value which returns the location of the workspace
 * 
 * @author tfaure
 */
public class DefaultParameterValue implements IDeferredParameterValue
{
    private static final String KEY_INPUT = "input";
    private static final String KEY_INPUT_EXT = "input_ext";

    private static final String KEY_DATE = "date";

    public Map<String, String> getValue()
    {
        Map<String, String> result = new HashMap<String, String>();

        // fill date of the generation
        Date actual = Calendar.getInstance().getTime();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        result.put(KEY_DATE, format.format(actual));

        return result;
    }

    public Map<String, IDeferredValue> getDeferredValues()
    {
        // these values are deferred to be sure that the document service is
        // already loaded
        Map<String, IDeferredValue> result = new HashMap<String, IDeferredValue>();
        IDeferredValue documentDeferred = new IDeferredValue()
        {
            String documentName = null;

            public String get()
            {
                if (documentName == null)
                {
                	String  document = getDocument();
                    return document.substring(0, document.lastIndexOf("."));
                }
                return documentName;
            }


        };
        // fill document name
        result.put(KEY_INPUT, documentDeferred);
        IDeferredValue documentDeferred2 = new IDeferredValue()
        {
            String documentName = null;

            public String get()
            {
                if (documentName == null)
                {
                	String  document = getDocument();
                    return document;
                }
                return documentName;
            }


        };
        // fill document name with extension
        result.put(KEY_INPUT_EXT, documentDeferred2);
        return result;
    }
    
    private String getDocument() {
    	try
    	{
    		IDocumentService docService = GendocServices.getDefault().getService(IDocumentService.class);
    		if (docService != null)
    		{
    			String name = docService.getDocument().getPath().substring(docService.getDocument().getPath().lastIndexOf('/') + 1, docService.getDocument().getPath().length());
    			return name;
    		}
    	}
    	catch (RuntimeException e)
    	{
    	}
    	return "";
    }
}
