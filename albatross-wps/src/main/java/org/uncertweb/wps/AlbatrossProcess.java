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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.uncertweb.wps.util.PostProcessingConfigFile;
import org.uncertweb.wps.util.ProjectFile;
import org.uncertweb.wps.util.ReadingThread;
import org.uncertweb.wps.util.Workspace;

import uw.odmatrix.Indicators;
import uw.odmatrix.ODMain;
import uw.odmatrix.ODMatrix;

import edu.emory.mathcs.backport.java.util.Collections;

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

	private final String inputIDExportFile = "export-file";
	private final String inputIDExportFileBin = "export-file-bin";

	private final String outputIDExportFile = "export-file";

	private String exportFile;
	private String exportFileBin;

	private String genpopHouseholds;
	private String rwdataHouseholds;
	private String municipalities;
	private String zones;
	private String postcodeAreas;
	
	private String indicators;
	private String odMatrix;

	private String targetProp, sourceProp, exportFileNameProp,
			publicFolderProp, serverAddressProp, publicFolderVisiblePartProp,
			exportFileBinNameProp;

	private List<String> filesToCopyProp;

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
		}
		if (id.equals(inputIDExportFile)) {
			return LiteralStringBinding.class;
		}
		if (id.equals(inputIDExportFileBin)) {
			return LiteralStringBinding.class;
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

		this.setupFolder();

		this.downloadFiles();

		this.runModel();

		this.setupPostProcessing();
		
		//Taos post processing
		this.runPostProcessing();
		
		
		//TODO
		//indicators
		//odMatrix

		Map<String, IData> result = new HashMap<String, IData>();

		result.put("export-file", new LiteralStringBinding(serverAddressProp
				+ "/" + publicFolderVisiblePartProp + "/" + exportFileNameProp));

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
		filesToCopyProp = Arrays.asList(properties.getProperty("filesToCopy")
				.split(";"));
		serverAddressProp = properties.getProperty("serverAddress");
		publicFolderVisiblePartProp = properties
				.getProperty("publicFolderVisiblePart");
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

	}

	private void setupFolder() {

		// workspace bauen
		ws = new Workspace(sourceProp, targetProp, publicFolderProp);

		// create projectFile...
		projectFile = new ProjectFile("ProjectFile.prj", ws
				.getWorkspaceFolder().getPath(), ws.getWorkspaceFolder()
				.getPath(), genpopHouseholds, rwdataHouseholds, municipalities,
				zones, postcodeAreas);

	}

	private void downloadFiles() {

		try {
			downloadFile(this.exportFile, ws.getWorkspaceFolder()
					+ File.separator + this.exportFileNameProp);
			downloadFile(this.exportFileBin, ws.getWorkspaceFolder()
					+ File.separator + this.exportFileBinNameProp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void downloadFile(String urlString, String filename) throws MalformedURLException, IOException
    {
		
		FileUtils.copyURLToFile(new URL(urlString), new File(filename));
		/*
		URL u = new URL(urlString);
	    URLConnection uc = u.openConnection();
	    
	    String contentType = uc.getContentType();
	    int contentLength = uc.getContentLength();
	    /*
	    if (contentType.startsWith("text/") || contentLength == -1) {
	      throw new IOException("This is not a binary file.");
	    }*
	    InputStream raw = uc.getInputStream();
	    
	    InputStream in = new BufferedInputStream(raw);
	    byte[] data = new byte[contentLength];
	    int bytesRead = 0;
	    int offset = 0;
	    while (offset < contentLength) {
	      bytesRead = in.read(data, offset, data.length - offset);
	      if (bytesRead == -1)
	        break;
	      offset += bytesRead;
	    }
	    in.close();

	    if (offset != contentLength) {
	      throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
	    }

	    FileOutputStream out = new FileOutputStream(filename);
	    out.write(data);
	    out.flush();
	    out.close();
		/*		
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try
        {
                in = new BufferedInputStream(new URL(urlString).openStream());
                fout = new FileOutputStream(filename);

                byte data[] = new byte[1024];
                int count;
                while ((count = in.read(data, 0, 1024)) != -1)
                {
                        fout.write(data, 0, count);
                }
        }
        finally
        {
                if (in != null)
                        in.close();
                if (fout != null)
                        fout.close();
        }*/
    }
	
	private void setupPostProcessing(){
		
		String postProcessingPath = ws.getWorkspaceFolder().getAbsolutePath()+File.separator+"postprocessing";
		
		PostProcessingConfigFile postProcessingConfigFile = new PostProcessingConfigFile(ws.getWorkspaceFolder()+File.separator+exportFileNameProp, "4pca in Rotterdam.csv","OUT_odmatrix.csv", "OUT_indicators.csv", postProcessingPath);
		
		indicators = postProcessingConfigFile.getIndicatorsPath();
		odMatrix = postProcessingConfigFile.getOdmPath();
		
	}
	
	private void runPostProcessing(){
		
		 String configFile = ws.getWorkspaceFolder().getAbsolutePath()+File.separator+"postprocessing"+File.separator+"config_uw.xml";
		 
		    ApplicationContext context = new FileSystemXmlApplicationContext(configFile);

		    ODMatrix objOD = (ODMatrix)context.getBean("source");
		    try {
				ODMain odmain = new ODMain(objOD.getFileSchedule(), objOD.getFileArea(), objOD.getFileODMtx());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		    Indicators indics = (Indicators)context.getBean("indexes");
		    try {
				Indicators indicators = new Indicators(objOD.getFileSchedule(), indics.getFileIndicators());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		    System.out.println("== >>> FINISH.");

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
