package org.uncertweb.wps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.uncertweb.wps.albatross.util.Pair;
import org.uncertweb.wps.albatross.util.ProcessMonitorThread;
import org.uncertweb.wps.albatross.util.ReadingThread;
import org.uncertweb.wps.albatross.util.WorkspaceCleanerThread;
import org.uncertweb.wps.util.ProjectFile;
import org.uncertweb.wps.util.Workspace;

/**
 * implements a process that invokes the Albatross Model and returns the outputs
 * according to the UncertWeb profiles
 *
 * @author Steffan Voss
 *
 */
public class SyntheticPopulationProcess extends AbstractAlgorithm {

	private static String targetProp;
	private static String sourceProp;
	private static String exportFileNameProp;
	private static String publicFolderProp;
	private static String serverAddressProp;
	private static String publicFolderVisiblePartProp;
	private static String exportFileBinNameProp;
	private static List<String> filesToCopyProp;
	private static int folderRemoveCycle;
	private static int processInterruptTime;

	private final String inputIDHouseholdsFraction = "households-fraction";
	private final String inputIDRwdataHouseholds = "rwdata-households";
	private final String inputIDMunicipalities = "municipalities";
	private final String inputIDZones = "zones";
	private final String inputIDPostcodeAreas = "postcode-areas";
	private final String inputIDIsBootstrapping ="isBootstrapping";
	private final String inputIDIsModelUncertainty = "isModelUncertainty";

	private final String outputIDProjectFile = "project-file";
	private final String outputIDExportFile = "export-file";

	private final String inputIDNoCases = "noCases";
	private final String inputIDNoCasesNew = "noCasesNew";

	private String householdFraction;
	private String rwdataHouseholds;
	private String municipalities;
	private String zones;
	private String postcodeAreas;
	private Boolean isBootstrapping;
	private String noCases;
	private String noCasesNew;
	private Boolean isModelUncertainty;

	private Workspace ws;
	private ProjectFile projectFile;

	//private static ProcessMonitorThread processMonitorThread = ProcessMonitorThread.getInstance();
	//private static WorkspaceCleanerThread workspaceCleanerThread = WorkspaceCleanerThread.getInstance();

	//scheduler valid for all instances of the wps
	private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
	protected static Logger log = Logger.getLogger(SyntheticPopulationProcess.class);

	//According to the language spec the static initializer block is only loaded once -> when the class is initialized by the JRE
	static{

		readProperties();

		//processMonitorThread.setInterruptTime(processInterruptTime);
		//workspaceCleanerThread.setInterruptTime(folderRemoveCycle);

		//This is ridiculous... no schedule methods for callables, on the other side the will run forever as the call is inside a try block
//		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
//
//			@Override
//			public void run() {
//				try {
//					workspaceCleanerThread.call();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//			}
//		}, 1,1, TimeUnit.MINUTES);
//
//		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
//
//			@Override
//			public void run() {
//				try {
//					processMonitorThread.call();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//			}
//		}, 1,1, TimeUnit.MINUTES);
//
	}

	@Override
	public List<String> getErrors() {

		return Collections.emptyList();
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if (id.equals(inputIDHouseholdsFraction)) {
			return LiteralDoubleBinding.class;
		}
		if (id.equals(inputIDRwdataHouseholds)) {
			return LiteralIntBinding.class;
		}
		if (id.equals(inputIDPostcodeAreas)) {
			return LiteralIntBinding.class;
		}
		if (id.equals(inputIDZones)) {
			return LiteralIntBinding.class;
		}
		if (id.equals(inputIDMunicipalities)) {
			return LiteralIntBinding.class;
		}
		if(id.equals(inputIDIsBootstrapping) || id.equals(inputIDIsModelUncertainty)){
			return LiteralBooleanBinding.class;
		}
		if(id.equals(inputIDNoCases)){
			return LiteralIntBinding.class;
		}
		if(id.equals(inputIDNoCasesNew)){
			return LiteralIntBinding.class;
		}
		else {
			return GenericFileDataBinding.class;
		}
	}

