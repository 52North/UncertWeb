package org.uncertweb.austalwps.util.austal.control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.uncertml.sample.RandomSample;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.UnknownSample;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.austalwps.U_Austal2000Algorithm;
import org.uncertweb.austalwps.util.AustalOutputReader;
import org.uncertweb.austalwps.util.Point;
import org.uncertweb.austalwps.util.Value;
import org.uncertweb.austalwps.util.austal.files.Austal2000Txt;
import org.uncertweb.austalwps.util.austal.files.Zeitreihe;
import org.uncertweb.austalwps.util.austal.geometry.EmissionSource;
import org.uncertweb.austalwps.util.austal.geometry.ReceptorPoint;
import org.uncertweb.austalwps.util.austal.geometry.StudyArea;
import org.uncertweb.austalwps.util.austal.timeseries.EmissionTimeSeries;
import org.uncertweb.austalwps.util.austal.timeseries.MeteorologyTimeSeries;

import ucar.ma2.Array;
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
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;

// class to manage Austal Setup

public class AustalSetupControl {

	private static final String FILE_PATH="C:\\UncertWeb\\Austal\\resources\\";

	// general Austal objects
	private Austal2000Txt austal;
	private Zeitreihe ts;
	//private StudyArea studyArea;
	//private List<ReceptorPoint> receptorPoints;

	// Austal objects that need to be changed
	private List<EmissionSource> emissionSources;
	private List<EmissionTimeSeries> emisList = new ArrayList<EmissionTimeSeries>();
	private MeteorologyTimeSeries metList = new MeteorologyTimeSeries();
	private final CharSequence pointsXMarker = "xp";
	private final CharSequence pointsYMarker = "yp";
	private final CharSequence pointsHMarker = "hp";
	private final CharSequence gxMarker = "gx";
	private final CharSequence gyMarker = "gy";

	// NetCDF variables
	private final static String TIME_VAR_NAME = "time";
	private final static String MAIN_VAR_NAME = "PM10_Austal2000";
	private final static String MAIN_VAR_LONG_NAME = "Particulate matter smaller 10 um diameter";
	private final static String X_VAR_NAME = "x";
	private final static String Y_VAR_NAME = "y";
	private final static String UNITS_ATTR_NAME = "units";
	private final static String MISSING_VALUE_ATTR_NAME = "missing_value";
	private final static String LONG_NAME_ATTR_NAME = "long_name";
	//private final static String GRID_MAPPING_VAR_NAME = "gauss_krueger_3";
	private final static String GRID_MAPPING_VAR_NAME = "crs";
	private final static String GRID_MAPPING_ATTR_NAME = "grid_mapping";

	private final static String MAIN_VAR_UNITS = "ug m-3";
	private final static String X_VAR_UNITS = "m";
	private final static String Y_VAR_UNITS = "m";

	private final static String COORD_AXIS_ATTR_NAME = "_CoordinateAxisType";
	private final static String X_COORD_AXIS = "GeoX";
	private final static String Y_COORD_AXIS = "GeoY";

	// number of realisations
	private final int rn = 50;


