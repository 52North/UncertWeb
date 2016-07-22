package org.uncertweb.api.gml.io;

import org.apache.xmlbeans.XmlException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uncertweb.api.gml.UwGMLUtil;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * JSON decoder for decoding geometries in GeoJSON as defined in the
 * UncertWeb GML Profile
 * 
 * follows the GeoJSON specification of http://www.geojson.org/geojson-spec.html
 * 
 * @author staschc
 *
 */
public class JSONGeometryDecoder implements IGeometryParser{

	
	@Override
	public Geometry parseUwGeometry(String geomJson)
			throws IllegalArgumentException, XmlException, JSONException {
		
		Geometry geom = null;
		JSONObject jo= new JSONObject(geomJson);
		String geomType = jo.getString("type");
		if (geomType.equals(UwGMLUtil.POINT_TYPE)){
			geom = parsePoint(jo);
		}
		else if (geomType.equals(UwGMLUtil.LINESTRING_TYPE)){
			geom = parseLineString(jo);
		}
		else if (geomType.equals(UwGMLUtil.POLYGON_TYPE)){
			geom = parsePolygon(jo);
		}
		else if (geomType.equals(UwGMLUtil.MULTIPOINT_TYPE)){
			geom = parseMultiPoint(jo);
		}
		else if (geomType.equals(UwGMLUtil.MULTILINESTRING_TYPE)){
			geom = parseMultiLineString(jo);
		}
		else if (geomType.equals(UwGMLUtil.MULTIPOLYGON_TYPE)){
			geom = parseMultiPolygon(jo);
		}
		
		else {
			throw new IllegalArgumentException("Geometrytype + " + geomType +" is not supported by the JSONParser!!");
		}
		
		return geom;
	}

	/**
	 * methods parses an JSON string representing a MultiPolygon
	 * 
	 * @param geomJson
	 * 			json object that should be parsed
	 * @return Returns JTS MultiPolygon
	 * @throws JSONException
	 * 			if parsing fails
	 */
	private Geometry parseMultiPolygon(JSONObject geomJson) throws JSONException {
		GeometryFactory geomFac = new GeometryFactory();
		JSONArray jsonPolys = geomJson.getJSONArray("coordinates");
		Polygon[] polygons = new Polygon[jsonPolys.length()];
		for (int i=0;i<jsonPolys.length();i++){
			JSONArray ringArray = jsonPolys.getJSONArray(i);
			LinearRing exterior = geomFac.createLinearRing(getCoordinates4JSONArray(ringArray.getJSONArray(0)));
			LinearRing[] holes = null;
			if (ringArray.length()>1){
				holes = new LinearRing[ringArray.length()-1];
				for (int j=1;j<ringArray.length();j++){
					holes[j-1]=geomFac.createLinearRing(getCoordinates4JSONArray(ringArray.getJSONArray(j)));
				}
			}
			polygons[i]=geomFac.createPolygon(exterior, holes);
		}
		MultiPolygon mp = geomFac.createMultiPolygon(polygons);
		mp.setSRID(getSrid(geomJson));
		return mp;
	}

	/**
	 * methods parses an JSON string representing a MultiLineString
	 * 
	 * @param geomJson
	 * 			json object that should be parsed
	 * @return Returns JTS MultiLineString
	 * @throws JSONException
	 * 			if parsing fails
	 */
	private Geometry parseMultiLineString(JSONObject geomJson) throws JSONException {
		GeometryFactory geomFac = new GeometryFactory();
		JSONArray ls = geomJson.getJSONArray("coordinates");
		LineString[] lineStrings = new LineString[ls.length()];
		for (int i=0;i<ls.length();i++){
			Coordinate[] coords = getCoordinates4JSONArray(ls.getJSONArray(i));
			lineStrings[i] = geomFac.createLineString(coords);
		}
		
		MultiLineString mls = new GeometryFactory().createMultiLineString(lineStrings);
		mls.setSRID(getSrid(geomJson));
		return mls;
	}

	/**
	 * methods parses an JSON string representing a MultiPoint
	 * 
	 * @param geomJson
	 * 			json object that should be parsed
	 * @return Returns JTS MultiPoint
	 * @throws JSONException
	 * 			if parsing fails
	 */
	private Geometry parseMultiPoint(JSONObject geomJson) throws JSONException {
		Coordinate[] coord = getCoordinates4JSONArray(geomJson.getJSONArray("coordinates"));
		MultiPoint point = new GeometryFactory().createMultiPoint(coord);
		point.setSRID(getSrid(geomJson));
		return point;
	}

