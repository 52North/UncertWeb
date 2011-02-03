package org.uncertweb.api.gml.io;

import com.vividsolutions.jts.geom.Geometry;

/**
 * interface for parsing geometries defined in the UncertWeb GML profile into JTS geometries
 * 
 * @author staschc
 *
 */
public interface IGeometryParser {
	
	/**
	 * method for parsing a String containing an UncertWeb GML file and converting it into a JTS geometry
	 * 
	 * @param geometryXml
	 * 				String containing UncertWeb GML geometries encoded as XML
	 * @return
	 * 				Returns JTS geometry containing the geometry or geometries
	 * @throws Exception
	 * 				if the parsing fails
	 */
	public Geometry parseUwGeometry(String geometryXml) throws Exception;
	
}
