package org.uncertweb.austalwps.util.austal.timeseries;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to store time series of emissions for one dynamic emission source
 * @author l_gerh01
 *
 */

public class EmissionTimeSeries {
	private List<Double> emisVals = new ArrayList<Double>();
	private List<String> timeStamps = new ArrayList<String>();
	private String dynamicSourceID;	// id of the source in the zeitreihe file
	
	public EmissionTimeSeries(){
		
	}
	
	public EmissionTimeSeries(String dynamicSourceID){
		this.dynamicSourceID = dynamicSourceID;
	}
	
	public String getSourceID(){
		return dynamicSourceID;
	}

	public void setSourceID(String dynamicSourceID){
		this.dynamicSourceID = dynamicSourceID;
	}
	
	// time stamps
	public List<String> getTimeStamps(){
		return timeStamps;
	}

	public void setTimeStamps(int index, String value) {
		timeStamps.set(index, value);
	}
	
	// emission values
	public Double getEmissionValue(int i){
		return emisVals.get(i);
	}
	
	public Double getEmissionValue(String timeStamp){
		return emisVals.get(getTimeStampIndex(timeStamp));
	}

	public void addEmissionValue(String timeStamp, Double value) {
		timeStamps.add(timeStamp);
		emisVals.add(value);
	}
	
	public void addEmissionValue(String timeStamp, String value) {
		timeStamps.add(timeStamp);
		emisVals.add(Double.parseDouble(value));
	}
	
	public void setEmissionValue(int column, Double value) {
		emisVals.set(column, value);
	}
	
	public void setEmissionValue(String timeStamp, Double value) {
		emisVals.set(getTimeStampIndex(timeStamp), value);
	}
	
	// emission list
	public List<Double> getEmissionSeries(){
		return emisVals;
	}
	
	public void setEmissionSeries(Double[] values) {
		for(int i=0; i<values.length; i++){
			emisVals.set(i, values[i]);
		}		
	}

	public void setEmissionSeries(String[] values) {
		for(int i=0; i<values.length; i++){
			emisVals.set(i, Double.parseDouble(values[i]));
		}		
	}

	// utilities
	private int getTimeStampIndex(String timeStamp){
		// loop through time series to find respective string
		for(int i=0; i<timeStamps.size(); i++){
			if(timeStamp.equalsIgnoreCase(timeStamps.get(i)))
				return i;
		}
		return -1;
	}
}
