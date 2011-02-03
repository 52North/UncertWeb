package org.uncertweb.api.gml.geometry;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * class extends JTS Polygon by a mandatory GML ID attribute
 * 
 * @author staschc
 *
 */
public class GmlPolygon extends Polygon implements IGmlGeometry{
	
	/** GML ID of the linestring */
	private String gmlID;

	/**
	 * constructor
	 * 
	 * @param points
	 * @param factory
	 * @param gmlID
	 */
	public GmlPolygon(LinearRing shell, LinearRing[] holes, GeometryFactory factory, String gmlID) {
		super(shell,holes,factory);
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

