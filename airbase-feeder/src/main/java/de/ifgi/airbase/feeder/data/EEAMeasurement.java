package de.ifgi.airbase.feeder.data;

import org.joda.time.DateTime;

/**
 * @author Christian Autermann
 */
public class EEAMeasurement {
	private double value;
	private int quality;
	private DateTime time;

	/**
	 * Constructs a new {@code EEAMeasurement}
	 * @param value the value
	 * @param quality the quality
	 * @param time the time
	 */
	public EEAMeasurement(double value, int quality, DateTime time) {
		this.value = value;
		this.quality = quality;
		this.time = time;
	}

	/**
	 * @return whether the quality is greater 0
	 */
	public boolean isValid(){
		return getQuality() > 0;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return this.value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * @return the quality
	 */
	public int getQuality() {
		return this.quality;
	}

	/**
	 * @param quality
	 *            the quality to set
	 */
	public void setQuality(int quality) {
		this.quality = quality;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(DateTime time) {
		this.time = time;
	}

	/**
	 * @return the time
	 */
	public DateTime getTime() {
		return this.time;
	}

}