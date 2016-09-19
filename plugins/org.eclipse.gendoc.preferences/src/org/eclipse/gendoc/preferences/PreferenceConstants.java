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
package org.eclipse.gendoc.preferences;

/**
 * A class with constants used in the by the Gendoc preferences.
 */
public class PreferenceConstants {
	
	/** The key to retrieve the globally configured templates from the 
	 * workspace preferences.<br>
	 * Note that it mas be used together with the {@link Activator#PLUGIN_ID}.*/
	public static final String P_GENDOC_TEMPLATES = "genDocTemplates";	
}
