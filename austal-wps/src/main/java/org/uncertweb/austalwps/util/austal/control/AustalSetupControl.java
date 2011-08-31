package org.uncertweb.austalwps.util.austal.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.austalwps.util.austal.files.Austal2000Txt;
import org.uncertweb.austalwps.util.austal.files.Zeitreihe;
import org.uncertweb.austalwps.util.austal.geometry.EmissionSource;
import org.uncertweb.austalwps.util.austal.timeseries.EmissionTimeSeries;
import org.uncertweb.austalwps.util.austal.timeseries.MeteorologyTimeSeries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;

// class to manage Austal Setup

public class AustalSetupControl {

	private static final String FILE_PATH="C:\\UncertWeb\\workspace\\AustalWPS\\src\\test\\resources\\";
	
	// general Austal objects
	private Austal2000Txt austal;
	private Zeitreihe ts;
	//private StudyArea studyArea;
	//private List<ReceptorPoint> receptorPoints;	
	
	// Austal objects that need to be changed
	private List<EmissionSource> emissionSources;
	private List<EmissionTimeSeries> emisList = new ArrayList<EmissionTimeSeries>();
	private MeteorologyTimeSeries metList = new MeteorologyTimeSeries();
	
	public static void main(String[] args) {
		AustalSetupControl control = new AustalSetupControl();
	}
	
	public AustalSetupControl(){
		// read files to create datamodel
//		this.readFiles();
			// 1. read files to create datamodel
				this.readFiles("austal2000_template.txt", "zeitreihe_0810.dmna");
				
			// 2. create new timeseries from O&M documents
			// 2a. Emission Sources	
				// make an ArrayList with emission sources
				ArrayList<EmissionSource> newEmissionSources = new ArrayList<EmissionSource>();
		
//				// Austal coordinates for line sources
//				EmissionSource line = lineGK3ToLocalCoords(3405540, 5758268, 3401540, 5758268, 3400540, 5759268);
//				line.setDynamicSourceID(1);	// IMPORTANT: id needs to start with 1!		
//				
//				// Austal coordinates for polygon sources		
//				EmissionSource polygon = cellPolygonGK3ToLocalCoords(3405540, 5758268, 3401540, 5758268, 3400540, 5759268); // coordinates are from lower left and upper right corner
//				polygon.setDynamicSourceID(2);
//					
//				// add sources to sources list
//				newEmissionSources.add(line);
//				newEmissionSources.add(polygon);
				
			// 2b. Emission TimeSeries
				// make an ArrayList with the respective timeseries per source
				ArrayList<EmissionTimeSeries> newEmisTS = new ArrayList<EmissionTimeSeries>();

				
				try {
					handleObservationCollection(newEmissionSources, newEmisTS);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// Add Emission TimeSeries per source
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
				Date timeStamp = null;
				try {
					timeStamp = dateFormat.parse("2010-03-01.01:00:00");
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				
				
//				EmissionTimeSeries lineTS = new EmissionTimeSeries(line.getDynamicSourceID());	// assign id of respective source	
//				lineTS.addEmissionValue(timeStamp, 0.5);	// make a loop to add all observations
//				
//				EmissionTimeSeries polygonTS = new EmissionTimeSeries(polygon.getDynamicSourceID());	// assign id of respective source
//				polygonTS.addEmissionValue(timeStamp, 0.1);	// make a loop to add all observations
//				
//				// add final timeseries to timeseries list
//				newEmisTS.add(lineTS);
//				newEmisTS.add(polygonTS);
				
			// 2b. Meteorology TimeSeries
				MeteorologyTimeSeries newMetList = new MeteorologyTimeSeries();
				
				
				try {
					handleMeteorology(newMetList);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
//				newMetList.addWindDirection(timeStamp, (double)220);
//				newMetList.addWindSpeed(timeStamp, 2.5);
//				
//				
//			// 3. substitute emissions and meteorology with new data
				substituteStreetEmissions(newEmissionSources, newEmisTS);
				substituteMeteoorology(newMetList);
//
//				// if necessary, cut all timeperiods to new one
//				//ts.setTimePeriod(startDate, endDate);
//				
//				// set or unset os parameter (will cause that the grid results are written for each hour)
//				
//				
//			// 4. write files
				writeFiles("austal2000.txt", "zeitreihe.dmna");
				
			// 5. run Austal
				
				
			// 6. read Austal results
				
			}
		
	
	private void handleMeteorology(MeteorologyTimeSeries newMetList) throws Exception{
		/*
		 * read meteo data
		 */
		BufferedReader bread = new BufferedReader(
				new FileReader(
						new File(
								"C:\\UncertWeb\\workspace\\AustalWPS\\src\\test\\resources\\xml\\Meteo1.xml")));

		String xmlString = "";

		String line = bread.readLine();

		xmlString = xmlString.concat(line);

		while ((line = bread.readLine()) != null) {
			xmlString = xmlString.concat(line);
		}
		
		XBObservationParser parser = new XBObservationParser();

		IObservationCollection coll = parser
				.parseObservationCollection(xmlString);
		
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
	
	
	private void handleObservationCollection(ArrayList<EmissionSource> newEmissionSources, ArrayList<EmissionTimeSeries> newEmisTS) throws Exception {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
		
		int gx = austal.getStudyArea().getGx();
		int gy = austal.getStudyArea().getGy();

		BufferedReader bread = new BufferedReader(
				new FileReader(
						new File(
								"C:\\UncertWeb\\workspace\\AustalWPS\\src\\test\\resources\\xml\\Streets1.xml")));

		String xmlString = "";

		String line = bread.readLine();

		xmlString = xmlString.concat(line);

		while ((line = bread.readLine()) != null) {
			xmlString = xmlString.concat(line);
		}

		XBObservationParser parser = new XBObservationParser();

		IObservationCollection coll = parser
				.parseObservationCollection(xmlString);

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
					EmissionSource tmpEMS = cellPolygonGK3ToLocalCoords(gx, gy, coords[1].x, coords[1].y, coords[3].x, coords[3].y);
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
						
						EmissionSource tmpEMS = cellPolygonGK3ToLocalCoords(gx, gy, coords[1].x, coords[1].y, coords[3].x, coords[3].y);
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
		
		// find point to the right which will stay fixed
		if(x1==x2){ // easiest case
			// convert to local coordinates
			xq = x1 - gx;
			yq = y1 - gy;		
			wq = 0;					// angle						
		} else if(x1>x2){	
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
