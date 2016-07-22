package org.uncertweb.api.om.result;

/**
 * Result representing a boolean
 * 
 * @author Kiesow
 * 
 */
public class BooleanResult implements IResult {

	private boolean value;

	/**
	 * Constructor
	 * 
	 * @param v
	 *            boolean value of this result
	 */
	public BooleanResult(boolean v) {
		this.value = v;
	}

	// specific getter & setter
	public Boolean getBooleanValue() {
		return Boolean.valueOf(value);
	}

	public void setBooleanValue(boolean v) {
		this.value = v;
	}

	// generic getter & setter
	public Object getValue() {
		return getBooleanValue();
	}

	public void setValue(Object v) {

		// as cast and instanceof are not allowed for primitive
		// types, values have to be converted this way
		if (v.toString().equalsIgnoreCase("true")) {
			setBooleanValue(true);
		} else if (v.toString().equalsIgnoreCase("false")) {
			setBooleanValue(false);
		} else {
			throw new ClassCastException(
					"Result value could not be set. Value has to be 'true' or 'false'.");
		}
	}
}
