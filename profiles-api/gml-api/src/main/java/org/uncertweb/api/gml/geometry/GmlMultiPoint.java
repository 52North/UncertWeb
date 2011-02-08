package org.uncertweb.api.gml.geometry;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

public class GmlMultiPoint extends MultiPoint implements IGmlGeometry{

	private String gmlID;
	
	public GmlMultiPoint(Point[] points, GeometryFactory factory, String gmlId) {
		super(points, factory);
		setGmlID(gmlId);
	}

	@Override
	public String getGmlId() {
		// TODO Auto-generated method stub
		return gmlID;
	}

	/**
	 * @param gmlID the gmlID to set
	 */
	public void setGmlID(String gmlID) {
		this.gmlID = gmlID;
	}

}
