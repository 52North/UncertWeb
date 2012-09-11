package org.uncertweb.wps.io.outputmapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.joda.time.DateTime;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.ObservationCollection;
import org.uncertweb.api.om.result.BooleanResult;
import org.uncertweb.api.om.result.CategoryResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.wps.util.AlbatrossOutputParser.HouseHold;
import org.uncertweb.wps.util.AlbatrossOutputParser.Individual;
import org.uncertweb.wps.util.AlbatrossOutputParser.Travel;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author s_voss13
 * 
 */
public class AlbatrossOutputMapper {

	/**
	 * identifier for the Albatross model
	 */
	public static final String PROC_ID_ALBATROSS = "http://www.uncertweb.org/models/albatross";
	public static final String ALBATROSS = "http://www.uncertweb.org/";
	public static final String REGISTER_PROCESS = "http://www.example.org/register/process/";

	public static final String ACTION_NUMBER = "http://www.uncertweb.org/variables/albatross/actionNumber/";
	public static final String ACTIVITY_TYPE = "http://www.uncertweb.org/variables/albatross/activityType";
	public static final String TRAVEL_MODE = "http://www.uncertweb.org/variables/albatross/travelMode";
	public static final String IS_HOME = "http://www.uncertweb.org/variables/albatross/isHome";
	
	public static final Map<String,String> ALBATROSS_CODE2ACTIVITY = new HashMap<String,String>(){
		  {
		   put("0", "work");
		   put("1", "business");
		   put("2", "bring/Gget goods and persons");
		   put("3", "shop from one store");
		   put("4", "shop from multiple store");
		   put("5", "service");
		   put("6", "social");
		   put("7", "leisure");
		   put("8", "tour");
		   put("9", "at home");
		  }
		 };
		 
	public static final Map<String,String> ALBATROSS_TRAVELMODE2ACTIVITY = new HashMap<String,String>(){
		  {
		   put("0", "car driver");
		   put("1", "slow(bike or walk)");
		   put("2", "public");
		   put("3", "car passenger");
		  }
		 };

	private static Map<String, Geometry> ppcMap = new HashMap<String, Geometry>(
			140);

	private AlbatrossOutputMapper() {
	};

	public static IObservationCollection encodeAlbatrossOutput(Set<HouseHold> households) throws IllegalArgumentException, URISyntaxException, IOException{
		
		IObservationCollection observationCollection = new ObservationCollection();
		
		//over all household
		Iterator<HouseHold> householdIterator = households.iterator();
		
		boolean individualChanged = false;
		
		while(householdIterator.hasNext()){
			
		HouseHold currentHouseHold = householdIterator.next();
		int counterForId = -1;
		
		//over all individuals
		Iterator<Individual> individualIterator =  currentHouseHold.getIndividuals().iterator();
				
		while(individualIterator.hasNext()){
			
		Individual currentIndividual = individualIterator.next();	
		individualChanged = true;
	
		//add all travels for the current individual
		Iterator<Travel> travelIterator = currentIndividual.getTravel().iterator();
		
		//continue inc OR new household id -> start at zero
		counterForId = individualChanged ? counterForId+1  : counterForId ;
		individualChanged = false;
		
		String id = currentIndividual.getGender()+"_"+currentIndividual.getAge()+"_"+currentIndividual.getHouseHoldID()+"_"+counterForId;
		
		while(travelIterator.hasNext()){
			
			Travel currentTravel = travelIterator.next();
			
			Identifier identifier = new Identifier(new URI(REGISTER_PROCESS),id);
			
			TimeObject phenomenonTime = new TimeObject(currentTravel.getBeginTime()+"/"+currentTravel.getEndTime()); 
			TimeObject resultTime = new TimeObject(new DateTime());
			URI procedure = new URI(REGISTER_PROCESS+id);
			URI observedProperty = new URI(ACTION_NUMBER);
			SpatialSamplingFeature featureOfInterest = new SpatialSamplingFeature(new Identifier(new URI(ALBATROSS),currentTravel.getPpc()),null, getGeometryFromPPC(currentTravel.getPpc()));
						
			CategoryObservation categoryObservation = new CategoryObservation(identifier, null, phenomenonTime, resultTime, null, procedure, observedProperty, featureOfInterest, null, new CategoryResult(currentTravel.getActionnumber(), ALBATROSS));
							
			observationCollection.addObservation(categoryObservation);
			
			//add activity type variable
			URI activityTypeObservedProperty = new URI(ACTIVITY_TYPE);
			String activityType = currentTravel.getActivityType();
			String activityResult = ALBATROSS_CODE2ACTIVITY.get(activityType);
			CategoryObservation activityTypeObservation = new CategoryObservation(phenomenonTime, resultTime, procedure, activityTypeObservedProperty, featureOfInterest, new CategoryResult(activityResult,ALBATROSS));
			observationCollection.addObservation(activityTypeObservation);
			
			
			//add travel mode variable
			URI travelModeObservedProperty = new URI(TRAVEL_MODE);
			String travelModeResult = ALBATROSS_TRAVELMODE2ACTIVITY.get(currentTravel.getTravelMode());
			CategoryObservation travelModeObservation = new CategoryObservation(phenomenonTime, resultTime, procedure, travelModeObservedProperty, featureOfInterest, new CategoryResult(travelModeResult,ALBATROSS));
			
			observationCollection.addObservation(travelModeObservation);
			
			//add home variable
			URI isHomeObservedProperty = new URI(IS_HOME);
			BooleanObservation isHomeObservation = new BooleanObservation(phenomenonTime, resultTime, procedure, isHomeObservedProperty, featureOfInterest, new BooleanResult(Boolean.valueOf(currentTravel.isHome())));
			
			observationCollection.addObservation(isHomeObservation);
			
			}
		
		}
		}
		return observationCollection;
		
	}

	private static Geometry getGeometryFromPPC(String ppc) throws IOException {

		if (ppcMap.containsKey(ppc))
			return ppcMap.get(ppc);

		URL url = AlbatrossOutputMapper.class
				.getResource("PCA4digits.shp");

		FileDataStore store = FileDataStoreFinder.getDataStore(url);

		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = store
				.getFeatureSource();

		FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource
				.getFeatures();

		Iterator<SimpleFeature> iter = features.iterator();

		Geometry geom = null;

		while (iter.hasNext()){
			SimpleFeature sf = iter.next();
			String ppcFromFile = sf.getAttribute("CEN_NUMBER").toString();
			
			geom = (Geometry)sf.getDefaultGeometry();
			geom.setSRID(4326);
			
			//and for all member of the collection
			int nGeo = geom.getNumGeometries();
			
			for(int i = 0; i < nGeo; i++){
				geom.getGeometryN(i).setSRID(4326);
			}

			//if not part of the map -> add
			if(!ppcMap.containsKey(ppcFromFile))
				ppcMap.put(ppcFromFile, geom);
		}
				
	return geom;
	}

}
