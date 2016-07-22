package org.uncertweb.api.gml.io.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.uncertweb.api.gml.geometry.GmlGeometryFactory;
import org.uncertweb.api.gml.geometry.RectifiedGrid;
import org.uncertweb.api.gml.io.JSONGeometryDecoder;
import org.uncertweb.api.gml.io.JSONGeometryEncoder;
import org.uncertweb.api.gml.io.XmlBeansGeometryEncoder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Junit tests for XmlBeans encoding
 * 
 * @author staschc
 *
 */
public class XmlBeansGeometryEncoderTestCase extends TestCase {
	
	private GeometryFactory geomFac;
	
	public void setUp() {
		geomFac = new GeometryFactory();
	}
	
	public void testPointEncoder() throws Exception {
		XmlBeansGeometryEncoder encoder = new XmlBeansGeometryEncoder();
		Point x = new GmlGeometryFactory().createPoint(52.77, 8.76, 4326);
		x.setSRID(4326);
		System.out.println(encoder.encodeGeometry(x));
		String jsonString = new JSONGeometryEncoder().encodeGeometry(x);
		System.out.println(jsonString);
		Geometry geometry = new JSONGeometryDecoder().parseUwGeometry(jsonString);
		System.out.println(new JSONGeometryEncoder().encodeGeometry(geometry));
	}
	
	public void testPolygonEncoder() throws Exception{
		XmlBeansGeometryEncoder encoder = new XmlBeansGeometryEncoder();
		Coordinate[] coords = new Coordinate[5];
		coords[0] = new Coordinate(0.0,0.0);
		coords[1] = new Coordinate(0.0,1.0);
		coords[2] = new Coordinate(1.0,1.0);
		coords[3] = new Coordinate(1.0,0.0);
		coords[4] = new Coordinate(0.0,0.0);
		
		Coordinate[] coords2 = new Coordinate[5];
		coords2[0] = new Coordinate(0.25,0.25);
		coords2[1] = new Coordinate(0.25,0.75);
		coords2[2] = new Coordinate(0.75,0.75);
		coords2[3] = new Coordinate(0.75,0.25);
		coords2[4] = new Coordinate(0.25,0.25);
		List<Coordinate[]> holes = new ArrayList<Coordinate[]>();
		holes.add(coords2);
		Polygon poly = new GmlGeometryFactory().createPolygon(coords, holes, 4326);
		System.out.println(encoder.encodeGeometry(poly));
		String jsonString = new JSONGeometryEncoder().encodeGeometry(poly);
		System.out.println(jsonString);
		Geometry geometry = new JSONGeometryDecoder().parseUwGeometry(jsonString);
		System.out.println(new JSONGeometryEncoder().encodeGeometry(geometry));
	}
	
	
	public void testLineStringEncoder() throws Exception{
		XmlBeansGeometryEncoder encoder = new XmlBeansGeometryEncoder();
		Coordinate[] coords = new Coordinate[4];
		coords[0] = new Coordinate(0.0,0.0);
		coords[1] = new Coordinate(0.0,1.0);
		coords[2] = new Coordinate(1.0,1.0);
		coords[3] = new Coordinate(1.0,0.0);
		LineString ls =  new GmlGeometryFactory().createLineString(coords, 4326);
		System.out.println(encoder.encodeGeometry(ls));
		String jsonString = new JSONGeometryEncoder().encodeGeometry(ls);
		System.out.println(jsonString);
		Geometry geom = new JSONGeometryDecoder().parseUwGeometry(jsonString);
		System.out.println(new JSONGeometryEncoder().encodeGeometry(geom));
	}
	
	public void testRectifiedGridEncoder() throws Exception{
		XmlBeansGeometryEncoder encoder = new XmlBeansGeometryEncoder();
		Coordinate[] coords = new Coordinate[4];
		coords[0] = new Coordinate(0.0,0.0);
		coords[1] = new Coordinate(0.0,1.0);
		coords[2] = new Coordinate(1.0,1.0);
		coords[3] = new Coordinate(1.0,0.0);
		Envelope env = new Envelope();
		env.init(new Coordinate(1,1), new Coordinate(5,5));
		Point p = new GmlGeometryFactory().createPoint(52.72, 7.82, 4326);
		Collection<Point> offsetVectors = new ArrayList<Point>(2);
		Point p1 = geomFac.createPoint(new Coordinate(0.52,3.3));
		p1.setSRID(4326);
		Point p2 = geomFac.createPoint(new Coordinate(1.52,1.1));
		p.setSRID(4326);
		offsetVectors.add(p1);
		offsetVectors.add(p2);
		List<String> axisLabels = new ArrayList<String>();
		axisLabels.add("u");
		axisLabels.add("v");
		RectifiedGrid rg = new RectifiedGrid( env, axisLabels, p, offsetVectors, geomFac);
		System.out.println(encoder.encodeGeometry(rg));
	}
	
	public void testMultiGeometryEncoder() throws Exception{
		XmlBeansGeometryEncoder encoder = new XmlBeansGeometryEncoder();
		Point[] geom = new Point[3];
		Point p = new GmlGeometryFactory().createPoint(54.72,7.82,4326 );
		geom[0]=p;
		p = new GmlGeometryFactory().createPoint(54.72,8.82,4326 );
		geom[1]=p;
		p = new GmlGeometryFactory().createPoint(54.72,9.82,4326 );
		geom[2]=p;
		MultiPoint geomCol = new GmlGeometryFactory().createMultiPoint(geom, 4326);
		System.out.println(encoder.encodeGeometry(geomCol));
		String jsonString = new JSONGeometryEncoder().encodeGeometry(geomCol);
		System.out.println(jsonString);
		Geometry geometry = new JSONGeometryDecoder().parseUwGeometry(jsonString);
		System.out.println(new JSONGeometryEncoder().encodeGeometry(geometry));
	}

}
