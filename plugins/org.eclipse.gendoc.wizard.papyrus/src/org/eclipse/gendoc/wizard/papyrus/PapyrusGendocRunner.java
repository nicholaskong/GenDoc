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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.gendoc.wizard.IGendocRunner;
import org.eclipse.gendoc.wizard.IGendocTemplate;

/**
 * The Class represent the loader for papyrus diagrams document generation.
 */
public class PapyrusGendocRunner implements IGendocRunner
{
    List<IGendocTemplate> templates = new ArrayList<IGendocTemplate>();

    public PapyrusGendocRunner()
    {
        templates.add(new GendocRunnerDocx());
        templates.add(new GendocRunnerOdt());
    }

    /**
     * @return specify all extension of model that papyrusGendocRunner can generate the documentation
     */
    public Pattern getPattern()
    {
        return Pattern.compile(".*\\.di");
    }

    /**
     * @return all the template format associated to this runner
     */
    public List<IGendocTemplate> getGendocTemplates()
    {
        return templates;
    }

    public String getLabel()
    {
        return "Document generation from Papyrus diagrams";
    }

	public Map<String, String> getAdditionnalParameters() {
		return null;
	}

}