	@Override
	public Class<?> getOutputDataType(String id) {

		if (id.equalsIgnoreCase(outputIDProjectFile)) {

			return LiteralStringBinding.class;
		}
		if (id.equalsIgnoreCase(outputIDExportFile)) {

			return LiteralStringBinding.class;
		}
		return null;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		this.checkAndCopyInput(inputData);

		this.setupFolder();

		//what we have to do if this is the case...
		//create a config input file wiith two additional parameters
		//run the sampleDraw exe and take care about the input project file
		if(isBootstrapping){

			this.setupSampleDraw();

			this.runSampleDraw();

		}

		this.runModel();

		ws.copyResultIntoPublicFolder(filesToCopyProp);

		Map<String, IData> result = new HashMap<String, IData>();

		result.put("project-file", new LiteralStringBinding(serverAddressProp + "/" +publicFolderVisiblePartProp+"/"+ ws.getFolderNumber()+"/"+ projectFile.getProjectFileName()));

		result.put("export-file", new LiteralStringBinding(serverAddressProp + "/" +publicFolderVisiblePartProp+"/"+ ws.getFolderNumber()+"/"+ exportFileNameProp));

		result.put("export-file-bin", new LiteralStringBinding(serverAddressProp + "/" +publicFolderVisiblePartProp+"/"+ ws.getFolderNumber()+"/"+ exportFileBinNameProp));

		try {
			FileUtils.deleteDirectory(this.ws.getWorkspaceFolder());
		} catch (IOException e) {
			throw new RuntimeException("Error while deleting temporary workspace folder: "+e.getLocalizedMessage());
		}

		return result;
	}

	/**
	 * Reads the properties from the 'albatross-synthetic-population-process.properties' file.
	 *
	 */
	private static void readProperties() {

		Properties properties = new Properties();

		try {

			File configFile = new File(WPSConfig.getInstance().getConfigPath());
			File propertiesFile = new File(configFile.getParent()
					+ File.separator
					+ "albatross-synthetic-population-process.properties");

			FileInputStream fileInputStream = new FileInputStream(
					propertiesFile);

			properties.load(fileInputStream);

		} catch (IOException e) {
			log.info("error while reading properties file "+e.getLocalizedMessage());
			throw new RuntimeException("error while reading properties file "+e.getLocalizedMessage());
		}
		sourceProp = properties.getProperty("originalData");
		targetProp = properties.getProperty("targetWorkspace");
		exportFileNameProp = properties.getProperty("exportFileName");
		exportFileBinNameProp = properties.getProperty("exportFileBinName");
		publicFolderProp = properties.getProperty("publicFolder");
		filesToCopyProp = Arrays.asList(properties.getProperty("filesToCopy").split(";"));
		serverAddressProp = properties.getProperty("serverAddress");
		publicFolderVisiblePartProp = properties.getProperty("publicFolderVisiblePart");
		folderRemoveCycle = Integer.valueOf(properties.getProperty("folderRemoveCycle"));
		processInterruptTime = Integer.valueOf(properties.getProperty("processInterruptTime"));

	}

