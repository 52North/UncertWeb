package org.uncertweb.api.gml.io.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.uncertweb.api.gml.geometry.RectifiedGrid;
import org.uncertweb.api.gml.io.XmlBeansGeometryParser;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Junit tests for XmlBeans parsing
 * 
 * @author staschc
 *
 */
public class XmlBeansGeometryParserTestCase extends TestCase {

	private final String EXAMPLES_PATH = "src/test/resources";
	private GeometryFactory geomFac;
	
	
	public void setUp() {
		geomFac = new GeometryFactory();
	}

	
	public void testPointParser() throws Exception {

		// read XML example file
		String xmlString = readXmlFile(EXAMPLES_PATH + "/Point.xml");
		
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser(geomFac);
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("Point", geom.getGeometryType());
		assertEquals(52.87, ((Point)geom).getX());
		assertEquals(7.78, ((Point)geom).getY());
		assertEquals(4326, ((Point)geom).getSRID());
	}

	
	public void testPolygonParser() throws Exception {
		// read XML example file
		String xmlString = readXmlFile(EXAMPLES_PATH + "/Polygon.xml");
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser(geomFac);
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("Polygon", geom.getGeometryType());
		assertEquals(52.79, ((Polygon)geom).getExteriorRing().getCoordinates()[0].x);
		assertEquals(4326, ((Polygon)geom).getSRID());
	}

	
	public void testLineStringParser() throws Exception {
		String xmlString = readXmlFile(EXAMPLES_PATH + "/LineString.xml");
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser(geomFac);
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("LineString", geom.getGeometryType());
		assertEquals(52.79, ((LineString)geom).getCoordinates()[0].x);
		assertEquals(4326, ((LineString)geom).getSRID());
	}

	
	public void testMultiGeometryParser() throws Exception {
		String xmlString = readXmlFile(EXAMPLES_PATH + "/MultiGeometry.xml");
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser(geomFac);
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("GeometryCollection", geom.getGeometryType());
		assertEquals(52.87, ((GeometryCollection)geom).getGeometryN(0).getCoordinate().x);
		assertEquals(4326, ((GeometryCollection)geom).getGeometryN(0).getSRID());
	}
	
	
	public void testGridParser() throws Exception {
		String xmlString = readXmlFile(EXAMPLES_PATH + "/Grid.xml");
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser(geomFac);
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("RectifiedGrid", geom.getGeometryType());
		assertEquals(52.77, ((RectifiedGrid)geom).getOrigin().getCoordinate().x);
		assertEquals(4326, ((RectifiedGrid)geom).getOrigin().getSRID());
	}

	public void tearDown() {

	}
	
	private String readXmlFile(String filePath) throws IOException{
		String result = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(filePath)));
		try {
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} finally {
			in.close();
		}
		return result;
	}
}
