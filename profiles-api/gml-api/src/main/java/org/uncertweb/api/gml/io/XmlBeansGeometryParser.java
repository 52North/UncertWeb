package org.uncertweb.api.gml.io;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.opengis.gml.x32.AbstractRingPropertyType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.GridEnvelopeType;
import net.opengis.gml.x32.LineStringDocument;
import net.opengis.gml.x32.LineStringPropertyType;
import net.opengis.gml.x32.LineStringType;
import net.opengis.gml.x32.LinearRingType;
import net.opengis.gml.x32.MultiLineStringDocument;
import net.opengis.gml.x32.MultiPointDocument;
import net.opengis.gml.x32.MultiPolygonDocument;
import net.opengis.gml.x32.PointDocument;
import net.opengis.gml.x32.PointPropertyType;
import net.opengis.gml.x32.PointType;
import net.opengis.gml.x32.PolygonDocument;
import net.opengis.gml.x32.PolygonPropertyType;
import net.opengis.gml.x32.PolygonType;
import net.opengis.gml.x32.RectifiedGridDocument;
import net.opengis.gml.x32.RectifiedGridType;
import net.opengis.gml.x32.VectorType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.uncertweb.api.gml.UwGMLUtil;
import org.uncertweb.api.gml.geometry.GmlGeometryFactory;
import org.uncertweb.api.gml.geometry.RectifiedGrid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
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
 * XMLBeans based parser implementation for parsing geometries defined in the
 * UncertWeb GML profile into JTS geometries
 *
 * @author staschc
 *
 */
public class XmlBeansGeometryParser implements IGeometryParser {

	/** factory used to create JTS geometries */
	private GeometryFactory geomFac;

	/**UncertWeb geometry factory*/
	private GmlGeometryFactory factory;

	/**
	 * constructor; initializes GeometryFactory
	 *
	 */
	public XmlBeansGeometryParser() {
		this.geomFac = new GeometryFactory();
		this.factory = new GmlGeometryFactory();
	}

	// TODO maybe add explicit exception handling in this method
	@Override
	public Geometry parseUwGeometry(String geometryXml) throws IllegalArgumentException, XmlException {

		Geometry geom = null;
		XmlObject xb_geomObj = XmlObject.Factory.parse(geometryXml);

		// geometry is Point
		if (xb_geomObj instanceof PointDocument) {
			geom = parsePoint(((PointDocument) xb_geomObj).getPoint());
			return geom;
		}

		// geometry is Polygon
		else if (xb_geomObj instanceof PolygonDocument) {
			geom = parsePolygon(((PolygonDocument) xb_geomObj).getPolygon());
			return geom;
		}

		// geometry is LineString
		else if (xb_geomObj instanceof LineStringDocument) {
			geom = parseLineString(((LineStringDocument) xb_geomObj)
					.getLineString());
			return geom;
		}

		// geometry is RectifiedGrid
		else if (xb_geomObj instanceof RectifiedGridDocument) {
			geom = parseRectifiedGrid(((RectifiedGridDocument) xb_geomObj)
					.getRectifiedGrid());
			return geom;
		}

		// geometry is MultiPoint
		else if (xb_geomObj instanceof MultiPointDocument) {
			geom = parseMultiPoint((MultiPointDocument) xb_geomObj);
			return geom;
		}

		// geometry is MultiLineString
		else if (xb_geomObj instanceof MultiLineStringDocument) {
			geom = parseMultiLineString((MultiLineStringDocument) xb_geomObj);
			return geom;
		}

		// geometry is MultiPolygon
		else if (xb_geomObj instanceof MultiPolygonDocument) {
			geom = parseMultiPolygon((MultiPolygonDocument) xb_geomObj);
			return geom;
		}

		else
			throw new IllegalArgumentException(
					"Geometry type is not supported by UncertWeb GML profile!");
	}

	/**
	 * method for parsing XmlBeans representation of point into JTS geometry
	 *
	 * @param xb_pointType
	 *            XmlBeans representation of point defined in UncertWeb GML
	 *            profile
	 * @return Returns JTS representation of point
	 * @throws IllegalArgumentException
	 *             If position is not 2-dim or the srsName is not starting with
	 *             correct EPSG code URL
	 */
	private Point parsePoint(PointType xb_pointType) throws IllegalArgumentException {
		Point point = null;

		DirectPositionType xb_pos = xb_pointType.getPos();
		int epsgCode = parseSrs(xb_pos.getSrsName());

		Coordinate pointCoords = parsePositionString(xb_pos.getStringValue());
		point = factory.createPoint(pointCoords.x, pointCoords.y, epsgCode);
		return point;
	}

