package org.uncertweb.api.gml.io;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.opengis.gml.x32.AbstractRingPropertyType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.GeometryPropertyType;
import net.opengis.gml.x32.GridEnvelopeType;
import net.opengis.gml.x32.LineStringDocument;
import net.opengis.gml.x32.LineStringType;
import net.opengis.gml.x32.LinearRingDocument;
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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.uncertweb.api.gml.UwAbstractFeature;
import org.uncertweb.api.gml.UwGMLUtil;
import org.uncertweb.api.gml.geometry.RectifiedGrid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Interface for encoding GML geometries and features in XMLBeans
 * 
 * @author staschc
 * 
 */
public class XmlBeansGeometryEncoder implements IGeometryEncoder {

	@Override
	public String encodeGeometry(Geometry geom) throws XmlException {

		String result = null;
		if (geom instanceof Point) {
			result = encodePoint((Point) geom);
		} else if (geom instanceof Polygon) {
			result = encodePolygon((Polygon) geom);
		} else if (geom instanceof LineString) {
			result = encodeLineString((LineString) geom);
		} else if (geom instanceof RectifiedGrid) {
			result = encodeRectifiedGrid((RectifiedGrid) geom);
		} else if (geom instanceof GeometryCollection) {
			result = encodeMultiGeometry((GeometryCollection) geom);
		}

		return result;
	}

	@Override
	public String encodeFeature(UwAbstractFeature feature) {
		// TODO needs to be implemented for certain feature types
		return null;
	}

	/**
	 * helper method for encoding multi geometry
	 * 
	 * @param geom
	 * 		JTS representatioin of multigeometry
	 * @return
	 * 		Returns GML string representing the multigeometry
	 * @throws XmlException
	 * 		If encoding of the multi geometry fails
	 */
	private String encodeMultiGeometry(GeometryCollection geom)
			throws XmlException {

		return encodeMultiGeometry2Doc(geom).xmlText(getGMLOptions());
	}
	
	/**
	 * helper method for encoding multi geometry
	 * 
	 * @param geom
	 * 		JTS representatioin of multigeometry
	 * @return
	 * 		Returns GML document representing the multigeometry
	 * @throws XmlException
	 * 		If encoding of the multi geometry fails
	 */
	public MultiGeometryDocument encodeMultiGeometry2Doc(GeometryCollection geom)
			throws XmlException {
		MultiGeometryDocument xb_mgDoc = MultiGeometryDocument.Factory
				.newInstance();
		MultiGeometryType xb_mg = xb_mgDoc.addNewMultiGeometry();
		int num = geom.getNumGeometries();
		for (int i = 0; i < num; i++) {
			GeometryPropertyType xb_member = xb_mg.addNewGeometryMember();
			XmlObject xb_geom = XmlObject.Factory.parse(encodeGeometry(geom
					.getGeometryN(i)));
			xb_member.set(xb_geom);
		}
		return xb_mgDoc;
	}

	/**
	 * helper method for encoding rectified grid
	 * 
	 * @param geom
	 * 		JTS representatioin of rectified grid
	 * @return
	 * 		Returns GML string representing the rectified grid
	 * 
	 */
	private String encodeRectifiedGrid(RectifiedGrid geom) {
	
		return encodeRectifiedGrid2Doc(geom).xmlText(getGMLOptions());
	}
	
	/**
	 * helper method for encoding rectified grid
	 * 
	 * @param geom
	 * 		JTS representatioin of rectified grid
	 * @return
	 * 		Returns GML document representing the rectified grid
	 * 
	 */
	public RectifiedGridDocument encodeRectifiedGrid2Doc(RectifiedGrid geom) {
		RectifiedGridDocument xb_rgDoc = RectifiedGridDocument.Factory
				.newInstance();
		RectifiedGridType xb_rg = xb_rgDoc.addNewRectifiedGrid();

		// set gmlID
		xb_rg.setId(geom.getGmlId());

		// create GridEnvelope
		GridEnvelopeType xb_limits = xb_rg.addNewLimits().addNewGridEnvelope();
		List<BigInteger> high = new ArrayList<BigInteger>();
		high.add(BigInteger
				.valueOf(new Double(geom.getGridEnvelope().getMaxX())
						.longValue()));
		high.add(BigInteger
				.valueOf(new Double(geom.getGridEnvelope().getMaxY())
						.longValue()));
		List<BigInteger> low = new ArrayList<BigInteger>();
		low.add(BigInteger.valueOf(new Double(geom.getGridEnvelope().getMinX())
				.longValue()));
		low.add(BigInteger.valueOf(new Double(geom.getGridEnvelope().getMinY())
				.longValue()));
		xb_limits.setHigh(high);
		xb_limits.setLow(low);

		// set axis label
		xb_rg.setAxisLabels2(geom.getAxisLabels());

		// encode origin
		PointType xb_origin = xb_rg.addNewOrigin().addNewPoint();
		xb_origin.addNewPos().setStringValue(
				geom.getOrigin().getCoordinate().x + " "
						+ geom.getOrigin().getCoordinate().y);
		if (geom.getOrigin().getSRID() != 0) {
			xb_origin.setSrsName(UwGMLUtil.EPSG_URL
					+ geom.getOrigin().getSRID());
		}

		// encode offset vectors
		Collection<Point> ovs = geom.getOffsetVectors();
		for (Point p : ovs) {
			VectorType xb_ov = xb_rg.addNewOffsetVector();
			if (p.getSRID() != 0) {
				xb_ov.setSrsName(UwGMLUtil.EPSG_URL + p.getSRID());
			}
			xb_ov.setStringValue(p.getCoordinate().x + " "
					+ p.getCoordinate().y);
		}

		return xb_rgDoc;
	}

