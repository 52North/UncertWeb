package org.uncertweb.wps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.wps.albatross.util.Pair;
import org.uncertweb.wps.albatross.util.ProcessMonitorThread;
import org.uncertweb.wps.albatross.util.ReadingThread;
import org.uncertweb.wps.albatross.util.WorkspaceCleanerThread;
import org.uncertweb.wps.io.data.binding.complex.AlbatrossUInput;
import org.uncertweb.wps.io.data.binding.complex.AlbatrossUInputBinding;
import org.uncertweb.wps.util.AreaSDFileGenerator;
import org.uncertweb.wps.util.LinkSDFileGenerator;
import org.uncertweb.wps.util.OutputMapper;
import org.uncertweb.wps.util.PostProcessingConfigFile;
import org.uncertweb.wps.util.ProjectFile;
import org.uncertweb.wps.util.Workspace;

import uw.odmatrix.Indicators;
import uw.odmatrix.ODMain;
import uw.odmatrix.ODMatrix;

/**
 * implements a process that invokes the Albatross Model and returns the outputs
 * according to the UncertWeb profiles
 * 
 * @author Steffan Voss
 * 
 */
public class AlbatrossProcess extends AbstractAlgorithm {

	private final String inputIDGenpopHouseholds = "genpop-households";
	private final String inputIDRwdataHouseholds = "rwdata-households";
	private final String inputIDMunicipalities = "municipalities";
	private final String inputIDZones = "zones";
	private final String inputIDPostcodeAreas = "postcode-areas";
	private final String inputIDRandomNumberSeed = "randomNumberSeed";

	private final String inputIDExportFile = "export-file";
	private final String inputIDExportFileBin = "export-file-bin";

	private final String inputIDUncertLink = "uncert-link";
	private final String inputIDUncertArea = "uncert-area";

	private final String outputIDExportFile = "export-file";
	private final String outputIDODMatrix = "ODmatrix";
	private final String outputIDindicators = "indicators";

	private String exportFile;
	private String exportFileBin;

	private String genpopHouseholds;
	private String rwdataHouseholds;
	private String municipalities;
	private String zones;
	private String postcodeAreas;
	private String randomNumberSeed;

	private String indicators;
	private String odMatrix;

	private List<AlbatrossUInput> uncertLink;
	private List<AlbatrossUInput> uncertArea;

	private static String targetProp;
	private static String sourceProp;
	private static String exportFileNameProp;
	private static String publicFolderProp;
	private static String serverAddressProp;
	private static String publicFolderVisiblePartProp;
	private static String exportFileBinNameProp;

	private static List<String> filesToCopyProp;

	private Workspace ws;
	private ProjectFile projectFile;

	private static ProcessMonitorThread processMonitorThread = ProcessMonitorThread
			.getInstance();
	private static WorkspaceCleanerThread workspaceCleanerThread = WorkspaceCleanerThread
			.getInstance();

	private static int folderRemoveCycle;
	private static int processInterruptTime;

	// scheduler valid for all instances of the wps
	private static ScheduledExecutorService scheduledExecutorService = Executors
			.newScheduledThreadPool(2);

