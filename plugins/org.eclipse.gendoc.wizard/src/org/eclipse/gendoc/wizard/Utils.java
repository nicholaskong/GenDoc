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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class Utils
{
    private static List<IGendocRunner> runners = null;

    /**
     * Get IFile from object
     * @param o the object
     * @return the file
     */
    public static IFile[] getIFiles (Object o)
    {
    	if (o instanceof IFile) {
			return new IFile[] {(IFile) o};
		}
    	else 
    	{
    		IFile f = (IFile) Platform.getAdapterManager().getAdapter(o, IFile.class);
    		if (f != null)
    		{
    			return new IFile[] {f} ;
    		}
    		Collection<?> collec = (Collection<?>) Platform.getAdapterManager().getAdapter(o, Collection.class);
    		if (collec != null)
    		{
    			boolean ok = true ;
    			for (Object o2 : collec)
    			{
    				if (!(o2 instanceof IFile))
    				{
    					ok = false ;
    					break ;
    				}
    			}
    			if (ok)
    			{
    				return collec.toArray(new IFile[]{});
    			}
    		}
    	}
    	return null ;
    }
    
    /**
     * @return the list of all Runners
     * 
     */
    public static List<IGendocRunner> getAllRunners()
    {
        if (runners == null)
        {
            runners = new LinkedList<IGendocRunner>();
            IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(Activator.PLUGIN_ID, "runner");
            for (IConfigurationElement e : elements)
            {
                try
                {
                    IGendocRunner runner = (IGendocRunner) e.createExecutableExtension("Instance");
                    runners.add(runner);
                }
                catch (CoreException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
        return runners;
    }

    /**
     * @return the list of all extensions of the template we need in order to execute document generation
     */
    public static List<Pattern> getAllExtensions()
    {
        List<IGendocRunner> runners = getAllRunners();
        List<Pattern> result = new LinkedList<Pattern>();
        for (IGendocRunner r : runners)
        {
            if (r.getPattern() != null)
            {
                result.add(r.getPattern());
            }
        }
        return result;
    }

    /**
     * @param fileName name with extension of file on witch user has click
     * @return return true, if the fileName has one of the good extension
     * @deprecated
     */
    public static boolean matches(IFile[] files)
    {
        List<Pattern> patterns = getAllExtensions();
        for (Pattern p : patterns)
        {
            if (p != null)
            {
            	for(IFile f : files) {
            		if (p.matcher(f.getName()).matches())
            		{
            			return true;
            		}
            	}
            }
        }
        return false;
    }

    /**
     * @param selectedObject the object on witch user has click
     * @return return true, if the selectedObject has one associated runner
     */
    public static boolean matches(Object selectedObject)
    {
        return ! getRunners(selectedObject).isEmpty();
    }
    
    /**
     * @param fileName name of the model selected in order to generate its documentation
     * @return the list of all the template possible for document generation.
     * @deprecated
     */
    public static List<IGendocRunner> getRunners(IFile[] files)
    {
        List<IGendocRunner> runners = getAllRunners();
        List<IGendocRunner> result = new ArrayList<IGendocRunner>(runners.size());
        for (IGendocRunner g : runners)
        {
        	for(IFile f : files) {
        		if (g.getPattern().matcher(f.getName()).matches())
        		{
        			result.add(g);
        		}
        	}
        }
        return result;
    }
    
    /**
     * @param selectedObject object selected in order to generate its documentation
     * @return the list of all the template possible for document generation.
     */
    public static List<IGendocRunner> getRunners(Object selectedObject)
    {
    	List<IGendocRunner> runners = new ArrayList<IGendocRunner>();
    	for (IGendocRunner runner : getAllRunners()) {
    		if (runner instanceof IGendocSelectionConverterRunner){
    			if (((IGendocSelectionConverterRunner) runner).getSelectionConverter().matches(selectedObject)) {
    				IFile selectedFile = ((IGendocSelectionConverterRunner) runner).getSelectionConverter().getFile(selectedObject);
    				if (runner.getPattern().matcher(selectedFile.getName()).matches()) {
    					runners.add(runner);
    				} else {
    					// invalid runner
    				}
    			} else {
    				// invalid runner
    			}
    		}
    		else{
    			List<IFile> files = new LinkedList<IFile>();
    			if (selectedObject instanceof IFile[]) {
					files.addAll(Arrays.asList((IFile[])selectedObject));
				}
    			else if (selectedObject instanceof IFile){
    				files.add((IFile) selectedObject);
    			}
    			for(IFile f : files) {
            		if (runner.getPattern().matcher(f.getName()).matches())
            		{
            			runners.add(runner);
            		}
            	}
    		}
    	}
    	return runners;
    }

}
