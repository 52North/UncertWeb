package org.uncertweb.api.gml.geometry;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * class GML Point extends JTS point by a mandatory GML ID attribute
 * 
 * @author staschc
 *
 */
public class GmlPoint extends Point implements IGmlGeometry{
	
	/** GML ID of the point */
	private String gmlID;

	/**
	 * constructor
	 * 
	 * @param coordinates
	 * @param factory
	 * @param gmlID
	 */
	public GmlPoint(CoordinateSequence coordinates, GeometryFactory factory, String gmlID) {
		super(coordinates, factory);
		this.gmlID = gmlID;
	}

	/**
	 * @return the gmlID
	 */
	public String getGmlId() {
		return gmlID;
	}

	/**
	 * @param gmlID the gmlID to set
	 */
	public void setGmlID(String gmlID) {
		this.gmlID = gmlID;
	}

}
