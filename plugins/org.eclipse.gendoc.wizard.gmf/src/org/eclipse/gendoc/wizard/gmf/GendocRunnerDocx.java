/*****************************************************************************
 * Copyright (c) 2011 Atos Origin.
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
package org.eclipse.gendoc.wizard.gmf;

import java.net.URL;

import org.eclipse.gendoc.wizard.IGendocTemplate;

/**
 * this class represent the format docx of template
 */
public class GendocRunnerDocx implements IGendocTemplate
{

    /** The description of this kind of template format */
	 private String description = "Generic MS Office Word template to display all GMF diagrams of a model, and the elements \n contained "
	            + "in each diagram";

    public String getOutPutExtension()
    {
        return "docx";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.wizard.IGendocTemplate#getTemplate()
     */
    public URL getTemplate()
    {
        return Activator.getDefault().getBundle().getEntry("/resource/templateGmf.docx");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.wizard.IGendocTemplate#getModelKey()
     */
    public String getModelKey()
    {
        return "generic_generation_model";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.wizard.IGendocTemplate#getOutputKey()
     */
    public String getOutputKey()
    {
        return "generic_generation_output";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.wizard.IGendocTemplate#getDescription()
     */
    public String getDescription()
    {
        return description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.wizard.IGendocTemplate#isSavable()
     */
	public boolean isSavable() {
		return true;
	}

}
