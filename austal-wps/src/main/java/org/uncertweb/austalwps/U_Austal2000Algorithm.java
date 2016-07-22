package org.uncertweb.austalwps;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.PlainStringBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.opengis.feature.simple.SimpleFeature;
import org.rosuda.REngine.REXP;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.austalwps.util.AustalOutputReader;
import org.uncertweb.austalwps.util.Point;
import org.uncertweb.austalwps.util.StreamGobbler;
import org.uncertweb.austalwps.util.Value;
import org.uncertweb.austalwps.util.austal.files.Austal2000Txt;
import org.uncertweb.austalwps.util.austal.files.Zeitreihe;
import org.uncertweb.austalwps.util.austal.geometry.EmissionSource;
import org.uncertweb.austalwps.util.austal.geometry.ReceptorPoint;
import org.uncertweb.austalwps.util.austal.timeseries.EmissionTimeSeries;
import org.uncertweb.austalwps.util.austal.timeseries.MeteorologyTimeSeries;

import ucar.ma2.InvalidRangeException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.PrecisionModel;

public class U_Austal2000Algorithm extends AbstractObservableAlgorithm{

	// system variables
	private List<String> errors = new ArrayList<String>();
	private static Logger LOGGER = Logger.getLogger(Austal2000Algorithm.class);
	private final String fileSeparator = System.getProperty("file.separator");
	//private String tmpDir = "D:\\PhD\\WP1.1_AirQualityModel\\WebServiceChain\\AustalWPS\\AustalRun";
	private String austalHome = "";
//private String workDirPath = austalHome + "\\PO";
	private String tmpDir = "";
	private String workDirPath = tmpDir + fileSeparator + "PO" + fileSeparator;
	private String resultsPath = "C:\\WebResources\\AustalWPS\\outputs";
	private String resourcePath = "C:\\WebResources\\AustalWPS\\inputs\\";


	// WPS inputs & outputs
	private final String inputIDOutputUncertainty = "OutputUncertaintyType";
	private final String inputIDNumberOfRealisations = "NumberOfRealisations";
	private final String inputIDReceptorPoints = "receptor-points";
	private final String inputIDStartTime = "start-time";
	private final String inputIDEndTime = "end-time";
	private final String outputIDResult = "result";

	// WPS variables
	private int numberOfRealisations;
	private DateTime startDate;
	private DateTime preStartDate;
	private DateTime endDate;

	//Austal variables
	private Austal2000Txt austal;
	private Zeitreihe ts;
	public static final String OS_Name = System.getProperty("os.name");
	private String zeitreiheFileName = "zeitreihe.dmna";
	private String austalFileName = "austal2000.txt";