	/**
	 * helper method for encoding line string
	 * 
	 * @param geom
	 * 		JTS representatioin of line string
	 * @return
	 * 		Returns GML string representing the line string
	 * 
	 */
	private String encodeLineString(LineString geom) {

		return encodeLineString2Doc(geom).xmlText(getGMLOptions());
	}
	
	/**
	 * helper method for encoding line string
	 * 
	 * @param geom
	 * 		JTS representatioin of line string
	 * @return
	 * 		Returns GML document representing the line string
	 * 
	 */
	public LineStringDocument encodeLineString2Doc(LineString geom) {
		LineStringDocument xb_lsDoc = LineStringDocument.Factory.newInstance();
		LineStringType xb_ls = xb_lsDoc.addNewLineString();
		if (geom.getSRID() != 0) {
			xb_ls.setSrsName(UwGMLUtil.EPSG_URL + geom.getSRID());
		}
		Coordinate[] coords = geom.getCoordinates();
		for (int i = 0; i < coords.length; i++) {
			DirectPositionType xb_pos = xb_ls.addNewPos();
			xb_pos.setStringValue(coords[i].x + " " + coords[i].y);
		}
		return xb_lsDoc;
	}

	/**
	 * helper method for encoding polygon
	 * 
	 * @param geom
	 * 		JTS representatioin of polygon
	 * @return
	 * 		Returns GML string representing the polygon
	 * 
	 */
	private String encodePolygon(Polygon geom) {

		return encodePolygon2Doc(geom).xmlText(getGMLOptions());
	}
	
	/**
	 * helper method for encoding polygon
	 * 
	 * @param geom
	 * 		JTS representatioin of polygon
	 * @return
	 * 		Returns GML document representing the polygon
	 * 
	 */
	public PolygonDocument encodePolygon2Doc(Polygon geom) {
		PolygonDocument xb_pd = PolygonDocument.Factory.newInstance();
		PolygonType xb_poly = xb_pd.addNewPolygon();
		xb_poly.setSrsName(UwGMLUtil.EPSG_URL + geom.getSRID());

		// create exterior
		AbstractRingPropertyType xb_exterior = xb_poly.addNewExterior();
		xb_exterior.set(encodeLinearRing(geom.getExteriorRing()));

		int interiorSize = geom.getNumInteriorRing();
		for (int i = 0; i < interiorSize; i++) {
			AbstractRingPropertyType xb_interior = xb_poly.addNewInterior();
			xb_interior.set(encodeLinearRing(geom.getInteriorRingN(i)));
		}
		// create interior
		return xb_pd;
	}

	/**
	 * helper method for point
	 * 
	 * @param geom
	 * 		JTS representatioin of point
	 * @return
	 * 		Returns GML string representing the point
	 * 
	 */
	private String encodePoint(Point geom) {

		return encodePoint2Doc(geom).xmlText(getGMLOptions());
	}
	
	/**
	 * helper method for point
	 * 
	 * @param geom
	 * 		JTS representatioin of point
	 * @return
	 * 		Returns GML document representing the point
	 * 
	 */
	public PointDocument encodePoint2Doc(Point geom) {
		PointDocument xb_pd = PointDocument.Factory.newInstance();
		PointType xb_pt = xb_pd.addNewPoint();
		DirectPositionType xb_pos = xb_pt.addNewPos();
		xb_pos.setSrsName(UwGMLUtil.EPSG_URL + geom.getSRID());
		xb_pos.setStringValue(geom.getCoordinate().x + " "
				+ geom.getCoordinate().y);
		return xb_pd;
	}

	/**
	 * helper method for linear ring encoding
	 * 
	 * @param geom
	 * 		JTS representatioin of linear ring
	 * @return
	 * 		Returns GML string representing the linear ring
	 * 
	 */
	private LinearRingDocument encodeLinearRing(LineString lr) {
		Coordinate[] coords = lr.getCoordinates();
		LinearRingDocument xb_lrDoc = LinearRingDocument.Factory.newInstance();
		LinearRingType xb_lrType = xb_lrDoc.addNewLinearRing();// LinearRingType.Factory.newInstance();
		DirectPositionType[] xb_posList = new DirectPositionType[coords.length];
		for (int i = 0; i < coords.length; i++) {
			DirectPositionType xb_pos = DirectPositionType.Factory
					.newInstance();
			xb_pos.setStringValue(coords[i].x + " " + coords[i].y);
			xb_posList[i] = xb_pos;
		}

		xb_lrType.setPosArray(xb_posList);
		return xb_lrDoc;
	}

	/**
	 * method returns XmlOptions which are used by XmlBeans for a proper encoding
	 * 
	 * @return
	 * 		Returns GML string representing the point
	 * 
	 */
	private XmlOptions getGMLOptions() {
		XmlOptions xmlOptions = new XmlOptions();
		Map<String, String> lPrefixMap = new Hashtable<String, String>();
		lPrefixMap.put("http://www.opengis.net/gml/3.2", "gml");
		xmlOptions.setSaveSuggestedPrefixes(lPrefixMap);
		xmlOptions.setSaveAggressiveNamespaces();
		xmlOptions.setSavePrettyPrint();
		return xmlOptions;
	}

}
