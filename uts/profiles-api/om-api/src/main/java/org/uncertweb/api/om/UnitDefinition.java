package org.uncertweb.api.om;

/**
 * class represents a unit definition; currently only contains the global
 * identifier plus the mandatory local identifier contained in the gmlId
 * attribute
 * 
 * @author staschc
 * 
 */
public class UnitDefinition {

	/** global identifier of the unit */
	//TODO maybe change to additional class and add codespace to identifier
	private String identifier;

	/** local identifier of the unit; has to be provided */
	private String gmlId;

	/**
	 * constructor
	 * 
	 * @param identifier
	 *            global identifier of the unit
	 */
	public UnitDefinition(String identifier) {
		setIdentifier(identifier);
	}

	/**
	 * constructor
	 * 
	 * @param identifier
	 *            global identifier of the unit
	 */
	public UnitDefinition(String identifier, String gmlId) {
		setIdentifier(identifier);
		setGmlId(gmlId);
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the gmlId
	 */
	public String getGmlId() {
		return gmlId;
	}

	/**
	 * @param gmlId
	 *            the gmlId to set
	 */
	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

}
