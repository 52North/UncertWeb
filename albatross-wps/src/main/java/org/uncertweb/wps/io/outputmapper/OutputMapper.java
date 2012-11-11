package org.uncertweb.wps.io.outputmapper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.joda.time.DateTime;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.TextObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.TextResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.wps.AlbatrossProcess;

import com.vividsolutions.jts.geom.Geometry;

/**
 * class maps Albatross outputs to U-O&M format
 * 
 * @author staschc
 *
 */
public class OutputMapper {
	
	protected static Logger log = Logger.getLogger(AlbatrossProcess.class);
	
	/**
	 * contains the observed properties
	 * 
	 * @author staschc
	 *
	 */
	abstract class ObservedProperties{
		public static final String MOVEMENT = "http://www.uncertweb.org/variables/albatross/movement";
		public static final String NUM_OF_TOURS_TOTAL = "http://www.uncertweb.org/variables/albatross/totalNumberOfTours";
		public static final String NUM_OF_PEOPLE_TOTAL= "http://www.uncertweb.org/variables/albatross/totalNumberOfPeople";
		public static final String NUM_OF_TRIPS_TOTAL = "http://www.uncertweb.org/variables/albatross/totalNumberOfTrips";
		public static final String DIST_TRAVELED_TOTAL = "http://www.uncertweb.org/variables/albatross/totalDistanceTraveled";
		public static final String RATIO_TRIPS_TOURS = "http://www.uncertweb.org/variables/albatross/ratioTripsTours";
		
		//Car Drivers
		public static final String NUM_OF_TRIPS_CAR_DRIVER = "http://www.uncertweb.org/variables/albatross/numberOfTripsCarDrivers";
		public static final String DIST_TRAVELED_CAR_DRIVER = "http://www.uncertweb.org/variables/albatross/distanceTraveledCarDrivers";
		
		//Slow Mode (Walk or Bike)
		public static final String NUM_OF_TRIPS_SLOW_MODE = "http://www.uncertweb.org/variables/albatross/numberOfTripsSlowMode";
		public static final String DIST_TRAVELED_SLOW_MODE = "http://www.uncertweb.org/variables/albatross/distanceTraveledSlowMode";
		
		//Public Transport
		public static final String NUM_OF_TRIPS_PUB_TRANSPORT = "http://www.uncertweb.org/variables/albatross/numberOfTripsPublicTransport";
		public static final String DIST_TRAVELED_PUB_TRANSPORT = "http://www.uncertweb.org/variables/albatross/distanceTraveledPublicTransport";
		
		//Car Passengers
		public static final String NUM_OF_TRIPS_CAR_PASSENGERS = "http://www.uncertweb.org/variables/albatross/numberOfTripsCarPassengers";
		public static final String DIST_TRAVELED_CAR_PASSENGERS = "http://www.uncertweb.org/variables/albatross/distanceTraveledCarPassengers";
		
		
	}
	
	/**
	 * identifier for the Albatross model
	 */
	public static final String PROC_ID_ALBATROSS = "http://www.uncertweb.org/models/albatross";
	
	/**
	 * maps the O-D-Matrix output to an om:TextObservation with O-D-matrix as result value
	 * 
	 * @param absoluteFilePath
	 * 			absolute path to output file of Albatros model (OUT_odmatrix.csv)
	 * @return observation collection that contains the observations
	 * @throws OMEncodingException
	 * 			if the file cannot be found or an error occured during parsing
	 */
	public IObservationCollection encodeODMatrix(String absoluteFilePath) throws OMEncodingException{
		IObservationCollection result = null;
		try {
			TextResult tr = new TextResult(readFileAsString(absoluteFilePath));
			SpatialSamplingFeature ssf = new SpatialSamplingFeature(new Identifier(new URI("http://www.uncertweb.org"),"Netherlands"),null, getRotterdamGeometry());
			TimeObject phenTime = new TimeObject("h03/h20");
			TimeObject resultTime = new TimeObject(new DateTime());
			URI observedProperty = new URI(ObservedProperties.MOVEMENT);
			URI procedure = new URI(PROC_ID_ALBATROSS);
			
			TextObservation to = new TextObservation(phenTime,resultTime,procedure,observedProperty,ssf,tr);
			result = new TextObservationCollection();
			result.addObservation(to);		
		} catch (Exception e){
			throw new OMEncodingException(e.getLocalizedMessage());
		}
		
		return result;
	}

