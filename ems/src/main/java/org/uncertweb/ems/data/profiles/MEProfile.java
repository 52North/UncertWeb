package org.uncertweb.ems.data.profiles;

import java.util.HashMap;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

public class MEProfile extends GeometryProfile {
	protected HashMap<Interval, Microenvironment> meList;
	protected HashMap<Interval, double[]> inConc;
	
	public MEProfile(IObservationCollection activityObservations, HashMap<Interval, Microenvironment> meList) {
		super(activityObservations);
		this.meList = meList;
	}
	
	public void setMEList(HashMap<Interval, Microenvironment> meList){
		this.meList = meList;
	}
	
	public HashMap<Interval, Microenvironment> getMEList(){
		return meList;
	}
	
}
