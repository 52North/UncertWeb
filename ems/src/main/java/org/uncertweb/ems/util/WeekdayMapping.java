package org.uncertweb.ems.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides mapping between weekday names from the DateTime class to the day type of the GeneralTimeInstant class
 * @author LydiaGerharz
 *
 */
public final class WeekdayMapping {

	public static final Map<String,Integer> DAY2INTEGER_EN = new HashMap<String,Integer>(){
		private static final long serialVersionUID = -1592467442783708174L;

		{
			put("Monday",1);
			put("Tuesday",2);
			put("Wednesday",3);
			put("Thursday",4);
			put("Friday",5);
			put("Saturday",6);
			put("Sunday",7);
		}
	};

	public static final Map<String,Integer> DAY2INTEGER_DE = new HashMap<String,Integer>(){
		private static final long serialVersionUID = -5687244043463227994L;

		{
			put("Montag",1);
			put("Dienstag",2);
			put("Mittwoch",3);
			put("Donnerstag",4);
			put("Freitag",5);
			put("Samstag",6);
			put("Sonntag",7);
		}
	};

}
