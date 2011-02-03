package org.uncertweb.api.gml.io.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.uncertweb.api.gml.geometry.GmlLineString;
import org.uncertweb.api.gml.geometry.GmlMultiGeometry;
import org.uncertweb.api.gml.geometry.GmlPoint;
import org.uncertweb.api.gml.geometry.GmlPolygon;
import org.uncertweb.api.gml.geometry.RectifiedGrid;
import org.uncertweb.api.gml.io.XmlBeansGeometryEncoder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
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
		Coordinate[] c = {new Coordinate(52.77,8.76)};
		GeometryFactory geomFac = new GeometryFactory();
		GmlPoint x = new GmlPoint(geomFac.getCoordinateSequenceFactory().create(c),geomFac,"point1");
		x.setSRID(4326);
		System.out.println(encoder.encodeGeometry(x));
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
		LinearRing lr = geomFac.createLinearRing(coords);
		LinearRing interior = geomFac.createLinearRing(coords2);
		LinearRing[] holes = {interior};
		GmlPolygon poly = new GmlPolygon(lr, holes,geomFac,"poly1");
		System.out.println(encoder.encodeGeometry(poly));
	}
	
	
	public void testLineStringEncoder() throws Exception{
		XmlBeansGeometryEncoder encoder = new XmlBeansGeometryEncoder();
		Coordinate[] coords = new Coordinate[4];
		coords[0] = new Coordinate(0.0,0.0);
		coords[1] = new Coordinate(0.0,1.0);
		coords[2] = new Coordinate(1.0,1.0);
		coords[3] = new Coordinate(1.0,0.0);
		GmlLineString ls = new GmlLineString(geomFac.getCoordinateSequenceFactory().create(coords),geomFac,"lineString1");
		ls.setSRID(4326);
		System.out.println(encoder.encodeGeometry(ls));
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
		Coordinate[] c = {new Coordinate(52.72,7.82)};
		GmlPoint p = new GmlPoint(geomFac.getCoordinateSequenceFactory().create(c),geomFac,"point1");
		p.setSRID(4326);
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
		RectifiedGrid rg = new RectifiedGrid("grid1", env, axisLabels, p, offsetVectors, geomFac);
		System.out.println(encoder.encodeGeometry(rg));
	}
	
	public void testMultiGeometryEncoder() throws Exception{
		XmlBeansGeometryEncoder encoder = new XmlBeansGeometryEncoder();
		Geometry[] geom = new Geometry[3];
		Coordinate[] c={new Coordinate(52.72,7.82)};
		CoordinateSequence cs = geomFac.getCoordinateSequenceFactory().create(c);
		GmlPoint p = new GmlPoint(cs,geomFac,"point1");
		p.setSRID(4326);
		geom[0]=p;
		c[0]=new Coordinate(53.72,8.82);
		cs = geomFac.getCoordinateSequenceFactory().create(c);
		p = new GmlPoint(cs,geomFac,"point1");
		p.setSRID(4326);
		geom[1]=p;
		c[0]=new Coordinate(54.72,9.82);
		cs = geomFac.getCoordinateSequenceFactory().create(c);
		p = new GmlPoint(cs,geomFac,"point1");
		p.setSRID(4326);
		geom[2]=p;
		GmlMultiGeometry geomCol = new GmlMultiGeometry(geom,geomFac,"multiGeom1");
		System.out.println(encoder.encodeGeometry(geomCol));
	}

}
