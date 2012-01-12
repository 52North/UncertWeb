package org.uncertweb.wps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.uncertweb.wps.util.ProjectFile;
import org.uncertweb.wps.util.ReadingThread;
import org.uncertweb.wps.util.Workspace;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * implements a process that invokes the Albatross Model and returns the outputs
 * according to the UncertWeb profiles
 * 
 * @author Steffan Voss
 * 
 */
public class SyntheticPopulationProcess extends AbstractAlgorithm {

	private String targetProp, sourceProp, exportFileNameProp,
			publicFolderProp, serverAddressProp, publicFolderVisiblePartProp, exportFileBinNameProp;
	private List<String> filesToCopyProp;

	private final String inputIDGenpopHouseholds = "genpop-households";
	private final String inputIDRwdataHouseholds = "rwdata-households";
	private final String inputIDMunicipalities = "municipalities";
	private final String inputIDZones = "zones";
	private final String inputIDPostcodeAreas = "postcode-areas";

	private final String outputIDProjectFile = "project-file";
	private final String outputIDExportFile = "export-file";

	private String genpopHouseholds;
	private String rwdataHouseholds;
	private String municipalities;
	private String zones;
	private String postcodeAreas;
	
	private Workspace ws;
	private ProjectFile projectFile;

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
		} else {
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

		this.readProperties();

		this.checkAndCopyInput(inputData);

		this.setupFolder();

		this.runModel();
		
		ws.copyResultIntoPublicFolder(filesToCopyProp);

		Map<String, IData> result = new HashMap<String, IData>();
		
		result.put("project-file", new LiteralStringBinding(serverAddressProp + "/" +publicFolderVisiblePartProp+"/"+ ws.getFolderNumber()+"/"+ projectFile.getProjectFileName()));

		result.put("export-file", new LiteralStringBinding(serverAddressProp + "/" +publicFolderVisiblePartProp+"/"+ ws.getFolderNumber()+"/"+ exportFileNameProp));
		
		result.put("export-file-bin", new LiteralStringBinding(serverAddressProp + "/" +publicFolderVisiblePartProp+"/"+ ws.getFolderNumber()+"/"+ exportFileBinNameProp));

		return result;
	}

	private void readProperties() {

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
		filesToCopyProp = Arrays.asList(properties.getProperty("filesToCopy").split(";"));
		serverAddressProp = properties.getProperty("serverAddress");
		publicFolderVisiblePartProp = properties.getProperty("publicFolderVisiblePart");
	}

	private void checkAndCopyInput(Map<String, List<IData>> inputData) {

		if (inputData == null)
			throw new NullPointerException("inputData cannot be null");

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

	}

	private void setupFolder() {

		// workspace bauen
		ws = new Workspace(sourceProp,targetProp,publicFolderProp);

		// create projectFile...
		projectFile = new ProjectFile("ProjectFile.prj", ws.getWorkspaceFolder().getPath(),
				ws.getWorkspaceFolder().getPath(), genpopHouseholds, rwdataHouseholds,
				municipalities, zones, postcodeAreas);

		
	}

	private void runModel() {

		Process proc = null;

		try {

			List<String> commands = new ArrayList<String>();

			commands.add(ws.getWorkspaceFolder().getPath() + File.separator + "Genpop.exe");

			ProcessBuilder pb = new ProcessBuilder(commands);

			pb.directory(ws.getWorkspaceFolder());

			proc = pb.start();

			new ReadingThread(proc.getInputStream(), "out").start();
			new ReadingThread(proc.getErrorStream(), "err").start();

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
			
			if(result != 0){
				throw new RuntimeException("could not run rwdata properly. Try again.");
			}

		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}

	}

}
