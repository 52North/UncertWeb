package org.uncertweb.austalwps.util.austal.timeseries;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
//import java.util.Date;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

public class MeteorologyTimeSeries {
	
	private static Logger LOGGER = Logger.getLogger(MeteorologyTimeSeries.class);
	
	private List<Double> winddirVals = new ArrayList<Double>();
	private List<Double> windspeedVals = new ArrayList<Double>();
	private List<Double> stabilityVals = new ArrayList<Double>();
	private List<DateTime> timeStamps = new ArrayList<DateTime>();
	
	public MeteorologyTimeSeries(){
		
	}
	
	// time stamps
	public List<DateTime> getTimeStamps(){
		return timeStamps;
	}

	public void setTimeStamps(int index, DateTime date) {
		timeStamps.set(index, date);
	}
	
	public DateTime getMinDate(){
		DateTime minDate = timeStamps.get(0);
		for(int i=1; i<timeStamps.size(); i++){
			if(timeStamps.get(i).isBefore(minDate))
				minDate = timeStamps.get(i);
		}
		return minDate;
	}
	
	public DateTime getMaxDate(){
		DateTime maxDate = timeStamps.get(0);
		for(int i=1; i<timeStamps.size(); i++){
			if(timeStamps.get(i).isAfter(maxDate))
				maxDate = timeStamps.get(i);
		}
		return maxDate;
	}
	
	public int getSize(){
		return timeStamps.size();
	}
	
	public void cutTimePeriod(DateTime start, DateTime end){
		// loop through dates and delete those outside the time period
		for(int i=0; i<timeStamps.size(); i++){
			if(timeStamps.get(i).isBefore(start)||timeStamps.get(i).isAfter(end)){
				timeStamps.remove(i);
				winddirVals.remove(i);
				windspeedVals.remove(i);
				stabilityVals.remove(i);
			}
		}
	}
	
	// meteorology values
	// getters
	public double[] getMeteorology(DateTime timeStamp, String[] identifiers){
		double[] values = new double[identifiers.length];
		for(int i=0; i<identifiers.length; i++){
			if(identifiers[i].contains("ra"))
				values[i] = winddirVals.get(getTimeStampIndex(timeStamp));
			else if(identifiers[i].contains("ua"))
				values[i] = windspeedVals.get(getTimeStampIndex(timeStamp));
			else if(identifiers[i].contains("lm"))
				values[i] = stabilityVals.get(getTimeStampIndex(timeStamp));
		}		
		return values;
	}
	
	public double[] getMeteorology(int index, String[] identifiers){
		double[] values = new double[identifiers.length];
		for(int i=0; i<identifiers.length; i++){
			if(identifiers[i].contains("ra"))
				values[i] = winddirVals.get(index);
			else if(identifiers[i].contains("ua"))
				values[i] = windspeedVals.get(index);
			else if(identifiers[i].contains("lm"))
				values[i] = stabilityVals.get(index);
		}		
		return values;
	}
	
	public String getMeteorologyToString(int index){
		DecimalFormat digit = new DecimalFormat("0.#");
		String met = Math.round(winddirVals.get(index)) + " " + digit.format(windspeedVals.get(index)) + " " + stabilityVals.get(index);
		String metNew = met.replace(",", ".");
		return (metNew);
	}
	
	public Double getWindDirection(int i){
		return winddirVals.get(i);
	}
	
	public Double getWindDirection(DateTime timeStamp){
		return winddirVals.get(getTimeStampIndex(timeStamp));
	}
	
	public Double getWindSpeed(int i){
		return windspeedVals.get(i);
	}

	public Double getWindSpeed(DateTime timeStamp){
		return windspeedVals.get(getTimeStampIndex(timeStamp));
	}
	
	public Double getStabilityClass(int i){
		return stabilityVals.get(i);
	}
	
