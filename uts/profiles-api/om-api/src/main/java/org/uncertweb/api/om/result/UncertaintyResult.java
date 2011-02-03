package org.uncertweb.api.om.result;

import org.uncertml.IUncertainty;

/**
 * Result representing an uncertainty value including unit of measurement
 * (optional)
 * 
 * @author Kiesow
 * 
 */
public class UncertaintyResult implements IResult {

	// TODO restrict value to AbstractUncertainty, redirect generic getter and
	// setter
	private IUncertainty value;
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

	public Object getUncertaintyValue() {
		return value;
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
	public Object getValue() {
		return getUncertaintyValue();
	}

	public void setValue(Object v) {
		setUncertaintyValue((IUncertainty)v);
	}

}
