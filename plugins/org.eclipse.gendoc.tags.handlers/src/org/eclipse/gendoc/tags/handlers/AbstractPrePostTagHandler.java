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
 * Papa Malick Wade (Atos) papa-malick.wade@atos.net - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.IPostTagHandler;
import org.eclipse.gendoc.tags.IPreTagHandler;
import org.eclipse.gendoc.tags.ITag;

/**
 * @author Papa Malick Wade
 * 
 */
public abstract class AbstractPrePostTagHandler extends AbstractTagHandler
{
    //this map contains all extensions which registered in pre extension point
    private static Map<String, SortedSet<PriorityPreHandler>> preHandlers = new HashMap<String, SortedSet<PriorityPreHandler>>();
    //this map contains all extensions which registered in post extension point
    private static Map<String, SortedSet<PriorityPostHandler>> postHandlers = new HashMap<String, SortedSet<PriorityPostHandler>>();

    static
    {
        initHandlers();
    }

    //
    /* (non-Javadoc)
     * @see org.eclipse.gendoc.tags.handlers.AbstractTagHandler#run(org.eclipse.gendoc.tags.ITag)
     */
    final public String run(ITag tag) throws GenDocException 
    {
        ITag newTag = preRun(tag);
        String value = doRun(newTag);
        value = postRun(newTag, value);
        return value;
    }

    /**
     * 
     */
    private static void initHandlers()
    {
        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        IConfigurationElement[] extensions = extensionRegistry.getConfigurationElementsFor(Activator.PLUGIN_ID, "sequencer");
        for (IConfigurationElement e : extensions)
        {
            String tagName = e.getAttribute("tag_name");
            int priority = Integer.parseInt(e.getAttribute("priority"));
            Object handler = null;
            if ("pre_tag_handler".equals(e.getName()))
            {
                try
                {
                    Set<PriorityPreHandler> list = preHandlers.get(tagName);
                    if (list == null)
                    {
                        list = new TreeSet<PriorityPreHandler>(new PriorityComparator());
                        preHandlers.put(tagName, (SortedSet<PriorityPreHandler>) list);
                    }
                    handler = e.createExecutableExtension("runnable");
                    list.add(new PriorityPreHandler((IPreTagHandler) handler, priority));
                }
                catch (CoreException e1)
                {
                    e1.printStackTrace();
                }
            }
            else if ("post_tag_handler".equals(e.getName()))
            {
                try
                {
                    Set<PriorityPostHandler> list = postHandlers.get(tagName);
                    if (list == null)
                    {
                        list = new TreeSet<PriorityPostHandler>(new PriorityComparator());
                        postHandlers.put(tagName, (SortedSet<PriorityPostHandler>) list);
                    }
                    handler = e.createExecutableExtension("runnable");
                    list.add(new PriorityPostHandler((IPostTagHandler) handler, priority));
                }
                catch (CoreException e1)
                {
                    e1.printStackTrace();
                }
            }

        }
    }

    protected String doRun(ITag tag) throws GenDocException
    {
        return super.run(tag);
    }

    protected ITag preRun(ITag tag) throws GenDocException
    {
        SortedSet<PriorityPreHandler> extensionPre = preHandlers.get(tag.getName());

        if (extensionPre != null)
        {
            for (PriorityPreHandler preRunnable : extensionPre)
            {
                tag = preRunnable.preRun(tag);
            }
        }
        return tag;
    }

    protected String postRun(ITag tag, String str) throws GenDocException
    {
        SortedSet<PriorityPostHandler> extensionPost = postHandlers.get(tag.getName());
        if (extensionPost != null)
        {
            for (PriorityPostHandler postRunnable : extensionPost)
            {
                str = postRunnable.postRun(tag, str);
            }
        }
        return str;

    }

    
    private interface IPriority
    {
        int getPriority() ;
    }

    private static class PriorityPreHandler implements IPreTagHandler,IPriority
    {

        private final IPreTagHandler handler;

        private final int priority;

        public PriorityPreHandler(IPreTagHandler handler, int priority)
        {
            this.handler = handler;
            this.priority = priority;
        }

        public int getPriority()
        {
            return priority;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.gendoc.tags.IPreTagHandler#preRun(org.eclipse.gendoc.tags.ITag)
         */
        public ITag preRun(ITag tag) throws GenDocException
        {
            return handler.preRun(tag);
        }

    }

    private static class PriorityPostHandler implements IPostTagHandler,IPriority
    {

        private final IPostTagHandler handler;

        private final int priority;

        public PriorityPostHandler(IPostTagHandler handler, int priority)
        {
            this.handler = handler;
            this.priority = priority;
        }

        public int getPriority()
        {
            return priority;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.gendoc.tags.IPostTagHandler#postRun(org.eclipse.gendoc.tags.ITag, java.lang.String)
         */
        public String postRun(ITag tag, String value) throws GenDocException
        {
            // TODO Auto-generated method stub
            return handler.postRun(tag, value);
        }

    }
    
    public static class PriorityComparator implements Comparator<IPriority>
    {
        public int compare(IPriority arg0, IPriority arg1)
        {
            Integer a = -1;
            Integer b = -1;
            if (arg0 != null)
            {
                a = arg0.getPriority();
            }
            if (arg1 != null)
            {
                b = arg1.getPriority();
            }
            if (a == b)
            {
                return new Integer(arg0.hashCode()).compareTo(new Integer(arg1.hashCode()));
            }
            return a.compareTo(b);
        }
        
    }
}