	public static void main(String[] args) {
	//	DateTimeFormatter dateFormat = ISODateTimeFormat.dateTime();
	//	DateTimeFormatter dateFormat2 = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.000+01");
	//	String startTime = "2010-03-01T01:00:00.000+01";
	//	DateTime startDate = dateFormat.parseDateTime(startTime);
	//	DateTime startDate2 = dateFormat2.parseDateTime(startTime);

		try {
			AustalSetupControl control = new AustalSetupControl();
				// read files to create datamodel
				control.readFiles("austal2000_template.txt", "zeitreihe_template.dmna");

				// read Austal results
				// read hourly results
				//String hourlyFolder = "D:/PhD/WP1.1_AirQualityModel/WebServiceChain/AustalWPS/AustalRun/hourly";

				String startTime = "2010-01-01T01:00:00.000+01";
				DateTime startDate = ISODateTimeFormat.dateTime().parseDateTime(startTime);
				DateTime preStartDate = startDate.minusDays(1);

				String hourlyFolder = "K:\\repo\\AustalRun\\results\\"+startDate.toString("yyyy-MM")+"\\PO";
				AustalOutputReader outputReader = new AustalOutputReader();

				// ids of UBA stations
				int id0 = 4603;
				int id1 = 3942;

				// list of realisations of points with time series
				ArrayList<ArrayList<Point>> realisationsList = new ArrayList<ArrayList<Point>>();

				// Time series objects
//				ArrayList<ArrayList<Double>> stat0 = new ArrayList<ArrayList<Double>>();
//				ArrayList<ArrayList<Double>> stat1 = new ArrayList<ArrayList<Double>>();
//				ArrayList<DateTime> dates = new ArrayList<DateTime>();
				int start = Hours.hoursBetween(preStartDate, startDate).getHours();

				// loop through realisations
				for(int i=0; i<control.rn; i++){
					String folder = hourlyFolder + i;
					HashMap<Integer, ArrayList<Double>> valueMap = outputReader.readHourlyFiles(folder, 1, false);
					Point p0 = new Point(3404586,5756753,"p0");
					Point p1 = new Point(3405158,5758603,"p1");

					// loop through time series
					for(int j=start; j<=valueMap.size(); j++){
						ArrayList<Double> vals = valueMap.get(j);
						p0.addValue(startDate.plusHours(j).toString(ISODateTimeFormat.dateTime()), vals.get(id0));
						p1.addValue(startDate.plusHours(j).toString(ISODateTimeFormat.dateTime()), vals.get(id1));


//						// for the first realisations create ArrayList
//						if(i==0){
//
//							ArrayList<Double> realisations0 = new ArrayList<Double>();
//							realisations0.add(vals.get(id0));
//							stat0.add(realisations0);
//
//							ArrayList<Double> realisations1 = new ArrayList<Double>();
//							realisations1.add(vals.get(id1));
//							stat1.add(realisations1);
//
//							dates.add(startDate.plusHours(j));
//						}else{
//							stat0.get(i).add(vals.get(id0));
//							stat1.get(i).add(vals.get(id1));
//						}
					}
					ArrayList<Point> pointsTS = new ArrayList<Point>();
					pointsTS.add(p0);
					pointsTS.add(p1);

					realisationsList.add(pointsTS);
				}

				// make observations collections
				UncertaintyObservationCollection uobs = control.createRealisationsCollection(realisationsList, startDate, startDate.plusDays(31));

				// save result locally
				String resultsPath = "D:\\PhD\\WP1.1_AirQualityModel\\WebServiceChain\\results";
				String filepath = resultsPath + "\\Austal_"+startDate.toString("yyyy-MM")+".xml";
				File file = new File(filepath);

				// encode, store (for using in austal request later)
				try {
					new StaxObservationEncoder().encodeObservationCollection(uobs,file);
				} catch (OMEncodingException e) {
					e.printStackTrace();
				}

//				// read several files to have realisations
//				HashMap<Integer, HashMap<Integer, ArrayList<Double>>> realisationsMap = new HashMap<Integer, HashMap<Integer, ArrayList<Double>>>();
//				for(int i=0; i<control.rn; i++){
//					// for NetCDF creation
//					realisationsMap.put(i+1, outputReader.readHourlyFiles(hourlyFolder, 1, false));
//				}
//				DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
//				DateTime minDate = df.parseDateTime("2009-03-01 01:00:00");

//				// write results to NetCDF file
//				NetcdfUWFile resultFile = null;
//				try {
//					resultFile = outputReader.createNetCDFfile("C:/UncertWeb/Austal/austal.nc",
//							control.austal.getStudyArea().getXcoords(), control.austal.getStudyArea().getYcoords(),
//							minDate, 1, realisationsMap);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (NetcdfUWException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (InvalidRangeException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				NetCDFBinding uwData = new NetCDFBinding(resultFile);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	private UncertaintyObservationCollection createRealisationsCollection(
			ArrayList<ArrayList<Point>> realisationsList, DateTime startDate, DateTime endDate){
		// make uncertainty observation
		UncertaintyObservationCollection ucoll = new UncertaintyObservationCollection();
	//	AustalOutputReader austal = new AustalOutputReader();
	//	ArrayList<ArrayList<Point>> realisationsList = new ArrayList<ArrayList<Point>>();

		try{
			// get results for each realisation
//			for(int i=0; i<numberOfRealisations; i++){
//				// read results for receptor points and add them to an observation collection
//				String folder = resultsPath + "\\"+ startDate.toString("yyyy-MM") +"\\PO"+i;
//				ArrayList<Point> points = austal.readReceptorPointList(folder, false);
//				realisationsList.add(points);
//			}

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

		}catch(Exception e){
			e.printStackTrace();
		}

		return(ucoll);
	}



	public static void writeCSV(ArrayList<Double> dates, ArrayList<ArrayList<Double>> data, String filepath){
		try {
			 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filepath)));

