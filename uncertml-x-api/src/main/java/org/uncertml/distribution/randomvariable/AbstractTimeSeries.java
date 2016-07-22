package org.uncertml.distribution.randomvariable;

import java.net.URL;

/**
 * abstract super class for all spatial random fields
 * 
 * @author staschc
 *
 */
public abstract class AbstractTimeSeries {
	
	/** reference to a set of samples; should be encoded as NetCDF-U or U-O&M*/
	protected SampleReference samples;

	/**
	 * @return the samples
	 */
	public SampleReference getSamples() {
		return samples;
	}

	/**
	 * @param samples the samples to set
	 */
	public void setSamples(SampleReference samples) {
		this.samples = samples;
	}
	
}
