package org.uncertweb.ems;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.ems.activityprofiles.Profile;
import org.uncertweb.ems.exposuremodelling.IndoorModel;
import org.uncertweb.ems.exposuremodelling.OutdoorModel;
import org.uncertweb.ems.util.Utils;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * Algorithm to estimate uncertain exposure towards air pollutants
 * @author Lydia Gerharz
 *
 */
public class EMSalgorithm extends AbstractObservableAlgorithm{

	private List<String> errors = new ArrayList<String>();
	
	private static String resourcesPath = "C:/WebResources/EMS";
	
	// WPS inputs & outputs
	private final String INPUT_IDENTIFIER_ACTIVITY_PROFILE = "activityProfile";
	private final String INPUT_IDENTIFIER_AIR_QUALITY = "airQualityData";
	private final String INPUT_IDENTIFIER_NUMBER_OF_SAMPLES = "numberOfSamples";
	private final String INPUT_IDENTIFIER_RESOLUTION = "minuteResolution";
	private final String INPUT_IDENTIFIER_OUTPUT_UNCERTAINTY = "outputUncertaintyType";
	private final String OUTPUT_IDENTIFIER = "result";
	
	public EMSalgorithm(){
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(LocalAlgorithmRepository.class.getCanonicalName());
		for(Property property : propertyArray){
			if (property.getName().equalsIgnoreCase("Resources")){
				resourcesPath = property.getStringValue();
				break;
			}
		}	
	}
	
	
	@Override
	public List<String> getErrors() {
		return errors;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if(id.equals(INPUT_IDENTIFIER_ACTIVITY_PROFILE)){
			return OMBinding.class;
		}else if(id.equals(INPUT_IDENTIFIER_AIR_QUALITY)){
			return NetCDFBinding.class;
		}else if(id.equals(INPUT_IDENTIFIER_NUMBER_OF_SAMPLES)){
			return LiteralIntBinding.class;
		}else if(id.equals(INPUT_IDENTIFIER_RESOLUTION)){
			return LiteralIntBinding.class;
		}else if(id.equals(INPUT_IDENTIFIER_OUTPUT_UNCERTAINTY)){
			return LiteralStringBinding.class;
		}else{
			return GenericFileDataBinding.class;			
		}
	}

