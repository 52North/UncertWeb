package org.uncertweb.aqms.interpolation;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.rosuda.REngine.REXP;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * Class to perform Kriging on the area of Münster using SOS observations of rural background concentrations.
 * The Kriging is done on a polygon representing the area of Münster.
 * Includes handling of invalid variograms -> TO BE DEFINED!!!
 * Afterwards, a samples can be drawn from the distribution within the polygon.
 * 
 * @author l_gerh01
 *
 *Inputs: ObservationCollection, Polygon
 *Output: UncertML Normal distribution
 */
public class PolygonKriging {

	private static Logger LOGGER = Logger.getLogger(PolygonKriging.class);
	private final static String URI_PROCEDURE = "http://www.uncertweb.org/interpolation/Kriging";
	private final static String URI_OBSERVED_PROPERTY = "http://www.uncertweb.org/phenomenon/pm10";
	private final static String URI_SAMPLING_FEATURE = "http://www.uncertweb.org";
	
	private double minX, maxX, minY, maxY;
	private int epsg_prediction; //31467
	private int epsg_observation; //4326
	
	public PolygonKriging(double minX, double maxX, double minY, double maxY, int epsg_prediction){
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.epsg_prediction = epsg_prediction;
	}
	
	public PolygonKriging(){
	}

	public UncertaintyObservationCollection performKriging(IObservationCollection iobs, DateTime start){
		// get rTable from ObservationCollection
		HashMap<String, HashMap<String, Double[]>> rTable = obsCollection2Table(iobs);
		ExtendedRConnection c = null;		
		UncertaintyObservationCollection uColl = null;
		try {		
			// establish connection to Rserve running on localhost
			//c = new ExtendedRConnection("giv-uw.uni-muenster.de");
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}
			
			// results storage
			uColl = new UncertaintyObservationCollection();
	
			// details for uncertainty collection
			URI procedure = new URI(URI_PROCEDURE);
			URI observedProperty = new URI(URI_OBSERVED_PROPERTY);
			
			// create spatial sampling feature
			Coordinate[] coordinates = new Coordinate[5];
			coordinates[0] = new Coordinate(maxX, minY);
			coordinates[1] = new Coordinate(minX, minY);
			coordinates[2] = new Coordinate(minX, maxY);
			coordinates[3] = new Coordinate(maxX, maxY);
			coordinates[4] = new Coordinate(maxX, minY);	
			CoordinateArraySequence coordinateSequence = new CoordinateArraySequence(coordinates);
			
			GeometryFactory factory = new GeometryFactory(new PrecisionModel(), epsg_prediction);
			LinearRing lr = new LinearRing(coordinateSequence, factory);
			Polygon polygon = new Polygon(lr, null, factory);
			Identifier identifier = new Identifier(new URI(URI_SAMPLING_FEATURE), "background");
			SpatialSamplingFeature spSamplingFeature = new SpatialSamplingFeature(identifier, "urn:ogc:def:nil:OGC:unknown", polygon);
			
			// R COMMANDS 
			
			// load libraries
			c.tryVoidEval("library(automap)");
			c.tryVoidEval("library(rgdal)");
			
			// define Polygon coordinates in R
			String cmd = "coords <- rbind(c("+maxX+","+minY+"),c("+minX+","+minY+"),c("+minX+","+maxY+"),c("+maxX+","+maxY+"),c("+maxX+","+minY+"))";
			c.tryVoidEval(cmd);
			c.tryVoidEval("p <- Polygon(coords, hole=as.logical(NA))");
			c.tryVoidEval("ps <- Polygons(list(p), ID='p1')");
			c.tryVoidEval("polygon <- SpatialPolygons(Sr=list(ps), pO=as.integer(1), proj4string=CRS('+init=epsg:"+epsg_prediction+"'))");	


			// create one container for all variograms
			c.tryVoidEval("var.container <- list()");
			
			// loop through time series of observations		
			for(int i=0; i<rTable.size(); i++){
				String date = start.plusHours(i).toString(ISODateTimeFormat.dateTime());
				
				// get measurements for this date
				HashMap<String, Double[]> stations = rTable.get(date);
				
				if(stations!=null){
					// create table in R
					c.tryVoidEval("obs <- NULL");
					
					// loop through stations
					Set<String> stKeys = stations.keySet();
					for(String stationName : stKeys){
						Double[] vals = stations.get(stationName);
						// add current station to table
						c.tryVoidEval("obs <- rbind(obs, c(" + vals[0] + "," + vals[1] + "," + vals[2] + "))");
					}
					
					// add names to table and make dataframe
					c.tryVoidEval("obs.df <- as.data.frame(obs)");
					c.tryVoidEval("names(obs.df) <- c('Lat', 'Lon', 'PM10')");
					
					// make spatial dataframe and project
					c.tryVoidEval("coordinates(obs.df) <- ~Lon+Lat");
					c.tryVoidEval("proj4string(obs.df) <- CRS('+init=epsg:"+epsg_observation+"')");
					c.tryVoidEval("obs.sp <- spTransform(obs.df, CRS=CRS('+init=epsg:"+epsg_prediction+"'))");
					
					// fit variogram automatically and get parameters
					c.tryVoidEval("v.fit <- autofitVariogram(PM10~1,obs.sp[!is.na(obs.sp$PM10),])");
					REXP nugget = c.tryEval("v.fit$var_model$psill[1]");
					REXP psill = c.tryEval("v.fit$var_model$psill[2]");
					REXP range = c.tryEval("v.fit$var_model$range[2]");
					
					//TODO: Do something with the variogram parameters
					// only continue if variogram is valid
					// check Sum squared errors
					REXP sserr = c.tryEval("v.fit$sserr");
					double sserrThr = 100d;
					if(sserr.asDouble()>sserrThr){
						System.out.println("Invalid variogram for: "+ date.toString());
						
						// use variogram from last hour?
						//c.tryVoidEval("v.fit = var.container[[length(var.container)]]")
						
						// what to do with the first hour?
					}
					else{
						//System.out.println(date.toString()+"\n Nugget: "+nugget.asDouble()+ "\n Psill: "+psill.asDouble()+ "\n Range: "+range.asDouble()+
						//		"\n SSErr: "+sserr.asDouble()+ "\n");
						// add valid variogram to variogram container
						c.tryVoidEval("var.container <- c(var.container,list(v.fit))");
					}
					
					// perform Kriging on polygon
					c.tryVoidEval("krige <- autoKrige(PM10~1, obs.sp[!is.na(obs.sp$PM10),],  polygon)");
					REXP mean = c.tryEval("krige$krige_output@data$var1.pred");
					REXP sd = c.tryEval("krige$krige_output@data$var1.stdev");
					REXP var = c.tryEval("krige$krige_output@data$var1.var");
					
					// put results to uncertainty collection
					TimeObject newT = new TimeObject(date);
					NormalDistribution nDist = new NormalDistribution(mean.asDouble(), var.asDouble());
					
					UncertaintyResult uResult = new UncertaintyResult(nDist, "ug/m3");
					UncertaintyObservation uObs = new UncertaintyObservation(
							newT, newT, procedure, observedProperty, spSamplingFeature,
							uResult);
					uColl.addObservation(uObs);
				}
				else{
					System.out.println("Problem with observations for "+date);
				}
			}
			
//			Set<DateTime> keys = rTable.keySet();
//			for(DateTime date : keys){
//				
//			}	

			// finally save variograms in workspace
			c.tryVoidEval("save(var.container, file=\"D:/PhD/WP1.1_AirQualityModel/WebServiceChain/UBAinterpolation/varContainer.RData\")");
			System.out.println("Interpolation successfully finished!");
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
		return uColl;
	}
	
