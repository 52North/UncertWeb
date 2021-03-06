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
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
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
import org.uncertweb.api.om.result.IResult;
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
import org.uncertweb.austalwps.util.austal.geometry.StudyArea;
import org.uncertweb.austalwps.util.austal.geometry.Utils;
import org.uncertweb.austalwps.util.austal.timeseries.EmissionTimeSeries;
import org.uncertweb.austalwps.util.austal.timeseries.MeteorologyTimeSeries;

import ucar.ma2.InvalidRangeException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class AUSTAL2000Process extends AbstractObservableAlgorithm{

	// system variables
	private List<String> errors = new ArrayList<String>();
	private static Logger LOGGER = Logger.getLogger(Austal2000Algorithm.class);
	private final String fileSeparator = System.getProperty("file.separator");
	private String tmpDir = "";
	private String workDirPath = tmpDir + fileSeparator + "PO" + fileSeparator;
	private String austalHome = "";
//	private String resultsPath = "D:\\PhD\\WP1.1_AirQualityModel\\WebServiceChain\\AustalWPS\\results";
	//private String resourcePath = "D:/JavaProjects/austal-wps/src/main/webapp/res/";

	// WPS inputs & outputs
	private final String inputIDCentralPoint = "central-point";
	private final String inputIDdd = "dd";
	private final String inputIDnx = "nx";
	private final String inputIDny = "ny";
	private final String inputIDqs = "qs";
	private final String inputIDz0 = "z0";
	private final String inputIDParameters = "model-parameters";
	private final String inputIDWindSpeed = "wind-speed";
	private final String inputIDWindDirection = "wind-direction";
	private final String inputIDStabilityClass = "stability-class";
	private final String inputIDStreetEmissions = "street-emissions";
	private final String inputIDVariableEmissions = "variable-emissions";
	private final String inputIDStaticEmissions = "static-emissions";
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
//	private Austal2000Txt austal;
//	private Zeitreihe ts;
	private int gx, gy;
	public static final String OS_Name = System.getProperty("os.name");
	private String zeitreiheFileName = "zeitreihe.dmna";
	private String zeitreiheFileNameEnglish = "series.dmna";
	private String austalFileName = "austal2000.txt";

	public AUSTAL2000Process(){
		if(OS_Name.contains("Windows")){
			tmpDir = System.getenv("TMP");
		}else{
			tmpDir = System.getenv("CATALINA_TMPDIR");
		}
	//	tmpDir = "c:/temp";
		workDirPath = tmpDir + fileSeparator + "PO" + fileSeparator;

		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(LocalAlgorithmRepository.class.getCanonicalName());
		for(Property property : propertyArray){
			if(property.getName().equalsIgnoreCase("Austal_Home")){
				austalHome = property.getStringValue();
				break;
			}
		}
//		}
	//	resourcePath = "http://" + host + ":" + hostPort + "/" +
	//	WebProcessingService.WEBAPP_PATH + "/" + "res" + "/" + "";
	}

	@Override
	public List<String> getErrors() {
		return errors;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if(id.equals(inputIDStreetEmissions)){
			return UncertWebIODataBinding.class;
		}else if(id.equals(inputIDVariableEmissions)){
			return UncertWebIODataBinding.class;
		}else if(id.equals(inputIDStaticEmissions)){
			return UncertWebIODataBinding.class;
		}else if(id.equals(inputIDWindDirection)){
			return UncertWebIODataBinding.class;
		}else if(id.equals(inputIDWindSpeed)){
			return UncertWebIODataBinding.class;
		}else if(id.equals(inputIDStabilityClass)){
			return UncertWebIODataBinding.class;
		}else if(id.equals(inputIDdd)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDnx)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDny)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDqs)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDz0)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDParameters)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDCentralPoint)){
			return GTVectorDataBinding.class;
		}else if(id.equals(inputIDStartTime)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDEndTime)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDReceptorPoints)){
			return GTVectorDataBinding.class;
			//return LiteralStringBinding.class;
		}else{
			return GenericFileDataBinding.class;
		}
	}

	@Override
	public Class<?> getOutputDataType(String arg0) {
		return UncertWebIODataBinding.class;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		// 1) Get inputs
		//TODO
		// 1.1) get study area parameters
		List<IData> inDataList = inputData.get(inputIDCentralPoint);
		if(!(inDataList == null) && inDataList.size() != 0){
			IData inData = inDataList.get(0);
			if(inData instanceof GTVectorDataBinding){
				//TODO: extract gx, gy and epsg from point
				FeatureCollection<?, ?> featColl = (FeatureCollection<?, ?>)inData.getPayload();
				FeatureIterator<?> iterator = featColl.features();

				while (iterator.hasNext()) {
					SimpleFeature feature = (SimpleFeature) iterator.next();
					if (feature.getDefaultGeometry() instanceof com.vividsolutions.jts.geom.Point) {
						Coordinate coord = ((Geometry) feature.getDefaultGeometry()).getCoordinate();
						gx = (int) coord.x ;
						gy = (int) coord.y;
					}
				}
			}
		}

		// dd input
		inDataList = inputData.get(inputIDdd);
		String dd = "";
		if(!(inDataList == null) && inDataList.size() != 0){
			IData inData = inDataList.get(0);
			if(inData instanceof LiteralIntBinding){
				dd = ""+(Integer) inData.getPayload();
			}
		}

		// nx input
		inDataList = inputData.get(inputIDnx);
		String nx = "";
		if(!(inDataList == null) && inDataList.size() != 0){
			IData inData = inDataList.get(0);
			if(inData instanceof LiteralIntBinding){
				nx = ""+(Integer) inData.getPayload();
			}
		}

		// ny input
		inDataList = inputData.get(inputIDny);
		String ny = "";
		if(!(inDataList == null) && inDataList.size() != 0){
			IData inData = inDataList.get(0);
			if(inData instanceof LiteralIntBinding){
				ny = ""+(Integer) inData.getPayload();
			}
		}

		// qs input
		inDataList = inputData.get(inputIDqs);
		String qs = "";
		if(!(inDataList == null) && inDataList.size() != 0){
			IData inData = inDataList.get(0);
			if(inData instanceof LiteralIntBinding){
				qs = ""+(Integer) inData.getPayload();
			}
		}

		// qs input
		inDataList = inputData.get(inputIDz0);
		String z0 = "";
		if(!(inDataList == null) && inDataList.size() != 0){
			IData inData = inDataList.get(0);
			if(inData instanceof LiteralDoubleBinding){
				z0 = ""+(Double) inData.getPayload();
			}
		}

		// parameters input
		inDataList = inputData.get(inputIDParameters);
		List<String> parameters = new ArrayList<String>();
		if(!(inDataList == null) && inDataList.size() != 0){
			for(IData inData : inDataList){
				if(inData instanceof LiteralStringBinding){
					parameters.add((String) inData.getPayload());
				}
			}
		}

		// create study area
		StudyArea sa = new StudyArea(gx+"", gy+"", dd, nx, ny);



		//1.2) get meteorology inputs
		MeteorologyTimeSeries metList = new MeteorologyTimeSeries();

		// windspeed
		inDataList = inputData.get(inputIDWindSpeed);
		if(!(inDataList == null) && inDataList.size() != 0 ){
			IData inData = inDataList.get(0);
			if(inData instanceof OMBinding){
				IObservationCollection obsCol = ((OMBinding)inData).getPayload();
				metList = addMeteorology(metList, obsCol);
			}
		}

		// winddirection
		inDataList = inputData.get(inputIDWindDirection);
		if(!(inDataList == null) && inDataList.size() != 0 ){
			IData inData = inDataList.get(0);
			if(inData instanceof OMBinding){
				IObservationCollection obsCol = ((OMBinding)inData).getPayload();
				metList = addMeteorology(metList, obsCol);
			}
		}

		// stabilityclass
		inDataList = inputData.get(inputIDStabilityClass);
		if(!(inDataList == null) && inDataList.size() != 0 ){
			IData inData = inDataList.get(0);
			if(inData instanceof OMBinding){
				IObservationCollection obsCol = ((OMBinding)inData).getPayload();
				metList = addMeteorology(metList, obsCol);
			}
		}

		// 1.3) get emission inputs
		List<EmissionSource> emissionSources = new ArrayList<EmissionSource>();

		// check if all emission sources have the same length as the meteorology time series
		int length = metList.getSize();

		// street emissions
		inDataList = inputData.get(inputIDStreetEmissions);
		if(!(inDataList == null) && inDataList.size() != 0){
			IData inData = inDataList.get(0);
			if(inData instanceof OMBinding){
				//input is O&M UncertWeb profile and can be processed now
				IObservationCollection obsCol = ((OMBinding)inData).getPayload();
				emissionSources = addVariableEmissionSources(emissionSources, obsCol, length);
			}
		}

		// variable emissions
		inDataList = inputData.get(inputIDVariableEmissions);
		if(!(inDataList == null) && inDataList.size() != 0){
			for(IData inData : inDataList){
				if(inData instanceof OMBinding){
					//input is O&M UncertWeb profile and can be processed now
					IObservationCollection obsCol = ((OMBinding)inData).getPayload();
					emissionSources = addVariableEmissionSources(emissionSources, obsCol, length);
				}
			}
		}

		// static emissions
		inDataList = inputData.get(inputIDStaticEmissions);
		if(!(inDataList == null) && inDataList.size() != 0){
			for(IData inData : inDataList){
				if(inData instanceof OMBinding){
					//input is O&M UncertWeb profile and can be processed now
					IObservationCollection obsCol = ((OMBinding)inData).getPayload();
					emissionSources = addStaticEmissionSources(emissionSources, obsCol);
				}
			}
		}

		// then get emission and meteorology time series and create zeitreihe and austal2000txt
		Austal2000Txt austal = new Austal2000Txt(sa, emissionSources);
		austal.setQs(Integer.parseInt(qs));
		Zeitreihe ts = new Zeitreihe(metList, emissionSources, z0);

		// adapt lengths of time series
	//	ts.setTimePeriod(ts.getMeteorologyTimeSeries().getMinDate(), ts.getMeteorologyTimeSeries().getMaxDate());

		// 1.4) get receptor point input
		List<IData> receptorPointsDataList = inputData.get(inputIDReceptorPoints);
		List<ReceptorPoint> pointList = new ArrayList<ReceptorPoint>();

		if(!(receptorPointsDataList == null) && receptorPointsDataList.size() != 0){
			IData receptorPointsData = receptorPointsDataList.get(0);
			if(receptorPointsData instanceof GTVectorDataBinding){
				handleReceptorPoints(pointList, (FeatureCollection<?, ?>)receptorPointsData.getPayload());
				// set Austal into non-grid mode
				austal.setOs("");
			}
		}else{ // for raster execution
			austal.setOs("\"NOSTANDARD;Kmax=1;Average=1;Interval=3600\"");
		}
		if(pointList.size()>0)
			austal.setReceptorPoints(pointList);

		// 1.5)  write files
		writeFiles(austal, ts);

		// 2) execute Austal for each sample and collect results
		try {
			// get command for execute Austal
//			String command = austalHome + fileSeparator + "austal2000.exe " + workDirPath;

			String command = "";

			String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			int webinfIndex = path.indexOf("WEB-INF");
			path = path.substring(0, webinfIndex);

			if (!OS_Name.startsWith("Windows")) {
				path = path + "/res/Austal_bin/Linux";
				command = path + fileSeparator + "austal2000 "
						+ workDirPath;
			} else {
				path = path + "/res/Austal_bin/Windows";
				command = path + fileSeparator + "austal2000.exe "
						+ workDirPath;
			}


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

		// 3) handle results
		Map<String, IData> result = new HashMap<String, IData>();

		// if receptor points were provided give their results back
		if(!(receptorPointsDataList == null) && receptorPointsDataList.size() != 0){
			try {
				// make uncertainty observation
				MeasurementCollection mcoll = (MeasurementCollection) createReceptorPointsResultCollection();

//				// save result locally
//				String filepath = resultsPath + "\\Austal_"+startDate.toString("yyyy-MM")+".xml";
//				File file = new File(filepath);
//
//				// encode, store (for using in austal request later)
//				try {
//					new StaxObservationEncoder().encodeObservationCollection(mcoll,file);
//				} catch (OMEncodingException e) {
//					e.printStackTrace();
//				}

				OMBinding omd = new OMBinding(mcoll);
				result.put(outputIDResult, omd);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// if no receptor points were given, read grid results from Austal execution
		else{
			//int h = Hours.hoursBetween(preStartDate, startDate).getHours();
			// get results only from start date to end date only
			HashMap<Integer, HashMap<Integer, ArrayList<Double>>> realisationsMap = new HashMap<Integer, HashMap<Integer, ArrayList<Double>>>();
			AustalOutputReader outputReader = new AustalOutputReader();
			realisationsMap.put(1, outputReader.readHourlyFiles(workDirPath, 1, false));

//			for(int i=0; i<numberOfRealisations; i++){
//				String folder = resultsPath + "\\"+ startDate.toString("yyyy-MM") +"\\PO"+i;
//				realisationsMap.put(i+1, outputReader.readHourlyFiles(folder, h+1, false));
//			}

			// write results to a NetCDF file
			String filepath = workDirPath + "Austal_"+startDate.toString("yyyy-MM")+".nc";
	//				+ System.currentTimeMillis() + ".nc";
			NetcdfUWFile resultFile = null;
			try {
				resultFile = outputReader.createNetCDFfile(filepath,
						austal.getStudyArea().getXcoords(), austal.getStudyArea().getYcoords(),
						startDate, 1, realisationsMap);
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


	private void writeFiles(Austal2000Txt austal, Zeitreihe ts){
		// test writer

		//create folder if necessary
		File po_folder = new File(workDirPath);
		if (!po_folder.exists()) {
			po_folder.mkdir();
		}
		File new_austalFile = new File(workDirPath+"/"+austalFileName);

		//create austal file if necessary
		if (!new_austalFile.exists()) {
			try {
				//file not existent, create new one
				new_austalFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//write austal file
		austal.writeFile(new_austalFile);

		//create Zeitreihe file if necessray
		File new_tsFile = new File(workDirPath+"/"+zeitreiheFileName);
		if (!new_tsFile.exists()) {
			try {
				new_tsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//write Zeitreihe
		ts.writeFile(new_tsFile);

		File new_tsFile2 = new File(workDirPath+"/"+zeitreiheFileNameEnglish);
		ts.writeFile(new_tsFile2);
	}


	private List<EmissionSource> addVariableEmissionSources(List<EmissionSource> emissions, IObservationCollection coll, int tsSize) {
			SpatialSamplingFeature spsam = null;
			int counter = emissions.size()+1;
			/*
			 * The samplingfeature of the observations is only defined explicitly once
			 * and the remainder of the observations holds just references.
			 * So we need to check all observations and if the sampling feature is
			 * "new" we add it to the list.
			 */
			EmissionTimeSeries emisTS = null;

			for (AbstractObservation abstractObservation : coll.getObservations()) {

				// for the first observation
				if(spsam == null){
					spsam = abstractObservation.getFeatureOfInterest();
					emisTS = new EmissionTimeSeries(counter);// assign id of respective source

					// create EmissionSource
					EmissionSource tmpEMS = FeatOfInt2EmisSource(abstractObservation.getFeatureOfInterest().getShape());
					tmpEMS.setDynamicSourceID(counter);
					emissions.add(tmpEMS);

					counter++;
				}
				// only if it's a new spatial sampling feature
				else if (!spsam.equals(abstractObservation.getFeatureOfInterest())) {
					// check if current time series has correct length
					if(emisTS.getTimeStamps().size()>=tsSize){
						// add emission time series to previous emission source
						emissions.get(emissions.size()-1).setEmissionList(emisTS);
					}else{
						// else remove last source and reset counter
						emissions.remove(emissions.size()-1);
						counter--;
					}

					// new emissions time series
					emisTS = new EmissionTimeSeries(counter);// assign id of respective source
					spsam = abstractObservation.getFeatureOfInterest();

					// create EmissionSource
					EmissionSource tmpEMS = FeatOfInt2EmisSource(abstractObservation.getFeatureOfInterest().getShape());
					tmpEMS.setDynamicSourceID(counter);
					emissions.add(tmpEMS);

					counter++;
				}
				// This do always:
				// add current observation to current time series
				DateTime dt = abstractObservation.getPhenomenonTime().getDateTime();
				IResult result = abstractObservation.getResult();
				Double obs = (Double) result.getValue();
				// correct for negative values
				if(obs<0)
					obs = 0d;
				emisTS.addEmissionValue(dt, obs);
			}

			// add last EmissionTimeSeries
			if(emisTS.getTimeStamps().size()==tsSize){
				emissions.get(emissions.size()-1).setEmissionList(emisTS);
			}else{
				// else remove last source and reset counter
				emissions.remove(emissions.size()-1);
			}
			return emissions;
	}

	private List<EmissionSource> addStaticEmissionSources(List<EmissionSource> emissions, IObservationCollection coll){
		// here each sampling feature has only one observation
		for (AbstractObservation abstractObservation : coll.getObservations()) {
			// create EmissionSource
			EmissionSource tmpEMS = FeatOfInt2EmisSource(abstractObservation.getFeatureOfInterest().getShape());

			// get observation result
			IResult result = abstractObservation.getResult();
			Double obs = (Double) result.getValue();

			// correct for negative values
			if(obs<0)
				obs = 0d;
			tmpEMS.setStaticStrength(obs);
			emissions.add(tmpEMS);
		}
		return emissions;
	}

	private EmissionSource FeatOfInt2EmisSource(Geometry geom){
		EmissionSource tmpEMS = null;
		if (geom instanceof MultiLineString) {
			MultiLineString mline = (MultiLineString) geom;
			Coordinate[] coords = mline.getCoordinates();
			tmpEMS = Utils.lineGK3ToLocalCoords(gx, gy, coords);
		}
		// polygon handling
		else if (geom instanceof MultiPolygon) {
			MultiPolygon mpoly = (MultiPolygon) geom;
			Coordinate[] coords = mpoly.getCoordinates();
			tmpEMS = Utils.polygonGK3ToLocalCoords(
					gx, gy, coords);
		}
		// point handling
		else if (geom instanceof com.vividsolutions.jts.geom.Point) {
			com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point) geom;
			Coordinate[] coords = point.getCoordinates();
			tmpEMS = Utils.pointGK3ToLocalCoords(gx, gy, coords);
		}
		return tmpEMS;
	}


	private MeteorologyTimeSeries addMeteorology(MeteorologyTimeSeries meteorology, IObservationCollection coll){
			/*
			 * the O&M is structured in that way that at first the wind direction values are listed
			 * and second the wind speed values. each timestamp has a wind direction value and on wind speed value
			 */
			for (AbstractObservation obs : coll.getObservations()) {
				// get result
				Double val = (Double)obs.getResult().getValue();
				DateTime dt = obs.getPhenomenonTime().getDateTime();

				// check which observation is contained
				if(obs.getObservedProperty().getPath().contains("winddirection")){
					// correct for outlier samples
					while(val<0)
						val = 360+val;
					while(val>360)
						val = val-360;
					meteorology.addWindDirection(dt, val);
				}else if(obs.getObservedProperty().getPath().contains("windspeed")){
					// correct for outlier samples
					if(val<0.1)
						val = 0.1;
					meteorology.addWindSpeed(dt, val);
				}else if(obs.getObservedProperty().getPath().contains("stabilityclass")){
					val= (Double)obs.getResult().getValue();
					meteorology.addStabilityClass(dt, val);
				}
			}

			return meteorology;
	}



	/**
	 * Method to create Receptor point list from FeatureCollection input
	 * @param pointList
	 * @param featColl
	 */
	private void handleReceptorPoints(List<ReceptorPoint> pointList,
			FeatureCollection<?, ?> featColl) {

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

	/**
	 * Method to create OM Observation Collection with Austal results for receptor points
	 * @return
	 * @throws URISyntaxException
	 */
	private IObservationCollection createReceptorPointsResultCollection() throws URISyntaxException{
		MeasurementCollection mcoll = new MeasurementCollection();
		AustalOutputReader austal = new AustalOutputReader();
		ArrayList<Point> points = austal.readReceptorPointList(workDirPath, false);

		URI procedure = new URI("http://www.uncertweb.org/models/austal2000");
		URI observedProperty = new URI("http://www.uncertweb.org/phenomenon/pm10");
		URI codeSpace = new URI("");

		// loop through points
		for (int j = 0; j < points.size(); j++) {
			Point p = points.get(j);
			ArrayList<Value> vals = p.values();
			double[] coords = p.coordinates();

			// get coordinates and create point
			Coordinate coord = new Coordinate(coords[0], coords[1]);
			PrecisionModel pMod = new PrecisionModel(PrecisionModel.FLOATING);
			GeometryFactory geomFac = new GeometryFactory(pMod, 31467);
			SpatialSamplingFeature featureOfInterest = new SpatialSamplingFeature("sampledFeature", geomFac.createPoint(coord));
			featureOfInterest.setIdentifier(new Identifier(codeSpace, "point" + j));

			// loop through observations
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
		return mcoll;
	}


}
