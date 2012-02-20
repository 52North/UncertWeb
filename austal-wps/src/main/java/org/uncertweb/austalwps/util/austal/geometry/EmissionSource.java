package org.uncertweb.austalwps.util.austal.geometry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.uncertweb.austalwps.util.austal.timeseries.EmissionTimeSeries;

public class EmissionSource implements Serializable{

	/**
	 * class to store the Austal data model for emission sources
	 * stores geometry in Austal coordinates
	 * stores emission strength
	 * stores emission time series if source is variable
	 */
	
	private static final long serialVersionUID = 7758710331178466677L;
	
	private double aq; // extent of the source in x direction
	private double bq; // extent of the source in y direction
	private double cq; // extent of the source in z direction
	private double wq; // angle source is turned around the lower left edge counter clockwise
	private double hq; // height of the source
	private double xq; // x coordinate of the source
	private double yq; // y coordinate of the source
	private String pm2; // PM10 emission strength of the source; '?' if source is dynamic
	private EmissionTimeSeries emisTS;	// time series of emission values
	private String sourceType; // describes type of sources, e.g. industry, traffic
	private int dynamicSourceID; // internal id of this source
	private boolean dynamic; // boolean if source is dynamic or static
	
	public EmissionSource(String sourceType){
		this.sourceType = sourceType;
	}
	
	public EmissionSource() {}
	
	// constructor with optional parameters to start with
	public EmissionSource(String xq, String yq, String hq) {
		this.xq = Double.parseDouble(xq);
		this.yq = Double.parseDouble(yq);
		this.hq = Double.parseDouble(hq);
	}
	
	public void setCoordinates(double x, double y){
		xq = x;
		yq = y;
	}
	
	public void setExtent(double xExtent, double yExtent, double zExtent, double angle, double height){
		aq = xExtent;
		bq = yExtent;
		cq = zExtent;
		wq = angle;
		hq = height;
	}
	
	public void setStaticStrength(String pm){
		dynamic = false;
		pm2 = pm;		
	}
	
	public void setStaticStrength(double pm){
		dynamic = false;
		pm2 = pm+"";		
	}
	
	public void setDynamic(int id, EmissionTimeSeries ts){
		dynamic = true;
		this.dynamicSourceID = id;		
		this.pm2 = "?";
		emisTS = ts;
		ts.setSourceID(id);
	}
	
	public void setDynamicSourceID(int dynamicSourceID){
		dynamic = true;
		this.dynamicSourceID = dynamicSourceID;		
		this.pm2 = "?";
		emisTS = new EmissionTimeSeries(dynamicSourceID);
	}
	
	public int getDynamicSourceID(){
		if(dynamic==true)
			return this.dynamicSourceID;
		else
			return 0;
	}
	
	public boolean isDynamic(){
		return dynamic;
	}
	
	// Getters & Setters
	public void setSourceType(String sourceType){
		this.sourceType = sourceType;
	}
	
	public String getSourceType(){
		return sourceType;
	}

	public double getXq() {
		return xq;
	}

	public void setXq(double xq) {
		this.xq = xq;
	}

	public void setXq(String xq) {
		this.xq = Double.parseDouble(xq);
	}
	
	public double getYq() {
		return yq;
	}

	public void setYq(double yq) {
		this.yq = yq;
	}

	public void setYq(String yq) {
		this.yq = Double.parseDouble(yq);
	}
	
	public double getHq() {
		return hq;
	}

	public void setHq(double hq) {
		this.hq = hq;
	}

	public void setHq(String hq) {
		this.hq = Double.parseDouble(hq);
	}
	
	public String getPm2() {
		return pm2;
	}

	public void setPm2(String pm) {
		this.pm2 = pm;
	}
	
	public void setEmissionList(EmissionTimeSeries ts) {
		this.emisTS = ts;
	}
	
	public EmissionTimeSeries getEmissionList() {
		return emisTS;
	}
	
	public double getWq() {
		return wq;
	}

	public void setWq(double wq) {
		this.wq = wq;
	}
	
	public void setWq(String wq) {
		this.wq = Double.parseDouble(wq);
	}
	
	public double getBq() {
		return bq;
	}

	public void setBq(double bq) {
		this.bq = bq;
	}
	
	public void setBq(String bq) {
		this.bq = Double.parseDouble(bq);
	}
	
	public double getAq() {
		return aq;
	}

	public void setAq(double aq) {
		this.aq = aq;
	}
	
	public void setAq(String aq) {
		this.aq = Double.parseDouble(aq);
	}
	
	public double getCq() {
		return cq;
	}

	public void setCq(double cq) {
		this.cq = cq;
	}
	
	public void setCq(String cq) {
		this.cq = Double.parseDouble(cq);
	}
	
	public EmissionSource getCopy() {
		EmissionSource as = new EmissionSource();
		as.aq = this.aq;
		as.bq = this.bq;
		as.cq = this.cq;
		as.hq = this.hq;
		as.pm2 = this.pm2;
		as.wq = this.wq;
		as.xq = this.xq;
		as.yq = this.yq;
		return as;
	}
	
}
