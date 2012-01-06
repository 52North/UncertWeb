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
import org.uncertweb.wps.util.ReadingThread;

import edu.emory.mathcs.backport.java.util.Collections;


/**
 * implements a process that invokes the Albatross Model and returns the outputs according to the
 * UncertWeb profiles
 * 
 * @author Steffan Voss
 *
 */
public class AlbatrossProcess extends AbstractAlgorithm{
	
	private final String inputIDProjectFile = "project-file";
	private final String inputIDExportFile = "export-file";
	private final String inputIDWorkspace = "workspace";
	
	private final String outputIDExportFile = "export-file";
	
	private String workspace;
	private String projectFile;
	private String exportFile;
	
	private String targetProp, sourceProp, exportFileNameProp,
	publicFolderProp, serverAddressProp, publicFolderVisiblePartProp;
	
	private List<String> filesToCopyProp;

	@Override
	public List<String> getErrors() {

		return Collections.emptyList();
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if (id.equals(inputIDProjectFile)) {
			return LiteralIntBinding.class;
		}
		if (id.equals(inputIDExportFile)) {
			return LiteralIntBinding.class;
		}
		if (id.equals(inputIDWorkspace)) {
			return LiteralIntBinding.class;
		}
		 else {
			return GenericFileDataBinding.class;
		}
	}

	@Override
	public Class<?> getOutputDataType(String id) {

		if (id.equalsIgnoreCase(outputIDExportFile)) {

			return LiteralStringBinding.class;
		}
		return null;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		
		this.readProperties();

		this.checkAndCopyInput(inputData);

		//this.setupFolder();

		this.runModel();
		
		//TODO Taos post processing
		
		//ws.copyResultIntoPublicFolder(filesToCopyProp);
		
		//TODO output des model runs liegt hier: workspace+File.separator+exportFile und nach post liegen indicator und matrix in workspace
		

		Map<String, IData> result = new HashMap<String, IData>();
		
		result.put("export-file", new LiteralStringBinding(serverAddressProp + "/" +publicFolderVisiblePartProp+"/"+  exportFileNameProp));

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
		publicFolderProp = properties.getProperty("publicFolder");
		filesToCopyProp = Arrays.asList(properties.getProperty("filesToCopy").split(";"));
		serverAddressProp = properties.getProperty("serverAddress");
		publicFolderVisiblePartProp = properties.getProperty("publicFolderVisiblePart");
	}
	
	private void checkAndCopyInput(Map<String, List<IData>> inputData) {

		if (inputData == null)
			throw new NullPointerException("inputData cannot be null");

		List<IData> workspaceList = inputData
				.get(inputIDWorkspace);

		if (workspaceList == null || workspaceList.isEmpty())
			throw new IllegalArgumentException(
					"workspace is missing");

		workspace = ((LiteralIntBinding) workspaceList.get(0))
				.getPayload().toString();

		List<IData> exportFileList = inputData
				.get(inputIDExportFile);

		if (exportFileList == null || exportFileList.isEmpty())
			throw new IllegalArgumentException(
					"exportfile is missing");

		exportFile = ((LiteralIntBinding) exportFileList.get(0))
				.getPayload().toString();

		List<IData> projectFileList = inputData.get(inputIDProjectFile);

		if (projectFileList == null || projectFileList.isEmpty())
			throw new IllegalArgumentException("projectfile is missing");

		projectFile = ((LiteralIntBinding) projectFileList.get(0))
				.getPayload().toString();

	}
	
	private void runModel() {

		Process proc = null;

		try {

			List<String> commands = new ArrayList<String>();

			commands.add(workspace + File.separator + "Rwdata.exe");

			ProcessBuilder pb = new ProcessBuilder(commands);

			pb.directory(new File(workspace));

			proc = pb.start();

			new ReadingThread(proc.getInputStream(), "out").start();
			new ReadingThread(proc.getErrorStream(), "err").start();

			OutputStream stdout = proc.getOutputStream();

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					stdout));

			out.write(workspace+File.separator+projectFile);
			out.newLine();
			out.flush();

			out.write(workspace+File.separator+exportFile);
			out.newLine();
			out.flush();

		} catch (IOException e1) {
			
			e1.printStackTrace();
		}

		try {

			int result = proc.waitFor();
			System.out.println("Return value: " + result);

		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}

	}

}
