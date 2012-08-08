package org.uncertweb.api.gml.io;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.opengis.gml.x32.AbstractRingPropertyType;
import net.opengis.gml.x32.DirectPositionListType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.GridEnvelopeType;
import net.opengis.gml.x32.LineStringDocument;
import net.opengis.gml.x32.LineStringPropertyType;
import net.opengis.gml.x32.LineStringType;
import net.opengis.gml.x32.LinearRingDocument;
import net.opengis.gml.x32.LinearRingType;
import net.opengis.gml.x32.MultiLineStringDocument;
import net.opengis.gml.x32.MultiLineStringType;
import net.opengis.gml.x32.MultiPointDocument;
import net.opengis.gml.x32.MultiPointType;
import net.opengis.gml.x32.MultiSurfaceDocument;
import net.opengis.gml.x32.MultiSurfaceType;
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
import org.apache.xmlbeans.XmlOptions;
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
 * Interface for encoding GML geometries and features in XMLBeans
 * 
 * @author staschc
 * 
 */
public class XmlBeansGeometryEncoder implements IGeometryEncoder {
	
	/**counter used for generation of gml IDs*/
	private int gmlIDcounter=0;

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
		} else if (geom instanceof MultiPoint) {
			result = encodeMultiPoint((MultiPoint) geom);
		}else if (geom instanceof MultiLineString) {
			result = encodeMultiLineString((MultiLineString) geom);
		}
		else if (geom instanceof MultiPolygon) {
			result = encodeMultiPolygon((MultiPolygon) geom);
		}

		return result;
	}
	
	

	@Override
	public String encodeFeature(UwAbstractFeature feature) {
		// TODO needs to be implemented for certain feature types
		return null;
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
		xb_ls.setId(generateGmlId());
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
		xb_poly.setId(generateGmlId());
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
		xb_pt.setId(generateGmlId());
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
		
		//TODO maybe change to posList!!
//		DirectPositionType[] xb_posList = new DirectPositionType[coords.length];
//		for (int i = 0; i < coords.length; i++) {
//			DirectPositionType xb_pos = DirectPositionType.Factory
//					.newInstance();
//			xb_pos.setStringValue(coords[i].x + " " + coords[i].y);
//			xb_posList[i] = xb_pos;
//		}
//		xb_lrType.setPosArray(xb_posList);
		
		DirectPositionListType xb_posList = xb_lrType.addNewPosList();
		String posString = "";
		for (int i = 0; i < coords.length; i++) {
			posString = posString + coords[i].x + " "+ coords[i].y;
			//append space, if it is not the last coordinate
			if (i!=coords.length-1){
				posString = posString + " ";
			}
		}
		xb_posList.setStringValue(posString);
		return xb_lrDoc;
	}
	
	/**
	 * helper mehtod for encoding MultiLineString
	 * 
	 * @param gmlMls
	 * 			JTS representation of MultiLineString
	 * @return
	 *			XML String of MultiLineString
	 */
	private String encodeMultiLineString(MultiLineString gmlMls){
		return encodeMultiLineString2Doc(gmlMls).xmlText(getGMLOptions());
	}
	
	/**
	 * helper mehtod for encoding MultiLineString
	 * 
	 * @param gmlMls
	 * 			JTS representation of MultiLineString
	 * @return
	 *			XMLBeans representation of MultiLineString
	 */
	public MultiLineStringDocument encodeMultiLineString2Doc(MultiLineString gmlMls){
		MultiLineStringDocument xb_mlsDoc = MultiLineStringDocument.Factory.newInstance();
		MultiLineStringType xb_mls = xb_mlsDoc.addNewMultiLineString();
		//set gml ID
		xb_mls.setId(generateGmlId());
		xb_mls.setSrsName(UwGMLUtil.EPSG_URL+gmlMls.getSRID());
		int size = gmlMls.getNumGeometries();
		for (int i=0;i<size;i++){
			LineStringPropertyType xb_ls = xb_mls.addNewLineStringMember();
			LineString gmlLs = (LineString)gmlMls.getGeometryN(i);
			gmlLs.setSRID(gmlMls.getSRID());
			LineStringDocument xb_lsDoc = encodeLineString2Doc(gmlLs);
			xb_ls.set(xb_lsDoc);
		}
		return xb_mlsDoc;
	}

	/**
	 * helper method for encoding MultiPoint
	 * 
	 * @param gmlMls
	 * 			JTS representation of MultiPoint
	 * @return
	 *			XML String of MultiPoint
	 */
	private String encodeMultiPoint(MultiPoint geom) {
		return encodeMultiPoint2Doc(geom).xmlText(getGMLOptions());
	}
	
	/**
	 * helper method for encoding MultiPoint
	 * 
	 * @param gmlMls
	 * 			JTS representation of MultiPoint
	 * @return
	 *			XMLBeans representation of MultiPoint
	 */
	public MultiPointDocument encodeMultiPoint2Doc(MultiPoint gmlMls){
		MultiPointDocument xb_mlsDoc = MultiPointDocument.Factory.newInstance();
		MultiPointType xb_mls = xb_mlsDoc.addNewMultiPoint();
		//set gml ID
		xb_mls.setId(generateGmlId());
		xb_mls.setSrsName(UwGMLUtil.EPSG_URL+gmlMls.getSRID());
		
		int size = gmlMls.getNumGeometries();
		for (int i=0;i<size;i++){
			PointPropertyType xb_ls = xb_mls.addNewPointMember();
			Point p = (Point)gmlMls.getGeometryN(i);
			p.setSRID(gmlMls.getSRID());
			PointDocument xb_lsDoc = encodePoint2Doc(p);
			xb_ls.set(xb_lsDoc);
		}
		return xb_mlsDoc;
	}
	
	/**
	 * helper method for encoding MultiPolygon
	 * 
	 * @param gmlMls
	 * 			JTS representation of MultiPolygon
	 * @return
	 *			XML String of MultiPolygon
	 */
	private String encodeMultiPolygon(MultiPolygon geom) {
//		return encodeMultiPolygon2Doc(geom).xmlText(getGMLOptions());
		return encodeMultiPolygon2MultiSurfaceDoc(geom).xmlText(getGMLOptions());
	}
	
	/**
	 * helper method for encoding MultiPolygon
	 * 
	 * @param gmlMls
	 * 			JTS representation of MultiPolygon
	 * @return
	 *			XMLBeans representation of MultiPolygon
	 */
	//public MultiSurfaceDocument encodeMultiPolygon2Doc(MultiPolygon gmlMls){
	public MultiSurfaceDocument encodeMultiPolygon2MultiSurfaceDoc(MultiPolygon gmlMls){
		MultiSurfaceDocument xb_mlsDoc = MultiSurfaceDocument.Factory.newInstance();
		MultiSurfaceType xb_mls = xb_mlsDoc.addNewMultiSurface();
		//set gml ID
		xb_mls.setId(generateGmlId());
		xb_mls.setSrsName(UwGMLUtil.EPSG_URL+gmlMls.getSRID());
		int size = gmlMls.getNumGeometries();
		for (int i=0;i<size;i++){
			PolygonPropertyType xb_ls = xb_mls.addNewSurfaceMember();
			Polygon poly = (Polygon)gmlMls.getGeometryN(i);
			poly.setSRID(gmlMls.getSRID());
			PolygonDocument xb_lsDoc = encodePolygon2Doc(poly);
			xb_ls.set(xb_lsDoc);
		}
		return xb_mlsDoc;
	}
	
	//TODO add further encoding methods for MultiPoint and MultiPolygon
	
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
	
	/**
	 * helper methods which generates GML ID using the counter
	 * 
	 * @return Returns GML ID
	 */
	private String generateGmlId(){
		String gmlID = "g"+this.gmlIDcounter;
		this.gmlIDcounter++;
		return gmlID;
	}

	/**
	 * resets the counter used to generate the GML IDs
	 */
	public void resetCounter(){
		this.gmlIDcounter=0;
	}
}
