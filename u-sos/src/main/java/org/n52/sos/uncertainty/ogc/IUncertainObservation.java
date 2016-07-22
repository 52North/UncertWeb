package org.n52.sos.uncertainty.ogc;

import org.uncertweb.api.om.DQ_UncertaintyResult;

/**
 * Interface to handle uncertain observations
 * 
 * @author Kiesow
 * 
 */
public interface IUncertainObservation extends IUncertainObject {

	/** get the xml type name of this observation */
	public String getName();

	/**
	 * get the gml id of this observation (consisting of code space and
	 * identifier; not to be confused with observation id/obsID)
	 */
	public String getIdentifier();
	
	/**
	 * get the gml id of this observation (consisting of code space and
	 * identifier; not to be confused with observation id/obsID)
	 */
	public void setIdentifier(String identifier);

	/**
	 * returns data quality as uncertainties, replacing om1 observations quality
	 */
	public DQ_UncertaintyResult[] getUncQuality();
	
	/**
	 * sets data quality as uncertainties, replacing om1 observations quality
	 */
	public void setUncQuality(DQ_UncertaintyResult[] resultQuality);
	
	/**
	 * returns unit of measurement
	 */
	public String getUnitsOfMeasurement();
}