	public U_Austal2000Algorithm(){
//		if(OS_Name.contains("Windows")){
//			tmpDir = System.getenv("TMP");
//		}else{
//			tmpDir = System.getenv("CATALINA_TMPDIR");
//		}

		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(LocalAlgorithmRepository.class.getCanonicalName());
		for(Property property : propertyArray){
			if(property.getName().equalsIgnoreCase("Austal_Home")){
				austalHome = property.getStringValue();
				break;
			}else if (property.getName().equalsIgnoreCase("Resources")){
				resourcePath = property.getStringValue();
				break;
			}
		}
	//	workDirPath = tmpDir + fileSeparator + "PO" + fileSeparator;
		workDirPath = austalHome + fileSeparator + "PO" + fileSeparator;
		resultsPath = austalHome + "\\outputs";

		// path to Austal and R resources
		String host = WPSConfig.getInstance().getWPSConfig().getServer().getHostname();
		String hostPort = WPSConfig.getInstance().getWPSConfig().getServer().getHostport();
		if(host == null) {
			try {
				host = InetAddress.getLocalHost().getCanonicalHostName();

			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	//	resourcePath = "http://" + host + ":" + hostPort + "/" +
	//	WebProcessingService.WEBAPP_PATH + "/" + "res" + "/" + "";
	}

	@Override
	public List<String> getErrors() {
		return errors;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if(id.equals(inputIDOutputUncertainty)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDNumberOfRealisations)){
			return LiteralIntBinding.class;
		}else if(id.equals(inputIDStartTime)){
			return LiteralStringBinding.class;
			//return PlainStringBinding.class;
		}else if(id.equals(inputIDEndTime)){
			return LiteralStringBinding.class;
			//return PlainStringBinding.class;
		}else if(id.equals(inputIDReceptorPoints)){
			return GTVectorDataBinding.class;
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

		// 1) Get inputs
		// 1.1) read files from local resources to create datamodel
		this.readFiles("austal2000_template.txt", "zeitreihe_0810.dmna");

		// 1.2) Retrieve number of Realisations from input. Must exist!
		List<IData> list = inputMap.get(inputIDNumberOfRealisations);
		numberOfRealisations = 3;
		if(list!=null){
			IData numberOfReal = list.get(0);
			numberOfRealisations = (Integer) numberOfReal.getPayload();
		}

		// 1.3) get start and end date
		//2010-03-01T01:00:00.000+01
		DateTimeFormatter dateFormat = ISODateTimeFormat.dateTime();
		//DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.000+01");
		List<IData> startList = inputMap.get(inputIDStartTime);
		String startTime = "2010-03-01T01:00:00.000+01";
		if(startList!=null){
			startTime = ((IData)startList.get(0)).getPayload().toString();
		}

		List<IData> endList = inputMap.get(inputIDEndTime);
		String endTime = "2010-03-05T00:00:00.000+01";
		if(endList!=null){
			endTime = ((IData)endList.get(0)).getPayload().toString();
		}

		startDate = dateFormat.parseDateTime(startTime);
		preStartDate = startDate.minusDays(1);
		endDate = dateFormat.parseDateTime(endTime);

		// Check if period is at least 2 days
		if(Days.daysBetween(startDate, endDate).getDays()<2){
			endDate = startDate.plusDays(2);
		}

		// Check if dates are within available data range
		DateTime minDate = ts.getMeteorologyTimeSeries().getMinDate();
		DateTime maxDate = ts.getMeteorologyTimeSeries().getMaxDate();
		if(preStartDate.isBefore(minDate)){
			preStartDate = minDate;
			startDate = preStartDate.plusDays(1);
			if(Days.daysBetween(startDate, endDate).getDays()<2)
				endDate = startDate.plusDays(2);
		}
		if(endDate.isAfter(maxDate)){
			endDate = maxDate;
			if(Days.daysBetween(startDate, endDate).getDays()<2)
				startDate = endDate.minusDays(2);
		}

		// 1.4) get receptor point input
		List<IData> receptorPointsDataList = inputMap.get(inputIDReceptorPoints);
		List<ReceptorPoint> pointList = new ArrayList<ReceptorPoint>();

		if(!(receptorPointsDataList == null) && receptorPointsDataList.size() != 0){
			IData receptorPointsData = receptorPointsDataList.get(0);
			if(receptorPointsData instanceof GTVectorDataBinding){
				handleReceptorPoints(pointList, (FeatureCollection<?, ?>)receptorPointsData.getPayload());
				// set Austal into non-grid mode
				austal.setOs("");
			}
		}

		// TODO: Split realisations if there are too many (max 20)
		if(numberOfRealisations>25){

		}

		// 2) Get samples for time series
		ArrayList<MeteorologyTimeSeries> metListRealisations = sampleMeteorologyTS();
		ArrayList<ArrayList<EmissionTimeSeries>> emisListRealisations = sampleStreetTrafficEmissionTS();

		// get street geometry (only once)
		ArrayList<EmissionSource> newEmissionSources = getStreetGeometry(emisListRealisations.get(0).size());

		// 3) execute Austal for each sample and collect results
		ArrayList<String> resultFolders = new ArrayList<String>();
		for (int i = 0; i < numberOfRealisations; i++) {
			// 3.1) substitute samples for meteo data and emissions
			substituteMeteorology(metListRealisations.get(i));
			substituteStreetEmissions(newEmissionSources, emisListRealisations.get(i));

			// 3.2) write files for this sample
			String outputfile = resultsPath + "\\"+ startDate.toString("yyyy-MM") +"\\PO"+i;
			writeFiles(outputfile);

			// 3.3) execute Austal
			try {
				// get command for execute Austal
				String command = austalHome + fileSeparator + "austal2000.exe " + workDirPath+i;
				resultFolders.add(workDirPath+i);
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(command);

				// any error message?
				StreamGobbler errorGobbler = new StreamGobbler(proc
						.getErrorStream(), "ERROR");

				// any output?
				StreamGobbler outputGobbler = new StreamGobbler(proc
						.getInputStream(), "OUTPUT");

				outputGobbler.setSubject(this);

				// kick them off
				errorGobbler.start();
				outputGobbler.start();

				// any error???
				int exitVal = -1;
				try {
					exitVal = proc.waitFor();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if(exitVal == 0){
					LOGGER.debug("Process finished normally.");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 4) handle results
		Map<String, IData> result = new HashMap<String, IData>();

		// if receptor points were provided give their results back
		if(!(receptorPointsDataList == null) && receptorPointsDataList.size() != 0){
			try {
				// make uncertainty observation
				UncertaintyObservationCollection mcoll = createReceptorPointsRealisationsCollection();

				// save result locally
				String filepath = resultsPath + "\\Austal_"+startDate.toString("yyyy-MM")+".xml";
				File file = new File(filepath);

				// encode, store (for using in austal request later)
				try {
					new StaxObservationEncoder().encodeObservationCollection(mcoll,file);
				} catch (OMEncodingException e) {
					e.printStackTrace();
				}

				OMBinding omd = new OMBinding(mcoll);
				result.put(outputIDResult, omd);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// if no receptor points were given, read grid results from Austal execution
		else{
			int h = Hours.hoursBetween(preStartDate, startDate).getHours();
			// get results only from start date to end date only
			HashMap<Integer, HashMap<Integer, ArrayList<Double>>> realisationsMap = new HashMap<Integer, HashMap<Integer, ArrayList<Double>>>();
			AustalOutputReader outputReader = new AustalOutputReader();
			for(int i=0; i<numberOfRealisations; i++){
				String folder = resultsPath + "\\"+ startDate.toString("yyyy-MM") +"\\PO"+i;
				realisationsMap.put(i+1, outputReader.readHourlyFiles(folder, h+1, false));
			}

			// write results to a NetCDF file
			String filepath = resultsPath + "\\Austal_"+startDate.toString("yyyy-MM")+".nc";
	//				+ System.currentTimeMillis() + ".nc";
			NetcdfUWFile resultFile = null;
			try {
				resultFile = outputReader.createNetCDFfile(filepath,
						austal.getStudyArea().getXcoords(), austal.getStudyArea().getYcoords(),
						startDate, h+1, realisationsMap);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NetcdfUWException e) {
				e.printStackTrace();
			} catch (InvalidRangeException e) {
				e.printStackTrace();
			}

			NetCDFBinding uwData = new NetCDFBinding(resultFile);
			result.put(outputIDResult, uwData);
			return result;
		}

		return  new HashMap<String, IData>();
	}

	private void readFiles(String austalFileName, String zeitreiheFileName){
		// read austal2000.txt
		File austalFile = new File(resourcePath+ austalFileName);
		austal = new Austal2000Txt(austalFile);

		// read zeitreihe.dmna
		File tsFile = new File(resourcePath+zeitreiheFileName);
		ts = new Zeitreihe(tsFile);

//		String host = WPSConfig.getInstance().getWPSConfig().getServer().getHostname();
//		String hostPort = WPSConfig.getInstance().getWPSConfig().getServer().getHostport();
//		if(host == null) {
//			try {
//				host = InetAddress.getLocalHost().getCanonicalHostName();
//
//			} catch (UnknownHostException e) {
//				e.printStackTrace();
//			}
//		}
//		String url = "http://" + host + ":" + hostPort + "/" +
//		WebProcessingService.WEBAPP_PATH + "/" + "res" + "/" + "";
//
//		try {
//			URL austalURL = new URL(url + austalFileName);
//			austal = new Austal2000Txt(austalURL.openStream());
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//			LOGGER.debug(e);
//		} catch (IOException e) {
//			LOGGER.debug(e);
//			e.printStackTrace();
//		}
//
//		try {
//			URL zeitReiheURL = new URL(url + zeitreiheFileName);
//			ts = new Zeitreihe(zeitReiheURL.openStream());
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//			LOGGER.debug(e);
//		} catch (IOException e) {
//			LOGGER.debug(e);
//			e.printStackTrace();
//		}

	}

	private void writeFiles(String folder){
		File new_Folder = new File(folder);
		if(!new_Folder.exists())
			new_Folder.mkdir();
		// test writer
		File new_austalFile = new File(new_Folder.getPath()+"\\"+austalFileName);
		austal.writeFile(new_austalFile);
		File new_tsFile = new File(new_Folder.getPath()+"\\"+zeitreiheFileName);
		ts.writeFile(new_tsFile);
	}



	/**
	 * Method to get samples for wind speed and direction
	 * @return
	 */
	private ArrayList<MeteorologyTimeSeries> sampleMeteorologyTS(){
		ArrayList<MeteorologyTimeSeries> metListRealisations = new ArrayList<MeteorologyTimeSeries>();

		ExtendedRConnection c = null;
		UncertaintyObservationCollection uColl = null;
		try {
			// establish connection to Rserve running on localhost
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}

			// load dataframe with meteo data
			String dfPath = resourcePath + "MeteoDataSampling.RData";
			c.tryVoidEval("load(\""+dfPath+"\")");

			// set variables
			c.tryVoidEval("n <- "+numberOfRealisations);
			//start_date = "2010-03-01T01:00:00.000+01"
			//end_date = "2010-03-05T00:00:00.000+01"
			String start_day = preStartDate.toString("yyyy-MM-dd");
			int start_hour = Integer.parseInt(preStartDate.toString("HH"));
			String end_day = endDate.toString("yyyy-MM-dd");
			int end_hour = Integer.parseInt(endDate.toString("HH"));

			// get ids of start and end date
			c.tryVoidEval("idStart <- which(meteo$day==\""+start_day+"\"&meteo$hour=="+start_hour+")");
			c.tryVoidEval("idEnd <- which(meteo$day==\""+end_day+"\"&meteo$hour=="+end_hour+")");

			// perform sampling
			c.tryVoidEval("sampleWS <- NULL");
			c.tryVoidEval("sampleWD <- NULL");
			c.tryVoidEval("for(i in idStart:idEnd){ sampleWS <- cbind(sampleWS, rnorm(n, meteo$WS_Nmean[i], sqrt(meteo$WS_Nvar[i]))); sampleWD <- cbind(sampleWD, rnorm(n, meteo$WD_Nmean[i], sqrt(meteo$WD_Nvar[i]))) }");

			// get time series for each realisation
			for(int i=1; i<=numberOfRealisations; i++){
				// get samples
				double[] wsTS = c.tryEval("round(sampleWS["+i+",], digits=2)").asDoubles();
				double[] wdTS = c.tryEval("round(sampleWD["+i+",], digits=0)").asDoubles();

				// add realisations to meteo time series
				MeteorologyTimeSeries newMetList = new MeteorologyTimeSeries();

				// loop through time steps
				for(int j=0; j<wsTS.length; j++){
					DateTime dt = preStartDate.plusHours(j);
					// check for negative values
					if(wsTS[j]<0.1)
						wsTS[j] = 0.1;
					while(wdTS[j]<0)
						wdTS[j] = wdTS[j] + 360;
					while(wdTS[j]>360)
						wdTS[j] = wdTS[j] - 360;
					newMetList.addWindDirection(dt, wdTS[j]);
					newMetList.addWindSpeed(dt, wsTS[j]);
				}

				metListRealisations.add(newMetList);
			}
			c.close();

		}catch (Exception e) {
			LOGGER
					.debug("Error while getting Meteorology samples: "
							+ e.getMessage());
			throw new RuntimeException(
					"Error while getting Meteorology samples: "
							+ e.getMessage(), e);
		}
		finally {
			if (c != null) {
				c.close();
			}
		}

		return metListRealisations;
	}


	// emission sampling
	private ArrayList<ArrayList<EmissionTimeSeries>> sampleStreetTrafficEmissionTS(){
		ArrayList<ArrayList<EmissionTimeSeries>> emisTSrealisations = new ArrayList<ArrayList<EmissionTimeSeries>>();

		// create emis Lists for each realisation
		for(int i=0; i<numberOfRealisations; i++){
			ArrayList<EmissionTimeSeries> newEmisTS = new ArrayList<EmissionTimeSeries>();
			emisTSrealisations.add(newEmisTS);
		}

		ExtendedRConnection c = null;
		UncertaintyObservationCollection uColl = null;
		try {
			// establish connection to Rserve running on localhost
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}

			// load dataframe with emission data
			String dfPath = resourcePath + "HE.RData";
			c.tryVoidEval("load(\""+dfPath+"\")");

			// set variables
			c.tryVoidEval("library(MASS)");
			c.tryVoidEval("library(shapefiles)");
			c.tryVoidEval("n <- "+numberOfRealisations);
			String start_day = preStartDate.toString("yyyy-MM-dd");
			String end_day = endDate.toString("yyyy-MM-dd");
			c.tryVoidEval("start_date <- "+preStartDate.toString("yyyy-MM-dd"));
			c.tryVoidEval("end_date <- "+endDate.toString("yyyy-MM-dd"));

			// read street files
			String shapePath = resourcePath + "shapes/";
			c.tryVoidEval("large <- read.dbf(\""+shapePath+"large.dbf\")$dbf");
			double[] largeDTVKM = c.tryEval("large$DTV_KM").asDoubles();
			c.tryVoidEval("small <- read.dbf(\""+shapePath+"small.dbf\")$dbf");
			// for small streets scale aadt*length up to total sum
			c.tryVoidEval("small$DTV_KM <- small$GRIDCODE/sum(small$GRIDCODE)*99345");
			double[] smallDTVKM = c.tryEval("small$DTV_KM").asDoubles();

			// get list of daytypes
			c.tryVoidEval("dayTypeList <- dayTypeTS(\""+start_day+"\", \""+end_day+"\")");

			// sample for large streets first
			int counter = 1;
			for(int i=0; i<largeDTVKM.length; i++){
				// get samples for whole time series
				c.tryVoidEval("largeHE <- NULL");
				if(numberOfRealisations==1)
					c.tryVoidEval("for(i in dayTypeList){largeHE <- c(largeHE, mvrnorm(n, HE_mean_large[[i]]*"+largeDTVKM[i]+", HE_cov_large[[i]]*"+largeDTVKM[i]+"^2)) }");
				else
					c.tryVoidEval("for(i in dayTypeList){largeHE <- cbind(largeHE, mvrnorm(n, HE_mean_large[[i]]*"+largeDTVKM[i]+", HE_cov_large[[i]]*"+largeDTVKM[i]+"^2)) }");

				// loop through realisations for this street
				for(int r=1; r<=numberOfRealisations; r++){
					// get time series for this realisation
					double[] ts;
					if(numberOfRealisations==1)
						ts = c.tryEval("signif(largeHE, digits=3)").asDoubles();
					else
						ts = c.tryEval("signif(largeHE["+r+",], digits=3)").asDoubles();

					// add time series to emission list
					EmissionTimeSeries emisTS = new EmissionTimeSeries();

					// loop through time steps
					for(int j=0; j<ts.length; j++){
						DateTime dt = preStartDate.plusHours(j);
						// only add data up to end date
						if(dt.isBefore(endDate)||dt.isEqual(endDate)){
							if(ts[j]<0)
								ts[j] = 0;
							emisTS.addEmissionValue(dt, ts[j]);
						}
					}
					emisTS.setSourceID(counter);
					// add time series for this realisation to ArrayList
					emisTSrealisations.get(r-1).add(emisTS);
				}
				counter++;
			}

			// sample for small streets
			for(int i=0; i<smallDTVKM.length; i++){
				// get samples for whole time series
				c.tryVoidEval("smallHE <- NULL");
				if(numberOfRealisations==1)
					c.tryVoidEval("for(i in dayTypeList){smallHE <- c(smallHE, mvrnorm(n, HE_mean_small[[i]]*"+smallDTVKM[i]+", HE_cov_large[[i]]*"+smallDTVKM[i]+"^2)) }");
				else
					c.tryVoidEval("for(i in dayTypeList){smallHE <- cbind(smallHE, mvrnorm(n, HE_mean_small[[i]]*"+smallDTVKM[i]+", HE_cov_large[[i]]*"+smallDTVKM[i]+"^2)) }");

				// loop through realisations for this street
				for(int r=1; r<=numberOfRealisations; r++){
					// get time series for this realisation
					double[] ts;
					if(numberOfRealisations==1)
						ts = c.tryEval("signif(smallHE, digits=3)").asDoubles();
					else
						ts = c.tryEval("signif(smallHE["+r+",], digits=3)").asDoubles();

					// add time series to emission list
					EmissionTimeSeries emisTS = new EmissionTimeSeries();

					// loop through time steps
					for(int j=0; j<ts.length; j++){
						DateTime dt = preStartDate.plusHours(j);
						// only add data up to end date
						if(dt.isBefore(endDate)||dt.isEqual(endDate)){
							if(ts[j]<0)
								ts[j] = 0;
							emisTS.addEmissionValue(dt, ts[j]);
						}
					}
					emisTS.setSourceID(counter);
					// add time series for this realisation to ArrayList
					emisTSrealisations.get(r-1).add(emisTS);
				}
				counter++;
			}

			//check arraylists
			Hours h = Hours.hoursBetween(preStartDate, endDate);
			for(int r=0; r<numberOfRealisations; r++){
				ArrayList<EmissionTimeSeries> newEmisList = emisTSrealisations.get(r);
				int length = newEmisList.size();
				for(int i=0; i<length; i++){
					EmissionTimeSeries emisTS = newEmisList.get(i);
					int tsLength = emisTS.getEmissionSeries().size();
					int sourceID = emisTS.getDynamicSourceID();
					if(tsLength<h.getHours()){
						System.out.println("Length of time series for "+ sourceID+ " is "+tsLength);
					}
				}
			}

		}catch (Exception e) {
			LOGGER
					.debug("Error while getting Kriging results for observation collection: "
							+ e.getMessage());
			throw new RuntimeException(
					"Error while getting Kriging results for observation collection: "
							+ e.getMessage(), e);
		}
		finally {
			if (c != null) {
				c.close();
			}
		}

		return emisTSrealisations;
	}

	// street geometry extraction
		private ArrayList<EmissionSource> getStreetGeometry(int sourceCounter){
			ArrayList<EmissionSource> newEmisGeometry = new ArrayList<EmissionSource>();
			List<EmissionSource> allSources = null;
			// read Austal file with new geometry configuration
			String host = WPSConfig.getInstance().getWPSConfig().getServer().getHostname();
			String hostPort = WPSConfig.getInstance().getWPSConfig().getServer().getHostport();
			if(host == null) {
				try {
					host = InetAddress.getLocalHost().getCanonicalHostName();

				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
			String url = "http://" + host + ":" + hostPort + "/" +
			WebProcessingService.WEBAPP_PATH + "/" + "res" + "/" + "";

			try {
				URL austalURL = new URL(url+"/austal2000_template_newGeom.txt");
				Austal2000Txt austalGeomNew = new Austal2000Txt(austalURL.openStream());
				allSources =  austalGeomNew.getEmissionSources();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// loop through emission sources and keep only street traffic
			int count = 589 + 188;
			for(int i=0; i<sourceCounter; i++){
				EmissionSource source = allSources.get(i);
				source.setDynamicSourceID(i+1);
				source.setSourceType("streets");
				//emissionSources.get(i).isDynamic()&&!emissionSources.get(i).getSourceType().contains("streets")
				newEmisGeometry.add(source);
			}
			return newEmisGeometry;
		}

	private void substituteStreetEmissions(ArrayList<EmissionSource> newEmissionSources, ArrayList<EmissionTimeSeries> newEmisTS){
			// get old emission lists
			ArrayList<EmissionSource> emissionSources = (ArrayList<EmissionSource>) austal.getEmissionSources();
			ArrayList<EmissionTimeSeries> emisList = (ArrayList<EmissionTimeSeries>) ts.getEmissionSourcesTimeSeries();

			// if new geometries are already in the file
			if(newEmissionSources.size()==emissionSources.size()){

				// if emission geometry has already been added only substitute time series
				for(int i=0; i<newEmisTS.size(); i++){
					EmissionTimeSeries ets = newEmisTS.get(i);
					if(ets.getDynamicSourceID()==emisList.get(i).getDynamicSourceID()){
						emisList.set(i, ets);
					}
					else{
						System.out.println("Problem with emission sources");
					}
				}

				ts.setEmissionSourcesTimeSeries(emisList);
			}
			else{
				int newID = newEmisTS.size()+1;
				//get length o
				DateTime minDate = newEmisTS.get(0).getMinDate();
				DateTime maxDate = newEmisTS.get(0).getMaxDate();

				// add only non-traffic and non-dynamic sources from the old list
				for(int i=0; i<austal.getEmissionSources().size(); i++){
					// get dynamic sources which are not street sources
					if(emissionSources.get(i).isDynamic()&&!emissionSources.get(i).getSourceType().contains("streets")){
						EmissionSource e = emissionSources.get(i);
						int oldID = e.getDynamicSourceID();
						e.setDynamicSourceID(newID);
						newEmissionSources.add(e);

						// get respective time series and change id
						EmissionTimeSeries ets = emisList.get(i);

						// check if dynamic id is correct
						if(ets.getDynamicSourceID()==oldID){
							ets.setSourceID(newID);
							// cut timeseries to length of new one
							ets.cutTimePeriod(minDate, maxDate);
							//check
							newEmisTS.add(ets);
						}else{ // in case the time series is not correct search for it
							for(int j=0; j<emisList.size(); j++){
								ets = emisList.get(j);
								if(ets.getDynamicSourceID()==oldID){
									ets.setSourceID(newID);
									newEmisTS.add(ets);
									return;
								}
							}
						}

						// set new id for nex source
						newID++;
					}
					else if(!emissionSources.get(i).isDynamic()){ 	// static sources are added without changes
						newEmissionSources.add(emissionSources.get(i));
					}
				}

				// finally add new list to austal and ts object
				austal.setEmissionSources(newEmissionSources);
				ts.setEmissionSourcesTimeSeries(newEmisTS);
			}

		}


	/**
	 * Method to substitute existing Meteorology time series parts in the Austal database with new observations
	 * @param newMetList
	 */
	private void substituteMeteorology(MeteorologyTimeSeries newMetList){
		// get old meteorology list
		MeteorologyTimeSeries metList = ts.getMeteorologyTimeSeries();
		ArrayList<DateTime> timeStampList = (ArrayList<DateTime>) newMetList.getTimeStamps();

		// add stability class values to new list
		for(int i=0; i<timeStampList.size(); i++){
			DateTime d = timeStampList.get(i);
			newMetList.addStabilityClass(d, metList.getStabilityClass(d));
		}

		// finally add new list to ts object
		ts.setMeteorologyTimeSeries(newMetList);
	}



	/**
	 * Method to create Receptor point list from FeatureCollection input
	 * @param pointList
	 * @param featColl
	 */
	private void handleReceptorPoints(List<ReceptorPoint> pointList,
			FeatureCollection<?, ?> featColl) {

		int gx = austal.getStudyArea().getGx();
		int gy = austal.getStudyArea().getGy();

		String xp = "";
		String yp = "";
	    String hp = "2";

		int coordinateCount = 0;

		FeatureIterator<?> iterator = featColl.features();

		while (iterator.hasNext()) {

			SimpleFeature feature = (SimpleFeature) iterator.next();

			if (feature.getDefaultGeometry() instanceof com.vividsolutions.jts.geom.Point) {
				Coordinate coord = ((Geometry) feature.getDefaultGeometry())
						.getCoordinate();

				xp = xp.concat("" + (coord.x - gx) + " ");
				yp = yp.concat("" + (coord.y - gy) + " ");
			} else if (feature.getDefaultGeometry() instanceof MultiLineString) {

				MultiLineString lineString = (MultiLineString) feature
						.getDefaultGeometry();

				coordinateCount = lineString.getCoordinates().length;

				LOGGER.debug("Linestring has " + coordinateCount + " coordinates.");

				for (int i = 0; i < lineString.getCoordinates().length; i++) {

					if (i == 20) {
						coordinateCount = 20;
						break;
					}
					Coordinate coord = lineString.getCoordinates()[i];

					xp = "" + Math.round(coord.x - gx);
					yp = "" + Math.round(coord.y - gy);

					ReceptorPoint rp = new ReceptorPoint(xp, yp, "2");

					pointList.add(rp);
				}
			} else if (feature.getDefaultGeometry() instanceof LineString) {

				LineString lineString = (LineString) feature
						.getDefaultGeometry();

				coordinateCount = lineString.getCoordinates().length;

				LOGGER.debug("Linestring has " + coordinateCount + " coordinates.");

				for (int i = 0; i < lineString.getCoordinates().length; i++) {

					if (i == 20) {
						coordinateCount = 20;
						break;
					}
					Coordinate coord = lineString.getCoordinates()[i];

					xp = "" + Math.round(coord.x - gx);
					yp = "" + Math.round(coord.y - gy);

					ReceptorPoint rp = new ReceptorPoint(xp, yp, hp);

					pointList.add(rp);
				}
			}

		}

	}

	private UncertaintyObservationCollection createReceptorPointsRealisationsCollection(){
		// make uncertainty observation
		UncertaintyObservationCollection ucoll = new UncertaintyObservationCollection();
		AustalOutputReader austal = new AustalOutputReader();
		ArrayList<ArrayList<Point>> realisationsList = new ArrayList<ArrayList<Point>>();

		try{
			// get results for each realisation
			for(int i=0; i<numberOfRealisations; i++){
				// read results for receptor points and add them to an observation collection
				String folder = resultsPath + "\\"+ startDate.toString("yyyy-MM") +"\\PO"+i;
				ArrayList<Point> points = austal.readReceptorPointList(folder, false);
				realisationsList.add(points);
			}

			URI procedure = new URI("http://www.uncertweb.org/models/austal2000");
			URI observedProperty = new URI("http://www.uncertweb.org/phenomenon/pm10");
			URI codeSpace = new URI("");

			// loop through receptor points
			for(int i=0; i<realisationsList.get(0).size(); i++){
				// get geometry only once for each point
				Point p = realisationsList.get(0).get(i);
				double[] coords = p.coordinates();
				Coordinate coord = new Coordinate(coords[0], coords[1]);
				PrecisionModel pMod = new PrecisionModel(PrecisionModel.FLOATING);
				GeometryFactory geomFac = new GeometryFactory(pMod, 31467);
				SpatialSamplingFeature featureOfInterest = new SpatialSamplingFeature("sampledFeature", geomFac.createPoint(coord));
				featureOfInterest.setIdentifier(new Identifier(codeSpace, "point" + i));

				// loop through time series values
				for(int v=0; v<realisationsList.get(0).get(0).values().size(); v++){

					// get observation details only once
					ArrayList<Value> vals = realisationsList.get(0).get(i).values();
					Identifier identifier = new Identifier(codeSpace, "o_" + v);
					String timeStamp = vals.get(v).TimeStamp().trim();
					timeStamp = timeStamp.replace(" ", "T");

					// check if date lies before start or after end data
					TimeObject newT = new TimeObject(timeStamp);
					if(!newT.getDateTime().isBefore(startDate)&&!newT.getDateTime().isAfter(endDate)){
						ArrayList<Double> values = new ArrayList<Double>();

						// loop through realisations
						for(int r=0; r<realisationsList.size(); r++){
							Value val = realisationsList.get(r).get(i).values().get(v);
							values.add(val.PM10val());
						}

						// add realisation to observation collection
						ContinuousRealisation r = new ContinuousRealisation(values, -1.0d, "id");
						UncertaintyResult uResult = new UncertaintyResult(r, "ug/m3");
						UncertaintyObservation uObs = new UncertaintyObservation(
								newT, newT, procedure, observedProperty, featureOfInterest,
								uResult);
						ucoll.addObservation(uObs);
					}

				}

			}

		}catch(Exception e){
			e.printStackTrace();
		}

		return(ucoll);
	}

	/**
	 * Method to create OM Observation Collection with Austal results for receptor points
	 * @return
	 * @throws URISyntaxException
	 */
	private IObservationCollection createReceptorPointsResultCollection(String directory) throws URISyntaxException{

		MeasurementCollection mcoll = new MeasurementCollection();

		AustalOutputReader austal = new AustalOutputReader();

		ArrayList<Point[]> points = austal.readReceptorPoints(directory, false);

		URI procedure = new URI("http://www.uncertweb.org/models/austal2000");
		URI observedProperty = new URI("http://www.uncertweb.org/phenomenon/pm10");

		URI codeSpace = new URI("");

		for (int j = 0; j < points.size(); j++) {

			Point[] p = points.get(j);

			for (int i = 0; i < p.length; i++) {

				ArrayList<Value> vals = p[i].values();
				double[] coords = p[i].coordinates();

				// get coordinates and create point
				Coordinate coord = new Coordinate(coords[0], coords[1]);

				PrecisionModel pMod = new PrecisionModel(PrecisionModel.FLOATING);

				GeometryFactory geomFac = new GeometryFactory(pMod, 31467);
				SpatialSamplingFeature featureOfInterest = new SpatialSamplingFeature("sampledFeature", geomFac.createPoint(coord));
				featureOfInterest.setIdentifier(new Identifier(codeSpace, "point" + i));
				for (int k = 0; k < vals.size(); k++) {
					Identifier identifier = new Identifier(codeSpace, "m" + k);

					String timeStamp = vals.get(k).TimeStamp().trim();

					timeStamp = timeStamp.replace(" ", "T");

					TimeObject phenomenonTime = new TimeObject(timeStamp);
					MeasureResult resultm = new MeasureResult(vals.get(k).PM10val(), "ug/m3");
					Measurement m1 = new Measurement(identifier, null, phenomenonTime, phenomenonTime, null, procedure, observedProperty, featureOfInterest, null, resultm);
					mcoll.addObservation(m1);
				}
			}
		}
		return mcoll;
	}


}
