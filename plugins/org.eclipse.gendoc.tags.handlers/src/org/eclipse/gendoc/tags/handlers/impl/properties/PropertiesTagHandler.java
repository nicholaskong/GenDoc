/*****************************************************************************
 * Copyright (c) 2016 Atos.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *  Tristan FAURE (Atos) tristan.faure@atos.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.impl.properties;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.InvalidTemplateParameterException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;
import org.eclipse.gendoc.tags.handlers.IPropertiesService;
import org.eclipse.gendoc.tags.handlers.impl.AbstractServicesTagHandler;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

public class PropertiesTagHandler extends AbstractServicesTagHandler {
	
	/*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.AbstractTagHandler#run(org.eclipse.gendoc.tags.ITag)
     */
    @Override
    public String doRun(ITag tag) throws GenDocException
    {
        super.doRun(tag);
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.AbstractTagHandler#runAttributes(org.eclipse.gendoc.tags.ITag, String)
     */
    @Override
    protected String runAttributes(ITag tag, String value) throws GenDocException
    {
        super.runAttributes(tag, value);
        IPropertiesService propertiesService = GendocServices.getDefault().getService(IPropertiesService.class);
        // model must be set ...
        for (String key : tag.getAttributes().keySet())
        {
            if (RegisteredTags.PROPERTIES_PATH.equalsIgnoreCase(key))
            {
                String path = this.replaceParameters(tag.getAttributes().get(key));
                File f = new File(path.replaceAll("\\\\", "/"));
                if (!f.exists()){
                	IGendocDiagnostician logger = GendocServices.getDefault().getService(IGendocDiagnostician.class); 
                	logger.addDiagnostic(IStatus.ERROR,"file containing properties : " + path + " does not exist", null);
                }
                else {
                	propertiesService.setPropertiesFile(f);
                }
                
            }
        }
        return value;
    }

    /**
     * Replaces param references in the given path with their actual values.
     * 
     * @param path the path
     * 
     * @return the path with parameter references replaced by actual values
     * 
     * @throws InvalidTemplateParameterException if a referenced parameter does not exist
     */
    private String replaceParameters(String path) throws InvalidTemplateParameterException
    {
        IConfigurationService configService = GendocServices.getDefault().getService(IConfigurationService.class);
        return configService.replaceParameters(path);
    }
	
}