	public Double getStabilityClass(DateTime timeStamp){
		int index = getTimeStampIndex(timeStamp);
		double stab = stabilityVals.get(index);
		return stab;
	}
	
	// setters
	public void addMeteorology(DateTime timeStamp, String[] identifiers, double[] values){
		timeStamps.add(timeStamp);
		for(int i=0; i<identifiers.length; i++){
			if(identifiers[i].contains("ra"))
				winddirVals.add(values[i]);
			else if(identifiers[i].contains("ua"))
				windspeedVals.add(values[i]);
			else if(identifiers[i].contains("lm"))
				stabilityVals.add(values[i]);
		}		
	}
	
	public void addMeteorology(DateTime timeStamp, String[] identifiers, String[] values){
		timeStamps.add(timeStamp);
		for(int i=0; i<identifiers.length; i++){
			if(identifiers[i].contains("ra"))
				winddirVals.add(Double.parseDouble(values[i]));
			else if(identifiers[i].contains("ua"))
				windspeedVals.add(Double.parseDouble(values[i]));
			else if(identifiers[i].contains("lm"))
				stabilityVals.add(Double.parseDouble(values[i]));
		}		
	}
	
	// wind direction
	public void addWindDirection(DateTime timeStamp, Double value) {
		// check if timestamp is already in the list
		int timeID = getTimeStampIndex(timeStamp);
		if(timeID==-1){
			timeStamps.add(timeStamp);
			winddirVals.add(value);
		}
		else if(winddirVals.size()==timeID){
			winddirVals.add(value);
		}
		else if(winddirVals.size()>timeID){
			winddirVals.set(timeID, value);
		}
		else{
			LOGGER.debug("Missing observation in winddirection.");
		}
	}
	
//	public void setWindDirection(int column, Double value) {
//		winddirVals.set(column, value);
//	}
	
	public void setWindDirection(DateTime timeStamp, Double value) {
		winddirVals.set(getTimeStampIndex(timeStamp), value);
	}
	
	// wind speed
	public void addWindSpeed(DateTime timeStamp, Double value) {
		// check if timestamp is already in the list
		int timeID = getTimeStampIndex(timeStamp);
		if(timeID==-1){
			timeStamps.add(timeStamp);
			windspeedVals.add(value);
		}
		else if(windspeedVals.size()==timeID){
			windspeedVals.add(value);
		}
		else if(windspeedVals.size()>timeID){
			windspeedVals.set(timeID, value);
		}
		else{
			LOGGER.debug("Missing observation in windspeed.");
		}
	}
	
//	public void setWindSpeed(int column, Double value) {
//		windspeedVals.set(column, value);
//	}
	
	public void setWindSpeed(DateTime timeStamp, Double value) {
		windspeedVals.set(getTimeStampIndex(timeStamp), value);
	}
	
	// stability class
	public void addStabilityClass(DateTime timeStamp, Double value) {
		// check if timestamp is already in the list
		int timeID = getTimeStampIndex(timeStamp);
		if(timeID==-1){
			timeStamps.add(timeStamp);
			stabilityVals.add(value);
		}
		else if(stabilityVals.size()==timeID){
			stabilityVals.add(value);
		}
		else if(stabilityVals.size()>timeID){
			stabilityVals.set(timeID, value);
		}
		else{
			LOGGER.debug("Missing observation in stability.");
		}
	}
	
//	public void setStabilityClass(int column, Double value) {
//		stabilityVals.set(column, value);
//	}
	
	public void setStabilityClass(DateTime timeStamp, Double value) {
		stabilityVals.set(getTimeStampIndex(timeStamp), value);
	}
	
	// meteorology lists
	
	
	// utilities
	private int getTimeStampIndex(DateTime timeStamp){
		// loop through time series to find respective string
		for(int i=0; i<timeStamps.size(); i++){
			if(timeStamp.isEqual(timeStamps.get(i)))
				return i;
		}
		return -1;
	}

}
