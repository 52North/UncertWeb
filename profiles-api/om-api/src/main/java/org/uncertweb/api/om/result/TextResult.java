package org.uncertweb.api.om.result;

/**
 * Result representing a text value
 *
 * @author Kiesow
 *
 */
public class TextResult implements IResult {

	private String value;

	/**
	 * Constructor
	 *
	 * @param v
	 *            text value of this result
	 */
	public TextResult(String v) {
		this.value = v;
	}

	// specific getter and setter
	public String getTextValue() {
		return value;
	}

	public void setTextValue(String v) {
		this.value = v;
	}

	// generic getter and setter
	public Object getValue() {
		return getTextValue();
	}

	public void setValue(Object v) {
		setTextValue(v.toString());
	}

}