	/***
     * Method to extract spatial information and measurements from the observation collection.
     * These are written as tables to be read by R for the interpolation.
     * @param iobs
     * @return Rtable
     */
	private HashMap<String, HashMap<String, Double[]>> obsCollection2Table(IObservationCollection iobs){
    	HashMap<String, HashMap<String, Double[]>> obsTS = new HashMap<String, HashMap<String, Double[]>>();
    	boolean first = true;
    	
    	// loop through observations
    	for (AbstractObservation obs : iobs.getObservations()) {  	
    		// TODO: get coordinate reference system
    		if(first){
    			int srid = obs.getFeatureOfInterest().getShape().getSRID();
    			epsg_observation = srid;
    			first = false;
    		}
    		
    		// If it is a measurements object, it is easier
    		String fid = obs.getFeatureOfInterest().getIdentifier().getIdentifier();
    		Coordinate c = obs.getFeatureOfInterest().getShape().getCoordinate();
			
			// get result values
    		Double res = (Double) obs.getResult().getValue();
    		Double[] vals = {c.x,c.y,res};
    		
    		// get sampling time
    		String st = obs.getResultTime().getDateTime().toString(ISODateTimeFormat.dateTime());
    		
    		// if it's a new date add it to the list
    		if(!obsTS.containsKey(st)){
    			HashMap<String, Double[]> station = new HashMap<String,Double[]>();
    			station.put(fid, vals);
    			obsTS.put(st, station);
    		}else{
    			// get existing map and add the current station values
    			HashMap<String, Double[]> stat = obsTS.get(st);
    			stat.put(fid, vals);
    			obsTS.put(st, stat);    			
    		}		
    	}    	
    	return obsTS;
    }
	
	
	public UncertaintyObservationCollection mergeUncertaintyObsColl(ArrayList<UncertaintyObservationCollection> uobsList){
		UncertaintyObservationCollection uobsAll = uobsList.get(0);
		
		// loop through collections
		for(int i=1; i<uobsList.size(); i++){
			UncertaintyObservationCollection uobs = uobsList.get(i);
			for (AbstractObservation obs : uobs.getObservations()) {
				uobsAll.addObservation(obs);
			}			
		}
		
		return uobsAll;
	}
}
