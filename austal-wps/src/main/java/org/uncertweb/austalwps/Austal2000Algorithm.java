package org.uncertweb.austalwps;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.joda.time.DateTime;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;

import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.n52.wps.server.WebProcessingService;
import org.opengis.feature.simple.SimpleFeature;
import org.uncertml.sample.UnknownSample;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.austalwps.util.AustalOutputReader;
import org.uncertweb.austalwps.util.Point;
import org.uncertweb.austalwps.util.StreamGobbler;
import org.uncertweb.austalwps.util.Value;
import org.uncertweb.austalwps.util.austal.files.Austal2000Txt;
import org.uncertweb.austalwps.util.austal.files.Zeitreihe;
import org.uncertweb.austalwps.util.austal.geometry.EmissionSource;
import org.uncertweb.austalwps.util.austal.geometry.ReceptorPoint;
import org.uncertweb.austalwps.util.austal.geometry.Utils;
import org.uncertweb.austalwps.util.austal.timeseries.EmissionTimeSeries;
import org.uncertweb.austalwps.util.austal.timeseries.MeteorologyTimeSeries;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class Austal2000Algorithm extends AbstractObservableAlgorithm{

	private static Logger LOGGER = Logger.getLogger(Austal2000Algorithm.class);
	// WPS inputs & outputs
	private final String inputIDWindSpeed = "wind-speed";
	private final String inputIDWindDirection = "wind-direction";
	private final String inputIDStreetEmissions = "street-emissions";
	private final String inputIDReceptorPoints = "receptor-points";
	private final String inputIDStartTime = "start-time";
	private final String inputIDEndTime = "end-time";
	private final String outputIDResult = "result";
	// Austal result variables
	private final String logFileMarkerBeginningEnglish = "File";
	private final String logFileMarkerEndEnglish = "written.";
	private final String logFileMarkerBeginningGerman = "Datei";
	private final String logFileMarkerEndGerman = "ausgeschrieben.";
	// system variables
	private final String fileSeparator = System.getProperty("file.separator");
	private String tmpDir = "";
	private String workDirPath = tmpDir + fileSeparator + "PO" + fileSeparator;
	private String austalHome = "";
	private List<String> errors = new ArrayList<String>();
	public static final int BUFFER = 2048;
	// general Austal objects
	private Austal2000Txt austal;
	private Zeitreihe ts;
	public static final String OS_Name = System.getProperty("os.name");
	private String zeitreiheFileNameEnglish = "series.dmna";
	private String zeitreiheFileName = "zeitreihe.dmna";
	private String austalFileName = "austal2000.txt";
	private Map<DateTime, AbstractObservation> timeObservationMap;
	
	
	public Austal2000Algorithm(){	
		
		timeObservationMap = new HashMap<DateTime, AbstractObservation>();
		
		if(OS_Name.contains("Windows")){
			tmpDir = System.getenv("TMP");
		}else{
			tmpDir = System.getenv("CATALINA_TMPDIR");
		}
		workDirPath = tmpDir + fileSeparator + "PO" + fileSeparator;
		
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(LocalAlgorithmRepository.class.getCanonicalName());
		for(Property property : propertyArray){
			if(property.getName().equalsIgnoreCase("Austal_Home")){
				austalHome = property.getStringValue();
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
		if(id.equals(inputIDStreetEmissions)){
			return UncertWebIODataBinding.class;
		}else if(id.equals(inputIDWindDirection)){
			return UncertWebIODataBinding.class;}
		else if(id.equals(inputIDWindSpeed)){
			return UncertWebIODataBinding.class;}
		else if(id.equals(inputIDStartTime)){
			return LiteralDateTimeBinding.class;
		}else if(id.equals(inputIDEndTime)){
			return LiteralDateTimeBinding.class;
		}else if(id.equals(inputIDReceptorPoints)){
			return GTVectorDataBinding.class;
			//return LiteralStringBinding.class;
		}else{
			return GenericFileDataBinding.class;			
		}
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		return UncertWebIODataBinding.class;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {		
	
		File workDir = new File(workDirPath);		
		if(!workDir.exists()){
			workDir.mkdir();
		}
		
		// 1. read files from local resources to create datamodel
		this.readFiles("austal2000_template.txt", "zeitreihe_template.dmna");		
		
		// 1.1 Get emission inputs
		List<IData> streetEmissionDataList = inputData.get(inputIDStreetEmissions);
		
		// new emission lists
		ArrayList<EmissionSource> newEmissionSources = new ArrayList<EmissionSource>();
		ArrayList<EmissionTimeSeries> newEmisTS = new ArrayList<EmissionTimeSeries>();				
		
		// if emission data has been provided, fill emission lists
		if(!(streetEmissionDataList == null) && streetEmissionDataList.size() != 0){

			IData streetEmissionData = streetEmissionDataList.get(0);
			
			if(streetEmissionData instanceof OMBinding){
				//input is O&M UncertWeb profile and can be processed now
				IObservationCollection obsCol = ((OMBinding)streetEmissionData).getPayload();
				try {
					handleEmissionObservationCollection(newEmissionSources, newEmisTS, obsCol);
				} catch (Exception e) {
					LOGGER.debug(e);
					e.printStackTrace();
				}
			}			
		}					
		
		
		// 1.2 Get meteorology inputs
		List<IData> windspeedDataList = inputData.get(inputIDWindSpeed);	
		List<IData> winddirectionDataList = inputData.get(inputIDWindDirection);
		
		// new meteorology list
		MeteorologyTimeSeries newMetList = new MeteorologyTimeSeries();		
		
		// if windspeed data has been provided, fill meteorology list		
		if(!(windspeedDataList == null) && windspeedDataList.size() != 0 ){

			IData windspeedData = windspeedDataList.get(0);
			
			if(windspeedData instanceof OMBinding){				
				IObservationCollection obsCol = ((OMBinding)windspeedData).getPayload();
				try {
					handleMeteorology(newMetList, obsCol);
				} catch (Exception e) {
					e.printStackTrace();
				}		
			}
		}			
		
		// if winddirection data has been provided, fill meteorology list
		if(!(winddirectionDataList == null) && winddirectionDataList.size() != 0 ){

			IData winddirectionData = winddirectionDataList.get(0);
			
			if(winddirectionData instanceof OMBinding){				
				IObservationCollection obsCol = ((OMBinding)winddirectionData).getPayload();				
				try {
					handleMeteorology(newMetList, obsCol);
				} catch (Exception e) {
					e.printStackTrace();
				}		
			}
		}			
	
		// 1.3 get receptor point input
		List<IData> receptorPointsDataList = inputData.get(inputIDReceptorPoints);
		List<ReceptorPoint> pointList = new ArrayList<ReceptorPoint>();
		
		if(!(receptorPointsDataList == null) && receptorPointsDataList.size() != 0){

			IData receptorPointsData = receptorPointsDataList.get(0);
			
			if(receptorPointsData instanceof GTVectorDataBinding){			
				handleReceptorPoints(pointList, (FeatureCollection<?, ?>)receptorPointsData.getPayload());
				// set Austal into non-grid mode
				austal.setOs("");
			}	
		}
		
		// 1.4 Add inputs to Austal Configuration and write files
		if(pointList.size()>0)
			austal.setReceptorPoints(pointList);
		substituteStreetEmissions(newEmissionSources, newEmisTS);
		substituteMeteorology(newMetList);		

		writeFiles();
		
		// 2. execute austal2000	
		try {
			//TODO: modify for linux use
			String command = austalHome + fileSeparator + "austal2000.exe " + workDir.getAbsolutePath();
			
//			String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
//			int webinfIndex = path.indexOf("WEB-INF");
//			path = path.substring(0, webinfIndex);
//			
//			if (!OS_Name.startsWith("Windows")) {
//				path = path + "/res/Austal_bin/Linux";
//				command = path + fileSeparator + "austal2000 "
//						+ workDir.getAbsolutePath();
//			} else {
//				path = path + "/res/Austal_bin/Windows";
//				command = path + fileSeparator + "austal2000.exe "
//						+ workDir.getAbsolutePath();
//			}
			
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
		
		//parse logfile and extract filenames TODO: maybe better list files in directory...			
//		ArrayList<String> fileList = new ArrayList<String>();
//		
//		try{
//			
//			File logFile = new File(workDirPath + fileSeparator + "austal2000.log");
//			
//			BufferedReader bufferedFileReader = new BufferedReader(new FileReader(logFile));
//			
//			String line = bufferedFileReader.readLine();
//			
//			while(line != null){
//				
//				if(line.contains(logFileMarkerBeginningEnglish)&&line.contains(logFileMarkerEndEnglish)){
//					
//					int beginning = line.indexOf(logFileMarkerBeginningEnglish);
//					int end = line.indexOf(logFileMarkerEndEnglish);
//					
//					String fileName = line.substring(beginning, end);
//					
//					fileName = fileName.replace("\"", "");
//					
//					fileName = fileName.replace(logFileMarkerBeginningEnglish, "");
//					
//					fileName = fileName.trim();
//					
//					fileName = fileName.concat(".dmna");
//					
//					// store in list
//					if (!fileList.contains(fileName)) {
//
//						fileList.add(fileName);
//					}
//					
//				}else if(line.contains(logFileMarkerBeginningGerman)&&line.contains(logFileMarkerEndGerman)){
//					
//					int beginning = line.indexOf(logFileMarkerBeginningGerman);
//					int end = line.indexOf(logFileMarkerEndGerman);
//					
//					String fileName = line.substring(beginning, end);
//					
//					fileName = fileName.replace("\"", "");
//					
//					fileName = fileName.replace(logFileMarkerBeginningGerman, "");
//					
//					fileName = fileName.trim();
//					
//					fileName = fileName.concat(".dmna");
//					
//					// store in list
//					if (!fileList.contains(fileName)) {
//
//						fileList.add(fileName);
//					}
//				}
//				
//				line = bufferedFileReader.readLine();
//			}
//			
//			bufferedFileReader.close();
//			
//		}catch (Exception e) {
//			e.printStackTrace();
//		}			
		
		// 3. prepare output
		Map<String, IData> result = new HashMap<String, IData>();	
		
		// if receptor points were provided give their results back
		if(!(receptorPointsDataList == null) && receptorPointsDataList.size() != 0){
			try {
				// read results for receptor points and add them to an observation collection
				IObservationCollection mcoll = createReceptorPointsResultCollection();		
				
				OMBinding omd = new OMBinding(mcoll);
				result.put(outputIDResult, omd);			
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// if no receptor points were given, read grid results from Austal execution
		else{
			// read grid files in the result folder
			AustalOutputReader outputReader = new AustalOutputReader();
			HashMap<Integer, HashMap<Integer, ArrayList<Double>>> realisationsMap = new HashMap<Integer, HashMap<Integer, ArrayList<Double>>>();
			realisationsMap.put(1, outputReader.readHourlyFiles(workDirPath, 1, false));			
			
			// write results to a NetCDF file
			String filepath = System.getProperty("java.io.tmpdir") + fileSeparator + "netCDFresult_"
					+ System.currentTimeMillis() + ".nc";		
			NetcdfUWFile resultFile = null;
			try {
				resultFile = outputReader.createNetCDFfile(filepath, 
						austal.getStudyArea().getXcoords(), austal.getStudyArea().getYcoords(), 
						ts.getMeteorologyTimeSeries().getMinDate(), 1, realisationsMap);
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
			
		//TODO: what is this for?
		FeatureCollection<?, SimpleFeature> collection = FeatureCollections.newCollection();		
		result.put("result", new GTVectorDataBinding(collection));		
		return result;
	}	

	private void readFiles(String austalFileName, String zeitreiheFileName){		
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
			URL austalURL = new URL(url + austalFileName);
			austal = new Austal2000Txt(austalURL.openStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			LOGGER.debug(e);
		} catch (IOException e) {
			LOGGER.debug(e);
			e.printStackTrace();
		}
		
		try {
			URL zeitReiheURL = new URL(url + zeitreiheFileName);
			ts = new Zeitreihe(zeitReiheURL.openStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			LOGGER.debug(e);
		} catch (IOException e) {
			LOGGER.debug(e);
			e.printStackTrace();
		}
		
	}
	
	private void writeFiles(){
		// test writer
		File new_austalFile = new File(workDirPath+"/"+austalFileName);
		austal.writeFile(new_austalFile);
		File new_tsFile = new File(workDirPath+"/"+zeitreiheFileName);
		ts.writeFile(new_tsFile);
		//File new_tsFile2 = new File(workDirPath+"/"+zeitreiheFileNameEnglish);
		//ts.writeFile(new_tsFile2);
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
				/*
				 * TODO points won't work right now
				 */
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
	
	/**
	 * Method to create meteorology time series from ObservationCollection input
	 * @param newMetList
	 * @param coll
	 * @throws Exception
	 */
	//TODO: this method can be simplified as now windspeed and winddirection are provided separately!
	private void handleMeteorology(MeteorologyTimeSeries newMetList, IObservationCollection coll) throws Exception{
				
		/*
		 * the O&M is structured in that way that at first the wind direction values are listed
		 * and second the wind speed values. each timestamp has a wind direction value and on wind speed value 
		 */
		for (AbstractObservation abstractObservation : coll.getObservations()) {
			LOGGER.debug(timeObservationMap.keySet().size());
			
			if(!timeObservationMap.containsKey(abstractObservation.getPhenomenonTime().getDateTime())){
				timeObservationMap.put(abstractObservation.getPhenomenonTime().getDateTime(), abstractObservation);
			}else{
				/*
				 * if the map already contains a observation we should now have both windspeed and winddirection
				 * either in the Observation already in the map or in the current abstractObservation
				 */
				AbstractObservation obs1 = timeObservationMap.get(abstractObservation.getPhenomenonTime().getDateTime());
				
				// check for wind direction value			
				Double obsWD = 0.0d;
				if(obs1.getObservedProperty().getPath().contains("winddirection")){
					obsWD = (Double)obs1.getResult().getValue();
				}else if(abstractObservation.getObservedProperty().getPath().contains("winddirection")){
					obsWD= (Double)abstractObservation.getResult().getValue();
				}
				// correct for outlier samples
				// TODO: This should be done during sampling instead!
				while(obsWD<0)
					obsWD = 360+obsWD;
				while(obsWD>360)
					obsWD = obsWD-360;
				
				// check for wind speed value				
				Double obsWS = 0.0d;
				if(obs1.getObservedProperty().getPath().contains("windspeed")){
					obsWS = (Double)obs1.getResult().getValue();
				}else if(abstractObservation.getObservedProperty().getPath().contains("windspeed")){
					obsWS = (Double)abstractObservation.getResult().getValue();
				}
				// correct for outlier samples
				// TODO: This should be done during sampling instead!
				if(obsWD<0.1)
					obsWD = 0.1;

				DateTime dt = abstractObservation.getPhenomenonTime().getDateTime();				
				/*
				 * if exact date format needed
				 */
//				Date timeStamp = null;
//				try {
//					timeStamp = dateFormat.parse(dt.toString("yyyy-MM-dd.HH:mm:ss"));
//				} catch (ParseException e) {
//					e.printStackTrace();
//				}
				newMetList.addWindDirection(dt, obsWD);
				newMetList.addWindSpeed(dt, obsWS);
				
			}
		}
	}
	
	/**
	 * Method to create new emission list from ObservationCollection input
	 * @param newEmissionSources
	 * @param newEmisTS
	 * @param coll
	 * @throws Exception
	 */
	private void handleEmissionObservationCollection(ArrayList<EmissionSource> newEmissionSources, ArrayList<EmissionTimeSeries> newEmisTS, IObservationCollection coll) throws Exception {
		
		int gx = austal.getStudyArea().getGx();
		int gy = austal.getStudyArea().getGy();

		SpatialSamplingFeature spsam = null;

		int counter = 1;
		/*
		 * The samplingfeature of the observations is only defined explicitly once
		 * and the remainder of the observations holds just references.
		 * So we need to check all observations and if the sampling feature is 
		 * "new" we add it to the list. 
		 */
		EmissionTimeSeries lineTS = null;
		
		for (AbstractObservation abstractObservation : coll.getObservations()) {
			
			if(spsam == null){
				spsam = abstractObservation.getFeatureOfInterest();

				lineTS = new EmissionTimeSeries(counter);// assign id of respective source
				
				if (abstractObservation.getFeatureOfInterest().getShape() instanceof MultiLineString) {

					MultiLineString mline = (MultiLineString) abstractObservation
							.getFeatureOfInterest().getShape();

					Coordinate[] coords = mline.getCoordinates();
					/*
					 * create EmissionSource
					 */
					EmissionSource tmpEMS = Utils.lineGK3ToLocalCoords(gx, gy, coords[0].x, coords[0].y, coords[coords.length-1].x, coords[coords.length-1].y);					
					tmpEMS.setDynamicSourceID(counter);
					newEmissionSources.add(tmpEMS);
					
				} else if (abstractObservation.getFeatureOfInterest()
						.getShape() instanceof MultiPolygon) {

					MultiPolygon mpoly = (MultiPolygon) abstractObservation
							.getFeatureOfInterest().getShape();
					
					Coordinate[] coords = mpoly.getCoordinates();
					/*
					 * create EmissionSource
					 */
					EmissionSource tmpEMS = Utils.cellPolygonGK3ToLocalCoords(gx, gy, coords[1].x, coords[1].y, coords[3].x, coords[3].y);
					tmpEMS.setDynamicSourceID(counter);
					newEmissionSources.add(tmpEMS);	
				}
				
				counter++;
			} else {
				if (!spsam.equals(abstractObservation.getFeatureOfInterest())) {
					newEmisTS.add(lineTS);
					lineTS = new EmissionTimeSeries(counter);// assign id of respective source
					spsam = abstractObservation.getFeatureOfInterest();
					if (abstractObservation.getFeatureOfInterest().getShape() instanceof MultiLineString) {

						MultiLineString mline = (MultiLineString) abstractObservation
								.getFeatureOfInterest().getShape();

						Coordinate[] coords = mline.getCoordinates();
						EmissionSource tmpEMS = Utils.lineGK3ToLocalCoords(gx, gy, coords[0].x, coords[0].y, coords[1].x, coords[1].y);
						tmpEMS.setDynamicSourceID(counter);
						newEmissionSources.add(tmpEMS);
					} else if (abstractObservation.getFeatureOfInterest()
							.getShape() instanceof MultiPolygon) {

						MultiPolygon mpoly = (MultiPolygon) abstractObservation
								.getFeatureOfInterest().getShape();
						
						Coordinate[] coords = mpoly.getCoordinates();
						
						EmissionSource tmpEMS = Utils.cellPolygonGK3ToLocalCoords(gx, gy, coords[1].x, coords[1].y, coords[3].x, coords[3].y);
						tmpEMS.setDynamicSourceID(counter);
						newEmissionSources.add(tmpEMS);
					}
					counter++;
				}
			}
			/*
			 * create EmissionTimeSeries
			 */
			DateTime dt = abstractObservation.getPhenomenonTime().getDateTime();
			IResult result = abstractObservation.getResult();
			
			Object o = result.getValue();
			Double obs = (Double) o;
			// correct for negative values
			// TODO: This should be done during sampling instead!
			if(obs<0)
				obs = 0d;
			
			/*
			 * if exact date format needed
			 */
//			Date timeStamp = null;
//			try {
//				timeStamp = dateFormat.parse(dt.toString("yyyy-MM-dd.HH:mm:ss"));
//			} catch (ParseException e) {
//				e.printStackTrace();
//			}
			lineTS.addEmissionValue(dt, obs);

		}
		/*
		 * add last EmissionTimeSeries
		 */
		newEmisTS.add(lineTS);
	}
	
	/**
	 * Method to substitute street emissions in the Austal datbase by new observations
	 * @param newEmissionSources
	 * @param newEmisTS
	 */
	private void substituteStreetEmissions(ArrayList<EmissionSource> newEmissionSources, ArrayList<EmissionTimeSeries> newEmisTS){
		// get old emission lists
		ArrayList<EmissionSource> emissionSources = (ArrayList<EmissionSource>) austal.getEmissionSources();
		ArrayList<EmissionTimeSeries> emisList = (ArrayList<EmissionTimeSeries>) ts.getEmissionSourcesTimeSeries();
		int newID = newEmissionSources.size()+1;
		
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
	 * Method to create OM Observation Collection with Austal results for receptor points
	 * @return
	 * @throws URISyntaxException
	 */
	private IObservationCollection createReceptorPointsResultCollection() throws URISyntaxException{
		
		MeasurementCollection mcoll = new MeasurementCollection();			
		
		AustalOutputReader austal = new AustalOutputReader();
		
		ArrayList<Point[]> points = austal.readReceptorPoints(workDirPath, false);
		
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
	
	
	public File zipFiles(String[] files){
		
		String result = tmpDir + fileSeparator + "result.zip";
		
		 try {
	         BufferedInputStream origin = null;
	         FileOutputStream dest = new 
	           FileOutputStream(result);
	         ZipOutputStream out = new ZipOutputStream(new 
	           BufferedOutputStream(dest));
	         byte data[] = new byte[BUFFER];

	         for (int i=0; i<files.length; i++) {
	            LOGGER.debug("Adding: "+files[i]);
	            
	            File f = new File(files[i]);
	            
	            FileInputStream fi = new 
	              FileInputStream(f);
	            origin = new 
	              BufferedInputStream(fi, BUFFER);
	            	            
	            ZipEntry entry = new ZipEntry(f.getName());
	            out.putNextEntry(entry);
	            int count;
	            while((count = origin.read(data, 0, 
	              BUFFER)) != -1) {
	               out.write(data, 0, count);
	            }
	            origin.close();
	         }
	         out.close();
	      } catch(Exception e) {
	         e.printStackTrace();
	      }
	      File resultingFile = new File(result);
	      
	      if(!resultingFile.exists()){
	    	  return null;
	      }	      
	      return resultingFile;		
	}
}