	@Override
	public Class<?> getOutputDataType(String arg0) {
		return UncertWebIODataBinding.class;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputMap) {				
		/*
		 *  1) get input data
		 */
		// other input parameters
		int cospIterations = 100;
		boolean useIndoorSources = true;
		
		
		// activity data
		IObservationCollection omFile = null;
		List<IData> omList = inputMap.get(INPUT_IDENTIFIER_ACTIVITY_PROFILE);	
		if(omList != null && omList.size()!=0){
			IData tmp = omList.get(0);
			if(tmp instanceof OMBinding){
				omFile = ((OMBinding)tmp).getPayload();
			}
		}
		
		// air quality data
		NetcdfUWFile ncFile = null;
		String nctempFile = "";
		List<IData> ncList = inputMap.get(INPUT_IDENTIFIER_AIR_QUALITY);
		if(ncList != null && ncList.size()!=0){
			IData tmp = ncList.get(0);
			if(tmp instanceof NetCDFBinding){
				ncFile = ((NetCDFBinding)tmp).getPayload();
				nctempFile = ncFile.getNetcdfFile().getLocation();
				nctempFile = nctempFile.replace("\\","/");
			}
		}

		//	number of samples	
		int indoorIterations = 100;
		List<IData> samplesList = inputMap.get(INPUT_IDENTIFIER_NUMBER_OF_SAMPLES);
		if(samplesList != null && samplesList.size()!=0){
			if(samplesList.get(0) instanceof LiteralIntBinding){
				indoorIterations = Integer.parseInt(samplesList.get(0).getPayload().toString());
			}
		}
		
		// temporal resolution
		int minuteResolution = 5;
		List<IData> resList = inputMap.get(INPUT_IDENTIFIER_RESOLUTION);
		if(resList != null && resList.size()!=0){
			if(resList.get(0) instanceof LiteralIntBinding){
				minuteResolution = Integer.parseInt(resList.get(0).getPayload().toString());
			}
		}
				
		// output uncertainty type
		List<String> statList = new ArrayList<String>();
		List<IData> statisticsList = inputMap.get(INPUT_IDENTIFIER_OUTPUT_UNCERTAINTY);
		if(!(statisticsList == null) && statisticsList.size() != 0){
			statList = extractStatisticsFromRequest(statisticsList);			
		}	
		
		/*
		 *  2) perform outdoor overlay
		 */	
		OutdoorModel outdoor = new OutdoorModel(resourcesPath);
		
		// A) for the moment, do the overlay for GPS tracks in MS with the local version			
//		String nctempFile = resourcesPath+"/tmp.nc";
		
		// get main variable as parameter
		String parameter = "";
		try{
			parameter = ncFile.getPrimaryVariable().getName();
		}catch (NetcdfUWException e) {
			e.printStackTrace();
		} 
		
		// create profile
		Profile profile = new Profile(omFile);
				
		// write observation geometry file locally
		String omtempFile = resourcesPath+"/tmp.csv";
		profile.writeObsCollGeometry2csv(omtempFile);
		
		// get outdoor concentration at profile locations
		outdoor.performOutdoorOverlay(profile, nctempFile, omtempFile,parameter);
	
		// estimate COSP uncertainties for PM10
		if(parameter.equals("PM10")){
			outdoor.estimateCOSPUncertainty(profile, omtempFile, cospIterations);
		}
		
		// perform averaging of profile observations
		profile.aggregateProfile(minuteResolution);		
		
		// B) TODO: later use STAS for overlay of Albatross data				
		
		/*
		 *  3) perform indoor model if activity data is available
		 */
		// create indoor model with parameters
		IndoorModel indoor = new IndoorModel();
		indoor.readParametersFile("DE", parameter.replace("_", "."), resourcesPath+"/indoorModel/parameters.csv");
					
		// estimate indoor concentration
		indoor.runModel(profile, indoorIterations, minuteResolution, useIndoorSources);
		
		/*
		 *  4) prepare result
		 */	
		// get OM file
		IObservationCollection exposureProfile = profile.getExposureProfileObservationCollection("lognormal");
		
		Map<String, IData> result = new HashMap<String, IData>(1);
		OMBinding uwData = new OMBinding(exposureProfile);
		result.put(OUTPUT_IDENTIFIER, uwData);
		return result;
		
	}

	
	private String writeNetCDFfile(NetcdfUWFile aqNCfile, String filepath){
		String mainVariable = "";
		
		try{
			// get main variable as parameter
			mainVariable = aqNCfile.getPrimaryVariable().getName();
						
			//new NetCDF file
			NetcdfFileWriteable ncFile = NetcdfUWFileWriteable.createNew(
					filepath, true);
			NetcdfUWFileWriteable ncUWfile = new NetcdfUWFileWriteable(ncFile);
			
			// write attributes
			// not necessary here
			
			// add dimensions
			ncUWfile.getNetcdfFileWritable().setRedefineMode(false);
			List<Dimension> dims = aqNCfile.getNetcdfFile().getDimensions();
			for (Dimension d : dims){
				ncUWfile.getNetcdfFileWritable().addDimension(null, d);
			}

			// write variables
			List<Variable> varIter = aqNCfile.getNetcdfFile().getVariables();
			for (Variable var : varIter) {
				// add variable
				ncFile.setRedefineMode(true);
				ncFile.addVariable(null, var);
				// write variable
				ncFile.setRedefineMode(false);
				ncFile.write(var.getName(), var.read());
			}
			
			// write data
			ncUWfile.getNetcdfFileWritable().setRedefineMode(false);
			
			// close file
			ncUWfile.getNetcdfFile().close();
		}catch (IOException ioe) {
	  		System.out.println("trying to open " + ""+ " " + ioe);
	  	} catch (NetcdfUWException e) {
			e.printStackTrace();
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		
		
		return mainVariable;
	}
	
	private List<String> extractStatisticsFromRequest(List<IData> statParams){
		List<String> params = new ArrayList<String>(statParams.size());
		for(IData statParam : statParams){
			params.add(((LiteralStringBinding) statParam).getPayload());
		}		
		return params;
	}
	
}
