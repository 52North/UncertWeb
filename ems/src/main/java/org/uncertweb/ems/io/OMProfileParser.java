package org.uncertweb.ems.io;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.PeriodType;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertweb.api.om.GeneralTimeInstant;
import org.uncertweb.api.om.GeneralTimeInterval;
import org.uncertweb.api.om.IGeneralTime;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.ObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.ems.data.profiles.AbstractProfile;
import org.uncertweb.ems.data.profiles.GeometryProfile;
import org.uncertweb.ems.exceptions.EMSInputException;
import org.uncertweb.ems.extension.profiles.Activity;
import org.uncertweb.ems.extension.profiles.ActivityProfile;
import org.uncertweb.ems.extension.profiles.MEProfile;
import org.uncertweb.ems.extension.profiles.Microenvironment;
import org.uncertweb.ems.util.ActivityMapping;
import org.uncertweb.ems.util.WeekdayMapping;

/**
 * Parses the activity O&M input into a profile containing the geometry and the activities if applicable
 * @author LydiaGerharz
 *
 */
public class OMProfileParser {
	
	int maxProfileSize = 30;
	
	/**
	 * Constructor
	 */
	public OMProfileParser(){
		
	}
	
	
	
	/**
	 *  Fills profile objects with information from Observation collection
	 *  @param iobs
	 *  @param netcdfList
	 *  @param minuteResolution
	 */
	public List<AbstractProfile> OM2Profiles(IObservationCollection iobs, ArrayList<DateTime> netcdfList, int minuteResolution){
		List<AbstractProfile> profilesList = new ArrayList<AbstractProfile>();

		// only add new spatial sampling features (for files with more than one property)
		URI observedProperty = null;
		
		// split into individuals (by procedure id)
		// procedure ID encodes the different individuals within a file
		HashMap<URI, IObservationCollection> obsCollMap = new HashMap<URI, IObservationCollection>();
//		HashMap<URI, HashMap<Interval,  ArrayList<Activity>>> activityMap = new HashMap<URI, HashMap<Interval, ArrayList<Activity>>>();
//		HashMap<URI, HashMap<Interval,  Microenvironment>> meMap = new HashMap<URI, HashMap<Interval, Microenvironment>>();		
	
		HashMap<String, String> rawActivities = new  HashMap<String, String>();
		ArrayList<Interval> newTimeList = new ArrayList<Interval>();
		URI lastProcedure = null;
		
		for (AbstractObservation obs : iobs.getObservations()) {  		
			
			// if current procedure ID (=individual) is not contained in the list, add it
			if(!obsCollMap.containsKey(obs.getProcedure())){
				obsCollMap.put(obs.getProcedure(), new ObservationCollection());
				// add new activity and microenvironment list for this individual
//				activityMap.put(obs.getProcedure(), new HashMap<Interval, ArrayList<Activity>>());
//				meMap.put(obs.getProcedure(), new HashMap<Interval, Microenvironment>());
				lastProcedure = obs.getProcedure();
			}
				
			 // for the first observation
			if(observedProperty == null)
				observedProperty = obs.getObservedProperty();					
			
			// if the selected observed property occurs, update the lists
			if(observedProperty.equals(obs.getObservedProperty())){				
				// 1) process time object to interval	
				TimeObject timeObject = obs.getPhenomenonTime();				
				newTimeList = handleTimeFromOM(timeObject, netcdfList, minuteResolution);
				// TODO: how to treat missing travel time?
				
				// only if time period could be matched
				if(!newTimeList.isEmpty()){										
					// 2) geometry extraction
					// add current observation to collection for each additional timestep
					for(Interval newTime : newTimeList){
						AbstractObservation newObs = new UncertaintyObservation(new TimeObject(newTime), new TimeObject(newTime), 
								obs.getProcedure(), obs.getObservedProperty(), obs.getFeatureOfInterest(), new UncertaintyResult(new ContinuousRealisation(new ArrayList<Double>())));
						obsCollMap.get(obs.getProcedure()).addObservation(newObs);
					}								
					
					// 3) activity mapping
					// use last activity list to determine exposure relevant activities and locations
//					if(rawActivities.size()!=0)
//						handleActivitiesFromOM(rawActivities, activityMap, meMap, newTimeList, lastProcedure);
//					
//					// empty list for next collection of activity observations
//					rawActivities = new  HashMap<String, String>();				
				}							
			}
						
			// for each observation: collect activities (observedProperty) 
//			rawActivities.put(obs.getObservedProperty().getPath(), obs.getResult().getValue().toString());
		}	
		
		//  process the last activity list
		// 3) activity mapping
		// use last activity list to determine exposure relevant activities and locations
//		if(rawActivities.size()!=0)
//			handleActivitiesFromOM(rawActivities, activityMap, meMap, newTimeList, lastProcedure);
			
		//  make profiles from the OM collections and activity lists
		Set<URI> individuals = obsCollMap.keySet();
		for(URI uri : individuals){
			// check if there are valid activities an microenvironments
			//TODO: for the moment we limit the profiles number to 10. This has to be changed afterwards!!!
//			if(profilesList.size()<maxProfileSize){
//				if(!meMap.get(uri).isEmpty()){
//					if(!activityMap.get(uri).isEmpty()){
//						profilesList.add(new ActivityProfile(obsCollMap.get(uri), meMap.get(uri), activityMap.get(uri)));
//					}else{
//						profilesList.add(new MEProfile(obsCollMap.get(uri), meMap.get(uri)));
//					}
//				}else 
				if(!obsCollMap.get(uri).getObservations().isEmpty()){ // only make a profile if observations are available
					profilesList.add(new GeometryProfile(obsCollMap.get(uri)));
				}
//			}			
		}
		if(!profilesList.isEmpty())
			return profilesList;
		else
			throw new EMSInputException("No valid profiles could be produced from the OM file.");
	}
	
