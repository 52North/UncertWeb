package org.uncertweb.api.gml.io;

import org.apache.xmlbeans.XmlException;
import org.json.JSONException;
import org.uncertweb.api.gml.UwAbstractFeature;

import com.vividsolutions.jts.geom.Geometry;


/**
 * Encoder for encoding JTS geometries representing GML geometries defined for UncertWeb into XML Strings
 * 
 * @author staschc
 *
 */
public interface IGeometryEncoder {

	/**
	 * method for encoding JTS geometries representing GML geometries defined for UncertWeb into XML Strings
	 * 
	 * @param geom
	 * 			JTS geometry representing GML geometry defined for UncertWeb
	 * @return
	 * 			Returns GML String of the geometry
	 * @throws XmlException 
	 * 			If encoding fails
	 * @throws JSONException 
	 */
	public String encodeGeometry(Geometry geom) throws XmlException, JSONException;
	
	/**
	 * method for encoding GML features as defined in the UncertWeb GML profile into XML strings
	 * 
	 * @param feature
	 * 			UncertWeb representation of featrue which shall be encoded
	 * @return
	 * 			Returns GML String of the feature
	 */
	public String encodeFeature(UwAbstractFeature feature);
}
