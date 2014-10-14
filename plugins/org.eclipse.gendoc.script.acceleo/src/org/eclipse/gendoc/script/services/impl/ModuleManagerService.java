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
package org.eclipse.gendoc.script.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.acceleo.model.mtl.Module;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gendoc.m2t.IFragmentService;
import org.eclipse.gendoc.m2t.IFragmentableM2TProcessor;
import org.eclipse.gendoc.m2t.IM2TProcessor;
import org.eclipse.gendoc.m2t.IScriptLanguageExtensionService;
import org.eclipse.gendoc.m2t.model.Fragment;
import org.eclipse.gendoc.script.acceleo.Generator;
import org.eclipse.gendoc.script.acceleo.IFileAndMMRegistry;
import org.eclipse.gendoc.script.acceleo.Messages;
import org.eclipse.gendoc.script.services.IModuleManagerService;
import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.GenerationException;
import org.eclipse.gendoc.services.exception.UnknownScriptLanguageException;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;

public class ModuleManagerService extends AbstractService implements IModuleManagerService
{

    // the metamodels are converted to a string used by uris and are stored as ids
    Map<String, Set<Module>> mapMetamodels2CompiledBundlesIds = new HashMap<String, Set<Module>>();

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.script.services.IModuleManagerService#getImportedModules(org.eclipse.emf.ecore.EObject)
     */
    public List<String> getImportedModules(EObject element) throws GenDocException
    {
        IConfigurationService config = GendocServices.getDefault().getService(IConfigurationService.class);
        IScriptLanguageExtensionService scriptLanguageExtensionService = GendocServices.getDefault().getService(IScriptLanguageExtensionService.class);
        try
        {
            IM2TProcessor processor = scriptLanguageExtensionService.getProcessor(config.getLanguage());
            if (processor instanceof IFragmentableM2TProcessor)
            {
                IFragmentableM2TProcessor fragmentable = (IFragmentableM2TProcessor) processor;
                IFragmentService fragmentService = GendocServices.getDefault().getService(IFragmentService.class);
                List<Fragment> fragments = fragmentService.getAllFragments();
                if (fragments != null)
                {
                    return manageFragments(element, fragments, fragmentable);
                }
            }
        }
        catch (UnknownScriptLanguageException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param element
     * @param fragments
     * @param fragmentable
     * @return
     * @throws GenDocException
     */
    protected List<String> manageFragments(EObject element, List<Fragment> fragments, IFragmentableM2TProcessor fragmentable) throws GenDocException
    {
        Collection<String> metamodels = getMetamodels(element);
        String id = getMMString(metamodels);
        Set<Module> result = mapMetamodels2CompiledBundlesIds.get(id);
        if (result == null)
        {
            result = new HashSet<Module>();
            // result is put at the start to remember the compiled modules
            mapMetamodels2CompiledBundlesIds.put(id, result);
            Map<String, Fragment> map = getFragmentsMap();
            // dependency analysis for compilation
            try
            {
                Collections.sort(fragments, new FragmentComparator());
            }
            // the fragment comparator throws a runtime exception
            // as the method compare declare any
            catch (DependencyCycleRuntimeException e)
            {
                // if the exception is catched a gendoc Exception is catched
                throw new GenDocException(e.getMessage())
                {
                    private static final long serialVersionUID = 1L;
                };
            }
            // the fragments are sorted
            // the first ones have no dependencies to their followers
            for (Fragment f : fragments)
            {
                String script = fragmentable.runFragmentScript(element, f);
                Generator generator;
                try
                {
                    List<URI> dependencies = new ArrayList<URI>();
                    // a dependency in the document can reference a fragment which does not exist
                    for (String dep : f.getImportedFragments())
                    {
                        if (!map.containsKey(dep))
                        {
                            throw new GenDocException(String.format("the fragment %s references an unknow fragment named : %s", f, dep))
                            {
                                private static final long serialVersionUID = 1L;
                            };
                        }
                        dependencies.add(getModuleURIForFragmentName(metamodels, dep));
                    }
                    generator = new Generator(element, script, dependencies);
                    Module m = generator.getModule();
                    m.eResource().setURI(generateURI(id, m));
                    if (m != null)
                    {
                        result.add(m);
                    }
                }
                catch (IOException e)
                {
                    throw new GenerationException("Error during generation of the following script : \n" + script, e);
                }
            }
            
        }
        List<String> toReturn = new LinkedList<String>();
        for (Module m : result)
        {
            toReturn.add(m.getName());
        }
        return toReturn;
    }

    private Map<String, Fragment> getFragmentsMap()
    {
        IFragmentService service = GendocServices.getDefault().getService(IFragmentService.class);
        Map<String, Fragment> allFragments = new HashMap<String, Fragment>();
        for (Fragment f : service.getAllFragments())
        {
            allFragments.put(f.getName(), f);
        }
        return allFragments;
    }

    /**
     * Returns an unique id for collection of metamodels uri
     * 
     * @param metamodels
     * @return
     */
    private String getMMString(Collection<String> metamodels)
    {
        StringBuilder id = new StringBuilder();
        List<String> list = new ArrayList<String>(metamodels);
        // the sort method ensure the order of the metamodels is always the same
        Collections.sort(list);
        for (String s : list)
        {
            id.append(s.replace(':', '_').replace('/', '_').replace('.', '_'));
        }
        return id.toString();
    }

    /**
     * Get the module uri for a list of metamodels and for a dependency
     * 
     * @param metamodels
     * @param dep
     * @return
     */
    private URI getModuleURIForFragmentName(Collection<String> metamodels, String dep)
    {
        return generateURI(getMMString(metamodels), dep);
    }

    @Override
    public void clear()
    {
        super.clear();
        for (Set<Module> list : mapMetamodels2CompiledBundlesIds.values())
        {
            for (Module m : list)
            {
                if (m.eResource() != null)
                {
                    m.eResource().unload();
                }
            }
        }
        mapMetamodels2CompiledBundlesIds.clear();
    }

    /**
     * Generate URI for a given module
     * 
     * @param generateUUID
     * @param m
     * @return
     */
    protected URI generateURI(String generateUUID, Module m)
    {
        return generateURI(generateUUID, m.getName());
    }

    /**
     * Create the URI for a unique id and a module name
     * 
     * @param generateUUID
     * @param m
     * @return
     */
    protected URI generateURI(String generateUUID, String moduleName)
    {
        return URI.createURI(Messages.GendocResourceFactory_BUNDLE_URI_SCHEME + "://" + generateUUID + "/" + moduleName + ".emtl");
    }

    /**
     * @param element
     * @return
     */
    protected Collection<String> getMetamodels(EObject element)
    {
        IFileAndMMRegistry registry = GendocServices.getDefault().getService(IFileAndMMRegistry.class);
        Collection<String> metamodels = registry.getMetamodels(element);
        return metamodels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.script.services.IModuleManagerService#getModuleURIs(org.eclipse.emf.ecore.EObject)
     */
    public List<URI> getModuleURIs(EObject element)
    {
        List<URI> result = new LinkedList<URI>();
        IFileAndMMRegistry registry = GendocServices.getDefault().getService(IFileAndMMRegistry.class);
        Collection<String> metamodels = registry.getMetamodels(element);
        Set<Module> modules = mapMetamodels2CompiledBundlesIds.get(getMMString(metamodels));
        for (Module m : modules)
        {
            result.add(m.eResource().getURI());
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.script.services.IModuleManagerService#getModule(org.eclipse.emf.common.util.URI)
     */
    public Module getModule(URI uri)
    {
        if (uri == null)
        {
            return null;
        }
        if (uri.segmentCount() != 1)
        {
            return null;
        }
        uri = uri.trimFileExtension();
        String id = uri.host();
        if (id != null)
        {
            Set<Module> modules = mapMetamodels2CompiledBundlesIds.get(id);
            if (modules != null)
            {
                String name = uri.segment(0);
                if (name != null)
                {
                    for (Module m : modules)
                    {
                        if (name.equals(m.getName()))
                        {
                            return m;
                        }
                    }
                }
            }

        }
        return null;
    }

    /**
     * A comparator analysing the tree of dependencies
     * 
     * @author tfaure
     * 
     */
    public class FragmentComparator implements Comparator<Fragment>
    {
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
	            Map<String, Fragment> allFragments = getFragmentsMap();
	            // to manage dependency link
	            boolean found = isDependant(o1, o2, allFragments);
        	
	            if( !found){
	            	if( isDependant(o2, o1, allFragments)){
	            	result = -1; /* o1 and o2 have fragments. o2 depends on o1*/
	            	}
	            	else{
	            		result = 0 ;/* o1 and o2 have fragments and do not depend on each other*/
	            	}
	            }
	            else{
	            	result = 1; // o1 and o2 have fragments. o1 depends on o2
	            }
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
                    throw new DependencyCycleRuntimeException(String.format("a cycle has been detected for fragment <<%s>> please check the attribute 'importedFragments'", o1.getName()));
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

    private class DependencyCycleRuntimeException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        public DependencyCycleRuntimeException(String message)
        {
            super(message);
        }

    }

}
