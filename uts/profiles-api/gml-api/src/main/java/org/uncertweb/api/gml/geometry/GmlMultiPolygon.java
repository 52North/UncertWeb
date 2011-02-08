package org.uncertweb.api.gml.geometry;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class GmlMultiPolygon extends MultiPolygon implements IGmlGeometry{
	
	private String gmlId;

	public GmlMultiPolygon(Polygon[] polygons, GeometryFactory factory, String gmlId) {
		super(polygons, factory);
		
		// TODO Auto-generated constructor stub
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public String getGmlId() {
		return gmlId;
	}

}
