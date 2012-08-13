package org.uncertweb.ems;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.io.datahandler.om.OMXmlParser;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
//import org.uncertweb.api.netcdf.NetcdfUWFile;
//import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
//import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.ems.data.profiles.AbstractProfile;
import org.uncertweb.ems.data.profiles.ActivityProfile;
import org.uncertweb.ems.data.profiles.GeometryProfile;
import org.uncertweb.ems.data.profiles.MEProfile;
import org.uncertweb.ems.data.profiles.Profile;
import org.uncertweb.ems.exposuremodel.IndoorModel;
import org.uncertweb.ems.exposuremodel.OutdoorModel;
import org.uncertweb.ems.io.OMProfileGenerator;
import org.uncertweb.ems.io.OMProfileParser;
import org.uncertweb.ems.util.ExposureModelConstants;
import org.uncertweb.ems.util.Utils;
import org.uncertweb.netcdf.NcUwConstants;
import org.uncertweb.netcdf.NcUwFile;
import org.uncertweb.netcdf.NcUwHelper;


import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import ucar.nc2.units.DateUnit;

/**
 * Algorithm to estimate uncertain exposure towards air pollutants
 * @author LydiaGerharz
 * 
 */

public class EMSalgorithm extends AbstractObservableAlgorithm{

	private List<String> errors = new ArrayList<String>();
	private static Logger log = Logger.getLogger(EMSalgorithm.class);
	
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
		 *  1) get WPS inputs
		 */
		// other input parameters
//		int cospIterations = 100;
//		boolean useIndoorSources = true;		
		
		// activity data
		IObservationCollection omFile = null;
		List<IData> omList = inputMap.get(INPUT_IDENTIFIER_ACTIVITY_PROFILE);	
		if(omList != null && omList.size()!=0){
			IData tmp = omList.get(0);
			if(tmp instanceof OMBinding){
				omFile = ((OMBinding)tmp).getPayload();
			}
		}
		
		NcUwFile ncFile = null;
		List<IData> ncList = inputMap.get(INPUT_IDENTIFIER_AIR_QUALITY);
		if(ncList != null && ncList.size()!=0){
			IData tmp = ncList.get(0);
			if(tmp instanceof NetCDFBinding){
				ncFile = ((NetCDFBinding)tmp).getPayload();				
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
		int minuteResolution = 0;
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
		 *  2) create internal data types
		 */	
		
		// 2.1) get time from Netcdf file
		Variable timeVar = ncFile.getVariable(NcUwConstants.StandardNames.TIME);

		// if no time variable has been found
		if(timeVar==null){
			//TODO: throw exception
		}
				
		// create time list from Netcdf time array
		//TODO: ensure that this is done in UTC/GMT!!!
		ArrayList<DateTime> ncTimeList = new ArrayList<DateTime>();
		String timeUnit = timeVar.getUnitsString();	
		try {
			DateUnit dateUnit = new DateUnit(timeUnit);
			for(int i=1; i<=timeVar.getDimension(0).getLength(); i++){
				ncTimeList.add(new DateTime(dateUnit.makeDate(i)));
			}	
			
			// if minuteResolution has not been provided make this as default resolution
			if(minuteResolution==0){
				String unit = dateUnit.getTimeUnitString();
				if(unit.equals("days"))
					minuteResolution = 60*24;
				else if (unit.equals("hours"))
					minuteResolution = 60;
				else if (unit.equals("minutes"))
					minuteResolution = 1;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}	
		
		// 2.2) create profile list from OM file
		List<AbstractProfile> profileList = new OMProfileParser().OM2Profiles(omFile, ncTimeList, minuteResolution);
					
		/*
		 *  3) check if modelling is possible
		 */	
		
			
		
		/*
		 * 4) OUTDOOR MODEL
		 */
		UncertaintyObservationCollection exposureProfiles = new UncertaintyObservationCollection();

		// get outdoor concentration at profile locations
		// A) for the moment, do the overlay for GPS tracks in MS with the local version
		OutdoorModel outdoor = new OutdoorModel();
		outdoor.run(profileList, ncFile);
	
		// perform averaging of profile observations
		// profile.aggregateProfile(minuteResolution);

		/*
		 * 5) INDOOR MODEL
		 */
		String parameter = ncFile.getStringAttribute(NcUwConstants.Attributes.PRIMARY_VARIABLES, true).split(" ")[0];
		String uom = ncFile.getVariable(parameter).findAttribute("units").getStringValue();
		// if activities are available, create indoor model with parameters
		// if(profile instanceof MEProfile || profile instanceof
		// ActivityProfile){
//		String parameter = ncFile.getPrimaryVariableNames().toArray(new String[1])[0];	
		// IndoorModel indoor = new IndoorModel();
		// indoor.readParametersFile("DE",parameter,
		// "src/main/resources/indoorModel/parameters.csv");
		
		// indoor.runModel(profileList, indoorIterations, minuteResolution,
		// false);
		//
		// }

		/*
		 * RESULT COLLECTION
		 */		
		// go through each individual profile
		for (AbstractProfile profile : profileList) {			
			// get OM file and add to overall observation collection
			exposureProfiles.addObservationCollection(new OMProfileGenerator()
					.createExposureProfileObservationCollection(profile,
							statList));
		}
		

		/*
		 *  5) prepare result
		 */	
		//TODO: implement handling of additional uncertainty loop for Albatross outputs
		
		// write output locally
		try {
			new StaxObservationEncoder().encodeObservationCollection(exposureProfiles,
					new File(resourcesPath+"/outputs/exposure_test.xml"));
		} catch (OMEncodingException e) {
			e.printStackTrace();
		}
		
		Map<String, IData> result = new HashMap<String, IData>(1);
		OMBinding uwData = new OMBinding(exposureProfiles);
		result.put(OUTPUT_IDENTIFIER, uwData);
		return result;
		
	}

	
	private List<String> extractStatisticsFromRequest(List<IData> statParams){
		List<String> params = new ArrayList<String>(statParams.size());
		for(IData statParam : statParams){
			params.add(((LiteralStringBinding) statParam).getPayload());
		}		
		return params;
	}
	
}
