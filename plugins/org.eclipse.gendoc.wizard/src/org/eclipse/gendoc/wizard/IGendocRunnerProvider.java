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
package org.eclipse.gendoc.wizard;

/**
 * An object implementing the IGendocRunnerProvider interface provides a 
 * number of {@link IGendocRunner} than can be used when invoking the 
 * {@link IGendocWizard} or by the Gendoc batch process. <br>
 * 
 * Objects implementing this interfaces through the extension point
 * org.eclipse.gendoc.wizard.runner.  
 */
public interface IGendocRunnerProvider {
	
	/**
	 * Return the a list of gendoc runners.
	 *
	 * @return the list of gendoc runners.
	 */
	public IGendocRunner[] getGendocRunners();
}
