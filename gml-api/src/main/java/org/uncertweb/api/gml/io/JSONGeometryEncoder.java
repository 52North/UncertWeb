package org.uncertweb.api.gml.io;

import org.apache.xmlbeans.XmlException;
import org.json.JSONException;
import org.json.JSONStringer;
import org.uncertweb.api.gml.UwAbstractFeature;
import org.uncertweb.api.gml.UwGMLUtil;
import org.uncertweb.api.gml.geometry.RectifiedGrid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * JSON encoder for encoding geometries as defined in the
 * UncertWeb GML Profile
 * 
 * follows the GeoJSON specification of http://www.geojson.org/geojson-spec.html
 * 
 * @author staschc
 *
 */
public class JSONGeometryEncoder implements IGeometryEncoder{

	/**
	 * 
	 * not yet implemented, as no feature types are defined in UncertWeb yet
	 */
	//TODO implement
	@Override
	public String encodeFeature(UwAbstractFeature feature) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * method for encoding geometries to JSON objects by passing an already used writer,
	 * this method can be used, if the geometry is part of another JSON object like an O&M
	 * observation
	 * 
	 * @param writer
	 * 			writer to which the geometry should be written
	 * @param geom
	 * 		geometry which should be written
	 * @throws JSONException 
	 */
	public void encodeGeometry(JSONStringer writer, Geometry geom) throws JSONException{
		if (geom instanceof Point) {
			encodePoint(writer,(Point) geom);
		} else if (geom instanceof Polygon) {
			encodePolygon(writer,(Polygon) geom);
		} else if (geom instanceof LineString) {
			encodeLineString(writer,(LineString) geom);
		} else if (geom instanceof RectifiedGrid) {
			encodeRectifiedGrid(writer,(RectifiedGrid) geom);
		} else if (geom instanceof MultiPoint) {
			encodeMultiPoint(writer,(MultiPoint) geom);
		}else if (geom instanceof MultiLineString) {
			encodeMultiLineString(writer,(MultiLineString) geom);
		}
		else if (geom instanceof MultiPolygon) {
			encodeMultiPolygon(writer,(MultiPolygon) geom);
		}
	}

	@Override
	public String encodeGeometry(Geometry geom) throws XmlException, JSONException {
		JSONStringer writer = new JSONStringer();
		encodeGeometry(writer,geom);
		return writer.toString();
	}

	/**
	 * encodes a MultiPolygon to JSON
	 * 
	 * @param writer
	 * 			writer to which MultiPolygon should be written
	 * @param geom
	 * 			MultiPolygon which should be encoded
	 * @throws JSONException
	 * 			if encoding fails
	 */
	private void encodeMultiPolygon(JSONStringer writer,MultiPolygon geom) throws JSONException {
		writer.object();
		writer.key("type").value("MultiPolygon");
		writer.key("coordinates");
		writer.array();
		int size = geom.getNumGeometries();
		for (int i=0;i<size;i++){
			Polygon poly = (Polygon)geom.getGeometryN(i);
			writer.array();
			encodeCoordinates(writer,poly.getExteriorRing().getCoordinates());
			int n = poly.getNumInteriorRing();
			for (int j=0;j<n;j++){
				encodeCoordinates(writer,poly.getInteriorRingN(j).getCoordinates());
			}
			writer.endArray();
		}
		writer.endArray();
		encodeCRS(writer, geom.getSRID());
		writer.endObject();
	}

	/**
	 * encodes a MultiLineString to JSON
	 * 
	 * @param writer
	 * 			writer to which MultiLineString should be written
	 * @param geom
	 * 			MultiLineString which should be encoded
	 * @throws JSONException
	 * 			if encoding fails
	 */
	private void encodeMultiLineString(JSONStringer writer,MultiLineString geom) throws JSONException {
		writer.object();
		writer.key("type").value(UwGMLUtil.MULTILINESTRING_TYPE);
		writer.key("coordinates");
		writer.array();
		int size = geom.getNumGeometries();
		for (int i=0;i<size;i++){
			LineString ls = (LineString)geom.getGeometryN(i);
			encodeCoordinates(writer,ls.getCoordinates());
		}
		encodeCRS(writer, geom.getSRID());
		writer.endObject();
	}