	/**
	 * Checks if the parameters are available and not <code>null</code>.
	 *
	 * @param inputData a {@link List} of input data.
	 */
	private void checkAndCopyInput(Map<String, List<IData>> inputData) {

		if (inputData == null)
			throw new NullPointerException("inputData cannot be null");

		List<IData> genpopHousholdsList = inputData
				.get(inputIDHouseholdsFraction);

		if (genpopHousholdsList == null || genpopHousholdsList.isEmpty())
			throw new IllegalArgumentException(
					"number of genpop-households is missing");

		householdFraction = ((LiteralDoubleBinding) genpopHousholdsList.get(0))
				.getPayload().toString();

		List<IData> rwdataHouseholdsList = inputData
				.get(inputIDRwdataHouseholds);

		if (rwdataHouseholdsList == null || rwdataHouseholdsList.isEmpty())
			throw new IllegalArgumentException(
					"number of rwdata-households is missing");

		rwdataHouseholds = ((LiteralIntBinding) rwdataHouseholdsList.get(0))
				.getPayload().toString();

		List<IData> postcodeAreasList = inputData.get(inputIDPostcodeAreas);

		if (postcodeAreasList == null || postcodeAreasList.isEmpty())
			throw new IllegalArgumentException("postcode-areas is missing");

		postcodeAreas = ((LiteralIntBinding) postcodeAreasList.get(0))
				.getPayload().toString();

		List<IData> zonesList = inputData.get(inputIDZones);

		if (zonesList == null || zonesList.isEmpty())
			throw new IllegalArgumentException("zones is missing");

		zones = ((LiteralIntBinding) zonesList.get(0)).getPayload().toString();

		List<IData> municipalitiesList = inputData.get(inputIDMunicipalities);

		if (municipalitiesList == null || municipalitiesList.isEmpty())
			throw new IllegalArgumentException("municipalities is missing");

		municipalities = ((LiteralIntBinding) municipalitiesList.get(0))
				.getPayload().toString();

		List<IData> modelUncertaintyList = inputData
				.get(inputIDIsModelUncertainty);

		isModelUncertainty = ((LiteralBooleanBinding) modelUncertaintyList.get(0)).getPayload();

		List<IData> isBootstrappingList = inputData.get(inputIDIsBootstrapping);

		if(isBootstrappingList == null || isBootstrappingList.isEmpty()){
			throw new IllegalArgumentException("isBootstrapping is missing");
		}

		isBootstrapping = ((LiteralBooleanBinding) isBootstrappingList.get(0)).getPayload();

		//if the user set the bootstrapping flag he/she has to provide two additional parameters
		if(isBootstrapping){

			List<IData> noCasesList = inputData.get(inputIDNoCases);

			if(noCasesList == null || noCasesList.isEmpty()){
				throw new IllegalArgumentException("number of cases is missing");
			}

			noCases = ((LiteralIntBinding) noCasesList.get(0))
					.getPayload().toString();

			List<IData> noCasesNewList = inputData.get(inputIDNoCasesNew);

			if(noCasesNewList == null || noCasesNewList.isEmpty()){
				throw new IllegalArgumentException("number of cases NEW is missing");
			}

			noCasesNew = ((LiteralIntBinding) noCasesNewList.get(0))
					.getPayload().toString();
		}


	}

	/**
	 * The synthetic population run requires a workspace, a public folder and a customized project file.
	 * The project file contains the paths to the current workspace.
	 * All these folder are created within this method and the required data is copied.
	 * Additionally the newly created files are supervised and deleted after a certain time (defines in the properties).
	 */
	private void setupFolder() {

		// workspace bauen
		ws = new Workspace(sourceProp,targetProp,publicFolderProp);

		// create projectFile...
		projectFile = new ProjectFile("ProjectFile.prj", ws.getWorkspaceFolder().getPath(),
				ws.getWorkspaceFolder().getPath(), householdFraction, rwdataHouseholds,
				municipalities, zones, postcodeAreas,isModelUncertainty);

		ProjectFile.newSysParProjectFile("SYSpars_test.txt", ws.getWorkspaceFolder().getPath(), householdFraction);


		//supervise the newly created folder and remove them after a specific time
		Set<Pair<File, Long>> fileSet = new HashSet<Pair<File,Long>>();

		fileSet.add(new Pair<File, Long>(ws.getWorkspaceFolder(), System.currentTimeMillis()));
		fileSet.add(new Pair<File, Long>(ws.getPublicFolder(), System.currentTimeMillis()));

		//workspaceCleanerThread.addFileSet(fileSet);

	}

	private void setupSampleDraw(){
		ProjectFile.newInputDrawProjectFile("config.txt", ws.getWorkspaceFolder().getPath(), noCases, noCasesNew);
	}

