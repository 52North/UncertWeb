package org.uncertweb.austalwps.util.austal.timeseries;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class MeteorologyTimeSeries {

	private List<Double> winddirVals = new ArrayList<Double>();
	private List<Double> windspeedVals = new ArrayList<Double>();
	private List<Double> stabilityVals = new ArrayList<Double>();
	private List<Date> timeStamps = new ArrayList<Date>();
	
	public MeteorologyTimeSeries(){
		
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
	
	public int getSize(){
		return timeStamps.size();
	}
	
	public void cutTimePeriod(Date start, Date end){
		// loop through dates and delete those outside the time period
		for(int i=0; i<timeStamps.size(); i++){
			if(timeStamps.get(i).before(start)||timeStamps.get(i).after(end)){
				timeStamps.remove(i);
				winddirVals.remove(i);
				windspeedVals.remove(i);
				stabilityVals.remove(i);
			}
		}
	}
	
	// meteorology values
	// getters
	public double[] getMeteorology(Date timeStamp, String[] identifiers){
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
	
	public Double getWindDirection(Date timeStamp){
		return winddirVals.get(getTimeStampIndex(timeStamp));
	}
	
	public Double getWindSpeed(int i){
		return windspeedVals.get(i);
	}

	public Double getWindSpeed(Date timeStamp){
		return windspeedVals.get(getTimeStampIndex(timeStamp));
	}
	
	public Double getStabilityClass(int i){
		return stabilityVals.get(i);
	}
	
	public Double getStabilityClass(Date timeStamp){
		int index = getTimeStampIndex(timeStamp);
		double stab = stabilityVals.get(index);
		return stab;
	}
	
	// setters
	public void addMeteorology(Date timeStamp, String[] identifiers, double[] values){
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
	
	public void addMeteorology(Date timeStamp, String[] identifiers, String[] values){
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
	public void addWindDirection(Date timeStamp, Double value) {
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
			System.out.println("Missing observation in winddirection.");
		}
	}
	
//	public void setWindDirection(int column, Double value) {
//		winddirVals.set(column, value);
//	}
	
	public void setWindDirection(Date timeStamp, Double value) {
		winddirVals.set(getTimeStampIndex(timeStamp), value);
	}
	
	// wind speed
	public void addWindSpeed(Date timeStamp, Double value) {
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
			System.out.println("Missing observation in windspeed.");
		}
	}
	
//	public void setWindSpeed(int column, Double value) {
//		windspeedVals.set(column, value);
//	}
	
	public void setWindSpeed(Date timeStamp, Double value) {
		windspeedVals.set(getTimeStampIndex(timeStamp), value);
	}
	
	// stability class
	public void addStabilityClass(Date timeStamp, Double value) {
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
			System.out.println("Missing observation in stability.");
		}
	}
	
//	public void setStabilityClass(int column, Double value) {
//		stabilityVals.set(column, value);
//	}
	
	public void setStabilityClass(Date timeStamp, Double value) {
		stabilityVals.set(getTimeStampIndex(timeStamp), value);
	}
	
	// meteorology lists
	
	
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
