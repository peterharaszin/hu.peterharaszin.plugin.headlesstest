package hu.peterharaszin.plugin.headlesstest;

import java.io.IOException;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class TestHeadlessApplication implements IApplication {
	// projects with the Java nature
	private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";
	private static final String myBundleName = "hu.peterharaszin.quicktest.java";
	private static Logger logger = Logger.getLogger("TestHeadlessLog");
	private static String logfilename = "TestHeadlessLogFile.log";
	private static FileHandler logFilehandler;

	/**
	 * 
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		System.out
				.println("Hello, world! This is my fantastic headless application.");
		final Map<?, ?> args = context.getArguments();
		final String[] appArgs = (String[]) args.get("application.args");

		if (appArgs.length == 0) {
			System.out.println("No arguments have been provided.");
		} else {
			System.out.println("The arguments are the following:");
			for (final String arg : appArgs) {
				System.out.println(arg);
			}
		}

		try {
			setLogger();

			IProject testProject = getProject();
			if (!testProject.exists()) {
				System.out.println("ERROR: Project named " + myBundleName
						+ " does not exist!");
				return IApplication.EXIT_OK;
			}

			// open if necessary
			if (!testProject.isOpen()) {
				testProject.open(null);
			}

			// listProjectsInWorkspace();
			buildProject(testProject);
		} catch (Exception e) {
			logger.severe("====================================================================");
			logger.severe("Whoooops: an exception has been thrown... see the stack trace below.");
			logger.log(Level.SEVERE, "Problems whoaaa", e);
			logger.severe("====================================================================");
		}

		return IApplication.EXIT_OK;
	}

	public void setLogger() throws SecurityException, IOException {
		// get current working directory ()
		String currentWorkingDirectory = java.nio.file.Paths.get(".")
				.toAbsolutePath().normalize().toString();
		// configuring the logger with the handler and formatter
		logFilehandler = new FileHandler(currentWorkingDirectory + "/"
				+ logfilename);
		logger.addHandler(logFilehandler);
		SimpleFormatter formatter = new SimpleFormatter();
		logFilehandler.setFormatter(formatter);
	}

	/**
	 * Get the project we will continue to work on.
	 * 
	 * @return
	 */
	public IProject getProject() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		return workspaceRoot.getProject(myBundleName);
	}

	/**
	 * List all the projects in the currently available workspace (which you can
	 * override with the -data argument)
	 */
	public void listProjectsInWorkspace() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IProject[] projects = workspaceRoot.getProjects();
		if (projects.length == 0) {
			System.out.println("There are no projects in this workspace!");
		} else {

			System.out.println("iterating through workspace projects");

			for (IProject currentProject : projects) {
				System.out.println("currentProject.getName(): "
						+ currentProject.getName());
			}
		}
	}

	/**
	 * Build Java project.
	 * 
	 * @param project
	 * @throws Exception
	 */
	public void buildProject(IProject project) throws Exception {
		System.out.println("Building project '" + project.getName() + "'...");
		// open if necessary
		if (!project.isOpen()) {
			project.open(null);
		}

		// projects with the Java nature
		boolean isJavaNatureEnabled = project.isNatureEnabled(JDT_NATURE);

		System.out.println("isJavaNatureEnabled: " + isJavaNatureEnabled);

		String[] natureIds = project.getDescription().getNatureIds();

		for (String natureId : natureIds) {
			// natureId: org.eclipse.jdt.core.javanature
			System.out.println("natureId: " + natureId);
			// IProjectNature projectNature = project.getNature(natureId);
			// IProject projectFromNature = projectNature.getProject();
			// System.out.println("projectname: "+projectFromNature.getName());
		}

		IProjectDescription projectDescription = project.getDescription();
		ICommand[] buildSpec = projectDescription.getBuildSpec();
		System.out.println("buildSpec: " + buildSpec);
		for (ICommand iCommand : buildSpec) {
			System.out.println("iCommand.getBuilderName(): "
					+ iCommand.getBuilderName());
		}
		IBuildConfiguration[] buildConfigurations = project.getBuildConfigs();
		for (IBuildConfiguration buildConfiguration : buildConfigurations) {
			System.out.println("buildConfiguration.getName(): '"
					+ buildConfiguration.getName() + "' ("
					+ buildConfiguration.toString() + ")");
		}

		IMarker[] problemMarkers = project.findMarkers(IMarker.PROBLEM, true,
				IResource.DEPTH_INFINITE);
		if (problemMarkers.length == 0) {
			System.out.println("No problem markers...");
		} else {
			for (IMarker problemMarker : problemMarkers) {
				System.out.println("problemMarker: " + problemMarker);
			}
		}

		System.out.println("OK, let's build...");

		// ILaunchManager launchManager =
		// DebugPlugin.getDefault().getLaunchManager();
		// ILaunchConfigurationType launchConfigurationType =
		// launchManager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		// ILaunchConfiguration[] launchConfigurations =
		// launchManager.getLaunchConfigurations(launchConfigurationType);
		// for (ILaunchConfiguration launchConfiguration : launchConfigurations)
		// {
		// System.out.println("launchConfiguration.getName(): "+launchConfiguration.getName());
		// System.out.println("Launching '"+launchConfiguration.getName()+"'...");
		// launchConfiguration.launch( ILaunchManager.RUN_MODE, null, true);
		// }

		//
		// launchManager.getLaunchConfigurations(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);

		// project.build(buildConfigurationSaved,
		// IncrementalProjectBuilder.FULL_BUILD, null);

		// nem szeretnénk monitort, ezért null a második paraméter
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		// project.build(IncrementalProjectBuilder.AUTO_BUILD, null);
		// project.build(IncrementalProjectBuilder.FULL_BUILD,
		// "org.eclipse.jdt.core.javabuilder", null, null);

		System.out.println("OK, building the project is ready!");
	}

	@Override
	public void stop() {

	}

}
