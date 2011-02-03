package org.uncertweb.api.gml.io;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.opengis.gml.x32.AbstractGeometryType;
import net.opengis.gml.x32.AbstractRingPropertyType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.GeometryPropertyType;
import net.opengis.gml.x32.GridEnvelopeType;
import net.opengis.gml.x32.LineStringDocument;
import net.opengis.gml.x32.LineStringType;
import net.opengis.gml.x32.LinearRingType;
import net.opengis.gml.x32.MultiGeometryDocument;
import net.opengis.gml.x32.MultiGeometryType;
import net.opengis.gml.x32.PointDocument;
import net.opengis.gml.x32.PointType;
import net.opengis.gml.x32.PolygonDocument;
import net.opengis.gml.x32.PolygonType;
import net.opengis.gml.x32.RectifiedGridDocument;
import net.opengis.gml.x32.RectifiedGridType;
import net.opengis.gml.x32.VectorType;

import org.apache.xmlbeans.XmlObject;
import org.uncertweb.api.gml.UwGMLUtil;
import org.uncertweb.api.gml.geometry.GmlLineString;
import org.uncertweb.api.gml.geometry.GmlMultiGeometry;
import org.uncertweb.api.gml.geometry.GmlPoint;
import org.uncertweb.api.gml.geometry.GmlPolygon;
import org.uncertweb.api.gml.geometry.RectifiedGrid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

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

	/**
	 * constructor
	 * 
	 * @param geomFacp
	 *            factory used to create JTS geometries
	 */
	public XmlBeansGeometryParser(GeometryFactory geomFacp) {
		this.geomFac = geomFacp;
	}

	// TODO maybe add explicit exception handling in this method
	@Override
	public Geometry parseUwGeometry(String geometryXml) throws Exception {

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

		// geometry is MultiGeometry
		else if (xb_geomObj instanceof MultiGeometryDocument) {
			geom = parseMultiGeometry((MultiGeometryDocument) xb_geomObj);
			return geom;
		}

		else
			throw new Exception(
					"Geometry type is not supported by UncertWeb GML profile!");
	}

	/**
	 * method for parsing XmlBeans representation of point into JTS geometry
	 * 
	 * @param xb_pointType
	 *            XmlBeans representation of point defined in UncertWeb GML
	 *            profile
	 * @return Returns JTS representation of point
	 * @throws Exception
	 *             If position is not 2-dim or the srsName is not starting with
	 *             correct EPSG code URL
	 */
	private GmlPoint parsePoint(PointType xb_pointType) throws Exception {
		GeometryFactory geomFac = new GeometryFactory();
		GmlPoint point = null;

		String gmlId = xb_pointType.getId();
		DirectPositionType xb_pos = xb_pointType.getPos();
		int epsgCode = parseSrs(xb_pos.getSrsName());

		Coordinate pointCoords = parsePositionString(xb_pos.getStringValue());
		Coordinate[] coords = {pointCoords};
		CoordinateSequence cs = geomFac.getCoordinateSequenceFactory().create(coords);
		point = new GmlPoint(cs,geomFac,gmlId);
		point.setSRID(epsgCode);
		return point;
	}

	/**
	 * method for parsing XmlBeans representation of polygon into JTS geometry
	 * 
	 * @param xb_polyDoc
	 * @return
	 * @throws Exception
	 */
	private GmlPolygon parsePolygon(PolygonType xb_polyType) throws Exception {
		GeometryFactory geomFac = new GeometryFactory();
		GmlPolygon poly = null;

		String gmlId = xb_polyType.getId();
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

		poly = new GmlPolygon(shell, holes, geomFac, gmlId);
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
	 * @throws Exception
	 *             if parsing of the postions or of the srsName fails
	 */
	private GmlLineString parseLineString(LineStringType xb_lsType) throws Exception {
		GeometryFactory geomFac = new GeometryFactory();
		String gmlId = xb_lsType.getId();
		GmlLineString lineString = null;
		int srid = parseSrs(xb_lsType.getSrsName());
		DirectPositionType[] xb_posArray = xb_lsType.getPosArray();
		Coordinate[] coordinates = new Coordinate[xb_posArray.length];
		for (int i = 0; i < xb_posArray.length; i++) {
			coordinates[i] = parsePositionString(xb_posArray[i]
					.getStringValue());
		}
		CoordinateSequence cs = geomFac.getCoordinateSequenceFactory().create(coordinates);
		lineString = new GmlLineString(cs,geomFac,gmlId);
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
	 * @throws Exception
	 */
	private RectifiedGrid parseRectifiedGrid(RectifiedGridType xb_rgType)
			throws Exception {
		RectifiedGrid rg = null;
		String gmlId=xb_rgType.getId();
		Envelope gridEnv = null;
		List<String> axisLabels = null;
		GmlPoint origin = null;
		Collection<Point> offsetVectors;

		// parse limits
		GridEnvelopeType xb_limits = xb_rgType.getLimits().getGridEnvelope();
		List highCoords = xb_limits.getHigh();
		List lowCoords = xb_limits.getLow();
		if (highCoords.size() != 2 || lowCoords.size() != 2) {
			throw new Exception("Grid must have 2-dim coords!");
		} else {
			Coordinate high = new Coordinate(((BigInteger)highCoords.get(0)).doubleValue(),
					((BigInteger)highCoords.get(0)).doubleValue());
			Coordinate low = new Coordinate(((BigInteger)lowCoords.get(0)).doubleValue(),
					((BigInteger)lowCoords.get(0)).doubleValue());
			gridEnv = new Envelope(low, high);
		}

		// parse axisLabels or axisNames
		List al = xb_rgType.getAxisLabels2();
		String[] an = xb_rgType.getAxisNameArray();
		axisLabels = new ArrayList<String>();
		if (al != null) {
			if (al.size() != 2) {
				throw new Exception(
						"Grid must have 2-dim coords and thus 2 axis labels. There are more than 2 axis labels defined!");
			} 
			axisLabels.add((String) al.get(0));
			axisLabels.add((String) al.get(1));
			
		}
		else {
			if (an.length != 2) {
				throw new Exception(
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
		
		rg= new RectifiedGrid(gmlId,gridEnv,axisLabels,origin,offsetVectors,geomFac);
		
		return rg;
	}

	/**
	 * method for parsing XmlBeans representation of multiGeometry into JTS
	 * geometry
	 * 
	 * @param xb_mgDoc
	 *            XmlBeans representation of multiGeometry
	 * @return Returns JTS representation of multiGeometry
	 * @throws Exception
	 */
	private Geometry parseMultiGeometry(MultiGeometryDocument xb_mgDoc)
			throws Exception {
		GmlMultiGeometry geomCol = null;
		MultiGeometryType xb_mg = xb_mgDoc.getMultiGeometry();
		String gmlId = xb_mg.getId();
		GeometryPropertyType[] xb_members = xb_mg.getGeometryMemberArray();
		Geometry[] geomArray = new Geometry[xb_members.length];
		for (int i = 0; i < xb_members.length; i++) {
			AbstractGeometryType xb_absGeom = xb_members[i]
					.getAbstractGeometry();
			if (xb_absGeom instanceof PointType) {
				geomArray[i] = parsePoint((PointType) xb_absGeom);
			} else if (xb_absGeom instanceof PolygonType) {
				geomArray[i] = parsePolygon((PolygonType) xb_absGeom);
			} else if (xb_absGeom instanceof LineStringType) {
				geomArray[i] = parseLineString((LineStringType) xb_absGeom);
			} else if (xb_absGeom instanceof RectifiedGridType) {
				geomArray[i] = parseRectifiedGrid((RectifiedGridType) xb_absGeom);
			}
			// TODO else throw Exception

		}
		geomCol =new GmlMultiGeometry(geomArray,geomFac,gmlId);
		return geomCol;
	}

	/**
	 * helper method for parsing epsg code from srsName
	 * 
	 * @param srsName
	 *            String representing the EPSG Code with OGC conform EPSG URL as
	 *            prefix
	 * @return Returns int representing the EPSG code
	 * @throws Exception
	 *             if srsName does not start with EPSG URL
	 */
	private int parseSrs(String srsName) throws Exception {
		int epsgCode = 0;
		if (srsName.startsWith(UwGMLUtil.EPSG_URL)) {
			epsgCode = Integer
					.parseInt(srsName.replace(UwGMLUtil.EPSG_URL, ""));
		} else {
			throw new Exception("SrsName has to start with URL of EPSG Code: "
					+ UwGMLUtil.EPSG_URL + "!");
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
	 * @throws Exception
	 *             if position is not 2-dim.
	 */
	public Coordinate parsePositionString(String position) throws Exception {
		Coordinate coord = new Coordinate();
		String[] coords = position.split(" ");
		if (coords.length != 2) {
			throw new Exception("Only 2-dimensional points are supported");
		}
		coord.x = Double.parseDouble(coords[0]);
		coord.y = Double.parseDouble(coords[1]);
		return coord;
	}

	/**
	 * 
	 * 
	 * @param xb_lrType
	 * @return
	 * @throws Exception
	 */
	private LinearRing parseLinearRing(LinearRingType xb_lrType)
			throws Exception {
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