			// write header
			out.write("Date");
			for(int i=0; i<data.get(0).size(); i++){
				out.write(", "+i);
			}
			out.newLine();

			// write each observation as one line
			for(int j=0; j<dates.size(); j++){
				out.write(dates.get(j)+"");
				ArrayList<Double> r = data.get(j);
				for(int i=0; i<r.size(); i++){
					out.write(", "+r.get(i));
				}
				out.newLine();
			}
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public AustalSetupControl() throws Exception{

	}

	private NetcdfUWFile writeNetCDFfile(String filepath, HashMap<Integer, HashMap<Integer, ArrayList<Double>>> realisationsMap){
		// get geometry for coordinates from study area
		int[] x = austal.getStudyArea().getXcoords();
		int[] y = austal.getStudyArea().getYcoords();
		ArrayInt xArray = new ArrayInt.D1(x.length);
		ArrayInt yArray = new ArrayInt.D1(y.length);

		// get dates for the time series results
		// format: "hours since 2011-01-02 00:00:00 00:00"
		DateTime minDate = ts.getMeteorologyTimeSeries().getMinDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String TIME_VAR_UNITS = "hours since "+dateFormat.format(minDate)+ " 00:00";
		int tn = realisationsMap.get(1).size();
		ArrayInt tArray = new ArrayInt.D1(tn);

		// fill arrays with values
		int xi=0;
		int yi=0;
		int r=0;
		int t=0;
		int c=0;

		for (xi = 0; xi < x.length; xi++) {
			xArray.setInt(xi, x[xi]);
		}
		for (yi = 0; yi < y.length; yi++) {
			yArray.setInt(yi, y[yi]);
		}
		for(t = 0; t < tn; t++){
			tArray.setInt(t, t+1);
		}

		// put all result arrays into a new NetCDF file
		// prepare new netCDF file for realisations
		NetcdfFileWriteable resultFile = null;
		NetcdfUWFileWriteable resultUWFile = null;
		try {
			resultFile = NetcdfUWFileWriteable.createNew(filepath, true);
			resultUWFile = new NetcdfUWFileWriteable(resultFile);

			// define dimensions
			Dimension xDim = new Dimension(X_VAR_NAME, x.length);
			Dimension yDim = new Dimension(Y_VAR_NAME, y.length);
			Dimension tDim = new Dimension(TIME_VAR_NAME, tn);

			if (!resultFile.isDefineMode()) {
				resultFile.setRedefineMode(true);
			}

			// A1) add dimensions to simple NetCDF file
			resultFile.addDimension(null, xDim);
			resultFile.addDimension(null, yDim);
			resultFile.addDimension(null, tDim);

			// A2) add dimensions as variables
			resultFile.addVariable(X_VAR_NAME, DataType.FLOAT,
					new Dimension[] {xDim });
			resultFile.addVariable(Y_VAR_NAME, DataType.FLOAT,
					new Dimension[] {yDim });
			resultFile.addVariable(TIME_VAR_NAME, DataType.FLOAT,
					new Dimension[] {tDim });

			// B1) add dimension variable attributes
			// units
			resultFile.addVariableAttribute(X_VAR_NAME,
					UNITS_ATTR_NAME, X_VAR_UNITS);
			resultFile.addVariableAttribute(Y_VAR_NAME,
					UNITS_ATTR_NAME, Y_VAR_UNITS);
			resultFile.addVariableAttribute(TIME_VAR_NAME,
					UNITS_ATTR_NAME, TIME_VAR_UNITS);

			// coordinate types
			resultFile.addVariableAttribute(X_VAR_NAME,
					COORD_AXIS_ATTR_NAME, X_COORD_AXIS);
			resultFile.addVariableAttribute(Y_VAR_NAME,
					COORD_AXIS_ATTR_NAME, Y_COORD_AXIS);


			//TODO: Additional lat, lon variables?
//		    x:standard_name = "projection_x_coordinate" ;
//		    y:standard_name = "projection_y_coordinate" ;

//		    double lat(y, x) ;
//		    double lon(y, x) ;

			// add CRS variable and attributes
			resultFile.addVariable(GRID_MAPPING_VAR_NAME, DataType.CHAR, new Dimension[] {});
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"grid_mapping_name", "gauss_krueger_3");
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"false_easting", "3500000");
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"longitude_of_projection_origin", "9");
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"false_northing", "0.0");
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"latitude_of_projection_origin", "0.0");
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"scale_factor_at_projection_origin", "1.0");
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"semi_major_axis", "6377397.155");
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"semi_minor_axis", "6356078.9628");
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"inverse_flattening", "299.1528");

