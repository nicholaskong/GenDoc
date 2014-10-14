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
package org.eclipse.gendoc.wizard.papyrus;

import java.net.URL;

import org.eclipse.gendoc.wizard.IGendocTemplate;

/**
 * The represent the odt format for document generation for papyrus to use it, create en instance in
 * PapyrusGendocRunner
 */
public class GendocRunnerOdt implements IGendocTemplate
{

    /** The description associated to this kind of template */
    private String description = "Generic OpenOffice odt template to display all diagrams of a Papyrus model, and the elements \n contained "
            + "in each diagram with their documentation";

    public String getOutPutExtension()
    {
        return "odt";
    }

    public URL getTemplate()
    {
        return Activator.getDefault().getBundle().getEntry("/resource/templatePapyrus.odt");
    }

    public String getModelKey()
    {
        return "generic_generation_model";
    }

    public String getOutputKey()
    {
        return "generic_generation_output";
    }

    public String getDescription()
    {
        return description;
    }

	public boolean isSavable() {
		return true;
	}

}
