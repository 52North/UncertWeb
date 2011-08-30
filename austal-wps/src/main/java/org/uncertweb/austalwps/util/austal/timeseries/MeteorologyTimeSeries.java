package org.uncertweb.austalwps.util.austal.timeseries;

import java.util.ArrayList;
import java.util.List;

public class MeteorologyTimeSeries {

	private List<Double> winddirVals = new ArrayList<Double>();
	private List<Double> windspeedVals = new ArrayList<Double>();
	private List<Double> stabilityVals = new ArrayList<Double>();
	private List<String> timeStamps = new ArrayList<String>();
	
	public MeteorologyTimeSeries(){
		
	}
	
	// time stamps
	public List<String> getTimeStamps(){
		return timeStamps;
	}

	public void setTimeStamps(int index, String value) {
		timeStamps.set(index, value);
	}
	
	public int getSize(){
		return timeStamps.size();
	}
	
	// meteorology values
	// getters
	public double[] getMeteorology(String timeStamp, String[] identifiers){
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
		return (winddirVals.get(index) + " " + windspeedVals.get(index) + " " + stabilityVals.get(index));
	}
	
	public Double getWindDirection(int i){
		return winddirVals.get(i);
	}
	
	public Double getWindDirection(String timeStamp){
		return winddirVals.get(getTimeStampIndex(timeStamp));
	}
	
	public Double getWindSpeed(int i){
		return windspeedVals.get(i);
	}

	public Double getWindSpeed(String timeStamp){
		return windspeedVals.get(getTimeStampIndex(timeStamp));
	}
	
	public Double getStabilityClass(int i){
		return stabilityVals.get(i);
	}
	
	public Double getStabilityClass(String timeStamp){
		return stabilityVals.get(getTimeStampIndex(timeStamp));
	}
	
	// setters
	public void addMeteorology(String timeStamp, String[] identifiers, double[] values){
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
	
	public void addMeteorology(String timeStamp, String[] identifiers, String[] values){
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
	
	public void addWindDirection(String timeStamp, Double value) {
		timeStamps.add(timeStamp);
		winddirVals.add(value);
	}
	
	public void setWindDirection(int column, Double value) {
		winddirVals.set(column, value);
	}
	
	public void setWindDirection(String timeStamp, Double value) {
		winddirVals.set(getTimeStampIndex(timeStamp), value);
	}
	
	public void addWindSpeed(String timeStamp, Double value) {
		timeStamps.add(timeStamp);
		windspeedVals.add(value);
	}
	
	public void setWindSpeed(int column, Double value) {
		windspeedVals.set(column, value);
	}
	
	public void setWindSpeed(String timeStamp, Double value) {
		windspeedVals.set(getTimeStampIndex(timeStamp), value);
	}
	
	public void addStabilityClass(String timeStamp, Double value) {
		timeStamps.add(timeStamp);
		stabilityVals.add(value);
	}
	
	public void setStabilityClass(int column, Double value) {
		stabilityVals.set(column, value);
	}
	
	public void setStabilityClass(String timeStamp, Double value) {
		stabilityVals.set(getTimeStampIndex(timeStamp), value);
	}
	
	// meteorology lists
	
	
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
