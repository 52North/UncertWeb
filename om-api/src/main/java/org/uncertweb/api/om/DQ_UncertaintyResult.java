package org.uncertweb.api.om;

import org.uncertml.IUncertainty;

/**
 * Data quality uncertainty result which can be contained in resultQuality of an observation.
 *
 * @author Kiesow, staschc
 *
 */
public class DQ_UncertaintyResult {


	/**array containing uncertainties*/
	private IUncertainty[] values;

	/**definition of value unit*/
	private String uom;

	/**
	 * Constructor
	 *
	 * @param values
	 *			values representing uncertainties
	 * @param valueUnit
	 * 			the value's unit
	 */
	public DQ_UncertaintyResult(IUncertainty[] values, String uom) {

		this.setValues(values);
		this.uom = uom;
	}



	// getters and setters



	/**
	 *
	 *
	 * @return Returns uncertainty values
	 */
	public IUncertainty[] getValues() {
		return values;
	}

	/**
	 *
	 * @param values
	 * 			uncertainty values
	 */
	public void setValues(IUncertainty[] values) {
		this.values = values;
	}



	/**
	 * @return the uom
	 */
	public String getUom() {
		return uom;
	}



	/**
	 * @param uom the uom to set
	 */
	public void setUom(String uom) {
		this.uom = uom;
	}


}