//			crs:spatial_ref = \"EPSG\",\"3035\"

			// B2) write dimension arrays to file
			resultFile.setRedefineMode(false);
			resultFile.write(X_VAR_NAME, xArray);
			resultFile.write(Y_VAR_NAME, yArray);
			resultFile.write(TIME_VAR_NAME, tArray);

			// C1) additional information for NetCDF-U file
			// a) Set dimensions for value variable
			ArrayList<Dimension> dims = new ArrayList<Dimension>(2);
			dims.add(xDim);
			dims.add(yDim);
			dims.add(tDim);

			// b) add data variable with dimensions and attributes
			Variable dataVariable = resultUWFile.addSampleVariable(
					MAIN_VAR_NAME, DataType.DOUBLE, dims,
					UnknownSample.class, rn);

			dataVariable.addAttribute(new Attribute(UNITS_ATTR_NAME, MAIN_VAR_UNITS));
			dataVariable.addAttribute(new Attribute(MISSING_VALUE_ATTR_NAME, -9999F));
			dataVariable.addAttribute(new Attribute(LONG_NAME_ATTR_NAME, MAIN_VAR_LONG_NAME));
			// add attribute for grid mapping
			dataVariable.addAttribute(new Attribute(GRID_MAPPING_ATTR_NAME, GRID_MAPPING_VAR_NAME));

			// c) add Austal values to data Array
			// data array for PM10 values
			ArrayDouble dataArray = new ArrayDouble.D4(rn,
					xDim.getLength(), yDim.getLength(), tDim.getLength());
			Index dataIndex = dataArray.getIndex();

			// loop through realisations
			for(r = 0; r < rn; r++){
				HashMap<Integer, ArrayList<Double>> valueMap = realisationsMap.get(r+1);

				// loop through time steps
				for(t = 0; t < tDim.getLength(); t++){
					ArrayList<Double> currentVals = valueMap.get(t+1);
					c=0;

					// loop through cells
					for (yi = (yDim.getLength()-1); yi >=0; yi--) {
						for (xi = 0; xi < xDim.getLength(); xi++) {
							// set data for each realisation
							Double v = currentVals.get(c);
							dataIndex.set(r, xi, yi, t);
							dataArray.set(dataIndex, v);
							c++;
						}
					}
				}
			}


			//TODO: Makes sense?
			// add CF conventions
			resultUWFile.getNetcdfFileWritable().setRedefineMode(true);
			Attribute conventions = resultUWFile.getNetcdfFileWritable().findGlobalAttribute("Conventions");
			String newValue =  conventions.getStringValue() + " CF-1.5";
			resultUWFile.getNetcdfFileWritable().deleteGlobalAttribute("Conventions");
			resultUWFile.getNetcdfFileWritable().addGlobalAttribute("Conventions", newValue);

			// C2) write data array to NetCDF-U file
			resultUWFile.getNetcdfFileWritable().setRedefineMode(false);
			resultUWFile.getNetcdfFileWritable().write(
					MAIN_VAR_NAME, dataArray);
			resultUWFile.getNetcdfFile().close();


		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("y: "+yi+", x: "+xi+", t: "+t);
		}

		return resultUWFile;
	}


	private void handleReceptorPoints(List<ReceptorPoint> pointList,
			FeatureCollection<?, ?> featColl) {

		int gx = austal.getStudyArea().getGx();
		int gy = austal.getStudyArea().getGy();

		String xp = "";
		String yp = "";
		// String hp = "";

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

				for (int i = 0; i < lineString.getCoordinates().length; i++) {

					if (i == 20) {
						coordinateCount = 20;
						break;
					}
					Coordinate coord = lineString.getCoordinates()[i];

					xp = "" + (coord.x - gx);
					yp = "" + (coord.y - gy);

					ReceptorPoint rp = new ReceptorPoint(xp, yp, "2.0");

					pointList.add(rp);
				}
			} else if (feature.getDefaultGeometry() instanceof LineString) {

				LineString lineString = (LineString) feature
						.getDefaultGeometry();

				coordinateCount = lineString.getCoordinates().length;

				for (int i = 0; i < lineString.getCoordinates().length; i++) {

					if (i == 20) {
						coordinateCount = 20;
						break;
					}
					Coordinate coord = lineString.getCoordinates()[i];

					xp = "" + (coord.x - gx);
					yp = "" + (coord.y - gy);

					ReceptorPoint rp = new ReceptorPoint(xp, yp, "2.0");

					pointList.add(rp);
				}
			}

		}

	}

	private void handleMeteorology(MeteorologyTimeSeries newMetList, IObservationCollection coll) throws Exception{

		Map<TimeObject, AbstractObservation> timeObservationMap = new HashMap<TimeObject, AbstractObservation>();

		/*
		 * the O&M is structured in that way that at first the wind direction values are listed
		 * and second the wind speed values. each timestamp has a wind direction value and on wind speed value
		 */
		for (AbstractObservation abstractObservation : coll.getObservations()) {
			if(!timeObservationMap.containsKey(abstractObservation.getPhenomenonTime())){
				timeObservationMap.put(abstractObservation.getPhenomenonTime(), abstractObservation);
			}else{
				/*
				 * if the map already contains a observation we should now have both windspeed and winddirection
				 * either in the Observation already in the map or in the current abstractObservation
				 */
				AbstractObservation obs1 = timeObservationMap.get(abstractObservation.getPhenomenonTime());

				/*
				 * check for wind direction value
				 */
				Double windDirection = 0.0d;
				if(obs1.getObservedProperty().getPath().contains("winddirection")){
					windDirection = (Double)obs1.getResult().getValue();
				}else if(abstractObservation.getObservedProperty().getPath().contains("winddirection")){
					windDirection = (Double)abstractObservation.getResult().getValue();
				}
				/*
				 * check for wind sped value
				 */
				Double windSpeed = 0.0d;
				if(obs1.getObservedProperty().getPath().contains("windspeed")){
					windSpeed = (Double)obs1.getResult().getValue();
				}else if(abstractObservation.getObservedProperty().getPath().contains("windspeed")){
					windSpeed = (Double)abstractObservation.getResult().getValue();
				}

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
				newMetList.addWindDirection(dt, windDirection);
				newMetList.addWindSpeed(dt, windSpeed);

			}
		}
	}


	private void handleObservationCollection(ArrayList<EmissionSource> newEmissionSources, ArrayList<EmissionTimeSeries> newEmisTS, IObservationCollection coll) throws Exception {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");

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
//
//				EmissionTimeSeries polygonTS = new EmissionTimeSeries(polygon.getDynamicSourceID());	// assign id of respective source

				lineTS = new EmissionTimeSeries(counter);// assign id of respective source

				if (abstractObservation.getFeatureOfInterest().getShape() instanceof MultiLineString) {

					MultiLineString mline = (MultiLineString) abstractObservation
							.getFeatureOfInterest().getShape();

					Coordinate[] coords = mline.getCoordinates();
					/*
					 * create EmissionSource
					 */
					EmissionSource tmpEMS = lineGK3ToLocalCoords(gx, gy, coords[0].x, coords[0].y, coords[1].x, coords[1].y);
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
					EmissionSource tmpEMS = cellPolygonGK3ToLocalCoords(gx, gy, coords[2].x, coords[2].y, coords[4].x, coords[4].y);
					tmpEMS.setDynamicSourceID(counter);
					newEmissionSources.add(tmpEMS);
//					System.out.println(tmpEMS);
				}

				counter++;
			} else {
				if (!spsam.equals(abstractObservation.getFeatureOfInterest())) {
					newEmisTS.add(lineTS);
					lineTS = new EmissionTimeSeries(counter);// assign id of respective source
					spsam = abstractObservation.getFeatureOfInterest();
//					System.out.println(counter);
//					counter++;
					if (abstractObservation.getFeatureOfInterest().getShape() instanceof MultiLineString) {

						MultiLineString mline = (MultiLineString) abstractObservation
								.getFeatureOfInterest().getShape();

						Coordinate[] coords = mline.getCoordinates();
						EmissionSource tmpEMS = lineGK3ToLocalCoords(gx, gy, coords[0].x, coords[0].y, coords[1].x, coords[1].y);
						tmpEMS.setDynamicSourceID(counter);
						newEmissionSources.add(tmpEMS);
//						System.out.println(tmpEMS);
					} else if (abstractObservation.getFeatureOfInterest()
							.getShape() instanceof MultiPolygon) {

						MultiPolygon mpoly = (MultiPolygon) abstractObservation
								.getFeatureOfInterest().getShape();

						Coordinate[] coords = mpoly.getCoordinates();

						EmissionSource tmpEMS = cellPolygonGK3ToLocalCoords(gx, gy, coords[2].x, coords[2].y, coords[4].x, coords[4].y);
						tmpEMS.setDynamicSourceID(counter);
						newEmissionSources.add(tmpEMS);
//						System.out.println(tmpEMS);
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

			/*
			 * if exact date format needed
			 */
//			Date timeStamp = null;
//			try {
//				timeStamp = dateFormat.parse(dt.toString("yyyy-MM-dd.HH:mm:ss"));
//			} catch (ParseException e) {
//				e.printStackTrace();
//			}
			lineTS.addEmissionValue(dt, (Double)o);

		}
		/*
		 * add last EmissionTimeSeries
		 */
		newEmisTS.add(lineTS);
	}


	private void readFiles(String austalFileName, String zeitreiheFileName){
		// read austal2000.txt
		File austalFile = new File(FILE_PATH+"//"+ austalFileName);
		austal = new Austal2000Txt(austalFile);

		// read zeitreihe.dmna
		File tsFile = new File(FILE_PATH+"//"+zeitreiheFileName);
		ts = new Zeitreihe(tsFile);
	}

	private void writeFiles(String austalFileName, String zeitreiheFileName){
		// test writer
		File new_austalFile = new File(FILE_PATH+"//"+austalFileName);
		austal.writeFile(new_austalFile);
		File new_tsFile = new File(FILE_PATH+"//"+zeitreiheFileName);
		ts.writeFile(new_tsFile);
	}

	// methods to calculate Gauss-Krüger-Coordinates to local austal coordinates
	private EmissionSource lineGK3ToLocalCoords(double gx, double gy, double x1, double y1, double x2, double y2){

		EmissionSource source = new EmissionSource();
		double xq, yq, wq;
		double bq = Math.sqrt(Math.pow((x1-x2), 2)+Math.pow((y1-y2), 2)); // extension in y direction = length
		double aq=0;					// extension in x direction
		double cq=1;					// extension in z direction
		double hq=0.2;					// height

		// find point to the left which will stay fixed
		if(x1==x2){ // easiest case
			// convert to local coordinates
			xq = x1 - gx;
			yq = y1 - gy;
			wq = 0;					// angle
		} else if(x1<x2){
			// convert to local coordinates
			xq = x1 - gx;
			yq = y1 - gy;
			if(y1>y2)
				wq = 180 - Math.atan(Math.abs(x1-x2)/Math.abs(y1-y2))*180/Math.PI;
			else
				wq = Math.atan(Math.abs(x1-x2)/Math.abs(y1-y2))*180/Math.PI;

		}else{
			// convert to local coordinates
			xq = x2 - gx;
			yq = y2 - gy;
			if(y2>y1)
				wq = 180 - Math.atan(Math.abs(x1-x2)/Math.abs(y1-y2))*180/Math.PI;
			else
				wq = Math.atan(Math.abs(x1-x2)/Math.abs(y1-y2))*180/Math.PI;
		}

		source.setCoordinates(xq, yq);
		source.setExtent(aq, bq, cq, wq, hq);
		return source;
	}

	// method to calculate Gauss-Krüger-Coordinates to local austal coordinates
	private EmissionSource cellPolygonGK3ToLocalCoords(double gx, double gy, double x1, double y1, double x2, double y2){
			EmissionSource source = new EmissionSource();
			double xq, yq;
			double bq = Math.abs(y1-y2); 	// extension in y direction
			double aq = Math.abs(x1-x2);	// extension in x direction
			double cq=1;					// extension in z direction
			double hq=0.2;					// height
			double wq = 0;					// this is zero for our case

			// get lower left point which will stay fixed
			if(x1<x2)
				xq = x1 - gx;
			else
				xq = x2 - gx;

			if(y1<y2)
				yq = y1 - gy;
			else
				yq = y2 - gy;

			source.setCoordinates(xq, yq);
			source.setExtent(aq, bq, cq, wq, hq);
			return source;
	}




}
