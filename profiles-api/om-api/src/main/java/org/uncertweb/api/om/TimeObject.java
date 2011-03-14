package org.uncertweb.api.om;

import java.net.URI;

import net.opengis.gml.x32.TimePeriodType;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Wrapper to hold one representation of time exclusively, e.g. a time instant
 * or an period of time
 * 
 * @author Kiesow, Stasch
 * 
 */
public class TimeObject {

	
	/** timeInstant of time object; if href attribute is set, this attribute is null */
	private DateTime dateTime;
	
	/**interval, if time object is TimePeriod; if href attribute is set, this attribute is null*/
	private Interval interval;
	
	/**reference; usually is null; if not, other properties are null*/
	private URI href;

	/**
	 * constructor for time instant with ISO 8601 string
	 * 
	 * @param iso8601time
	 * 			ISO 8601 string of time instant
	 */
	public TimeObject(String iso8601time){
		this.dateTime = parseTimePosition(iso8601time);
	}
	
	/**
	 * constructor for time period with ISO 8601 strings
	 * 
	 * @param beginIso8601String
	 * 			ISO 8601 string of begin instant
	 * @param endIso8601String
	 * 			ISO 8601 string of end instant
	 */
	public TimeObject(String beginIso8601String, String endIso8601String){
		DateTime beginTime = parseTimePosition(beginIso8601String);
		DateTime endTime = parseTimePosition(endIso8601String);
		this.interval = new Interval(beginTime.getMillis(), endTime
				.getMillis());
	}
	
	/**
	 * Constructor
	 * 
	 * @param dateTime
	 *            a point of time
	 */
	public TimeObject(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	/**
	 * Constructor
	 * 
	 * @param timePeriod
	 *            a period of time
	 */
	public TimeObject(Interval interval) {
		this.interval = interval;
	}
	
	/**
	 * Constructor
	 * 
	 * @param href
	 *            reference to another time property
	 */
	public TimeObject(URI href) {
		this.href = href;
	}

	// getters and setters
	public DateTime getDateTime() {
		return dateTime;
	}

	/**
	 * sets a new time instance and deletes all other representations
	 * 
	 * @param dateTime
	 */
	public void setDateTime(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Interval getInterval() {
		return interval;
	}

	/**
	 * sets a new time interval and deletes all other representations
	 * 
	 * @param interval
	 */
	public void setInterval(Interval interval) {
		this.interval = interval;
	}
	
	public URI getHref() {
		return href;
	}

	/**
	 * sets a new time reference and deletes all other representations
	 * 
	 * @param href
	 */
	public void setHref(URI href) {
		this.href = href;
	}
	
	/**
	 * helper method for parsing timePosition to DateTime
	 * 
	 * @param timePosition
	 *            time as a string e.g. 1970-01-01T00:00:00Z
	 * @return time as an Object
	 */
	private DateTime parseTimePosition(String timePosition) {
		DateTime dateTime = null;

		DateTimeFormatter dtf = ISODateTimeFormat.dateTimeParser();
		dateTime = dtf.withOffsetParsed().parseDateTime(timePosition);

		return dateTime;
	}
	
	

}
