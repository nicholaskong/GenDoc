/*****************************************************************************
 * Copyright (c) 2015 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Benoit Maggi (CEA LIST) benoit.maggi@cea.fr - Initial API and implementation
 *   
 *****************************************************************************/
package org.eclipse.gendoc.batch.tests;

import static org.junit.Assert.fail;

import org.eclipse.gendoc.batch.GetOpt;
import org.junit.Test;

public class GetOptTest {

	@Test(expected=Exception.class)
	public void testGetArguments() throws Exception {
		GetOpt getOpt = new GetOpt();
		getOpt.getArguments(null, null);
	}

	@Test
	public void testGetFlatArguments() {
		//TODO		fail("Not yet implemented");
	}

	@Test
	public void testPrintHelpArgOptArrayPrintStream() {
		//TODO		fail("Not yet implemented");
	}

	@Test
	public void testPrintHelpArgOptArray() {
		//TODO		fail("Not yet implemented");
	}

}