	/**
	 * method for parsing XmlBeans representation of polygon into JTS geometry
	 *
	 * @param xb_polyDoc
	 * @return
	 * @throws IllegalArgumentException
	 */
	private Polygon parsePolygon(PolygonType xb_polyType) throws IllegalArgumentException {
		GeometryFactory geomFac = new GeometryFactory();
		Polygon poly = null;

		int srid = parseSrs(xb_polyType.getSrsName());

		// parse exterior ring
		LinearRingType xb_exterior = (LinearRingType) xb_polyType.getExterior()
				.getAbstractRing();
		LinearRing shell = parseLinearRing(xb_exterior);

		// parse interior rings
		AbstractRingPropertyType[] xb_interiorRings = xb_polyType
				.getInteriorArray();
		LinearRing[] holes = new LinearRing[xb_interiorRings.length];
		for (int i = 0; i < xb_interiorRings.length; i++) {
			LinearRingType xb_interior = (LinearRingType) xb_interiorRings[i]
					.getAbstractRing();
			holes[i] = parseLinearRing(xb_interior);
		}

		poly = geomFac.createPolygon(shell, holes);
		poly.setSRID(srid);
		return poly;
	}

	/**
	 * method for parsing XmlBeans representation of lineString into JTS
	 * geometry
	 *
	 * @param xb_lsType
	 *            XMLBeans representation of line string
	 * @return Returns JTS LineString
	 * @throws IllegalArgumentException
	 *             if parsing of the postions or of the srsName fails
	 */
	private LineString parseLineString(LineStringType xb_lsType) throws IllegalArgumentException {
		GeometryFactory geomFac = new GeometryFactory();
		LineString lineString = null;
		int srid = parseSrs(xb_lsType.getSrsName());
		DirectPositionType[] xb_posArray = xb_lsType.getPosArray();
		Coordinate[] coordinates = new Coordinate[xb_posArray.length];
		for (int i = 0; i < xb_posArray.length; i++) {
			coordinates[i] = parsePositionString(xb_posArray[i]
					.getStringValue());
		}
		lineString = geomFac.createLineString(coordinates);
		lineString.setSRID(srid);
		return lineString;
	}

	/**
	 * method for parsing XmlBeans representation of rectified grid into JTS
	 * geometry
	 *
	 * @param xb_rgType
	 *            XmlBeans representation of rectified grid
	 * @return Returns JTS representation of Rectified grid
	 * @throws IllegalArgumentException
	 */
	private RectifiedGrid parseRectifiedGrid(RectifiedGridType xb_rgType)
			throws IllegalArgumentException {
		RectifiedGrid rg = null;
		Envelope gridEnv = null;
		List<String> axisLabels = null;
		Point origin = null;
		Collection<Point> offsetVectors;

		// parse limits
		GridEnvelopeType xb_limits = xb_rgType.getLimits().getGridEnvelope();
		List<?> highCoords = xb_limits.getHigh();
		List<?> lowCoords = xb_limits.getLow();
		if (highCoords.size() != 2 || lowCoords.size() != 2) {
			throw new IllegalArgumentException("Grid must have 2-dim coords!");
		} else {
			Coordinate high = new Coordinate(((BigInteger)highCoords.get(0)).doubleValue(),
					((BigInteger)highCoords.get(0)).doubleValue());
			Coordinate low = new Coordinate(((BigInteger)lowCoords.get(0)).doubleValue(),
					((BigInteger)lowCoords.get(0)).doubleValue());
			gridEnv = new Envelope(low, high);
		}

		// parse axisLabels or axisNames
		List<?> al = xb_rgType.getAxisLabels2();
		String[] an = xb_rgType.getAxisNameArray();
		axisLabels = new ArrayList<String>();
		if (al != null) {
			if (al.size() != 2) {
				throw new IllegalArgumentException(
						"Grid must have 2-dim coords and thus 2 axis labels. There are more than 2 axis labels defined!");
			}
			axisLabels.add((String) al.get(0));
			axisLabels.add((String) al.get(1));

		}
		else {
			if (an.length != 2) {
				throw new IllegalArgumentException(
						"Grid must have 2-dim coords and thus 2 axis names. There are more than 2 axis labels defined!");
			}
			axisLabels.add(an[0]);
			axisLabels.add(an[1]);
		}

		//parse origin
		origin = parsePoint(xb_rgType.getOrigin().getPoint());

		//parse offset vectors
		VectorType[] xb_offsets = xb_rgType.getOffsetVectorArray();
		offsetVectors = new ArrayList<Point>(xb_offsets.length);
		for (int i=0;i<xb_offsets.length;i++){
			int srs = parseSrs(xb_offsets[i].getSrsName());
			Point p = geomFac.createPoint(parsePositionString(xb_offsets[i].getStringValue()));
			p.setSRID(srs);
			offsetVectors.add(p);
		}

		rg= new RectifiedGrid(gridEnv,axisLabels,origin,offsetVectors,geomFac);

		return rg;
	}


