package org.uncertweb.ems.data.profiles;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.CSVEncoder;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.ems.data.exposure.ExposureValue;
import org.uncertweb.ems.exceptions.EMSProcessingException;

/**
 * Abstract class for different exposure relevant profile types
 * stores the geometry and information about activities of individual trajectories
 * @author LydiaGerharz
 *
 */
public abstract class AbstractProfile {

	protected IObservationCollection obsColl;
	protected HashMap<Interval, ArrayList<ExposureValue>> expoVals;
		
		public AbstractProfile(IObservationCollection activityObservations){
			this.obsColl = activityObservations;
			expoVals = new HashMap<Interval, ArrayList<ExposureValue>>();
		}

		public int getSize(){
			return obsColl.getObservations().size();
		}
		
		// get and set for OM observation collection
		public void setObservationCollection(IObservationCollection observationCollection) {
			this.obsColl = observationCollection;		
		}

		public IObservationCollection getObservationCollection() {	
			if(obsColl!=null)
				return obsColl;
			else
				throw new EMSProcessingException("Observation Collection for the profile has not been set.");
		}

		/**
		 * returns a map to identify the respective time interval if only the start time is known
		 * @return
		 */
		public TreeMap<DateTime,Interval> getStartDatesMap(){
			TreeMap<DateTime,Interval> dateMap = new TreeMap<DateTime,Interval>();
			for(AbstractObservation obs : obsColl.getObservations()){
				dateMap.put(obs.getPhenomenonTime().getInterval().getStart(),obs.getPhenomenonTime().getInterval());
			}
			return dateMap;
		}
		
		//TODO: implement correct getters and setters for expo vals
		public void setExposureValue(Interval in, double[] c, String type, String pollutant, String uom){

			// if ArrayList with values exists already for this timestep
			if(expoVals.containsKey(in)&&!expoVals.get(in).isEmpty()){
				for(ExposureValue val : expoVals.get(in)){
					// if this type of exposure is already available, remove it to overwrite it
					if(val.getType().equals(type)){
						expoVals.get(in).remove(val);
						break;
					}
				}
				expoVals.get(in).add(new ExposureValue(c,type,pollutant,uom));
			}
			// else create a new ArrayList and add it to the map
			else{
				ArrayList<ExposureValue> valueList = new ArrayList<ExposureValue>();
				valueList.add(new ExposureValue(c,type,pollutant,uom));
				expoVals.put(in, valueList);
			}

		}		
		
		public ArrayList<ExposureValue> getExposureValues(Interval in){
			return expoVals.get(in);
		}
		
		/***
	     * Writes spatial and temporal information from the Observation Collection to csv file
	     * @param filepath
	     * 
	     */
		public void writeObsCollGeometry2csv(String filepath){ 		
			// use CSVEncoder 
			try {
				new CSVEncoder().encodeObservationCollection(obsColl, new File(filepath));
			} catch (OMEncodingException e) {
				e.printStackTrace();
			}	
			
	    }
}
