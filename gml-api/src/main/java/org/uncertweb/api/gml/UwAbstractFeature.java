package org.uncertweb.api.gml;


import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Super class for all GML features used in UncertWeb
 * 
 * @author staschc
 *
 */
public abstract class UwAbstractFeature {
	
	/**GML id of feature*/
	private String gmlId;
	
	/**bounded by element describing the boundary of the feature*/
	private Envelope boundedBy;
	
	/** location attribute which contains one of the geometries defined in the UncertWeb GML profile*/
	private Geometry location;

	///////////////////////////////////////////////////////////
	//getters and setters
	public String getGmlId() {
		return gmlId;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public Envelope getBoundedBy() {
		return boundedBy;
	}

	public void setBoundedBy(Envelope boundedBy) {
		this.boundedBy = boundedBy;
	}

	public Geometry getLocation() {
		return location;
	}

	public void setLocation(Geometry location) {
		this.location = location;
	}
}
