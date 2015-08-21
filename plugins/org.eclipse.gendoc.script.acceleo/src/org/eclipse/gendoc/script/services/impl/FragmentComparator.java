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
 *  Anne Haugommard (Atos) - anne.haugommard@atos.net
 * 
 *****************************************************************************/
package org.eclipse.gendoc.script.services.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.gendoc.m2t.model.Fragment;

/**
 * A comparator analysing the tree of dependencies
 * 
 * @author tfaure
 * 
 */
public class FragmentComparator implements Comparator<Fragment>
{
    /**
	 * 
	 */
	private final ModuleManagerService moduleManagerService;

	/**
	 * @param moduleManagerService
	 */
	FragmentComparator(ModuleManagerService moduleManagerService) {
		this.moduleManagerService = moduleManagerService;
	}


	public int compare(Fragment o1, Fragment o2)
    {
    	int result; 
    	if ((o1.getImportedFragments().size() == 0) && (o2.getImportedFragments().size() == 0)){
    			result = 0;	// none has fragment : no order.
        }
    	else if (o1.getImportedFragments().size() == 0){
    		// ONLY o2 has fragments
    		result = -1;
    	}
    	else if (o2.getImportedFragments().size() == 0){
    		// ONLY o1 has fragments
    		result = 1;
    	}
    	else{
            Map<String, Fragment> allFragments = this.moduleManagerService.getFragmentsMap();
            // to manage dependency link
            boolean found = isDependant(o1, o2, allFragments);
    	
            if( !found){
            	if(isDependant(o2, o1, allFragments)){
            		result = -1; /* o1 and o2 have fragments. o2 depends on o1*/
            	}
            	else{
            		result = 0;
            	}
            }
            else{
            	result = 1; // o1 and o2 have fragments. o1 depends on o2
            }
    	}
        return result;
    }


	private Set<String> getAllAncestors(Fragment o1,
			Map<String, Fragment> allFragments) {
		Set<String> result = new HashSet<String>();
		for (String o : o1.getImportedFragments()){
			result.add(o);
			result.addAll(getAllAncestors(allFragments.get(o), allFragments));
		}
		return result;
	}

	private boolean isDependant(Fragment o1, Fragment o2,
			Map<String, Fragment> allFragments) {
		Stack<String> deps = new Stack<String>();
        deps.addAll(o1.getImportedFragments());
        boolean found = false;
        while (!deps.isEmpty() && !found)
        {
            String aDep = deps.pop();
            if (aDep.equals(o1.getName()))
            {
                throw this.moduleManagerService.new DependencyCycleRuntimeException(String.format("a cycle has been detected for fragment <<%s>> please check the attribute 'importedFragments'", o1.getName()));
            }
            found |= (o2 != null && o2.getName().equals(aDep));
            Fragment f = allFragments.get(aDep);
            if (f != null)
            {
                for (String s : f.getImportedFragments())
                {
                    deps.push(s);
                }
            }
        }
		return found;
	}
}