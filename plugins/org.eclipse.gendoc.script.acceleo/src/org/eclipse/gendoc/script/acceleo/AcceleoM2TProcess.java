/*****************************************************************************
 * Copyright (c) 2010 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Caroline Bourdeu d'Aguerre (Atos Origin) caroline.bourdeudaguerre@atosorigin.com - Initial API and implementation
 *  Tristan Faure (Atos) tristan.faure@atos.net - refactoring
 *  Anne Haugommard (Atos) anne.haugommard@atos.net - special character handling
 *
 *****************************************************************************/
package org.eclipse.gendoc.script.acceleo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.gendoc.m2t.IFragmentableM2TProcessor;
import org.eclipse.gendoc.m2t.model.Fragment;
import org.eclipse.gendoc.script.acceleo.impl.FragmentScriptGenerator;
import org.eclipse.gendoc.script.acceleo.impl.ScriptGenerator;
import org.eclipse.gendoc.script.services.IModuleManagerService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IProgressMonitorService;
import org.eclipse.gendoc.services.exception.ElementNotFoundException;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.GenerationException;
import org.eclipse.gendoc.services.exception.ModelNotFoundException;
import org.eclipse.gendoc.services.exception.ParsingException;

/**
 * The Class AcceleoM2TProcess.
 * 
 * @author cbourdeu
 */
public class AcceleoM2TProcess implements IFragmentableM2TProcessor
{
    /**
     * Execute the given script and return the generated text.
     * 
     * @param script to execute as a String
     * @param context the context
     * 
     * @return the generated text as a String
     * 
     * @throws ElementNotFoundException the element not found exception
     * @throws ParsingException the parsing exception
     * @throws ModelNotFoundException the model not found exception
     * @throws GenerationException Other exception thrown during generation
     */
    public String runScript(String script, EObject element) throws ModelNotFoundException, ElementNotFoundException, ParsingException, GenerationException
    {
        return doRunScript(script, element, new ScriptGenerator(getScriptPatterns()));
    }

    protected String doRunScript(String script, EObject element, ScriptGenerator scriptGenerator) throws GenerationException
    {
        String result = "";
        String formattedScript = "";
        try
        {
        	formattedScript = scriptGenerator.formatScript(element, script);
//FIXME does not work for TPC 5.2  
//        	formattedScript = removeSpecialAcceleoCharactersFromXMLTags(script);
//        	formattedScript = scriptGenerator.formatScript(element, formattedScript);
            result = generate(element, formattedScript);
        }
        catch (IOException e)
        {
            throw new GenerationException("Error during generation of the following script : \n" + script, e);
        }
        catch (Exception e)
        {
            if (e instanceof IndexOutOfBoundsException && "index: 0, size: 1".equals(e.getMessage()))
            {
                throw new GenerationException("Error while trying to access index 0 of list.\n TIP : In Acceleo, index start at 1 and not 0.");
            }
            throw new GenerationException("Error during generation of Gendoc script :" + e.getMessage(), e);
        }

        return result;
    }

//	/**
//	 * Remove special characters from XML tags : replace brackets by Acceleo special bracket 
//	 * <ul>
//	 * <li>replace <code>[</code> by <code>['['/]</code></li>
//	 * <li>replace <code>]</code> by <code>[']'/]</code></li>
//	 * </ul>
//	 * @param script the initial script
//	 * @return the script without special characters
//	 */
//	private String removeSpecialAcceleoCharactersFromXMLTags(
//			String script) {
//		/* Pattern for brackets inside an xml tag : <.a.[.b.].c.> 
//		 * where .a. has no [ or ] or >
//		 * where .b. has no ] or >
//		 * where .c. has no >
//		*/
//		Pattern bracketsInsideXML = Pattern.compile("<[^>\\[\\]]*[[^>\\]]*][^>]*>");
//		StringBuffer resultBuffer = new StringBuffer();
//		Matcher bracketsInXML = bracketsInsideXML.matcher(script);
//		int index = 0;
//		// For each <.a.[.b.].c.> found
//		while(bracketsInXML.find()){
//			
//			// append what is before
//			resultBuffer.append(script.substring(index, bracketsInXML.start()));
//			//replace and append <.a.['['/].b.[']'/].c.>
//			String tag = script.substring(bracketsInXML.start(), bracketsInXML.end());
//			//FIXME find a better way to do that in 2 replace only...
//			tag = tag.replaceAll("\\[","____ENTERING_BRACKETS");
//			tag = tag.replaceAll("\\]","[']'/]");
//			tag = tag.replaceAll("____ENTERING_BRACKETS","['['/]");
//			resultBuffer.append(tag);
//			// set index to end of >
//			index = bracketsInXML.end();
//		}
//		script = resultBuffer.toString();
//		return script;
//	}

    protected String generate(EObject element, String formattedScript) throws IOException, ParsingException, GenerationException
    {
        IModuleManagerService service = GendocServices.getDefault().getService(IModuleManagerService.class);
        Generator generator = new Generator(element, formattedScript, service.getModuleURIs(element));
        IProgressMonitorService progressMonitor = GendocServices.getDefault().getService(IProgressMonitorService.class);
        String result = generator.doGenerate(progressMonitor.getDelegatingMonitor());
        // Unload
        unload(generator.getResourceSet());
        return result;
    }

    private void unload(ResourceSet resourceSet)
    {
        for (Resource r : resourceSet.getResources())
        {
            try
            {
                // the Gendocbundle:// uris shall not be unloaded
                if (!r.getURI().scheme().equals(Messages.GendocResourceFactory_BUNDLE_URI_SCHEME))
                {
                    r.unload();
                }
            }
            catch (Exception e)
            {
                // Sometimes the unload throw an exception
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.handlers.IM2TProcess#getAllAvailableBundles()
     */
    public List<String> getAllAvailableBundles()
    {
        return ServicesExtension.getInstance().getServices();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.handlers.IM2TProcess#getScriptPatterns()
     */
    public List<Pattern> getScriptPatterns()
    {
        // A pattern for elements matching : [XXXX] or [/XXXX] or [XXXX/]
        Pattern braquets = Pattern.compile("(\\[[^\\[\\]]*\\])|(\\[/[^\\[\\]]*\\])");
        List<Pattern> patterns = new ArrayList<Pattern>(1);
        patterns.add(braquets);
        return patterns;
    }

    public String runFragmentScript(EObject element, Fragment fragment) throws GenDocException
    {
        FragmentScriptGenerator generator = new FragmentScriptGenerator(getScriptPatterns(), fragment);
        return generator.formatScript(element, fragment.getScriptValue());
    }

    public void clear()
    {
        // do nothing
    }

}
