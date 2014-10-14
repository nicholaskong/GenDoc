/*****************************************************************************
 * Copyright (c) 2012 Atos.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Alexia Allanic (Atos Origin) alexia.allanic@atosorigin.com - Initial API and implementation
 * Aïcha Boudjelal (Atos) aicha.boudjelal@atos.net - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.wizard.batch;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.gendoc.GendocProcess;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.IProgressMonitorService;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;
import org.eclipse.gendoc.wizard.Activator;
import org.eclipse.gendoc.wizard.IGendocRunner;
import org.eclipse.gendoc.wizard.IGendocTemplate;
import org.eclipse.gendoc.wizard.Utils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;

public class GendocWizardBatchModeApplication implements IApplication
{

    private static final String RUNNERS_LIST = "runners_list";

    private static final String OUTPUT = "output";

    private static final String MODEL = "model";

    private static final String RUNNER_CLASS = "runner_class";

    private boolean validArguments = true;

    private ArgOpt runnerClassArg = new ArgOpt(RUNNER_CLASS, ArgOpt.REQUIRED_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "rc", "runner class ");

    private ArgOpt modelArg = new ArgOpt(MODEL, ArgOpt.REQUIRED_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "mdl", "UML model ");

    private ArgOpt outputArg = new ArgOpt(OUTPUT, ArgOpt.REQUIRED_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "out", "output file complete path ");

    private ArgOpt allRunnersArg = new ArgOpt(RUNNERS_LIST, ArgOpt.OPTIONAL_ARGUMENT, ArgOpt.OPTIONAL_ARGUMENT_VALUE, "rl", "list all runners ");

    private String argRunnerClass = "";

    private String argModel = "";

    private String argOutput = "";

    public Object start(IApplicationContext context) throws Exception
    {
        Display.getDefault().wake();
        setupWorkbench();

        String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);

        GetOpt getOpt = new GetOpt();
        String argFlat = getOpt.getFlatArguments(args);
        if (argFlat.indexOf("--runners_list") >= 0 || argFlat.indexOf("-rl") >= 0)
        {
            System.out.println("Available runners :");
            for (IGendocRunner lPlatformRunner : Utils.getAllRunners())
            {
                System.out.println(String.format("\t- %s ", lPlatformRunner.getClass().getName()));

                Map<String, String> lAddParams = lPlatformRunner.getAdditionnalParameters();
                if (lAddParams != null && !lAddParams.isEmpty())
                {
                    System.out.println("\t\t-> Additional parameters: ");
                    for (String lAddParam : lAddParams.keySet())
                    {
                        System.out.println(String.format("\t\t\t* name: %s; description: %s ", lAddParam, lAddParams.get(lAddParam)));
                    }
                }
            }

            return EXIT_OK;
        }

        long lStartTime = Calendar.getInstance().getTimeInMillis();

        ArgOpt[] listOpts = {runnerClassArg, modelArg, outputArg, allRunnersArg};
        HashMap<String, String> parameters = checkAndLoadParam(args, listOpts);

        if (validArguments)
        {
            argRunnerClass = parameters.get(RUNNER_CLASS);
            argModel = parameters.get(MODEL);
            argOutput = parameters.get(OUTPUT);

            try
            {
                System.out.println("Generation starting...");
                System.out.println("---------------------");
                Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "Starting the batch mode..."));
                Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "---------------------"));

                IGendocRunner lRunner = null;
                IGendocTemplate lTemplateToUse = null;

                for (IGendocRunner lPlatformRunner : Utils.getAllRunners())
                {
                    if (lPlatformRunner.getClass().getName().equals(argRunnerClass))
                    {
                        lRunner = lPlatformRunner;
                        break;
                    }
                }
                if (lRunner == null)
                {
                    throw new Exception("The runner [" + argRunnerClass + "] is not defined ");
                }

                String lExtension = argOutput.substring(argOutput.toLowerCase().lastIndexOf(".") + 1);
                if (lExtension == null)
                {
                    throw new Exception("You shall define the extension in output parameter");
                }

                if (lRunner.getGendocTemplates() != null)
                {
                    for (IGendocTemplate lTemplate : lRunner.getGendocTemplates())
                    {
                        if (lTemplate.getOutPutExtension() == null)
                        {
                            throw new Exception("You shall define output extension in runner templates");
                        }

                        if (lExtension.equals(lTemplate.getOutPutExtension()))
                        {
                            lTemplateToUse = lTemplate;
                        }
                    }
                }
                else
                {
                    throw new Exception("No template defined in the runner [" + argRunnerClass + "]");
                }

                if (lTemplateToUse == null)
                {
                    throw new Exception("No template is defined for output extension [" + lExtension + "] in runner [" + argRunnerClass + "]");
                }

                IGendocDiagnostician diagnostician = GendocServices.getDefault().getService(IGendocDiagnostician.class);
                diagnostician.init();

                IConfigurationService parameter = GendocServices.getDefault().getService(IConfigurationService.class);
                parameter.addParameter(lTemplateToUse.getOutputKey(), argOutput);
                parameter.addParameter(lTemplateToUse.getModelKey(), Platform.getLocation().toFile().getPath() + "/" + argModel);

                if (lRunner.getAdditionnalParameters() != null && !lRunner.getAdditionnalParameters().isEmpty())
                {
                    ArgOpt[] listAddOptsTab = new ArgOpt[1];
                    for (String lParam : lRunner.getAdditionnalParameters().keySet())
                    {
                        listAddOptsTab[0] = new ArgOpt(lParam, ArgOpt.REQUIRED_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, lParam.substring(0, 2), "Additionnal param " + lParam);

                        HashMap<String, String> addParam = checkAndLoadParam(args, listAddOptsTab);

                        if (validArguments)
                        {
                            parameter.addParameter(lParam, addParam.get(lParam));
                        }
                    }
                }

                if (validArguments)
                {
                    IProgressMonitorService service = GendocServices.getDefault().getService(IProgressMonitorService.class);
                    service.setMonitor(new NullProgressMonitor());
                    GendocProcess process = new GendocProcess();
                    String resultFile = process.runProcess(lTemplateToUse.getTemplate());
                    // Message in case of error in generation
                    handleDiagnostic(diagnostician.getResultDiagnostic(), "The file has been generated but contains errors :\n", resultFile);

                    long lEndTime = Calendar.getInstance().getTimeInMillis();

                    long lGenDuration = lEndTime - lStartTime;

                    System.out.println("Generation end (" + convertTime(lGenDuration) + ")");
                }
            }
            catch (Exception e)
            {
                System.out.println("\n$$$$$$$$$$$$$--ERROR--$$$$$$$$$$$$$");
                Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "\n$$$$$$$$$$$$$--ERROR--$$$$$$$$$$$$$"));
                String message = " Exception occured in the generation ";
                if (e.getMessage() != null)
                {
                    message = message + ": " + e.getMessage();
                }
                if (e.getCause() != null)
                {
                    message = message + "::" + e.getCause();
                }

                System.out.println(message);
                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
                Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message));
                Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n"));
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
            Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "The document has been generated successfully: \n" + resultFilePath));

            System.out.println("The document has been generated successfully: \n" + resultFilePath);
        }
        else
        {
            String path = "";
            if (resultFilePath != null)
            {
                path = resultFilePath;
            }

            Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message + path));
            IStatus lStatus = BasicDiagnostic.toIStatus(resultDiagnostic);
            Activator.getDefault().getLog().log(lStatus);
            
            System.out.println(message + " " + lStatus.getMessage() == null ? "" : lStatus.getMessage());
            for (IStatus lStatChild : lStatus.getChildren()) {
                System.out.println("\t" + lStatChild.getMessage());
            }
        }

    }

    public void stop()
    {
        System.out.println("End of the generation");
        System.out.println("---------------------");
        Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "End of the generation"));
        Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "---------------------"));
    }

    private HashMap<String, String> checkAndLoadParam(String[] pArgs, ArgOpt[] pArgOpts) throws Exception
    {
        HashMap<String, String> params = new HashMap<String, String>();
        GetOpt getOpt = new GetOpt();
        String argFlat = getOpt.getFlatArguments(pArgs);

        if (argFlat.indexOf("--help") >= 0 || argFlat.indexOf("-h") >= 0)
        {
            getOpt.printHelp(pArgOpts);
            validArguments = false;
        }
        else
        {
            params = getOpt.getArguments(pArgOpts, pArgs);
            if (GetOpt.error == null || GetOpt.error.length() > 0)
            {
                System.out.println(GetOpt.error);
                validArguments = false;
            }
        }

        return params;
    }

    private String convertTime(long ms)
    {
        ms = ms / 1000;
        long seconds = ms % 60;
        ms = ms / 60;
        long minutes = ms % 60;
        ms = ms / 60;
        long hours = ms;

        return ("Duration: " + hours + "h " + minutes + "min " + seconds + "s");
    }

    private void setupWorkbench()
    {
        Workbench.createAndRunWorkbench(Display.getDefault(), new WorkbenchAdvisor()
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
