package org.uncertweb.api.om;

import org.uncertml.IUncertainty;

/**
 * Data quality uncertainty result which can be contained in resultQuality of an observation.
 * 
 * @author Kiesow, staschc
 *
 */
public class DQ_UncertaintyResult {

	/**gml id*/
	private String id;
	
	/**uu id inherited from DQ_AbstractResult*/
	private String uuid;
	
	/**array containing uncertainties*/
	private IUncertainty[] values;
	
	/**definition of value unit*/
	private UnitDefinition valueUnit;
	
	/**
	 * Constructor
	 * 
	 * @param values 
	 *			values representing uncertainties
	 * @param valueUnit 
	 * 			the value's unit
	 */
	public DQ_UncertaintyResult(IUncertainty[] values, UnitDefinition valueUnit) {
		
		this.setValues(values);
		this.valueUnit = valueUnit;
	}
	
	/**
	 * Constructor
	 * 
	 * @param id (optional)
	 * 			gml id
	 * @param uuid (optional)
	 * 			uu id inherited from DQ_AbstractResult
	 * @param values 
	 * 			values of uncertainties
	 * @param valueUnit 
	 * 			the value's uni
	 */
	public DQ_UncertaintyResult(String id, String uuid, IUncertainty[] value, UnitDefinition valueUnit) {
		this(value, valueUnit);
		
		this.setId(id);
		this.uuid = uuid;
	}


	// getters and setters
	/**
	 * 
	 * @return Returns GML ID
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * 
	 * @param id
	 * 		GML ID
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * 
	 * @return Returns UU ID inherited from DQ_AbstractResult
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * 
	 * @param uuid
	 * 			UU ID inherited from DQ_AbstractResult
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

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
	 * 
	 * @return Returns the value's unit
	 */
	public UnitDefinition getValueUnit() {
		return valueUnit;
	}
	
	/**
	 * 
	 * @param valueUnit
	 * 			the value's unit
	 */
	public void setValueUnit(UnitDefinition valueUnit) {
		this.valueUnit = valueUnit;
	}

}
