package org.uncertweb.austalwps.util.austal.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
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
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.uncertml.sample.RandomSample;
import org.uncertml.sample.Realisation;
import org.uncertml.sample.UnknownSample;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.austalwps.util.AustalOutputReader;
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
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;

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
	private final static String REALISATION_VAR_NAME = "realisations";
	private final static String UNITS_ATTR_NAME = "units";
	private final static String MISSING_VALUE_ATTR_NAME = "missing_value";
	private final static String LONG_NAME_ATTR_NAME = "long_name";
	
	private final static String MAIN_VAR_UNITS = "ug m-3";
	private final static String X_VAR_UNITS = "m";
	private final static String Y_VAR_UNITS = "m";
	private final static String REALISATION_VAR_UNITS = "count";
	
	private final static String COORDINATE_SYSTEM = "ProjectionCoordinateSystem";
	private final static String COORD_AXIS_ATTR_NAME = "_CoordinateAxisType";
	private final static String X_COORD_AXIS = "GeoX";
	private final static String Y_COORD_AXIS = "GeoY";
	
	private final int rn = 1;
	
	
	public static void main(String[] args) {
		try {
			AustalSetupControl control = new AustalSetupControl();
			// read files to create datamodel
//			this.readFiles();
				// 1. read files to create datamodel
					control.readFiles("austal2000_old.txt", "zeitreihe_old.dmna");
					
				// 2. create new timeseries from O&M documents
				// 2a. Emission Sources	
					// make an ArrayList with emission sources
//					ArrayList<EmissionSource> newEmissionSources = new ArrayList<EmissionSource>();
			
//					// Austal coordinates for line sources
//					EmissionSource line = lineGK3ToLocalCoords(3405540, 5758268, 3401540, 5758268, 3400540, 5759268);
//					line.setDynamicSourceID(1);	// IMPORTANT: id needs to start with 1!		
//					
//					// Austal coordinates for polygon sources		
//					EmissionSource polygon = cellPolygonGK3ToLocalCoords(3405540, 5758268, 3401540, 5758268, 3400540, 5759268); // coordinates are from lower left and upper right corner
//					polygon.setDynamicSourceID(2);
//						
//					// add sources to sources list
//					newEmissionSources.add(line);
//					newEmissionSources.add(polygon);
					
				// 2b. Emission TimeSeries
					// make an ArrayList with the respective timeseries per source
					
//					BufferedReader bread = new BufferedReader(
//							new FileReader(
//									new File(
//											"C:\\UncertWeb\\workspace\\AustalWPS\\src\\test\\resources\\xml\\Streets1.xml")));
//
//					String xmlString = "";
//
//					String line = bread.readLine();
//
//					xmlString = xmlString.concat(line);
//
//					while ((line = bread.readLine()) != null) {
//						xmlString = xmlString.concat(line);
//					}
//
//					XBObservationParser parser = new XBObservationParser();
//
//					IObservationCollection coll = parser
//							.parseObservationCollection(xmlString);
//					
//					
//					ArrayList<EmissionTimeSeries> newEmisTS = new ArrayList<EmissionTimeSeries>();
//
//					
//					try {
//						control.handleObservationCollection(newEmissionSources, newEmisTS, coll);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					
//					// Add Emission TimeSeries per source
//					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
//					Date timeStamp = null;
//					try {
//						timeStamp = dateFormat.parse("2010-03-01.01:00:00");
//					} catch (ParseException e) {
//						e.printStackTrace();
//					}
					
					
					
//					EmissionTimeSeries lineTS = new EmissionTimeSeries(line.getDynamicSourceID());	// assign id of respective source	
//					lineTS.addEmissionValue(timeStamp, 0.5);	// make a loop to add all observations
//					
//					EmissionTimeSeries polygonTS = new EmissionTimeSeries(polygon.getDynamicSourceID());	// assign id of respective source
//					polygonTS.addEmissionValue(timeStamp, 0.1);	// make a loop to add all observations
//					
//					// add final timeseries to timeseries list
//					newEmisTS.add(lineTS);
//					newEmisTS.add(polygonTS);
					
				// 2b. Meteorology TimeSeries
//					MeteorologyTimeSeries newMetList = new MeteorologyTimeSeries();
//					
//					/*
//					 * read meteo data
//					 */
//					bread = new BufferedReader(
//							new FileReader(
//									new File(
//											"C:\\UncertWeb\\workspace\\AustalWPS\\src\\test\\resources\\xml\\Meteo1.xml")));
//
//					xmlString = "";
//
//					line = bread.readLine();
//
//					xmlString = xmlString.concat(line);
//
//					while ((line = bread.readLine()) != null) {
//						xmlString = xmlString.concat(line);
//					}
//					coll = parser
//							.parseObservationCollection(xmlString);				
//					
//					try {
//						
//						
//						
//						control.handleMeteorology(newMetList, coll);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
					
//					newMetList.addWindDirection(timeStamp, (double)220);
//					newMetList.addWindSpeed(timeStamp, 2.5);
//					
//					
//				// 3. substitute emissions and meteorology with new data
//					control.substituteStreetEmissions(newEmissionSources, newEmisTS);
//					control.substituteMeteoorology(newMetList);
	//
//					// if necessary, cut all timeperiods to new one
//					//ts.setTimePeriod(startDate, endDate);
//					
//					// set or unset os parameter (will cause that the grid results are written for each hour)
//					
//					
//				// 4. write files
//					control.writeFiles("austal2000.txt", "zeitreihe.dmna");
					
				// 5. run Austal
					
					
				// 6. read Austal results
				// read hourly results
				//String hourlyFolder = "D:/PhD/WP1.1_AirQualityModel/WebServiceChain/AustalWPS/AustalRun/hourly";				
//				String hourlyFolder = "C:/UncertWeb/Austal/hourly";	
//				AustalOutputReader outputReader = new AustalOutputReader();
//				HashMap<Integer, ArrayList<Double>> valueMap = outputReader.readHourlyFiles(hourlyFolder, false);
//					
//				// write results to NetCDF file
//				control.writeNetCDFfile("C:/UncertWeb/Austal/test.nc", valueMap);
					
				// Austal coordinates for line sources
				int[] coords1 = {3410874, 5761616, 3410902, 5761614};
				int[] coords2 = {3409650, 5766571, 3409735, 5766464};
				EmissionSource line1 = control.lineGK3ToLocalCoords(control.austal.getStudyArea().getGx(), control.austal.getStudyArea().getGy(), coords1[0], coords1[1], coords1[2], coords1[3]);
				line1.setDynamicSourceID(1);	// IMPORTANT: id needs to start with 1!		
					
				EmissionSource line2 = control.lineGK3ToLocalCoords(control.austal.getStudyArea().getGx(), control.austal.getStudyArea().getGy(), coords2[0], coords2[1], coords2[2], coords2[3]);
				line2.setDynamicSourceID(2);	// IMPORTANT: id needs to start with 1!		
					
					
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	public AustalSetupControl() throws Exception{
	
	}
		
	private NetcdfUWFile writeNetCDFfile(String filepath, HashMap<Integer, ArrayList<Double>> valueMap){		
		// get geometry for coordinates from study area
		int[] x = austal.getStudyArea().getXcoords();
		int[] y = austal.getStudyArea().getYcoords();
		ArrayInt xArray = new ArrayInt.D1(x.length);
		ArrayInt yArray = new ArrayInt.D1(y.length);
		
		//TODO: Also add lat and lon variables?
		//TODO: Where to define coordinate system?		
		
		// get dates for the time series results
		// format: "hours since 2011-01-02 00:00:00 00:00"
		Date minDate = ts.getMeteorologyTimeSeries().getMinDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
		String TIME_VAR_UNITS = "hours since "+dateFormat.format(minDate)+ " 00:00";
		int tn = valueMap.size();
		ArrayInt tArray = new ArrayInt.D1(tn);
		
		// realisations
		ArrayInt rArray = new ArrayInt.D1(rn);
		
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
			Dimension rDim = new Dimension(REALISATION_VAR_NAME, rn);
			
			if (!resultFile.isDefineMode()) {
				resultFile.setRedefineMode(true);
			}
			
			// A1) add dimensions to simple NetCDF file
			resultFile.addDimension(null, xDim);
			resultFile.addDimension(null, yDim);
			resultFile.addDimension(null, tDim);
		//	resultFile.addDimension(null, rDim);

			// A2) add dimensions as variables
			resultFile.addVariable(X_VAR_NAME, DataType.FLOAT,
					new Dimension[] {xDim });
			resultFile.addVariable(Y_VAR_NAME, DataType.FLOAT,
					new Dimension[] {yDim });
			resultFile.addVariable(TIME_VAR_NAME, DataType.FLOAT,
					new Dimension[] {tDim });
		//	resultFile.addVariable(REALISATION_VAR_NAME, DataType.FLOAT,
		//			new Dimension[] {rDim });
			
			// B1) add dimension variable attributes
			// a) units
			resultFile.addVariableAttribute(X_VAR_NAME,
					UNITS_ATTR_NAME, X_VAR_UNITS);
			resultFile.addVariableAttribute(Y_VAR_NAME,
					UNITS_ATTR_NAME, Y_VAR_UNITS);
			resultFile.addVariableAttribute(TIME_VAR_NAME,
					UNITS_ATTR_NAME, TIME_VAR_UNITS);
		//	resultFile.addVariableAttribute(REALISATION_VAR_NAME,
		//			UNITS_ATTR_NAME, REALISATION_VAR_UNITS);
			
			// b) coordinate types
			resultFile.addVariableAttribute(X_VAR_NAME,
					COORD_AXIS_ATTR_NAME, X_COORD_AXIS);
			resultFile.addVariableAttribute(Y_VAR_NAME,
					COORD_AXIS_ATTR_NAME, Y_COORD_AXIS);	
			resultFile.setRedefineMode(false);			

			// B2) write dimension arrays to file
			resultFile.write(X_VAR_NAME, xArray);
			resultFile.write(Y_VAR_NAME, yArray);
			resultFile.write(TIME_VAR_NAME, tArray);
		//	resultFile.write(REALISATION_VAR_NAME, rArray);
			
			// C1) additional information for NetCDF-U file
			// a) Set dimensions for value variable			
			ArrayList<Dimension> dims = new ArrayList<Dimension>(2);
			dims.add(xDim);
			dims.add(yDim);
			dims.add(tDim);
			//dims.add(rDim);
			
			// b) add data variable with dimensions and attributes 
			//resultUWFile.getNetcdfFileWritable().setRedefineMode(true);
			Variable dataVariable = resultUWFile.addSampleVariable(
					MAIN_VAR_NAME, DataType.DOUBLE, dims,
					UnknownSample.class, rn);
			Attribute data_units = new Attribute(UNITS_ATTR_NAME, MAIN_VAR_UNITS);
			Attribute data_missingValue = new Attribute(MISSING_VALUE_ATTR_NAME, -9999F);
			Attribute data_longName = new Attribute(LONG_NAME_ATTR_NAME, MAIN_VAR_LONG_NAME);
						
			dataVariable.addAttribute(data_units);
			dataVariable.addAttribute(data_missingValue);
			dataVariable.addAttribute(data_longName);
	
			// data array for PM10 values
			ArrayDouble dataArray = new ArrayDouble.D4(rDim.getLength(),
					xDim.getLength(), yDim.getLength(), tDim.getLength());
			
			// c) add Austal values to data Array		
			Index dataIndex = dataArray.getIndex();
			
			// loop through time steps
			for(t = 0; t < tDim.getLength(); t++){
				ArrayList<Double> currentVals = valueMap.get(t+1);
				c=0;
				
				// loop through cells
				for (yi = (yDim.getLength()-1); yi >=0; yi--) {
					for (xi = 0; xi < xDim.getLength(); xi++) {					
						Double v = currentVals.get(c);

						// set data for each realisation
						for (r = 0; r < rn; r++) {
							dataIndex.set(r, xi, yi, t);
							dataArray.set(dataIndex, v);
						}
						c++;
					}
					//c++;
				}
			}
			
			// C2) write data array to NetCDF-U file
			resultUWFile.getNetcdfFileWritable().setRedefineMode(false);
			resultUWFile.getNetcdfFileWritable().write(
					MAIN_VAR_NAME, dataArray);
			resultUWFile.getNetcdfFile().close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
				newMetList.addWindDirection(dt.toDate(), windDirection);
				newMetList.addWindSpeed(dt.toDate(), windSpeed);
				
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
			lineTS.addEmissionValue(dt.toDate(), (Double)o);

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
			double wq = 0;					//TODO: This is zero for our case
			
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
	
	
	private void substituteStreetEmissions(ArrayList<EmissionSource> newEmissionSources, ArrayList<EmissionTimeSeries> newEmisTS){
		// get old emission lists
		ArrayList<EmissionSource> emissionSources = (ArrayList) austal.getEmissionSources();
		ArrayList<EmissionTimeSeries> emisList = (ArrayList) ts.getEmissionSourcesTimeSeries();
		int newID = newEmissionSources.size()+1;
		
		//get length o
		Date minDate = newEmisTS.get(0).getMinDate();
		Date maxDate = newEmisTS.get(0).getMaxDate();
		
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
					Date min = ets.getMinDate();
					Date max = ets.getMaxDate();
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
	
	private void substituteMeteoorology(MeteorologyTimeSeries newMetList){
		// get old meteorology list
		MeteorologyTimeSeries metList = ts.getMeteorologyTimeSeries();
		ArrayList<Date> timeStampList = (ArrayList) newMetList.getTimeStamps();
		
		// add stability class values to new list
		for(int i=0; i<timeStampList.size(); i++){
			Date d = timeStampList.get(i);
			newMetList.addStabilityClass(d, metList.getStabilityClass(d));
		}
		
		// finally add new list to ts object
		ts.setMeteorologyTimeSeries(newMetList);
	}
	
	private void readAustalResults(){
		
	}

}