	/**
	 * encodes a MultiPoint to JSON
	 * 
	 * @param writer
	 * 			writer to which MultiPoint should be written
	 * @param geom
	 * 			MultiPoint which should be encoded
	 * @throws JSONException
	 * 			if encoding fails
	 */
	private void encodeMultiPoint(JSONStringer writer,MultiPoint geom) throws JSONException {
		writer.object();
		writer.key("type").value(UwGMLUtil.MULTIPOINT_TYPE);
		writer.key("coordinates");
		encodeCoordinates(writer,geom.getCoordinates());
		encodeCRS(writer, geom.getSRID());
		writer.endObject();
	}

	//TODO ENCODE!!
	private String encodeRectifiedGrid(JSONStringer writer,RectifiedGrid geom) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * encodes a LineString to JSON
	 * 
	 * @param writer
	 * 			writer to which LineString should be written
	 * @param geom
	 * 			LineString which should be encoded
	 * @throws JSONException
	 * 			if encoding fails
	 */
	private void encodeLineString(JSONStringer writer,LineString geom) throws JSONException {
		writer.object();
		writer.key("type").value(UwGMLUtil.LINESTRING_TYPE);
		writer.key("coordinates");
		encodeCoordinates(writer,geom.getCoordinates());
		encodeCRS(writer, geom.getSRID());
		writer.endObject();
	}

	/**
	 * encodes Polygons into GeoJSON
	 * 
	 * @param writer
	 * 			writer to which polygon should be written
	 * @param geom
	 * 			Polygon which should be encoded
	 * @throws JSONException 
	 * 			if encoding fails
	 */
	private void encodePolygon(JSONStringer writer,Polygon geom) throws JSONException {
		writer.object();
		writer.key("type").value(UwGMLUtil.POLYGON_TYPE);
		writer.key("coordinates");
		writer.array();
		encodeCoordinates(writer,geom.getExteriorRing().getCoordinates());
		int n = geom.getNumInteriorRing();
		for (int i=0;i<n;i++){
			encodeCoordinates(writer,geom.getInteriorRingN(i).getCoordinates());
		}
		writer.endArray();
		encodeCRS(writer, geom.getSRID());
		writer.endObject();
	}

	/**
	 * encodes Points into GeoJSON
	 * 
	 * @param writer
	 * 			writer to which point should be written
	 * @param geom
	 * 			Point which should be encoded
	 * @throws JSONException
	 * 			if encoding fails
	 */
	private void encodePoint(JSONStringer writer,Point geom) throws JSONException {
		writer.object();
		writer.key("type").value(UwGMLUtil.POINT_TYPE);
		writer.key("coordinates");
		encodeCoordinate(writer,geom.getCoordinate());
		encodeCRS(writer, geom.getSRID());
		writer.endObject();
	}
	
	/**
	 * encodes Coordinate to GeoJSON
	 * 
	 * @param writer
	 * 			writer to which coordinate should be written
	 * @param coord
	 * 			JTS Coordinate which should be encoded
	 * @throws JSONException
	 * 			if encoding fails
	 */
	private void encodeCoordinate(JSONStringer writer,Coordinate coord) throws JSONException{
		writer.array();
		writer.value(coord.x);
		writer.value(coord.y);
		writer.endArray();
	}
	
	/**
	 * encodes an array of Coordinates to GeoJSON
	 * 
	 * @param writer
	 * 			writer to which coordinates should be written
	 * @param coord
	 * 			array of JTS Coordinates which should be encoded
	 * @throws JSONException
	 * 			if encoding fails
	 */
	private void encodeCoordinates(JSONStringer writer, Coordinate[] coords) throws JSONException{
		writer.array();
		for (int i=0;i<coords.length;i++){
			encodeCoordinate(writer,coords[i]);
		}
		writer.endArray();
	}

	/**
	 * encodes a SRID representing the EPSG code of geometry to a GeoJSON crs property
	 * 
	 * @param writer
	 * 			writer to which crs should be written
	 * @param srid
	 * 			EPSG code of crs
	 * @throws JSONException
	 * 			if encoding fails
	 */
	private void encodeCRS(JSONStringer writer, int srid) throws JSONException{
		writer.key("crs");
		writer.object();
		writer.key("type").value("name");
		writer.key("properties");
		writer.object();
		writer.key("name").value(UwGMLUtil.EPSG_URL+srid);
		writer.endObject();
		writer.endObject();
	}
}
