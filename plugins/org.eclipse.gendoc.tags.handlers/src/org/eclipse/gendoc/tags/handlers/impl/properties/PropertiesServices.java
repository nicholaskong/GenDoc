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
 * Tristan FAURE (Atos) tristan.faure@atos.net - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.impl.properties;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.services.exception.InvalidTemplateParameterException;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;
import org.eclipse.gendoc.tags.handlers.IPropertiesService;

public class PropertiesServices extends AbstractService implements IPropertiesService {

	@Override
	public void setPropertiesFile(File file) {
		if (file.getPath() != null && file.getPath().endsWith("properties")){
			managePropertiesFile(file);
		}
	}

	private void managePropertiesFile(File file) {
		IConfigurationService config = GendocServices.getDefault().getService(IConfigurationService.class);
		BufferedInputStream inStream = null;
		try {
			Properties p = new Properties();
			inStream = new BufferedInputStream(new FileInputStream(file));
			p.load(inStream);
			for (Entry<Object,Object> e : p.entrySet()){
				if (e.getKey() instanceof String && e.getValue() instanceof String){
					config.addParameter((String)e.getKey(), config.replaceParameters((String)e.getValue()));
				}
			}
		} catch (FileNotFoundException e) {
			ILogger logger = GendocServices.getDefault().getService(ILogger.class);
			logger.log(e.getMessage(), IStatus.ERROR);
		} catch (IOException e) {
			ILogger logger = GendocServices.getDefault().getService(ILogger.class);
			logger.log(e.getMessage(), IStatus.ERROR);
		} catch (InvalidTemplateParameterException e1) {
			e1.printStackTrace();
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
