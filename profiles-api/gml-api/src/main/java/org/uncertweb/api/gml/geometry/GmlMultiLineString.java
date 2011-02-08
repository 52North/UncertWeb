package org.uncertweb.api.gml.geometry;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class GmlMultiLineString extends MultiLineString implements IGmlGeometry{

	private String gmlId;
	
	public GmlMultiLineString(LineString[] lineStrings, GeometryFactory factory, String gmlId) {
		super(lineStrings, factory);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getGmlId() {
		return gmlId;
	}
	
	public void setGmlId(String gmlId) {
		this.gmlId=gmlId;
	}

}
