package org.uncertweb.api.gml.io.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.uncertweb.api.gml.geometry.GmlLineString;
import org.uncertweb.api.gml.geometry.GmlPoint;
import org.uncertweb.api.gml.geometry.GmlPolygon;
import org.uncertweb.api.gml.geometry.RectifiedGrid;
import org.uncertweb.api.gml.geometry.collections.GmlMultiGeometry;
import org.uncertweb.api.gml.geometry.collections.GmlMultiLineString;
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

	private final String EXAMPLES_PATH = "D:/IfGI/Projekte/UncertWeb/Implementations/uw_workspace/profiles-api/gml-api/src/test/resources";
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
		assertEquals("UOMlocation",((GmlPoint)geom).getGmlId());
	}

	
	public void testPolygonParser() throws Exception {
		// read XML example file
		String xmlString = readXmlFile(EXAMPLES_PATH + "/Polygon.xml");
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser(geomFac);
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("Polygon", geom.getGeometryType());
		assertEquals(52.79, ((Polygon)geom).getExteriorRing().getCoordinates()[0].x);
		assertEquals(4326, ((Polygon)geom).getSRID());
		assertEquals("polygon1",((GmlPolygon)geom).getGmlId());
	}

	
	public void testLineStringParser() throws Exception {
		String xmlString = readXmlFile(EXAMPLES_PATH + "/LineString.xml");
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser(geomFac);
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("LineString", geom.getGeometryType());
		assertEquals(52.79, ((LineString)geom).getCoordinates()[0].x);
		assertEquals(4326, ((LineString)geom).getSRID());
		assertEquals("lineString1",((GmlLineString)geom).getGmlId());
	}

	
	public void testMultiLineStringParser() throws Exception {
		String xmlString = readXmlFile(EXAMPLES_PATH + "/MultiLineString.xml");
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser(geomFac);
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("MultiLineString", geom.getGeometryType());
		assertEquals("col1",((GmlMultiLineString)geom).getGmlId());
	}
	
	
	public void testGridParser() throws Exception {
		String xmlString = readXmlFile(EXAMPLES_PATH + "/Grid.xml");
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser(geomFac);
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("RectifiedGrid", geom.getGeometryType());
		assertEquals(52.77, ((RectifiedGrid)geom).getOrigin().getCoordinate().x);
		assertEquals(4326, ((RectifiedGrid)geom).getOrigin().getSRID());
		assertEquals("grid1",((RectifiedGrid)geom).getGmlId());
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
