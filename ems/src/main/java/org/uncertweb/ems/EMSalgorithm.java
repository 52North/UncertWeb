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
import org.joda.time.Interval;
import org.joda.time.PeriodType;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.io.datahandler.om.OMXmlParser;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
//import org.uncertweb.api.netcdf.NetcdfUWFile;
//import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
//import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertml.IUncertainty;
import org.uncertml.sample.AbstractSample;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.ems.data.profiles.AbstractProfile;
import org.uncertweb.ems.data.profiles.GeometryProfile;
import org.uncertweb.ems.data.profiles.Profile;
import org.uncertweb.ems.exceptions.EMSInputException;
import org.uncertweb.ems.exposuremodel.OutdoorModel;
import org.uncertweb.ems.extension.model.IndoorModel;
import org.uncertweb.ems.extension.profiles.ActivityProfile;
import org.uncertweb.ems.extension.profiles.MEProfile;
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
	
	// EMS data structures
	private List<IObservationCollection> omList;
	private NcUwFile ncFile;
	private int indoorIterations;
	private int minuteResolution;
	private List<String> statList = new ArrayList<String>();
	
	public EMSalgorithm(){
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(LocalAlgorithmRepository.class.getCanonicalName());
		for(Property property : propertyArray){
			if (property.getName().equalsIgnoreCase("Resources")){
				resourcesPath = property.getStringValue();
				break;
			}
		}	
	}
	
	
	public List<String> getErrors() {
		return errors;
	}

	public Class<?> getInputDataType(String id) {
		return ExposureModelConstants.ProcessInputs.INPUT_DATA_TYPES.get(id);
//		if(id.equals(INPUT_IDENTIFIER_ACTIVITY_PROFILE)){
//			return UncertWebIODataBinding.class;
//	//		return OMBinding.class;
//		}else if(id.equals(INPUT_IDENTIFIER_AIR_QUALITY)){
//			return NetCDFBinding.class;
//		}else if(id.equals(INPUT_IDENTIFIER_NUMBER_OF_SAMPLES)){
//			return LiteralIntBinding.class;
//		}else if(id.equals(INPUT_IDENTIFIER_RESOLUTION)){
//			return LiteralIntBinding.class;
//		}else if(id.equals(INPUT_IDENTIFIER_OUTPUT_UNCERTAINTY)){
//			return LiteralStringBinding.class;
//		}else{
//			return GenericFileDataBinding.class;			
//		}
	}

	public Class<?> getOutputDataType(String arg0) {
		return ExposureModelConstants.ProcessInputs.INPUT_DATA_TYPES.get(arg0);
//		return UncertWebIODataBinding.class;
	}

	public Map<String, IData> run(Map<String, List<IData>> inputMap) {				
		// ********* Get WPS inputs********* 
		getWPSInputs(inputMap);
		
		// ********* Create internal data types *********
		// output collection
		UncertaintyObservationCollection exposureProfiles = new UncertaintyObservationCollection();

		//  create time list from Netcdf time array
		ArrayList<DateTime> ncTimeList = null;
		ncTimeList = this.getTimeArrayFromNcUwFile(ncFile);				
		
		// if minuteResolution has not been provided make this as default resolution
		if(minuteResolution==0){
			minuteResolution = new Interval(ncTimeList.get(0),ncTimeList.get(1)).toPeriod(PeriodType.minutes()).getMinutes();
		}
		
		// TODO
		// if the activity input is uncertain, we can only make daily averages
//		if(omList.size()>1)

		
		// go through the OM files in the list
		for(IObservationCollection omFile : omList){
			// create profile list from OM file
			List<AbstractProfile> profileList = new OMProfileParser().OM2Profiles(omFile, ncTimeList, minuteResolution);
						
			// TODO: ********* Check if modelling is possible *********
						
			
			// ********* OUTDOOR MODEL *********
			// get outdoor concentration at profile locations
			// A) for the moment, do the overlay for GPS tracks in MS with the local version
			OutdoorModel outdoor = new OutdoorModel();
			outdoor.run(profileList, ncFile);
		
			// perform averaging of profile observations
			// profile.aggregateProfile(minuteResolution);

			// ********* INDOOR MODEL *********
			String parameter = ncFile.getStringAttribute(NcUwConstants.Attributes.PRIMARY_VARIABLES, true).split(" ")[0];
			String uom = ncFile.getVariable(parameter).findAttribute("units").getStringValue();
			// if activities are available, create indoor model with parameters
			// if(profile instanceof MEProfile || profile instanceof
			// ActivityProfile){
//			String parameter = ncFile.getPrimaryVariableNames().toArray(new String[1])[0];	
			// IndoorModel indoor = new IndoorModel();
			// indoor.readParametersFile("DE",parameter,
			// "src/main/resources/indoorModel/parameters.csv");
			
			// indoor.runModel(profileList, indoorIterations, minuteResolution,
			// false);
			//
			// }

			// ********* RESULT COLLECTION *********	
			//TODO: add handling for different activity realisations -> mapping to one O&M file!
			// go through each individual profile
			for (AbstractProfile profile : profileList) {			
				// get OM file and add to overall observation collection
				exposureProfiles.addObservationCollection(new OMProfileGenerator()
						.createExposureProfileObservationCollection(profile,
								statList));
			}
		}
			

		// ********* Prepare WPS result ********* 
		//TODO: this is for testing purposes and should be removed before final deployment
		// write output locally
		try {
			new StaxObservationEncoder().encodeObservationCollection(exposureProfiles,
					new File(resourcesPath+"/outputs/exposure_test.xml"));
			new JSONObservationEncoder().encodeObservationCollection(exposureProfiles,
					new File(resourcesPath+"/outputs/exposure_test.json"));
		} catch (OMEncodingException e) {
			e.printStackTrace();
		}
		
		Map<String, IData> result = new HashMap<String, IData>(1);
		OMBinding uwData = new OMBinding(exposureProfiles);
		result.put(ExposureModelConstants.ProcessInputs.OUTPUT_IDENTIFIER, uwData);
		return result;
		
	}

	private void getWPSInputs(Map<String, List<IData>> inputMap){
		// activity data
				omList = new ArrayList<IObservationCollection>();
				List<IData> omInputList = inputMap.get(INPUT_IDENTIFIER_ACTIVITY_PROFILE);	
				if(omInputList != null && omInputList.size()!=0){
					IData tmp = omInputList.get(0);
					// if one OM file is provided directly
					if(tmp instanceof OMBinding){
						omList.add(((OMBinding)tmp).getPayload());
					}
					//TODO: implement handling of additional uncertainty for Albatross outputs			
					// if an UncertML file with refs to OM documents is provided
					else if(tmp instanceof UncertMLBinding){
						IUncertainty uncertOMList  = ((UncertMLBinding)tmp).getPayload();
//						if(uncertOMList instanceof ISample){
//							AbstractSample sample = (AbstractSample) uncertainty;
//							ContiuousRealisations realisations = (ContinuousRealisation) sample.getRealisations().get(0);
//						}
					}else{
						throw new EMSInputException("Activity input has to be in O&M or UncertML!");
					}			
				}else{	
					throw new EMSInputException("Activity input is missing!");
				}
				
				ncFile = null;
				List<IData> ncList = inputMap.get(INPUT_IDENTIFIER_AIR_QUALITY);
				if(ncList != null && ncList.size()!=0){
					IData tmp = ncList.get(0);
					if(tmp instanceof NetCDFBinding){
						ncFile = ((NetCDFBinding)tmp).getPayload();				
					}else{
						throw new EMSInputException("Air Quality input has to be in NetCDF-U format!");
					}
				}else{	
					throw new EMSInputException("Air quality input is missing!");
				}
				
				//	number of samples	
				List<IData> samplesList = inputMap.get(INPUT_IDENTIFIER_NUMBER_OF_SAMPLES);
				if(samplesList != null && samplesList.size()!=0){
					if(samplesList.get(0) instanceof LiteralIntBinding){
						indoorIterations = Integer.parseInt(samplesList.get(0).getPayload().toString());
					}else{
						throw new EMSInputException("numberOfSamples has to be of stat type integer!");
					}
				}else{ //default
					indoorIterations = 100;
				}
				
				// temporal resolution
				List<IData> resList = inputMap.get(INPUT_IDENTIFIER_RESOLUTION);
				if(resList != null && resList.size()!=0){
					if(resList.get(0) instanceof LiteralIntBinding){
						minuteResolution = Integer.parseInt(resList.get(0).getPayload().toString());
						if(minuteResolution<=0)
							minuteResolution = 0;
					}else{
						throw new EMSInputException("minuteResolution has to be of stat type integer!");
					}
				}else{ //default
					minuteResolution = 0;
				}
						
				// output uncertainty type		
				List<IData> statisticsList = inputMap.get(INPUT_IDENTIFIER_OUTPUT_UNCERTAINTY);
				if(!(statisticsList == null) && statisticsList.size() != 0){
					statList = extractStatisticsFromRequest(statisticsList);			
				}else{ //default
					statList.add("http://www.uncertml.org/samples/realisation");
				}
	}

	
	private ArrayList<DateTime> getTimeArrayFromNcUwFile(NcUwFile ncFile) throws EMSInputException{
		//TODO: ensure that this is done in UTC/GMT!!!
		Variable timeVar = ncFile.getVariable(NcUwConstants.StandardNames.TIME);
		
		// if no time variable has been found
		if(timeVar==null){
			throw new EMSInputException("No valid time variable could be found for the input NetCDF file.");
		}
		
		ArrayList<DateTime> ncTimeList = new ArrayList<DateTime>();
		String timeUnit = timeVar.getUnitsString();	
		try {
			DateUnit dateUnit = new DateUnit(timeUnit);
			for(int i=1; i<=timeVar.getDimension(0).getLength(); i++){
				ncTimeList.add(new DateTime(dateUnit.makeDate(i)));
			}	

		} catch (Exception e) {
			throw new EMSInputException("No valid DateTime object could be created from the NetCDF time unit String.", e);
		}	
		
		return ncTimeList;
	}
	
	private List<String> extractStatisticsFromRequest(List<IData> statParams){
		List<String> params = new ArrayList<String>(statParams.size());
		for(IData statParam : statParams){
			String parameter = ((LiteralStringBinding) statParam).getPayload();
			if(ExposureModelConstants.allowedOutputUncertaintyTypes.contains(parameter))
				params.add(parameter);
		}		
		return params;
	}
	
}