	// According to the language spec the static initializer block is only
	// loaded once -> when the class is initialized by the JRE
	static {

		readProperties();

		processMonitorThread.setInterruptTime(processInterruptTime);
		workspaceCleanerThread.setInterruptTime(folderRemoveCycle);

		// This is ridiculous... no schedule methods for callables, on the other
		// side the will run forever as the call is inside a try block
		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				try {
					workspaceCleanerThread.call();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}, 1, 1, TimeUnit.MINUTES);

		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				try {
					processMonitorThread.call();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}, 1, 1, TimeUnit.MINUTES);

	}

	@Override
	public List<String> getErrors() {

		return Collections.emptyList();
	}

	@Override
	public Class<?> getInputDataType(String id) {

		if (id.equals(inputIDGenpopHouseholds)) {
			return LiteralIntBinding.class;
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
		if (id.equals(inputIDExportFile)) {
			return LiteralStringBinding.class;
		}
		if (id.equals(inputIDRandomNumberSeed)) {
			return LiteralIntBinding.class;
		}
		if (id.equals(inputIDExportFileBin)) {
			return LiteralStringBinding.class;
		}
		if (id.equals(inputIDUncertArea) || id.equals(inputIDUncertLink)) {
			return AlbatrossUInputBinding.class;
		} else {
			return GenericFileDataBinding.class;
		}
	}

	@Override
	public Class<?> getOutputDataType(String id) {

		if (id.equalsIgnoreCase(outputIDExportFile)) {

			return LiteralStringBinding.class;
		} else if (id.equals(outputIDODMatrix) || id.equals(outputIDindicators)) {
			return OMBinding.class;
		}
		return null;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		this.checkAndCopyInput(inputData);

		this.setupFolder();

		this.downloadFiles();

		// is input draw required?
		// TODO
		if (uncertArea != null && uncertLink != null) {

			this.setupInputDraw();

			this.runInputDraw();
		}

		this.runModel();

		// Taos post processing
		this.setupPostProcessing();

		this.runPostProcessing();

		OutputMapper om = new OutputMapper();
		IObservationCollection indicatorCol = null;
		IObservationCollection odMatrixCol = null;
		try {
			indicatorCol = om.encodeIndicators(indicators);
			odMatrixCol = om.encodeODMatrix(odMatrix);
		} catch (OMEncodingException e) {
			throw new RuntimeException(
					"Error while encoding observation responses from Albatross Output: "
							+ e.getMessage());
		}

		Map<String, IData> result = new HashMap<String, IData>();
		OMBinding indOutput = new OMBinding(indicatorCol);
		OMBinding odMatrixOutput = new OMBinding(odMatrixCol);

		result.put(outputIDODMatrix, odMatrixOutput);
		result.put(outputIDindicators, indOutput);
		return result;
	}

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

			e.printStackTrace();
		}
		sourceProp = properties.getProperty("originalData");
		targetProp = properties.getProperty("targetWorkspace");
		exportFileNameProp = properties.getProperty("exportFileName");
		exportFileBinNameProp = properties.getProperty("exportFileBinName");
		publicFolderProp = properties.getProperty("publicFolder");
		filesToCopyProp = Arrays.asList(properties.getProperty("filesToCopy")
				.split(";"));
		serverAddressProp = properties.getProperty("serverAddress");
		publicFolderVisiblePartProp = properties
				.getProperty("publicFolderVisiblePart");

		folderRemoveCycle = Integer.valueOf(properties
				.getProperty("folderRemoveCycle"));
		processInterruptTime = Integer.valueOf(properties
				.getProperty("processInterruptTime"));
	}

	private void checkAndCopyInput(Map<String, List<IData>> inputData) {

		if (inputData == null)
			throw new NullPointerException("inputData cannot be null");

		List<IData> exportFileList = inputData.get(inputIDExportFile);

		if (exportFileList == null || exportFileList.isEmpty())
			throw new IllegalArgumentException("exportfile is missing");

		exportFile = ((LiteralStringBinding) exportFileList.get(0))
				.getPayload().toString();

		List<IData> exportFileBinList = inputData.get(inputIDExportFileBin);

		if (exportFileBinList == null || exportFileBinList.isEmpty())
			throw new IllegalArgumentException("exportfilebin is missing");

		exportFileBin = ((LiteralStringBinding) exportFileBinList.get(0))
				.getPayload().toString();

		List<IData> genpopHousholdsList = inputData
				.get(inputIDGenpopHouseholds);

		if (genpopHousholdsList == null || genpopHousholdsList.isEmpty())
			throw new IllegalArgumentException(
					"number of genpop-households is missing");

		genpopHouseholds = ((LiteralIntBinding) genpopHousholdsList.get(0))
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

		List<IData> randomNumberSeedList = inputData
				.get(inputIDRandomNumberSeed);
		if (randomNumberSeedList == null || randomNumberSeedList.isEmpty()) {
			throw new IllegalArgumentException("randomNumberSeed is missing");
		}
		randomNumberSeed = ((LiteralIntBinding) randomNumberSeedList.get(0))
				.getPayload().toString();

		List<IData> uncertLinkList = inputData.get(inputIDUncertLink);

		if (uncertLinkList != null) {
			for (IData currentLinkList : uncertLinkList) {

				uncertLink.add((AlbatrossUInput) currentLinkList.getPayload());
			}

		}
		List<IData> uncertAreaList = inputData.get(inputIDUncertArea);

		if (uncertAreaList != null) {

			for (IData currentAreaList : uncertAreaList) {
				uncertArea.add((AlbatrossUInput) currentAreaList.getPayload());

			}
		}

	}

	private void setupFolder() {

		// workspace bauen
		ws = new Workspace(sourceProp, targetProp, publicFolderProp);

		// create projectFile...
		projectFile = new ProjectFile("ProjectFile.prj", ws
				.getWorkspaceFolder().getPath(), ws.getWorkspaceFolder()
				.getPath(), genpopHouseholds, rwdataHouseholds, municipalities,
				zones, postcodeAreas, randomNumberSeed);

		Set<Pair<File, Long>> fileSet = new HashSet<Pair<File, Long>>();

		fileSet.add(new Pair<File, Long>(ws.getWorkspaceFolder(), System
				.currentTimeMillis()));
		fileSet.add(new Pair<File, Long>(ws.getPublicFolder(), System
				.currentTimeMillis()));

		workspaceCleanerThread.addFileSet(fileSet);

	}

	/**
	 * The albatross model requires at least two files from the synthetic
	 * population service. The export file (text) and the exportFileBin
	 * (binary). The are expected to be avaiable somewhere in the web.
	 */
	private void downloadFiles() {

		try {
			downloadFile(this.exportFile, ws.getWorkspaceFolder()
					+ File.separator + this.exportFileNameProp);
			downloadFile(this.exportFileBin, ws.getWorkspaceFolder()
					+ File.separator + this.exportFileBinNameProp);
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	/**
	 * Wrapper for the apache URL2file method.
	 * 
	 * @param urlString
	 * @param filename
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void downloadFile(String urlString, String filename)
			throws MalformedURLException, IOException {

		FileUtils.copyURLToFile(new URL(urlString), new File(filename));

	}

	/**
	 * Every workspace has a folder called postprocessing. The postprocessing
	 * code was initially delivered as jar but later decompiled and directly
	 * integrated. However, the system expects a config file with the paths
	 * pointing to the OUT_x files (the will be written) and the export file.
	 * The name of the file is config_uw.xml. All of them are stored in the
	 * postprocessing folder.this methods created the required config.xml and
	 * sets the paths to the output files, which will be parsed afterwards (this
	 * is actually the output of the model run).
	 * 
	 * @see AlbatrossProcess#runPostProcessing()
	 */
	private void setupPostProcessing() {

		String postProcessingPath = ws.getWorkspaceFolder().getAbsolutePath()
				+ File.separator + "postprocessing";

		PostProcessingConfigFile postProcessingConfigFile = new PostProcessingConfigFile(
				ws.getWorkspaceFolder() + File.separator + exportFileNameProp,
				"4pca in Rotterdam.csv", "OUT_odmatrix.csv",
				"OUT_indicators.csv", postProcessingPath);

		indicators = postProcessingConfigFile.getIndicatorsPath();
		odMatrix = postProcessingConfigFile.getOdmPath();

	}

	/**
	 * In order to setup the InputDraw.exe an inputDraw project file is
	 * required. Furthermore the two files link-sd.txt and area-sd.txt are
	 * needed to run the executable. The method generates the three files and
	 * writes them into the workspace.
	 * 
	 * Moreover the uncertML encoded data for the area and link sd are
	 * transformed to the expected inputDraw format.
	 */
	private void setupInputDraw() {

		ProjectFile.newInputDrawProjectFile("InputDrawProjectFile.prj", ws
				.getWorkspaceFolder().getPath(), ws.getWorkspaceFolder()
				.getPath(), genpopHouseholds, rwdataHouseholds, municipalities,
				zones, postcodeAreas, randomNumberSeed);

		// schreiben der beiden dateien
		LinkSDFileGenerator linkSDFileGenerator = new LinkSDFileGenerator(
				uncertLink);

		linkSDFileGenerator.toFile(new File(ws.getWorkspaceFolder().getPath()
				+ File.pathSeparator + "link-sd.txt"));

		AreaSDFileGenerator areaSDFileGenerator = new AreaSDFileGenerator(
				uncertArea);

		areaSDFileGenerator.toFile(new File(ws.getWorkspaceFolder().getPath()
				+ File.pathSeparator + "area-sd.txt"));

	}

	/**
	 * The external inputDraw.exe will be called. The methods waits until the
	 * inputDraw.exe returns. It it recommended that you call
	 * {@link AlbatrossProcess#setupInputDraw()} before calling this method. The
	 * inputDraw.exe requires a project file and the link-sd and area-sd file.
	 * The process is supervised by the system and will be terminated after the
	 * by the user specified time for a run expires. The output, usually written
	 * to std out will be read by a thread and printed to the console.
	 * 
	 * @see AlbatrossProcess#setupInputDraw()
	 */
	private void runInputDraw() {

		Process proc = null;

		try {

			List<String> commands = new ArrayList<String>();

			commands.add(ws.getWorkspaceFolder().getPath() + File.separator
					+ "Inputdraw.exe");

			ProcessBuilder pb = new ProcessBuilder(commands);

			pb.directory(ws.getWorkspaceFolder());

			proc = pb.start();

			// make sure the new process is supervised
			Set<Pair<Process, Long>> currentProcess = new HashSet<Pair<Process, Long>>();
			currentProcess.add(new Pair<Process, Long>(proc, System
					.currentTimeMillis()));

			processMonitorThread.addProcessSet(currentProcess);

			ExecutorService executorService = Executors.newFixedThreadPool(2);

			executorService.submit(new ReadingThread(proc.getInputStream(),
					"out"));
			executorService.submit(new ReadingThread(proc.getErrorStream(),
					"err"));

			OutputStream stdout = proc.getOutputStream();

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					stdout));

			out.write("InputDrawProjectFile.prj");
			out.newLine();
			out.flush();

			out.write("link-sd.txt");
			out.newLine();
			out.flush();

			out.write("area-sd.txt");
			out.newLine();
			out.flush();

		} catch (IOException e1) {

			e1.printStackTrace();
		}

		try {

			int result = proc.waitFor();
			System.out.println("Return value for input draw: " + result);
			if (result != 0) {
				throw new RuntimeException(
						"could not run input draw properly. Try again.");
			}

		} catch (InterruptedException e) {

			e.printStackTrace();
		}

	}

	private void runPostProcessing() {

		String configFile = ws.getWorkspaceFolder().getAbsolutePath()
				+ File.separator + "postprocessing" + File.separator
				+ "config_uw.xml";

		ApplicationContext context = new FileSystemXmlApplicationContext(
				configFile);

		ODMatrix objOD = (ODMatrix) context.getBean("source");
		try {
			ODMain odmain = new ODMain(objOD.getFileSchedule(),
					objOD.getFileArea(), objOD.getFileODMtx());
		} catch (IOException e1) {

			e1.printStackTrace();
		}

		Indicators indics = (Indicators) context.getBean("indexes");
		try {
			Indicators indicators = new Indicators(objOD.getFileSchedule(),
					indics.getFileIndicators());
		} catch (IOException e) {

			e.printStackTrace();
		}

		// System.out.println("== >>> FINISH.");

	}

	private void runModel() {

		Process proc = null;

		try {

			List<String> commands = new ArrayList<String>();

			commands.add(ws.getWorkspaceFolder().getPath() + File.separator
					+ "Rwdata.exe");

			ProcessBuilder pb = new ProcessBuilder(commands);

			pb.directory(ws.getWorkspaceFolder());

			proc = pb.start();

			// make sure the new process is supervised
			Set<Pair<Process, Long>> currentProcess = new HashSet<Pair<Process, Long>>();
			currentProcess.add(new Pair<Process, Long>(proc, System
					.currentTimeMillis()));

			processMonitorThread.addProcessSet(currentProcess);

			ExecutorService executorService = Executors.newFixedThreadPool(2);

			executorService.submit(new ReadingThread(proc.getInputStream(),
					"out"));
			executorService.submit(new ReadingThread(proc.getErrorStream(),
					"err"));

			OutputStream stdout = proc.getOutputStream();

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					stdout));

			out.write(projectFile.getProjectFileName());
			out.newLine();
			out.flush();

			out.write(exportFileNameProp);
			out.newLine();
			out.flush();

		} catch (IOException e1) {

			e1.printStackTrace();
		}

		try {

			int result = proc.waitFor();
			System.out.println("Return value: " + result);
			if (result != 0) {
				throw new RuntimeException(
						"could not run rwdata properly. Try again.");
			}

		} catch (InterruptedException e) {

			e.printStackTrace();
		}

	}

}