	/**
	 * methods parses an JSON string representing a Polygon
	 * 
	 * @param geomJson
	 * 			json object that should be parsed
	 * @return Returns JTS Polygon
	 * @throws JSONException
	 * 			if parsing fails
	 */
	private Geometry parsePolygon(JSONObject geomJson) throws JSONException {
		
		GeometryFactory geomFac = new GeometryFactory();
		JSONArray rings = geomJson.getJSONArray("coordinates");
		
		//check number of rings in polygon
		int ringsLength=rings.length();
		
		//extract exterior ring
		Coordinate[] exterior = getCoordinates4JSONArray(rings.getJSONArray(0));
		LinearRing shell = geomFac.createLinearRing(exterior);
		
		//extract interior rings
		LinearRing[] interior = new LinearRing[ringsLength-1];
		if (ringsLength>1){
			for (int i=1;i<ringsLength;i++){
				Coordinate[] interiorCoords = getCoordinates4JSONArray(rings.getJSONArray(i));
				interior[i-1]= geomFac.createLinearRing(interiorCoords);
			}
		}
		
		Polygon poly = geomFac.createPolygon(shell, interior);
		poly.setSRID(getSrid(geomJson));
		return poly;
	}

	/**
	 * methods parses an JSON string representing a LineString
	 * 
	 * @param geomJson
	 * 			json object that should be parsed
	 * @return Returns JTS LineString
	 * @throws JSONException 
	 * @throws JSONException
	 * 			if parsing fails
	 */
	private Geometry parseLineString(JSONObject geomJson) throws JSONException {
		Coordinate[] coords = getCoordinates4JSONArray(geomJson.getJSONArray("coordinates"));
		LineString ls = new GeometryFactory().createLineString(coords);
		ls.setSRID(getSrid(geomJson));
		return ls;
	}

	/**
	 * methods parses an JSON string representing a Point
	 * 
	 * @param geomJson
	 * 			json object that should be parsed
	 * @return Returns JTS Point
	 * @throws JSONException
	 * 			if parsing fails
	 */
	private Geometry parsePoint(JSONObject geomJson) throws JSONException {
		Coordinate coord = getCoordinate4JSONArray(geomJson.getJSONArray("coordinates"));
		Point point = new GeometryFactory().createPoint(coord);
		point.setSRID(getSrid(geomJson));
		return point;
	}

	/**
	 * method for parsing an EPSG code from a GeoJSON object
	 * 
	 * @param geomJson
	 * 		json object
	 * @return Returns epsg code
	 * @throws JSONException
	 * 			if parsing fails
	 */
	private int getSrid(JSONObject geomJson) throws JSONException {
		String srid = geomJson.getJSONObject("crs").getJSONObject("properties").getString("name");
		srid = srid.replace(UwGMLUtil.EPSG_URL, "");
		return Integer.parseInt(srid);
	}
	
	/**
	 * creates Coordinate from JSONArray
	 * 
	 * @param array
	 * 			GeoJSONArray containing the coordinates
	 * @return
	 * 			Returns JTS Coordinate
	 * @throws JSONException
	 * 			if parsing fails
	 */
	private Coordinate getCoordinate4JSONArray(JSONArray array) throws JSONException{
		double x = array.getDouble(0);
		double y = array.getDouble(1);
		return new Coordinate(x,y);
	}
	
	/**
	 * creates Coordinate array 4 JSONArray
	 * 
	 * @param array
	 * 			JSONArray containing an array of coordinates in GeoJSON
	 * @return
	 * 			Returns array of JTS Coordinates
	 * @throws JSONException
	 * 			if parsing fails
	 */
	private Coordinate[] getCoordinates4JSONArray(JSONArray array) throws JSONException{
		int coordsN = array.length();
		Coordinate[] coords = new Coordinate[coordsN];
		for (int i=0;i<coordsN;i++){
			JSONArray coordArray = array.getJSONArray(i);
			coords[i]=getCoordinate4JSONArray(coordArray);
		}
		return coords;
	}

}
