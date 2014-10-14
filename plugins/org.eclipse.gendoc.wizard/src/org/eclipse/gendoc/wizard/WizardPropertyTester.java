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
package org.eclipse.gendoc.wizard;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;

public class WizardPropertyTester extends PropertyTester
{

    public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
    {
    	IFile[] part = null ;
    	if ("isCollectionEnabled".equals(property))
    	{
    		part = Utils.getIFiles(receiver);
    	}
    	else if ("isEnabled".equals(property))
    	{
    		part = Utils.getIFiles(receiver);
    	}
    	if (part != null)
    	{
    		return Utils.matches(part);
    	}
    	return false ;
    	
    }

}
