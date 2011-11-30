package org.uncertml.distribution.randomvariable;

import java.net.URL;

/**
 * abstract super class for all spatio-temporal random fields
 * 
 * @author staschc
 *
 */
public abstract class AbstractSpatioTemporalField {

	/** reference to a set of samples; should be encoded as NetCDF-U or U-O&M*/
	protected URL samples;
	
}
