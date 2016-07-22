package org.uncertweb.api.om.result;

import java.math.BigInteger;

/**
 * Result representing an integer value, using Java's {@link java.math.BigInteger}
 * 
 * @author Kiesow
 * 
 */
public class IntegerResult implements IResult {

	private BigInteger value;

	/**
	 * Constructor
	 * 
	 * @param bigInteger
	 *            integer value of this result
	 */
	public IntegerResult(BigInteger bigInteger) {
		this.value = bigInteger;
	}

	// specific getter and setter
	public BigInteger getIntegerValue() {
		return value;
	}

	public void setIntegerValue(BigInteger v) {
		this.value = v;
	}

	// generic getter and setter
	public Object getValue() {
		return getIntegerValue();
	}

	public void setValue(Object v) {

		if (v instanceof BigInteger) {
			setIntegerValue((BigInteger) v);
		} else {
			throw new NumberFormatException(
					"Result value could not be set. Value has to be of type BigInteger.");
		}
	}

}
