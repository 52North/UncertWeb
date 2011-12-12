package org.uncertweb.wps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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

	private static String targetProp, sourceProp, exportFileNameProp;

	private final String inputIDGenpopHouseholds = "genpop-households";
	private final String inputIDRwdataHouseholds = "rwdata-households";
	private final String inputIDMunicipalities = "municipalities";
	private final String inputIDZones = "zones";
	private final String inputIDPostcodeAreas = "postcode-areas";

	private final String outputIDProjectFile = "project-file";

	private String genpopHouseholds;
	private String rwdataHouseholds;
	private String municipalities;
	private String zones;
	private String postcodeAreas;

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
		return null;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		readProperties();

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

		// workspace bauen
		String projectFile = "ProjectFile.prj";
		Workspace ws = new Workspace();

		File target = generateTargetWorkspace(targetProp);
		File source = new File(sourceProp);

		ProjectFile pF = new ProjectFile(projectFile, target.getAbsolutePath(),
				source.getAbsolutePath(), genpopHouseholds, rwdataHouseholds,
				municipalities, zones, postcodeAreas);
		ws.getWorkspace(source, target, pF);

		Process proc = null;

		try {

			List<String> commands = new ArrayList<String>();

			commands.add(target + File.separator + "Genpop.exe");

			ProcessBuilder pb = new ProcessBuilder(commands);

			pb.directory(target);

			//System.out.println("working directory: " + pb.directory());
			//System.out.println(pb.environment());

			proc = pb.start();

			new ReadingThread(proc.getInputStream(), "out").start();
			new ReadingThread(proc.getErrorStream(), "err").start();

			OutputStream stdout = proc.getOutputStream();

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					stdout));

			out.write(projectFile);
			out.newLine();
			out.flush();

			out.write(exportFileNameProp);
			out.newLine();
			out.flush();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {

			int result = proc.waitFor();
			System.out.println("Return value: " + result);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, IData> result = new HashMap<String, IData>();

		result.put("project-file", new LiteralStringBinding(
				target+File.separator+projectFile+ " exportfile: "+target+File.separator+exportFileNameProp));
		
		result.put("export-file", new LiteralStringBinding(target+File.separator+exportFileNameProp));

		return result;
	}

	private void readProperties() {

		Properties properties = new Properties();
									 
		
		try {
			
			File configFile = new File(WPSConfig.getInstance().getConfigPath());
			File propertiesFile = new File(configFile.getParent()+File.separator+"albatross-synthetic-population-process.properties");
			
			FileInputStream fileInputStream =  new FileInputStream(propertiesFile);
			
			properties.load(fileInputStream);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sourceProp = properties.getProperty("originalData");
		targetProp = properties.getProperty("targetWorkspace");
		exportFileNameProp = properties.getProperty("exportFileName");
	}
	
	private File generateTargetWorkspace(String target){
		
 
		File f = new File(target+File.separator+generateRandomNumber());
		
		while(f.exists()){
			
			f = new File(target+File.separator+generateRandomNumber());
		}
		
		f.mkdir();
		
		return f;
		
	}
	
	/**
	 * [1000,Integer.Max[
	 * 
	 * @return
	 */
	private int generateRandomNumber(){
	
		int low = 1000;
		return (int) (Math.random() * (Integer.MAX_VALUE - low) + low); 
	}

}
