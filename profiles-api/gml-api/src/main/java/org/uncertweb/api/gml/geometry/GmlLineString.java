package org.uncertweb.api.gml.geometry;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * class extends JTS LineString by a mandatory GML ID attribute
 * 
 * @author staschc
 *
 */
public class GmlLineString extends LineString implements IGmlGeometry{
	
	/** GML ID of the linestring */
	private String gmlID;

	/**
	 * constructor
	 * 
	 * @param points
	 * @param factory
	 * @param gmlID
	 */
	public GmlLineString(CoordinateSequence points, GeometryFactory factory, String gmlID) {
		super(points,factory);
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
	public void setGmlId(String gmlID) {
		this.gmlID = gmlID;
	}

}