	/**
	 * method for parsing XmlBeans representation of multiLineString into JTS
	 * geometry
	 *
	 * @param xb_mlsDoc
	 *            XmlBeans representation of multiLineString
	 * @return Returns JTS representation of multiLineString
	 * @throws IllegalArgumentException
	 * 				if parsing fails
	 */
	private Geometry parseMultiLineString(MultiLineStringDocument xb_mlsDoc) throws IllegalArgumentException{
		MultiLineString mp = null;
		int srid = parseSrs(xb_mlsDoc.getMultiLineString().getSrsName());
		LineStringPropertyType[] xb_lsArray = xb_mlsDoc.getMultiLineString().getLineStringMemberArray();
		LineString[] ls = new LineString[xb_lsArray.length];
		for (int i=0; i<xb_lsArray.length;i++){
			ls[i]=parseLineString(xb_lsArray[i].getLineString());
		}
		mp = factory.createMultiLineString(ls, srid);
		return mp;
	}

	/**
	 * method for parsing XmlBeans representation of multiPoint into JTS
	 * geometry
	 *
	 * @param xb_mlsDoc
	 * 				XmlBeans representation of multiPoint
	 * @return Returns JTS representation of multiPoint
	 * @throws IllegalArgumentException
	 * 			if parsing fails
	 */
	private Geometry parseMultiPoint(MultiPointDocument xb_mlsDoc) throws IllegalArgumentException{
		MultiPoint mp = null;
		int srid = parseSrs(xb_mlsDoc.getMultiPoint().getSrsName());
		PointPropertyType[] xb_lsArray = xb_mlsDoc.getMultiPoint().getPointMemberArray();
		Point[] ls = new Point[xb_lsArray.length];
		for (int i=0; i<xb_lsArray.length;i++){
			ls[i]=parsePoint(xb_lsArray[i].getPoint());
		}
		mp = factory.createMultiPoint(ls, srid);
		return mp;
	}

	/**
	 * method for parsing XmlBeans representation of MultiPolygon into JTS
	 * geometry
	 *
	 * @param xb_mlsDoc
	 * 				XmlBeans representation of MultiPolygon
	 * @return Returns JTS representation of MultiPolygon
	 * @throws IllegalArgumentException
	 * 			if parsing fails
	 */
	private Geometry parseMultiPolygon(MultiPolygonDocument xb_mlsDoc) throws IllegalArgumentException{
		MultiPolygon mp = null;
		int srid = parseSrs(xb_mlsDoc.getMultiPolygon().getSrsName());
		PolygonPropertyType[] xb_lsArray = xb_mlsDoc.getMultiPolygon().getPolygonMemberArray();
		Polygon[] ls = new Polygon[xb_lsArray.length];
		for (int i=0; i<xb_lsArray.length;i++){
			ls[i]=parsePolygon(xb_lsArray[i].getPolygon());
		}
		mp = factory.createMultiPolygon(ls, srid);
		return mp;
	}

	/**
	 * helper method for parsing epsg code from srsName
	 *
	 * @param srsName
	 *            String representing the EPSG Code with OGC conform EPSG URL as
	 *            prefix
	 * @return Returns int representing the EPSG code
	 * @throws IllegalArgumentException
	 *             if srsName does not start with EPSG URL
	 */
	private int parseSrs(String srsName) throws IllegalArgumentException {
		int epsgCode = 0;
		if (srsName!=null){
			if (srsName.startsWith(UwGMLUtil.EPSG_URL)) {
				epsgCode = Integer
						.parseInt(srsName.replace(UwGMLUtil.EPSG_URL, ""));
			} else {
				throw new IllegalArgumentException("SrsName has to start with URL of EPSG Code: "
						+ UwGMLUtil.EPSG_URL + "!");
			}
		}
		return epsgCode;
	}

	/**
	 * helper method for parsing postion strings; ATTENTION: coordinates are
	 * just parsed per order and NOT switched depending on coordinate system
	 * (e.g. EPSG:4326 is not switched, thus latitude will be x and longitude
	 * will be y)
	 *
	 * @param position
	 *            position string contained in XML document
	 * @return Returns JTS Coordinate
	 * @throws IllegalArgumentException
	 *             if position is not 2-dim.
	 */
	public Coordinate parsePositionString(String position) throws IllegalArgumentException {
		Coordinate coord = new Coordinate();
		String[] coords = position.split(" ");
		if (coords.length != 2) {
			throw new IllegalArgumentException("Only 2-dimensional points are supported");
		}
		coord.x = Double.parseDouble(coords[0]);
		coord.y = Double.parseDouble(coords[1]);
		return coord;
	}

	/**
	 * helper method for parsing an XMLBeans representation of  linear ring to a JTS representation
	 *
	 * @param xb_lrType
	 * 			XMLBeans representation of  linear ring
	 * @return JTS representation of linear ring
	 * @throws IllegalArgumentException
	 * 			if parsing of linear ring fails
	 */
	private LinearRing parseLinearRing(LinearRingType xb_lrType)
			throws IllegalArgumentException {
		DirectPositionType[] xb_posArray = xb_lrType.getPosArray();
		Coordinate[] coords = new Coordinate[xb_posArray.length];
		for (int i = 0; i < xb_posArray.length; i++) {
			Coordinate coord = parsePositionString(xb_posArray[i]
					.getStringValue());
			coords[i] = coord;
		}
		return geomFac.createLinearRing(coords);
	}

}
