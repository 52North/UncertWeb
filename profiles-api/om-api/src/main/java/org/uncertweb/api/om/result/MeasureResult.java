package org.uncertweb.api.om.result;

/**
 * Result representing a measured value including unit of measurement
 * 
 * @author Kiesow
 * 
 */
public class MeasureResult implements IResult {

	private double value;
	private String uom;

	/**
	 * Constructor
	 * 
	 * @param v
	 *            double value of this result
	 * @param u
	 *            unit of measurement
	 */
	public MeasureResult(double v, String u) {
		this.value = v;
		this.uom = u;
	}

	// specific getters and setters
	public double getMeasureValue() {
		return value;
	}

	public void setMeasureValue(double v) {
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
		return new Double(getMeasureValue());
	}

	public void setValue(Object v) {

		try {
			setMeasureValue(Double.parseDouble(v.toString()));
		} catch (NumberFormatException e) {
			throw new NumberFormatException(
					"Result value could not be set. Value has to be of type double.");
		}
	}

}
