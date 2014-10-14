/*****************************************************************************
 * Copyright (c) 2011 Atos
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Tristan FAURE (Atos) tristan.faure@atos.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.gendoc.m2t.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Just a way to store Fragment
 * @author tfaure
 *
 */
public class Fragment {
	private String name;
	private String scriptValue;
	private List<String> importedBundles = new LinkedList<String>();
	private List<String> importedFragments = new LinkedList<String>();
	
	List<Argument> arguments = new LinkedList<Argument>();

	public Fragment(String name) {
		this.name = name;
	}

	public List<Argument> getArguments ()
	{
		return arguments;
	}
	
	public void addImportedBundle (String bundle)
	{
		importedBundles.add(bundle);
	}
	
	public void addImportedFragment (String fragment)
	{
		importedFragments.add(fragment);
	}
	
	public List<String> getImportedBundles()
	{
		return Collections.unmodifiableList(importedBundles);
	}
	
	public List<String> getImportedFragments()
	{
		return Collections.unmodifiableList(importedFragments);
	}
	
	public boolean containsArgument(String name2) {
		for (Argument a : arguments)
		{
			if (a.getName().equalsIgnoreCase(name2))
			{
				return true ;
			}
		}
		return false;
	}

	public void addArgument(Argument a) {
		arguments.add(a);
	}

	public String getName() {
		return name;
	}

	public String getScriptValue() {
		return scriptValue;
	}
	
	public void setScriptValue(String value) {
		scriptValue = value ;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(name);
		builder.append(" (");
		boolean first = true ;
		for (Argument a : arguments)
		{
			if (!first)
			{
				builder.append(", ");
			}
			builder.append(a.getName());
			builder.append(":");
			builder.append(a.getType());
			first=false;
		}
		builder.append(") ");
		return builder.toString();
	}
	
	

}