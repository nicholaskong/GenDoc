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
 *  Vincent Hemery (Atos Origin) - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.bundle.acceleo.papyrus.process;

import org.eclipse.gendoc.bundle.acceleo.papyrus.service.PapyrusContextService;
import org.eclipse.gendoc.process.AbstractProcess;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.handlers.IContextService;
import org.eclipse.gendoc.tags.handlers.impl.context.ContextService;

/**
 * This process ensures a correct model resource set will be constructed for Papyrus models.
 * 
 * @author vhemery
 */
public class PapyrusModelLoadingOverriding extends AbstractProcess
{
    /**
     * Override the context service.
     */
    @Override
    protected void doRun() throws GenDocException
    {
        ContextService contextService = new PapyrusContextService();
        contextService.setServiceId("ContextService");//$NON-NLS-1$
        GendocServices.getDefault().setService(IContextService.class, contextService);

    }

    /**
     * Get number of work units
     * 
     * @return 1
     */
    @Override
    protected int getTotalWork()
    {
        return 1;
    }

}
