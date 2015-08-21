/*****************************************************************************
 * Copyright (c) 2015 Atos.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 	Tristan faure (Atos) - tristan.faure@atos.net - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.script.services.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.gendoc.m2t.model.Fragment;
import org.eclipse.gendoc.script.services.impl.TopologicalSort.Edge;

public class FragmentEdge implements Edge<Fragment> {
	
	private Map<String, Fragment> map;

	public FragmentEdge(Map<String, Fragment> map) {
		this.map = map;
	}

	public List<? super Fragment> from(Fragment n) {
		List<Fragment> result = new LinkedList<Fragment>();
		List<String> importedFragments = n.getImportedFragments();
		if (importedFragments != null){
			for (String s : importedFragments){
				Fragment fragment = map.get(s);
				if (fragment != null){
					result.add(fragment);
				}
			}
		}
		return result;
	}

}