	/**
	 * Handles the translation of general time to concrete time for, e.g., the Albatross output
	 * @param timeObject
	 * @param netcdfList
	 * @return
	 */
	private ArrayList<Interval> handleTimeFromOM(TimeObject timeObject, ArrayList<DateTime> netcdfList, int minuteResolution){
		Interval time = null;
	
		// make General time to datetime
		if(timeObject.isGeneralTime()){
			// matching of time
			IGeneralTime gt = timeObject.getGeneralTime();
			if(gt instanceof GeneralTimeInstant){
				GeneralTimeInstant timeInstant = (GeneralTimeInstant) gt;			
				
				// loop through netcdf dates to find the respective weekday for the observation
				for(DateTime dt : netcdfList){
					String dtDay = dt.dayOfWeek().getAsText(new Locale("en"));
					// if day and hour are correct, use this date for the generic time
					if(timeInstant.getDay()==WeekdayMapping.DAY2INTEGER_EN.get(dtDay)&&
							timeInstant.getHour()==dt.hourOfDay().get()){
						time = new Interval(dt.plusMinutes(timeInstant.getMinute()-dt.getMinuteOfHour()), 
												dt.plusMinutes(timeInstant.getMinute()-dt.getMinuteOfHour()));
						break;
					}	
				}			
			}else{
				GeneralTimeInterval timeInterval = (GeneralTimeInterval) gt;
				DateTime start = null, end = null;
				
				// loop through netcdf dates to find the respective weekday for the observation
				for(DateTime dt : netcdfList){
					String dtDay = dt.dayOfWeek().getAsText(new Locale("en"));
					int startDay = timeInterval.getStart().getDay();
					int endDay = timeInterval.getEnd().getDay();
					// if day and hour are correct, use this date for the generic time
					if(start==null&&timeInterval.getStart().getDay()==WeekdayMapping.DAY2INTEGER_EN.get(dtDay)&&
							timeInterval.getStart().getHour()==dt.hourOfDay().get()){
						start = dt.plusMinutes(timeInterval.getStart().getMinute()-dt.getMinuteOfHour());												
					}	
					// if day and hour are correct, use this date for the generic time
					if(start!=null&&end==null&&timeInterval.getEnd().getDay()==WeekdayMapping.DAY2INTEGER_EN.get(dtDay)&&
							timeInterval.getEnd().getHour()==dt.hourOfDay().get()){
						end = dt.plusMinutes(timeInterval.getEnd().getMinute()-dt.getMinuteOfHour());	
						break;
					}	
				}
				
				if(start!=null&end!=null)
					time = new Interval(start, end);
			}
		}else if(timeObject.isInterval()){
			time = timeObject.getInterval();
		}else { //TODO: what to do with time instants?
			DateTime dt = timeObject.getDateTime();
			time = new Interval(dt, dt);
		}		
		
		ArrayList<Interval> newTimeList = new ArrayList<Interval>();
		if(time!=null){
			//changed to match hourly forecasts of NetCDF file
			DateTime start = time.getStart();
			DateTime end = time.getEnd();
			DateTime newTime = start;
			DateTime oldTime=null;
			while (newTime.isBefore(end)){
				oldTime=newTime;
				newTime = newTime.plusHours(1);
				newTime = newTime.minusMinutes(newTime.getMinuteOfHour());
				newTime = newTime.minusSeconds(newTime.getSecondOfMinute());
				if (newTime.isBefore(end)){
					newTimeList.add(new Interval(oldTime,newTime));
				}
			}
			if (oldTime!=null){
				newTimeList.add(new Interval(oldTime,end));
			}
			else {
				newTimeList.add(new Interval(start,end));
			}
			
//			// if the interval is too long, create additional timesteps according to the minuteResolution				
//			int minuteDuration = time.toPeriod(PeriodType.minutes()).getMinutes();
//			if(minuteDuration>minuteResolution){	
//				int rep = 0;
//				while(minuteDuration>=minuteResolution){
//					newTimeList.add(new Interval(time.getStart().plusMinutes(minuteResolution*rep),time.getStart().plusMinutes(minuteResolution*(rep+1))));
//					minuteDuration -= minuteResolution;
//					rep++;
//				}
//			}else
//				newTimeList.add(time);
		}
			
		return newTimeList;
	}
	
	
	
