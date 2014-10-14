/*****************************************************************************
 * Copyright (c) 2014 Atos.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Anne Haugommard (Atos) anne.haugommard@atos.net - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.wizard;

/**
 * This class represents an additional parameter item
 */
public class AdditionnalParameterItem {
	/**
	 * The label display in the wizard page
	 */
  	  private String label;

  	  /**
  	   * The value to replace the paramName
  	   */
  	  private String value;
  	  
  	  /**
  	   * The paramName inside the Gendoc template
  	   */
  	  private String paramName;

  	  public AdditionnalParameterItem(String label, String paramName) {
  	    this.setLabel(label);
  	    this.setParamName(paramName);
  	    this.value="";
  	  }

		public String getParamName() {
			return paramName;
		}

		public void setParamName(String paramName) {
			this.paramName = paramName;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
}
