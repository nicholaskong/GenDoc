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

import org.eclipse.gendoc.batch.ArgOpt;
import org.junit.Assert;
import org.junit.Test;

public class ArgOptTest {

	@Test
	public void testArgOptCreation() {
		ArgOpt argOpt = new ArgOpt("input_DocTemplate", ArgOpt.REQUIRED_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "idt",
				"input document template URL ");
		Assert.assertEquals("ArgOpt should be required", ArgOpt.REQUIRED_ARGUMENT, argOpt.getArg_type());
	}

}

