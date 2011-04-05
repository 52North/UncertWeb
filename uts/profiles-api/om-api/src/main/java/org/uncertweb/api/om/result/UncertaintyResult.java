package org.uncertweb.api.om.result;

import org.uncertml.IUncertainty;

/**
 * Result representing an uncertainty value including unit of measurement
 * (optional)
 * 
 * @author Kiesow, staschc
 * 
 */
public class UncertaintyResult implements IResult {

	/**uncertainty value of the observation*/
	private IUncertainty value;
	
	/** UCUM code of the unit of measure (optional)*/
	private String uom;

	
	/**
	 * Constructor
	 * 
	 * @param v
	 *            value of this result
	 * @param u
	 *            (optional) unit of measurement
	 */
	public UncertaintyResult(IUncertainty v, String u) {
		this.value = v;
		this.uom = u;
	}

	/**
	 * Constructor
	 * 
	 * @param v
	 *            value of this result
	 */
	public UncertaintyResult(IUncertainty v) {
		this(v, null);
	}

	// specific getter and setter

	public IUncertainty getUncertaintyValue() {
		return (IUncertainty)value;
	}

	public void setUncertaintyValue(IUncertainty v) {
		this.value = v;
	}

	public String getUnitOfMeasurement() {
		return uom;
	}

	public void setUnitOfMeasurement(String u) {
		this.uom = u;
	}

	// generic getter and setter
	public IUncertainty getValue() {
		return getUncertaintyValue();
	}

	public void setValue(Object v) {
		setUncertaintyValue((IUncertainty)v);
	}

}
