package org.uncertweb.ems;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.PeriodType;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
//import org.uncertweb.api.netcdf.NetcdfUWFile;
//import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
//import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertml.IUncertainty;
import org.uncertml.sample.AbstractRealisation;
import org.uncertml.sample.AbstractSample;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.ISample;
import org.uncertml.sample.RandomSample;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.ems.data.profiles.AbstractProfile;
import org.uncertweb.ems.exceptions.EMSInputException;
import org.uncertweb.ems.exposuremodel.OutdoorModel;
import org.uncertweb.ems.io.OMProfileGenerator;
import org.uncertweb.ems.io.OMProfileParser;
import org.uncertweb.ems.util.ExposureModelConstants;
import org.uncertweb.netcdf.NcUwConstants;
import org.uncertweb.netcdf.NcUwFile;


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

	private static String resourcesPath = "";

	// WPS inputs & outputs
	private final String INPUT_IDENTIFIER_ACTIVITY_PROFILE = "activityProfile";
	private final String INPUT_IDENTIFIER_AIR_QUALITY = "airQualityData";
	private final String INPUT_IDENTIFIER_NUMBER_OF_SAMPLES = "numberOfSamples";
	private final String INPUT_IDENTIFIER_RESOLUTION = "minuteResolution";
	private final String INPUT_IDENTIFIER_OUTPUT_UNCERTAINTY = "outputUncertaintyType";
	private final String OUTPUT_IDENTIFIER = "result";
	private boolean uncertml = false;

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


	@Override
	public List<String> getErrors() {
		return errors;
	}

	@Override
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

	@Override
	public Class<?> getOutputDataType(String arg0) {
		return ExposureModelConstants.ProcessInputs.INPUT_DATA_TYPES.get(arg0);
//		return UncertWebIODataBinding.class;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputMap) {
		// ********* Get WPS inputs*********
		getWPSInputs(inputMap);

		File baseDir = new File(System.getProperty("catalina.base") + File.separator + "webapps/public");
		if(!baseDir.exists()){
			try {
				baseDir.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String baseDirPath = baseDir.getAbsolutePath();

		// ********* Create internal data types *********
		//  create time list from Netcdf time array
		ArrayList<DateTime> ncTimeList = null;
		ncTimeList = this.getTimeArrayFromNcUwFile(ncFile);

		// if minuteResolution has not been provided make this as default resolution
		if(minuteResolution==0){
			minuteResolution = new Interval(ncTimeList.get(0),ncTimeList.get(1)).toPeriod(PeriodType.minutes()).getMinutes();
		}

		// output collection
		UncertaintyObservationCollection exposureProfiles = new UncertaintyObservationCollection();
//		ArrayList<ContinuousRealisation> realisations = new ArrayList<ContinuousRealisation>();
		HashMap<URI, ArrayList<ContinuousRealisation>> individualList= new HashMap<URI, ArrayList<ContinuousRealisation>>();

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
			OutdoorModel outdoor = new OutdoorModel(resourcesPath);
			outdoor.run(profileList, ncFile);

			// perform averaging of profile observations
			// profile.aggregateProfile(minuteResolution);

			// ********* INDOOR MODEL *********
//			String parameter = ncFile.getStringAttribute(NcUwConstants.Attributes.PRIMARY_VARIABLES, true).split(" ")[0];
//			String uom = ncFile.getVariable(parameter).findAttribute("units").getStringValue();
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

			// for a single OM file make an OM Collection as result
			if(!uncertml){
				// go through each individual profile
				for (AbstractProfile profile : profileList) {
					// get OM file and add to overall observation collection
					exposureProfiles.addObservationCollection(new OMProfileGenerator()
							.createExposureProfileObservationCollection(profile,
									statList));
				}

				// ********* Prepare WPS result *********
				Map<String, IData> result = new HashMap<String, IData>(1);
				OMBinding uwData = new OMBinding(exposureProfiles);
				result.put(ExposureModelConstants.ProcessInputs.OUTPUT_IDENTIFIER, uwData);
				return result;
			}
			// for more than one OM file (realisations) make an UncertML file with refs to these realisations
			else{
				// get results for the individuals
				ArrayList<UncertaintyObservationCollection> individualRealisations = new OMProfileGenerator().createIndividualExposureProfileObservationCollections(profileList, statList);

				// write results to separate files
				for(UncertaintyObservationCollection realisation : individualRealisations){
					String uuidString = UUID.randomUUID().toString().substring(0, 5);
					File file = new File(baseDirPath + File.separator + uuidString + ".xml");
					try {
						new StaxObservationEncoder().encodeObservationCollection(realisation,
								file);
					} catch (OMEncodingException e) {
						e.printStackTrace();
					}

					// make URL from this file path
					String host = WPSConfig.getInstance().getWPSConfig().getServer().getHostname();
					String hostPort = WPSConfig.getInstance().getWPSConfig().getServer().getHostport();
					URL url = null;
					try {
						url = new URL("http://" + host + ":" + hostPort+ "/" + "public/" + file.getName());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}

					// make realisation
					ContinuousRealisation cr = new ContinuousRealisation(url);

					// get the current individual URI
					URI currentURI = realisation.getObservations().get(0).getProcedure();

					// add it to the individual list
					if(individualList.get(currentURI)==null)
						individualList.put(currentURI, new ArrayList<ContinuousRealisation>());
					individualList.get(currentURI).add(cr);
				}

			}

		}


		// ********* Prepare WPS result for more than one realisation *********
		Map<String, IData> result = new HashMap<String, IData>(1);

		//TODO: workaround as long as we do not have a sample collection
		ArrayList<ContinuousRealisation> reals = new ArrayList<ContinuousRealisation>();

		// add each individual with its realisations as one sample
		for(ArrayList<ContinuousRealisation> realisations : individualList.values()){
		//	RandomSample rs = new RandomSample(realisations.toArray(new ContinuousRealisation[] {}));
			reals.addAll(realisations);
		}

		RandomSample rs = new RandomSample(reals.toArray(new ContinuousRealisation[] {}));
		UncertMLBinding ub = new UncertMLBinding(rs);
		result.put(ExposureModelConstants.ProcessInputs.OUTPUT_IDENTIFIER, ub);
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
						uncertml = true;
						IUncertainty uncertOMList  = ((UncertMLBinding)tmp).getPayload();
						if(uncertOMList instanceof ISample){
							AbstractSample sample = (AbstractSample) uncertOMList;
							List<AbstractRealisation> realisations = sample.getRealisations();
							// get values from realisations URL ref
							if(((ContinuousRealisation)realisations.get(0)).isReferenced()){
								for(AbstractRealisation absReal : realisations){
									URL url = ((ContinuousRealisation)absReal).getReferenceURL();
									try {
										// parse OM file in this reference

										try {
											XBObservationParser omParser = new XBObservationParser();
											XmlObject xml = XmlObject.Factory.parse(url.openConnection().getInputStream());
											IObservationCollection obs = (IObservationCollection) omParser.parse(xml.xmlText());
											omList.add((IObservationCollection)obs);
										}catch (XmlException e) {
											e.printStackTrace();
										}catch (OMParsingException e) {
											e.printStackTrace();
										}
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
							// TODO: Add handler for non-referenced UncertML file
							else{

							}

						}else{
							throw new EMSInputException("Activity input has to be of type samples or realisations in UncertML!");
						}

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