	private void runSampleDraw(){
		Process proc = null;
		BufferedWriter out = null;
		try {

			List<String> commands = new ArrayList<String>();

			commands.add(ws.getWorkspaceFolder().getPath() + File.separator + "SampleDraw.exe");

			ProcessBuilder pb = new ProcessBuilder(commands);

			pb.directory(ws.getWorkspaceFolder());

			proc = pb.start();

			//make sure the new process is supervised
			Set<Pair<Process, Long>> currentProcess = new HashSet<Pair<Process,Long>>();
			currentProcess.add(new Pair<Process, Long>(proc, System.currentTimeMillis()));

			//processMonitorThread.addProcessSet(currentProcess);

			ExecutorService executorService = Executors.newFixedThreadPool(2);

			executorService.submit(new ReadingThread(proc.getInputStream(), "out"));
			executorService.submit(new ReadingThread(proc.getErrorStream(), "err"));

			OutputStream stdout = proc.getOutputStream();

			out = new BufferedWriter(new OutputStreamWriter(
					stdout));

			//the project file as argument
			out.write(projectFile.getProjectFileName());
			out.newLine();
			out.flush();

			//the configt.txt as argument
			out.write("config.txt");
			out.newLine();
			out.flush();

		} catch (IOException e1) {
			log.info("Error while running sample draw: "+e1.getLocalizedMessage());
			throw new RuntimeException("Error while running sample draw: "+e1.getLocalizedMessage());
		}
		finally{
			try {
				out.close();
			} catch (IOException e) {
				log.info("Error while running sample draw: "+e.getLocalizedMessage());
				throw new RuntimeException("Error while running sample draw: "+e.getLocalizedMessage());
			}
		}

		int result = 0;

		try {

			result = proc.waitFor();

			log.info("Return value: " + result);

		} catch (Exception e) {

			log.info("Error while running sample draw: "+e.getLocalizedMessage());
			throw new RuntimeException("Error while running sample draw: "+e.getLocalizedMessage());

		}
		finally{

			if(result != 0){
				throw new RuntimeException("Could not run sample draw properly. Try again.");
			}
		}

	}

	/**
	 * The 'Genpop.exe' is the core component of the synthetic population service.
	 * The method runs the exe inside the workspace and sets the two required arguemnts.
	 * The first argument is the projectfile, which contains the paths to the necessary data files.
	 * The second argument is the filename of the output. This name can be specified in the properties file and is usually 'export.txt'.
	 *
	 * The process itself is also monitored. If the process runs longer then the specified time for processes from the properties file it will be canceled.
	 */
	private void runModel() {

		Process proc = null;
		BufferedWriter out =null;

		try {

			List<String> commands = new ArrayList<String>();

			commands.add(ws.getWorkspaceFolder().getPath() + File.separator + "Genpop.exe");

			ProcessBuilder pb = new ProcessBuilder(commands);

			pb.directory(ws.getWorkspaceFolder());

			proc = pb.start();

			//make sure the new process is supervised
			Set<Pair<Process, Long>> currentProcess = new HashSet<Pair<Process,Long>>();
			currentProcess.add(new Pair<Process, Long>(proc, System.currentTimeMillis()));

			//processMonitorThread.addProcessSet(currentProcess);

			ExecutorService executorService = Executors.newFixedThreadPool(2);

			executorService.submit(new ReadingThread(proc.getInputStream(), "out"));
			executorService.submit(new ReadingThread(proc.getErrorStream(), "err"));

			OutputStream stdout = proc.getOutputStream();

			out = new BufferedWriter(new OutputStreamWriter(
					stdout));

			/*
			 * and now... it gets dirty
			 * if the sample draw run was made (bootstrapping) the name of the project files changes, because the new
			 * project file points to different files it works like projFile.txt -> projFile-0.prj
			 */

			if(!isBootstrapping){

				//the project file as argument
				out.write(projectFile.getProjectFileName());
				out.newLine();
				out.flush();

			}

			if(isBootstrapping){

				//the project file as argument
				out.write(projectFile.getProjectFileNameAfterSampleDrawRun());
				out.newLine();
				out.flush();

			}

			//the export.txt as argument
			out.write(exportFileNameProp);
			out.newLine();
			out.flush();
		} catch (IOException e1) {
			log.info("Error while running sample draw: "+e1.getLocalizedMessage());
			throw new RuntimeException("Error while running sample draw: "+e1.getLocalizedMessage());
		} finally{
			try {
				out.close();
			} catch (IOException e) {
				log.info("Error while running sample draw: "+e.getLocalizedMessage());
				throw new RuntimeException("Error while running sample draw: "+e.getLocalizedMessage());
			}
		}

		int result = 0;

		try {
			result = proc.waitFor();
			log.info("Return value: " + result);
		} catch (Exception e) {
			log.info("Error while running sample draw: "+e.getLocalizedMessage());
			throw new RuntimeException("Error while running sample draw: "+e.getLocalizedMessage());
		}
		finally{

			if(result != 0){
				throw new RuntimeException("Could not run synpop properly. Try again.");
			}
		}

	}

}
