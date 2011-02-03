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
		reset();
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
		reset();
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
		reset();
		this.id = null;
		this.href = href;
	}

	/**
	 * helper method to set all attributes null
	 */
	private void reset() {
		this.dateTime = null;
		this.interval = null;
		this.href = null;
	}

}
