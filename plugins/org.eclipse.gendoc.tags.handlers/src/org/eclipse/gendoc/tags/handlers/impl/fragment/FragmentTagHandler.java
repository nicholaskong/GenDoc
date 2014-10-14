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
 * Tristan FAURE (Atos) tristan.faure@atos.net - refactoring 
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.impl.fragment;

import org.eclipse.gendoc.m2t.IFragmentService;
import org.eclipse.gendoc.m2t.IFragmentableM2TProcessor;
import org.eclipse.gendoc.m2t.IM2TProcessor;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.ElementNotFoundException;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.ModelNotFoundException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.AbstractScriptTagHandler;
import org.eclipse.gendoc.tags.handlers.IContextService;
import org.eclipse.gendoc.tags.handlers.exceptions.FragmentUnavailableException;
import org.eclipse.gendoc.tags.handlers.exceptions.IncorrectTagException;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

/**
 * A fragment tag handler is nearly the same as a script handler except it must use an instance of
 * {@link IFragmentableM2TProcessor}
 * 
 * @author tfaure
 * 
 */
public class FragmentTagHandler extends AbstractScriptTagHandler
{

    private static final String delimiter = ";";

    protected String runAttributes(ITag tag, String value) throws GenDocException
    {
        super.runChildren(tag, value);
        String result = super.runAttributes(tag, value);
        String name = tag.getAttributes().get(RegisteredTags.FRAGMENT_NAME);
        if (name == null || name.length() == 0)
        {
            throw new IncorrectTagException(tag, "name is mandatory");
        }
        final IFragmentService service = GendocServices.getDefault().getService(IFragmentService.class);
        service.addFragment(name);
        manageImport(tag, name, service, RegisteredTags.FRAGMENT_IMPORTED_BUNDLES, new IAdder()
        {

            public void add(String from, String toAdd)
            {
                service.addBundleToFragment(from, toAdd);
            }
        });
        manageImport(tag, name, service, RegisteredTags.FRAGMENT_IMPORTED_FRAGMENTS, new IAdder()
        {

            public void add(String from, String toAdd)
            {
                service.addFragmentImportToFragment(from, toAdd);
            }
        });
        return result;
    }

    protected void manageImport(ITag tag, String name, IFragmentService service, String attributeName, IAdder run)
    {
        String imported = tag.getAttributes().get(attributeName);
        if (imported != null && imported.length() > 0)
        {
            // Add the new bundles
            String[] toImport = imported.split(delimiter);
            for (String bundle : toImport)
            {
                run.add(name, bundle);
            }
        }
    }

    @Override
    protected String doRun(ITag tag) throws GenDocException
    {
        String result = super.doRun(tag);
        IFragmentService service = GendocServices.getDefault().getService(IFragmentService.class);
        service.addFragmentScriptContent(tag.getAttributes().get(RegisteredTags.FRAGMENT_NAME), result);
        return "";
    }

    @Override
    protected String runProcessorScript(IM2TProcessor processor, IContextService context, String cleanedTagValue, ITag tag) throws GenDocException, ModelNotFoundException, ElementNotFoundException
    {
        return cleanedTagValue;
    }

    @Override
    protected IM2TProcessor getProcessor(String language) throws GenDocException
    {
        IM2TProcessor processor = super.getProcessor(language);
        if (!(processor instanceof IFragmentableM2TProcessor))
        {
            throw new FragmentUnavailableException(processor.getClass());
        }
        return processor;
    }

    /**
     * An interface to factorize code
     * @author tfaure
     *
     */
    private interface IAdder
    {
        void add(String from, String toAdd);
    }
}