	/**
	 * maps the O-D-Matrix output to an om:TextObservation with O-D-matrix as result value
	 * 
	 * @param file
	 * 			output file of Albatros model (OUT_indicators.csv)
	 * @return absolute path to observation collection that contains the observations
	 * @throws OMEncodingException 
	 */
	public IObservationCollection encodeIndicators(String absoluteFilePath) throws OMEncodingException{
		IObservationCollection result = new MeasurementCollection();
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		try {
			
			//load observation Properties
			SpatialSamplingFeature ssf = new SpatialSamplingFeature(new Identifier(new URI("http://www.uncertweb.org"),"Netherlands"),null, getNLGeometry());
			URI procedure = new URI(PROC_ID_ALBATROSS);
			TimeObject phenTime = new TimeObject("h03/h20");
			TimeObject resultTime = new TimeObject(new DateTime());
			URI obsProp = null;
			MeasureResult obsResult = null;
			
			fstream = new FileInputStream(absoluteFilePath);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				String[] lineParts = strLine.split("\t");
				//jump over empty lines and over line declaring mode columns
				if (!lineParts[0].equals("\"\"")&&!lineParts[0].equals("\"mode\"")){
					
					if (lineParts.length==2){
						obsProp = getObsProp4Identifier(lineParts[0]);
						//set uom property; empty for all counts, km for distances
						String uom="";
						if (obsProp.toString().contains("Distance")){
							uom="km";
						}
						String resultString = lineParts[1].replace("\"", "").trim();
						double resultValue = Double.parseDouble(resultString);
						obsResult = new MeasureResult(resultValue,uom);
						result.addObservation(new Measurement(phenTime,resultTime,procedure,obsProp,ssf,obsResult));
					}
					else if (lineParts.length==3){
						
						//check mode at first
						String mode=lineParts[0];
						String distResult = lineParts[1].replace("\"","").trim();
						String numTripsResult = lineParts[2].replace("\"","").trim();
						
						//mode is Cardriver
						if (mode.equals("\"0\"")){
							obsProp = new URI(ObservedProperties.DIST_TRAVELED_CAR_DRIVER);
							double resultValue = Double.parseDouble(distResult);
							obsResult = new MeasureResult(resultValue,"km");
							result.addObservation(new Measurement(phenTime,resultTime,procedure,obsProp,ssf,obsResult));
							
							obsProp = new URI(ObservedProperties.NUM_OF_TRIPS_CAR_DRIVER);
							resultValue  = Double.parseDouble(numTripsResult);
							obsResult = new MeasureResult(resultValue,"");
						}
						
						//mode is slow mode (walk or bike)
						else if (mode.equals("\"1\"")){
							obsProp = new URI(ObservedProperties.DIST_TRAVELED_SLOW_MODE);
							double resultValue = Double.parseDouble(distResult);
							obsResult = new MeasureResult(resultValue,"km");
							result.addObservation(new Measurement(phenTime,resultTime,procedure,obsProp,ssf,obsResult));
							
							obsProp = new URI(ObservedProperties.NUM_OF_TRIPS_SLOW_MODE);
							resultValue  = Double.parseDouble(numTripsResult);
							obsResult = new MeasureResult(resultValue,"");
						}
						
						//mode is public transport
						else if (mode.equals("\"2\"")){
							obsProp = new URI(ObservedProperties.DIST_TRAVELED_PUB_TRANSPORT);
							double resultValue = Double.parseDouble(distResult);
							obsResult = new MeasureResult(resultValue,"km");
							result.addObservation(new Measurement(phenTime,resultTime,procedure,obsProp,ssf,obsResult));
							
							obsProp = new URI(ObservedProperties.NUM_OF_TRIPS_PUB_TRANSPORT);
							resultValue  = Double.parseDouble(numTripsResult);
							obsResult = new MeasureResult(resultValue,"");
						}
						
						//mode is car passenger
						else if (mode.equals("\"3\"")){
							obsProp = new URI(ObservedProperties.DIST_TRAVELED_CAR_PASSENGERS);
							double resultValue = Double.parseDouble(distResult);
							obsResult = new MeasureResult(resultValue,"km");
							result.addObservation(new Measurement(phenTime,resultTime,procedure,obsProp,ssf,obsResult));
							
							obsProp = new URI(ObservedProperties.NUM_OF_TRIPS_CAR_PASSENGERS);
							resultValue  = Double.parseDouble(numTripsResult);
							obsResult = new MeasureResult(resultValue,"");
						}
						
						
					}
				}
			}
	} catch (FileNotFoundException e) {
		throw new OMEncodingException("Outputfile OUT_indicators.csv cannot be found!");
	} catch (IOException e) {
		throw new OMEncodingException("Outputfile OUT_indicators.csv cannot be found!");
	} catch (IllegalArgumentException e) {
		throw new OMEncodingException("Error while encoding ");
	} catch (URISyntaxException e) {
		throw new OMEncodingException("Outputfile OUT_indicators.csv cannot be found!");
	}
		finally{
			try {
				br.close();
				in.close();
				fstream.close();
				
			} catch (IOException e) {
				log.info("Error while closing streams from OUT_indicators.csv file during postprocessing.");
			}
			
		}
		return result;
	}


	private URI getObsProp4Identifier(String string) throws URISyntaxException {
		if (string.equals("\"The number of tours \"")){
			return new URI(ObservedProperties.NUM_OF_TOURS_TOTAL);
		}
		else if (string.equals("\"The number of people \"")){
			return new URI(ObservedProperties.NUM_OF_PEOPLE_TOTAL);
		}
		else if (string.equals("\"The number of trips \"")){
			return new URI(ObservedProperties.NUM_OF_TRIPS_TOTAL);
		}
		else if (string.equals("\"Ratio trips/tours \"")){
			return new URI(ObservedProperties.RATIO_TRIPS_TOURS);
		}
		else if (string.equals("\"Total distance traveled \"")){
			return new URI(ObservedProperties.DIST_TRAVELED_TOTAL);
		}
		return null;
	}


	private Geometry getNLGeometry() throws IOException{
		Geometry geom = null;
		URL url = OutputMapper.class.getResource("Netherlands.shp");
		ShapefileDataStore store = new ShapefileDataStore(url);
		FeatureSource<SimpleFeatureType, SimpleFeature> source = store.getFeatureSource("Netherlands");
		FeatureCollection<SimpleFeatureType,SimpleFeature> features = source.getFeatures();
		Iterator<SimpleFeature> iter = features.iterator();
		while (iter.hasNext()){
			SimpleFeature sf = iter.next();
			geom = (Geometry)sf.getDefaultGeometry();
			geom.setSRID(4326);
			
			//and for all member of the collection
			int nGeo = geom.getNumGeometries();
			
			for(int i = 0; i < nGeo; i++){
				
				geom.getGeometryN(i).setSRID(4326);
			}
		}
		return geom;
	}
	
	
	/**
	 * helper method for retrieving geometry of Rotterdam
	 * 
	 * @return geometry of rotterdam
	 * @throws IOException
	 * 			if shp file containing geometry cannot be read
	 */
	private Geometry getRotterdamGeometry() throws IOException{
		Geometry geom = null;
		URL url = OutputMapper.class.getResource("Rotterdam.shp");
		ShapefileDataStore store = new ShapefileDataStore(url);
		FeatureSource<SimpleFeatureType, SimpleFeature> source = store.getFeatureSource();
		FeatureCollection<SimpleFeatureType,SimpleFeature> features = source.getFeatures();
		Iterator<SimpleFeature> iter = features.iterator();
		while (iter.hasNext()){
			SimpleFeature sf = iter.next();
			geom = (Geometry)sf.getDefaultGeometry();
			geom.setSRID(4326);
			
			//and for all member of the collection
			int nGeo = geom.getNumGeometries();
			
			for(int i = 0; i < nGeo; i++){
				
				geom.getGeometryN(i).setSRID(4326);
			}
		}
		return geom;
	}
	
	/**
	 * helper method for reading file as string
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 * 			if file cannot be read
	 */
	private String readFileAsString(String filePath) throws java.io.IOException{
		String result = "";
	    byte[] buffer = new byte[(int) new File(filePath).length()];
	    BufferedInputStream f = null;
	    try {
	        f = new BufferedInputStream(new FileInputStream(filePath));
	        f.read(buffer);
	        result = new String(buffer);
	    } finally {
	        if (f != null) try { f.close(); } catch (IOException ignored) { }
	    }
	    return result;
	    
	}
}