	/**
	 * Handles the translation of activities from the observation collection into exposure relevant factors
	 * @param rawActivities
	 * @param activityMap
	 * @param meMap
	 * @param newTimeList
	 * @param lastProcedure
	 */
	private void handleActivitiesFromOM(HashMap<String, String> rawActivities, HashMap<URI, 
			HashMap<Interval,  ArrayList<Activity>>> activityMap, HashMap<URI, 
			HashMap<Interval,  Microenvironment>> meMap,
			ArrayList<Interval> newTimeList, URI lastProcedure){
		// albatross: actionNumber, activityType, travelMode, isHome
		// ms: io, microenvironment, microenvironmentDetail, activity, noOfPersons, smoker, windowOpen
		
		// try to identify activities
		ArrayList<String> actMapping = ActivityMapping.mapActivityList2Activity(rawActivities);
					
		// if relevant activity could be identified, add it for all timesteps
		if(!actMapping.isEmpty()){
			ArrayList<Activity> activityList = new ArrayList<Activity>();
			for(String a : actMapping){
				activityList.add(new Activity(a));
			}
			for(Interval newTime : newTimeList){
				activityMap.get(lastProcedure).put(newTime, activityList);						
			}	
						
		}
						
		// try to identify microenvironments
		String meMapping = ActivityMapping.mapActivityList2ME(rawActivities);
					
		// if  microenvironment could be identified, add it for all timesteps
		if(!meMapping.equals("")){
			for(Interval newTime : newTimeList){
				meMap.get(lastProcedure).put(newTime, new Microenvironment(meMapping));				
			}
		}
		
	}
	

	
}
