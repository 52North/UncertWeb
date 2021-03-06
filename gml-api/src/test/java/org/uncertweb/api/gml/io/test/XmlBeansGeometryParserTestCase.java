package org.uncertweb.api.gml.io.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.uncertweb.api.gml.geometry.RectifiedGrid;
import org.uncertweb.api.gml.io.XmlBeansGeometryEncoder;
import org.uncertweb.api.gml.io.XmlBeansGeometryParser;

import com.vividsolutions.jts.geom.Geometry;
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


	private String localPath = "D:/IfGI/Projekte/UncertWeb/Implementations/uw_workspace/profiles-api/";
	private String pathToExamples = "src/test/resources";



	public void setUp() {
	}


	public void testPointParser() throws Exception {
		// read XML example file
		String xmlString;
		try {
		 xmlString = readXmlFile(pathToExamples
				+ "/Point.xml");
		}
		catch (IOException ioe){
			xmlString = readXmlFile(localPath + pathToExamples
					+ "/Point.xml");
		}
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser();
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("Point", geom.getGeometryType());
		assertEquals(52.87, ((Point)geom).getX());
		assertEquals(7.78, ((Point)geom).getY());
		assertEquals(4326, ((Point)geom).getSRID());
	}


	public void testPolygonParser() throws Exception {
		// read XML example file
		String xmlString;
		try {
		 xmlString = readXmlFile(pathToExamples
				+ "/Polygon.xml");
		}
		catch (IOException ioe){
			xmlString = readXmlFile(localPath + pathToExamples
					+ "/Polygon.xml");
		}
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser();
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("Polygon", geom.getGeometryType());
		assertEquals(52.79, ((Polygon)geom).getExteriorRing().getCoordinates()[0].x);
		assertEquals(4326, ((Polygon)geom).getSRID());
		XmlBeansGeometryEncoder encoder = new XmlBeansGeometryEncoder();
		System.out.println(encoder.encodeGeometry(geom));
	}


	public void testLineStringParser() throws Exception {
		String xmlString;
		try {
		 xmlString = readXmlFile(pathToExamples
				+ "/LineString.xml");
		}
		catch (IOException ioe){
			xmlString = readXmlFile(localPath + pathToExamples
					+ "/LineString.xml");
		}
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser();
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("LineString", geom.getGeometryType());
		assertEquals(52.79, ((LineString)geom).getCoordinates()[0].x);
		assertEquals(4326, ((LineString)geom).getSRID());
	}


	public void testMultiLineStringParser() throws Exception {
		String xmlString;
		try {
		 xmlString = readXmlFile(pathToExamples
				+ "/MultiLineString.xml");
		}
		catch (IOException ioe){
			xmlString = readXmlFile(localPath + pathToExamples
					+ "/MultiLineString.xml");
		}
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser();
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("MultiLineString", geom.getGeometryType());
	}

	public void testMultiPolygonParser() throws Exception {
		String xmlString;
		try {
		 xmlString = readXmlFile(pathToExamples
				+ "/MultiPolygon.xml");
		}
		catch (IOException ioe){
			xmlString = readXmlFile(localPath + pathToExamples
					+ "/MultiPolygon.xml");
		}
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser();
		Geometry geom = parser.parseUwGeometry(xmlString);
		assertEquals("MultiPolygon", geom.getGeometryType());
		System.out.println(new XmlBeansGeometryEncoder().encodeGeometry(geom));
	}


	public void testGridParser() throws Exception {
		String xmlString;
		try {
		 xmlString = readXmlFile(pathToExamples
				+ "/Grid.xml");
		}
		catch (IOException ioe){
			xmlString = readXmlFile(localPath + pathToExamples
					+ "/Grid.xml");
		}
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser();
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
