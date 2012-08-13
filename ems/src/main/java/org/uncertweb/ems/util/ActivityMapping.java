package org.uncertweb.ems.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.uncertweb.ems.data.profiles.Activity;


/**
 * Provides mapping between activity categories provided by albatross activity profiles and exposure relevant activities
 * @author l_gerh01
 *
 */
public class ActivityMapping {

	// Albatross activityType: 0:Work, 1: Business, 2: Bring/Get goods and persons, 3: shop from one store, 4: shop from multiple store, 5: service, 6: social, 7: leisure, 8;tour, 9:at home
	// Albatross travelMode: 0: car driver, 1: slow(bike or walk), 2: public, 3: car passenger
	// Albatross isHome: 0: not at home, 1: at home
	
	// activity2emissionSource mapping:

	/*
	 * Albatross mappings
	 */
	public static final Map<String,String> ALBATROSS_CODE2ACTIVITY = new HashMap<String,String>(){
		private static final long serialVersionUID = -5183645860669636587L;

		{
			put("", "");
			put("0", "work");
			put("1", "business");
			put("2", "bring/Gget goods and persons");
			put("3", "shop from one store");
			put("4", "shop from multiple store");
			put("5", "service");
			put("6", "social");
			put("7", "leisure");
			put("8", "tour");
			put("9", "at home");
		}
	};
	
	public static final Map<String,String> ALBATROSS_TRAVELMODE2ACTIVITY = new HashMap<String,String>(){
		private static final long serialVersionUID = 6107971476316773428L;

		{
			put("", "");
			put("0", "car driver");
			put("1", "slow(bike or walk)");
			put("2", "public");
			put("3", "car passenger");
		}
	};
	
	
	/*
	 * Mappings from generic activities to exposure relevant activities and microenvironments
	 */
	
	private static final Map<String,String> ACTIVITY2EXPOSURE_ACTIVITY = new HashMap<String,String>(){
		private static final long serialVersionUID = 3161110991333967276L;
		// Activities: smoke, cook, neutral, aerate, human
		{
			put("smoke", "smoke");
			put("smoking", "smoke");
			put("cigarette", "smoke");
			put("cigarettes", "smoke");
			put("cook","cook");
			put("cooking","cook");
			put("sleeping","neutral");
			put("resting","neutral");
			put("relaxing","neutral");
			put("aerating","aerate");
			put("opening window","aerate");
			put("getting up","human");
			put("making bed","human");
			put("cleaning","human");
			put("laundry","human");
			put("sports","human");
			put("dressing","human");
			put("walking","human");
		}
	};
	
	private static final Map<String,String> ACTIVITY2MICROENVIRONMENT = new HashMap<String,String>(){
		private static final long serialVersionUID = -1573329646301968442L;
		// MEs: car, bus, train, home, other indoor, work, outdoor, restaurant, disco, pub
		{
			// transport and outdoor
			put("car","car");
			put("bus", "bus");
			put("train", "train");
			put("public", "bus");
			put("transport", "bus");
			put("bike", "outdoor");
			put("walk", "outdoor");
			
			// indoors
			put("home", "home");
			put("Home", "home");
			put("work", "work");
			put("Work", "work");
			put("office", "work");
			put("business", "work");
			put("Business", "work");
			put("store", "otherindoor");
			put("other indoor", "otherindoor");
			
			// specific indoors
			put("restaurant", "restaurant");
			put("bar", "pub");
			put("pub", "pub");
			put("disco", "disco");
			put("club", "club");
		}
	};
	

