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
package org.eclipse.gendoc.preferences.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.gendoc.wizard.IGendocTemplate;

/**
 * The Class PreferenceGendocTemplate is a implementation of  
 * @{link IGendocTemplate} used in the workspace preferences or 
 * workspace project properties, together with {@link PreferenceGendocRunner}.
 */
public class PreferenceGendocTemplate implements IGendocTemplate, Cloneable {

	/** The extension of the template file, identifying the file format 
	 *  supported by this template. */
	private String extension;
	
	/** The url where the template is located. 
	 * 	URLs with any known svhema are allowed. This include the eclipse 
	 * <code>platform:</code> URL schema.
	 */
	private URL url;
	
	/** The key of the parameter holding the model object. */
	private String modelKey = "generic_generation_model";
	
	/** The key of the parameter holding the output path of the result file. */
	private String outputKey = "generic_generation_output";
	
	/** The description of the template. */
	private String description = "";	

	/**
	 * Instantiates a new preference gendoc template with given output 
	 * extension, defined the file format of the template, and the URL
	 * where the template file is located.<br>
	 * 
	 * URLs with any known svhema are allowed. This include the eclipse 
	 * <code>platform:</code> URL schema.
	 *
	 * @param outputExtension the output extension defining the file format
	 * @param url the url where the template file is located.
	 */
	public PreferenceGendocTemplate(String outputExtension, URL url) {
		this.extension = outputExtension;
		this.url = url;
		this.modelKey = "model";
		this.outputKey = "output";
		this.description = String.format("A %s GenDoc template",outputExtension);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.IGendocTemplate#getOutPutExtension()
	 */
	@Override
	public String getOutPutExtension() {
		return extension;
	}

	/**
	 * Sets the output extension for the template file, which
	 * implicitly define the file format supported by this template.
	 *
	 * @param extension
	 *            the new out put extension
	 */
	public void setOutPutExtension(String extension) {
		this.extension = extension;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.IGendocTemplate#getTemplate()
	 */
	@Override
	public URL getTemplate() {
		return url;
	}

	/**
	 * Sets the template file.
	 *
	 * @param url a URL with the location of the file. <br>
	 * 
	 * URLs with any known svhema are allowed. This include the eclipse 
	 * <code>platform:</code> URL schema.
	 *
	 */
	public void setTemplate(URL url) {
		this.url = url;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.IGendocTemplate#getModelKey()
	 */
	@Override
	public String getModelKey() {
		return modelKey;
	}

	/**
	 * Sets the model parameter key. The model it will be accesible
	 * in the template through this key.
	 *
	 * @param modelKey the key or name of the parameter where the model
	 *        object will be set.
	 */
	public void setModelKey(String modelKey) {
		this.modelKey = modelKey;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.IGendocTemplate#getOutputKey()
	 */
	@Override
	public String getOutputKey() {
		return outputKey;
	}

	/**
	 * Sets the output parameter key. The output path of the result file 
	 * it will be set in the parameter identified by this key
	 *
	 * @param outputKey the key or name of the parameter where the path of 
	 * the result file will be set.
	 */
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.IGendocTemplate#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the template.
	 *
	 * @param description the description of the template
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.IGendocTemplate#isSavable()
	 */
	@Override
	public boolean isSavable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		PreferenceGendocTemplate clone = (PreferenceGendocTemplate)super.clone();
		if (url != null) {
			try {
				clone.url = new URL(url.toExternalForm());
			} catch (MalformedURLException e) {
			}
		}
		return clone;
	}

}
