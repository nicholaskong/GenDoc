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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.gendoc.preferences.IGendocConfiguration;
import org.eclipse.gendoc.wizard.IGendocRunner;
import org.eclipse.gendoc.wizard.IGendocSelectionConverterRunner;
import org.eclipse.gendoc.wizard.IGendocTemplate;
import org.eclipse.gendoc.wizard.ISelectionConverter;

/**
 * The Class PreferenceGendocRunner implements a {@link IGendocRunner} to be
 * used in the {@link IGendocConfiguration} or by the workspace preferences.
 */
public class PreferenceGendocRunner implements IGendocSelectionConverterRunner, Cloneable {
	
	/** The project that configure this runner, or <code>null</code> if the 
	 * runner is configured in the workspace. */
	private IProject project;
	
	/** The label of the runner. Used for display purpose. */
	private String label;
	
	/** The description of the runner. */
	private String description;
	
	/** The pattern with the regular expression to match the target model
	 *  for which this runner is applicable. */
	private Pattern pattern;
	
	/** The parameters description the runner configures. */
	private List<Parameter> parameters = new ArrayList<Parameter>();
	
	/** The templates the runner provides. */
	private List<IGendocTemplate> templates = new ArrayList<IGendocTemplate>();
	
	/** The template file configured as default. */
	private IGendocTemplate defaultTemplate;

	/**
	 * The Class Parameter hold the data related to the configured Parameter 
	 * associated with the runner: name and label (human readable name).
	 */
	public static class Parameter {
		
		/**
		 * Instantiates a new parameter.
		 *
		 * @param name
		 *            the name
		 * @param value
		 *            the value
		 */
		public Parameter(String name, String value) {
			super();
			this.name = name;
			this.label = value;
		}		
		
		/**
		 * Gets the name.
		 *
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Sets the name.
		 *
		 * @param name
		 *            the new name
		 */
		public void setName(String name) {
			this.name = name;
		}
		
		/**
		 * Gets the description.
		 *
		 * @return the description
		 */
		public String getLabel() {
			return label;
		}
		
		/**
		 * Sets the description.
		 *
		 * @param value
		 *            the new description
		 */
		public void setLabel(String value) {
			this.label = value;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Parameter other = (Parameter) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		/** The name. */
		private String name;
		
		/** The description. */
		private String label;
	}
	
	/**
	 * Instantiates a new preference gendoc runner for the given project.
	 *
	 * @param proj the proj for which this runner is configured. It can be 
	 * <code>null</code> to represent a runner provided by the workspace.  
	 * @param label
	 *            the label
	 */
	public PreferenceGendocRunner(IProject proj, String label) {
		super();
		this.project = proj;
		this.label = label;
	}
	
	/**
	 * Gets the project for which this runner is configured.
	 *
	 * @return the project for which this runner is configured.
	 */
	public IProject getProject() {
		return project;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.IGendocRunner#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label for this runner, used for display.
	 *
	 * @param label the new label of the runner
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the description of the runner.
	 *
	 * @return the description of the runner
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the runner.
	 *
	 * @param description the new description of the runner
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.IGendocRunner#getPattern()
	 */
	@Override
	public Pattern getPattern() {
		return pattern;
	}

	/**
	 * Sets the regular expression used to match the model target file
	 * for which this runner is applicable.
	 *
	 * @param pattern the new pattern containing the regular expression
	 *                to match the model files for which this runner is 
	 *                applicable.
	 */
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	/**
	 * Sets the gendoc templates this runner provides.
	 *
	 * @param templates the new gendoc templates for this runner.
	 */
	public void setGendocTemplates(List<IGendocTemplate> templates) {
		this.templates = templates;			
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.IGendocRunner#getGendocTemplates()
	 */
	@Override
	public List<IGendocTemplate> getGendocTemplates() {
		return templates;			
	}

	/**
	 * Adds a {@link IGendocTemplate} to the runner.
	 *
	 * @param template a the template to add to the runner.
	 */
	public void addTemplate(IGendocTemplate template) {
		templates.add(template);
	}
	
	/**
	 * Set the default template for this runner.
	 *
	 * @param template the default template
	 */
	public void addDefaultTemplate(IGendocTemplate template) {
		addTemplate(template);
		defaultTemplate = template;
	}

	/**
	 * Removes the given template from the runner.
	 *
	 * @param template the template to remove from runner.
	 */
	public void removeTemplate(IGendocTemplate template) {
		if (template == defaultTemplate)
			defaultTemplate = null;
		templates.remove(template.getOutPutExtension());
	}

	/**
	 * Gets the template configured for the given format.
	 *
	 * @param format the format file 
	 * @return the template configured in the runner for the given format 
	 */
	public IGendocTemplate getTemplate(String format) {
		for (IGendocTemplate t : templates) {
			if (t.getOutPutExtension().equals(format))
				return t;
		}
		return null;
	}

	/**
	 * Gets the template configured as default.
	 *
	 * @return the default template
	 */
	public IGendocTemplate getDefaultTemplate() {
		return defaultTemplate;
	}

	/**
	 * Gets the parameter definitions the template requires to have configured.
	 *
	 * @return the parameters definitions defined in the runner.
	 */
	public List<Parameter> getParameters() {
		return parameters;
	}
	
	/**
	 * Sets the parameter definitions the template requires to have configured.
	 *
	 * @param params the new parameter definitions.
	 */
	public void setParameters(List<Parameter> params) {
		this.parameters = params;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.IGendocRunner#getAdditionnalParameters()
	 */
	@Override
	public Map<String, String> getAdditionnalParameters() {
		HashMap<String, String> ap = new LinkedHashMap<String,String>();
		for (Parameter p : parameters)
			ap.put(p.getName(),p.getLabel());
		return ap;
	}

	/**
	 * Adds a the parameter definition to the runner
	 *
	 * @param name the name or key of the parameter.
	 * @param label the label to display in the UI
	 */
	public void addParameter(String name, String label) {
		parameters.add(new Parameter(name, label));
	}
	
	/**
	 * Removes the parameter definition from the runner
	 *
	 * @param name the name of the parameter to be removed.
	 */
	public void removeParameter(String name) {
		parameters.remove(new Parameter(name, ""));		
	}

	/**
	 * Gets the parameter description.
	 *
	 * @param name the name of the parameter to retrieve.
	 * @return the label of the requested parameter
	 */
	public String getParameterLabel(String name) {
		for (Parameter p : parameters) {
			if (p.getName().equals(name))
				return p.label;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.IGendocSelectionConverterRunner#getSelectionConverter()
	 */
	@Override
	public ISelectionConverter getSelectionConverter() {
		return new PreferenceGendocSelectionConverter(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		PreferenceGendocRunner runner = (PreferenceGendocRunner)super.clone();
		if (pattern != null)
			runner.pattern = Pattern.compile(pattern.toString());
		
		runner.parameters = new ArrayList<Parameter>();
		for (Parameter p : parameters) {
			runner.parameters.add(new Parameter(p.getName(), p.getLabel()));
		}

		runner.templates = new ArrayList<IGendocTemplate>();
		for (IGendocTemplate t : templates) {
			runner.templates.add(t);
			if (t == defaultTemplate)
				runner.defaultTemplate = t;
		}
		return runner;
	}
}
