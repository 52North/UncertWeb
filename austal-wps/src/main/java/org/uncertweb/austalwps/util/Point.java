package org.uncertweb.austalwps.util;

import java.util.ArrayList;

public class Point {
	private double x;
	private double y;
	private String fid;
	private ArrayList<Value> values;

	public Point(String id){
		this.fid = id;
		values = new ArrayList<Value>();
	}

	public Point(double x_coord, String id){
		this.x = x_coord;
		this.fid = id;
		values = new ArrayList<Value>();

	}

	public Point(double x_coord, double y_coord, String id){
		this.x = x_coord;
		this.y = y_coord;
		this.fid = id;
		values = new ArrayList<Value>();

	}

	public void set_xCoordinate(double x){
		this.x = x;
	}

	public void set_yCoordinate(double y){
		this.y = y;
	}
	public void addValue(String time, double val){
		values.add(new Value(time,val));
	}

	public String get_fid(){
		return fid;
	}

	public double[] coordinates(){
		double[] coords = new double[]{x,y};
		return coords;
	}

	public ArrayList<Value> values(){
		return values;
	}

}
