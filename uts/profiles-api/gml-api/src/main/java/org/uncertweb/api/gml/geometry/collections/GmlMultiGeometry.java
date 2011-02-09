package org.uncertweb.api.gml.geometry.collections;

import org.uncertweb.api.gml.geometry.IGmlGeometry;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * class extends JTS GeometryCollection with an gmlId attribute
 * 
 * @author staschc
 *
 */
public class GmlMultiGeometry extends GeometryCollection implements IGmlGeometry{

	/** GML Id attribute*/
	private String gmlId;
	
	/**
	 * 
	 * 
	 * @param geometries
	 * @param factory
	 * @param gmlId
	 * @throws Exception 
	 */
	public GmlMultiGeometry(Geometry[] geometries, GeometryFactory factory, String gmlId) throws Exception {
		super(geometries, factory);
		for (int i=0;i<geometries.length;i++){
			if (!(geometries[i] instanceof IGmlGeometry)){
				throw new Exception("Only GmlGeometries are allowed in GmlMultiGeometry!!");
			}
		}
		this.gmlId = gmlId;
	}

	/**
	 * @return the gmlId
	 */
	public String getGmlId() {
		return gmlId;
	}

	/**
	 * @param gmlId the gmlId to set
	 */
	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}
	
	

}
