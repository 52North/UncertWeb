package org.n52.sos.uncertainty.ds.pgsql;

import org.uncertml.IUncertainty;

/**
 * Wrapper to hold a requested uncertainty plus corresponding data
 * @author Kiesow
 *
 */
public final class WrappedUncertainty {

	private final String obsID;
	private final String gmlID;
	private final String valueUnit;
	private final IUncertainty unc;
	
	/**
	 * constructor
	 * @param observationID the corresponding observation's ID 
	 * @param gmlID gml identifier
	 * @param valueUnit value unit 
	 * @param uncertainty the uncertainty itself
	 */
	public WrappedUncertainty(String observationID, String gmlID, String valueUnit, IUncertainty uncertainty) {
		this.obsID = observationID;
		this.gmlID = gmlID;
		this.valueUnit = valueUnit;
		this.unc = uncertainty;
	}
	
	public String getObservationID() {
		return obsID;
	}
	public String getGmlID() {
		return gmlID;
	}
	public String getValueUnit() {
		return valueUnit;
	}
	public IUncertainty getUncertainty() {
		return unc;
	}
}