	/**
	 * Maps from a list of activity attributes to exposure relevant activities and microenvironments
	 * @param rawActivities
	 * @return
	 */
	public static ArrayList<String> mapActivityList2Activity (HashMap<String, String> rawActivities){
		ArrayList<String> activityList = new ArrayList<String>();
		// albatross: actionNumber, activityType, travelMode, isHome
		// ms: io, microenvironment, microenvironmentDetail, activity, noOfPersons, smoker, windowOpen
		
		Set<String> activityIDs = rawActivities.keySet();
		for(String id : activityIDs){
			String value = rawActivities.get(id);
			
			//*********************************************************************
			// TODO: THIS IS A WORKAROUND AS LONG AS WE ONLY GET THE CODES FROM THE ALBATROSS MODEL 
			// check if we have Albatross codes and convert them if necessary
			if(id.contains("travelMode")){
				value = ALBATROSS_TRAVELMODE2ACTIVITY.get(value);
			}else if(id.contains("activityType")){
				value = ALBATROSS_CODE2ACTIVITY.get(value);
			}
			// TODO: The "isHome" parameter is currently wrong, so ignore it
//			else if(id.contains("isHome")){
//				if(value.equals("1")||value.equals("true"))
//					value = "home";
//			}
			
			//*********************************************************************
			
			if(id.contains("activity")){
				Set<String> keys = ACTIVITY2EXPOSURE_ACTIVITY.keySet();
				for(String s : keys){
					if(value.contains(s)){
						activityList.add(ACTIVITY2EXPOSURE_ACTIVITY.get(s));
					}
				}
			}else if(id.contains("smoker")){
				value = "smoker";
				Set<String> keys = ACTIVITY2EXPOSURE_ACTIVITY.keySet();
				for(String s : keys){
					if(value.contains(s)){
						activityList.add(ACTIVITY2EXPOSURE_ACTIVITY.get(s));
					}
				}
			}else if(id.contains("windowOpen")){
				value = "aerate";
				Set<String> keys = ACTIVITY2EXPOSURE_ACTIVITY.keySet();
				for(String s : keys){
					if(value.contains(s)){
						activityList.add(ACTIVITY2EXPOSURE_ACTIVITY.get(s));
					}
				}
			}		
		}
		
		return activityList;
	}
	
	/**
	 * Maps from a list of activity attributes to exposure relevant activities and microenvironments
	 * @param rawActivities
	 * @return
	 */
	public static String mapActivityList2ME (HashMap<String, String> rawActivities){
		String me = "";
		// albatross: actionNumber, activityType, travelMode, isHome
		// ms: io, microenvironment, microenvironmentDetail, activity, noOfPersons, smoker, windowOpen
				
		Set<String> activityIDs = rawActivities.keySet();
		for(String id : activityIDs){
			String value = rawActivities.get(id);
					
			//*********************************************************************
			// TODO: THIS IS A WORKAROUND AS LONG AS WE ONLY GET THE CODES FROM THE ALBATROSS MODEL 
			// check if we have Albatross codes and convert them if necessary
			if(id.contains("travelMode")){
				value = ALBATROSS_TRAVELMODE2ACTIVITY.get(value);
			}else if(id.contains("activityType")){
				value = ALBATROSS_CODE2ACTIVITY.get(value);
			}
			// TODO: The "isHome" parameter is currently wrong, so ignore it
//			else if(id.contains("isHome")){
//				if(value.equals("1")||value.equals("true"))
//					value = "home";
//			}
					
			//*********************************************************************
					
			if(id.contains("microenvironment")||id.contains("activity")||id.contains("travel")){
				Set<String> keys = ACTIVITY2MICROENVIRONMENT.keySet();
				for(String s : keys){
					if(value.contains(s)){
						me = ACTIVITY2MICROENVIRONMENT.get(s);
					}
				}
			}
			
		}
		
		return me;
	}
	
	
//	public static String mapActivitytoExposureActivity(String activity){
//		Set<String> keys = ACTIVITY2EXPOSURE_ACTIVITY.keySet();
//		for(String s : keys){
//			if(activity.contains(s)){
//				return(ACTIVITY2EXPOSURE_ACTIVITY.get(s));
//			}
//		}
//		return null;
//	}
//	
//	public static String mapActivitytoMicroenvironment(String activity){
//		Set<String> keys = ACTIVITY2MICROENVIRONMENT.keySet();
//		for(String s : keys){
//			if(activity.contains(s)){
//				return(ACTIVITY2MICROENVIRONMENT.get(s));
//			}
//		}
//		return null;
//	}
}
