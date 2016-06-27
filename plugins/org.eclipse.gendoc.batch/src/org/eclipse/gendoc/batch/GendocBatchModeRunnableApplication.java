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
 * Alexia Allanic (Atos Origin) alexia.allanic@atosorigin.com - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.batch;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.gendoc.GendocProcess;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.IProgressMonitorService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;

public class GendocBatchModeRunnableApplication implements IApplication
{
    private boolean validArguments = true;

    public static Logger logger = Logger.getRootLogger();;

    private ArgOpt inputDocArg = new ArgOpt("input_DocTemplate", ArgOpt.REQUIRED_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "idt", "input document template URL ");

    private String argDocTemplate = "";

    public Object start(IApplicationContext context) throws Exception
    {
        Display.getDefault().wake();
        setupWorkbench();
        String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        GetOpt getOpt = new GetOpt();
        String argFlat = getOpt.getFlatArguments(args);
        ArgOpt[] listOpts = {inputDocArg};
        HashMap<String, String> parameters = new HashMap<String, String>();
        if (argFlat.indexOf("--help") >= 0 || argFlat.indexOf("-h") >= 0)
        {

            getOpt.printHelp(listOpts);
            this.validArguments = false;
        }
        else
        {
            parameters = getOpt.getArguments(listOpts, args);
            if (GetOpt.error == null || GetOpt.error.length() > 0)
            {
                System.out.println(GetOpt.error);
                this.validArguments = false;
            }

        }
        /**
         * If the arguments are valid
         */
        if (this.validArguments)
        {
            argDocTemplate = parameters.get("input_DocTemplate");
            try
            {
                logger.log(Level.INFO, "Starting the batch mode...");
                logger.log(Level.INFO, "---------------------");
                File templateDoc = new File(argDocTemplate);
                String templateName = templateDoc.getName();
                /*
                 * Manage the existence/validity of the document template
                 */
                if (!templateDoc.exists())
                {
                    throw new Exception("Your input document does not exist ");
                }
                if (!templateDoc.isDirectory())
                {
                    boolean isSupported = templateName.substring(templateName.lastIndexOf(".")).equalsIgnoreCase(".docx")
                            || templateName.substring(templateName.lastIndexOf(".")).equalsIgnoreCase(".odt");
                    if (!isSupported)
                    {
                        throw new Exception("Your input document is not supported");
                    }
                }
                IGendocDiagnostician diagnostician = GendocServices.getDefault().getService(IGendocDiagnostician.class);
                diagnostician.init();
                IProgressMonitorService service = GendocServices.getDefault().getService(IProgressMonitorService.class);
                service.setMonitor(new NullProgressMonitor());
                GendocProcess process = new GendocProcess();
                String resultFile = process.runProcess(templateDoc);
             // Message in case of error in generation
                handleDiagnostic(diagnostician.getResultDiagnostic(), "The file has been generated but contains errors :\n", resultFile);
            }
            catch (Exception e)
            {
                logger.log(Level.INFO, "\n$$$$$$$$$$$$$--ERROR--$$$$$$$$$$$$$");
                String message = " Exception occured in the generation ";
                if(e.getMessage() != null) {
                    message = message + ": " + e.getMessage();
                }
                if(e.getCause() != null) {
                    message = message + "::" + e.getCause();
                }
                logger.log(Level.ERROR,  message);
                logger.log(Level.INFO, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
            }
        }
        
        /*
         * Stop the application
         */
        this.stop();
        return EXIT_OK;
    }
    
    /**
     * Handle diagnostic.
     * 
     * @param resultDiagnostic the result diagnostic
     */
    private void handleDiagnostic(final Diagnostic resultDiagnostic, final String message, final String resultFilePath)
    {
        if (resultDiagnostic.getSeverity() == Diagnostic.OK)
        {
            logger.log(Level.INFO,"The document has been generated successfully: \n"
                            + resultFilePath);
        }
        else
        {
            String path = "";
            if (resultFilePath != null)
            {
                path = resultFilePath;
            }
            logger.log(Level.ERROR, message + path);
        }

    }

    public void stop()
    {
        logger.log(Level.INFO, "End of the generation");
        logger.log(Level.INFO, "---------------------");
    }

    private void setupWorkbench()
    {
        PlatformUI.createAndRunWorkbench(Display.getDefault(), new WorkbenchAdvisor()
        {
            @Override
            public String getInitialWindowPerspectiveId()
            {
                return "";
            }

            @Override
            public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer)
            {
                return null;
            }

            @Override
            public IAdaptable getDefaultPageInput()
            {
                return null;
            }

            @Override
            public String getMainPreferencePageId()
            {
                return "";
            }

            @Override
            protected IWorkbenchConfigurer getWorkbenchConfigurer()
            {
                return null;
            }

            @Override
            public synchronized AbstractStatusHandler getWorkbenchErrorHandler()
            {
                return null;
            }

            @Override
            public void initialize(IWorkbenchConfigurer configurer)
            {
            }

            @Override
            public boolean openWindows()
            {
                return false;
            }

            @Override
            public IStatus restoreState(IMemento memento)
            {
                return null;
            }

            @Override
            public IStatus saveState(IMemento memento)
            {
                return null;
            }
        });
    }

}
