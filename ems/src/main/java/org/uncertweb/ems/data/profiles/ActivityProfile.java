package org.uncertweb.ems.data.profiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

public class ActivityProfile extends MEProfile {
	
	protected HashMap<Interval, ArrayList<Activity>> activityList;
	
	public ActivityProfile(IObservationCollection activityObservations, 
			HashMap<Interval, Microenvironment> meList, 
			HashMap<Interval, ArrayList<Activity>> activityList) {
		super(activityObservations, meList);
		this.activityList = activityList;
	}

	public void setActivityList(HashMap<Interval, ArrayList<Activity>> activityList){
		this.activityList = activityList;
	}
	
	public HashMap<Interval, ArrayList<Activity>> getActivityList(){
		return activityList;
	}
}
