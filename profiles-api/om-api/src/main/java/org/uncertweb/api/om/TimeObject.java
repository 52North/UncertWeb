package org.uncertweb.api.om;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * Wrapper to hold one representation of time exclusively, e.g. a time instant
 * or an period of time
 * 
 * @author Kiesow, Stasch
 * 
 */
public class TimeObject {

	/** gml:id of time object; if href attribute is set, this attribute is null */
	private String id;
	
	/** timeInstant of time object; if href attribute is set, this attribute is null */
	private DateTime dateTime;
	
	/**interval, if time object is TimePeriod; if href attribute is set, this attribute is null*/
	private Interval interval;
	
	/**reference; usually is null; if not, other properties are null*/
	private String href;

	/**
	 * Constructor
	 * 
	 * @param dateTime
	 *            a point of time
	 */
	public TimeObject(String id, DateTime dateTime) {
		this.dateTime = dateTime;
		this.id=id;
	}

	/**
	 * Constructor
	 * 
	 * @param timePeriod
	 *            a period of time
	 */
	public TimeObject(String id, Interval interval) {
		this.interval = interval;
		this.id=id;
	}
	
	/**
	 * Constructor
	 * 
	 * @param href
	 *            reference to another time property
	 */
	public TimeObject(String href) {
		this.href = href;
	}

	// getters and setters
	
	public String getId() {
		return id;
	}
	
	/**
	 * sets a new id and deletes the reference
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.href = null;
		this.id = id;
	}

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
	
	public String getHref() {
		return href;
	}

	/**
	 * sets a new time reference and deletes all other representations
	 * 
	 * @param href
	 */
	public void setHref(String href) {
		this.href = href;
	}
	
	

}
