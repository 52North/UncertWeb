package org.uncertweb.austalwps.util.austal.timeseries;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Class to store time series of emissions for one dynamic emission source
 * @author l_gerh01
 *
 */

public class EmissionTimeSeries {
	private List<Double> emisVals = new ArrayList<Double>();
	private List<Date> timeStamps = new ArrayList<Date>();
	private String dynamicIDToken;	// id of the source in the zeitreihe file
	private int dynamicID;
	
	public EmissionTimeSeries(){
		
	}
	
	public EmissionTimeSeries(String dynamicSourceIDToken){
		this.dynamicIDToken = dynamicSourceIDToken;
		this.dynamicID = Integer.parseInt(dynamicSourceIDToken.substring(1, 3));
	}
	
	public EmissionTimeSeries(int dynamicSourceID){
		String count;
		if(dynamicSourceID<10)
			count = "\"0"+ dynamicSourceID;
		else
			count = "\"" + dynamicSourceID;
		this.dynamicIDToken = count + ".pm-2%10.3e\"";
		this.dynamicID = dynamicSourceID;
	}
	
	public String getDynamicSourceIDToken(){
		return dynamicIDToken;
	}
	
	public int getDynamicSourceID(){
		return dynamicID;
	}

	public void setSourceIDString(String dynamicSourceIDToken){
		this.dynamicIDToken = dynamicSourceIDToken;
		this.dynamicID = Integer.parseInt(dynamicSourceIDToken.substring(0, 2));
	}
	
	public void setSourceID(int id){
		String count;
		if(id<10)
			count = "\"0"+ id;
		else
			count = "\"" + id;
		this.dynamicIDToken = count + ".pm-2%10.3e\"";
		this.dynamicID = id;
	}
	
	public void cutTimePeriod(Date start, Date end){
		// loop through dates and delete those outside the time period
		List<Date> newTimeStamps = new ArrayList<Date>();
		List<Double> newEmisVals = new ArrayList<Double>();
		for(int i=0; i<timeStamps.size(); i++){
			if((timeStamps.get(i).after(start)&&timeStamps.get(i).before(end))||timeStamps.get(i).equals(start)||timeStamps.get(i).equals(end)){
				newTimeStamps.add(timeStamps.get(i));
				newEmisVals.add(emisVals.get(i));
			}
		}
		this.timeStamps = newTimeStamps;
		this.emisVals = newEmisVals;
	}
	
	// time stamps
	public List<Date> getTimeStamps(){
		return timeStamps;
	}

	public void setTimeStamps(int index, Date date) {
		timeStamps.set(index, date);
	}
	
	public Date getMinDate(){
		Date minDate = timeStamps.get(0);
		for(int i=1; i<timeStamps.size(); i++){
			if(timeStamps.get(i).before(minDate))
				minDate = timeStamps.get(i);
		}
		return minDate;
	}
	
	public Date getMaxDate(){
		Date maxDate = timeStamps.get(0);
		for(int i=1; i<timeStamps.size(); i++){
			if(timeStamps.get(i).after(maxDate))
				maxDate = timeStamps.get(i);
		}
		return maxDate;
	}
	
	// emission values
	public Double getEmissionValue(int i){
		return emisVals.get(i);
	}
	
	public Double getEmissionValue(Date timeStamp){
		return emisVals.get(getTimeStampIndex(timeStamp));
	}

	public void addEmissionValue(Date timeStamp, Double value) {
		timeStamps.add(timeStamp);
		emisVals.add(value);
	}
	
	public void addEmissionValue(Date timeStamp, String value) {
		timeStamps.add(timeStamp);
		emisVals.add(Double.parseDouble(value));
	}
	
	public void setEmissionValue(int column, Double value) {
		emisVals.set(column, value);
	}
	
	public void setEmissionValue(Date timeStamp, Double value) {
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
	private int getTimeStampIndex(Date timeStamp){
		// loop through time series to find respective string
		for(int i=0; i<timeStamps.size(); i++){
			if(timeStamp.equals(timeStamps.get(i)))
				return i;
		}
		return -1;
	}
}
