package org.uncertweb.austalwps.util;

public class Value {

	private String timestamp;
	private double PM10;
	
	public Value(String timestamp, double PM10result){
		this.timestamp = timestamp;
		this.PM10 = PM10result;
	}
	
	public double PM10val(){
		return PM10;
	}
	
	public String TimeStamp(){
		return timestamp;
	}
}
