package org.uncertweb.api.om.io.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.GaussianDistribution;
import org.uncertml.statistic.Probability;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.UncertaintyResult;

import com.vividsolutions.jts.geom.Point;

/**
 * JUnit tests for O&M parsing
 * 
 * @author Kiesow, staschc
 * 
 */
public class XBObservationParserTestCase extends TestCase {

	private String localPath = "D:/IfGI/Projekte/UncertWeb/Implementations/uw_workspace/profiles-api/";
	private String pathToExamples = "om-api/src/test/resources";
	
	
	public void testObservationParser() throws Exception {
		point_TimeInstant_Double();
		obsCol_Measurement();
		point_TimeInstant_Uncertainty();
		point_TimeInstant_FOIref();
	}

	private void obsCol_Measurement() throws Exception {

		// read XML example file
		String xmlString;
		try {
		 xmlString = readXmlFile(pathToExamples
				+ "/ObsCol_Measurements.xml");
		}
		catch (IOException ioe){
			xmlString = readXmlFile(localPath + pathToExamples
					+ "/ObsCol_Measurements.xml");
		}
		XBObservationParser parser = new XBObservationParser();
		IObservationCollection oc = parser.parseObservationCollection(xmlString);
		XBObservationEncoder encoder = new XBObservationEncoder();
		System.out.println(encoder.encodeObservationCollection(oc));
	}
		


	private void point_TimeInstant_Double() throws Exception {

		// read XML example file
		String xmlString;
		try {
		 xmlString = readXmlFile(pathToExamples
				+ "/Obs_Point_TimeInstant_double.xml");
		}
		catch (IOException ioe){
			xmlString = readXmlFile(localPath + pathToExamples
					+ "/Obs_Point_TimeInstant_double.xml");
		}

		XBObservationParser parser = new XBObservationParser();
		AbstractObservation obs = parser.parseObservation(xmlString);

		// test id;
		assertEquals("obsTest1", obs.getIdentifier());

		// test boundedBy (optional parameter)

		// test phenomenonTime
		assertEquals("ot1t", obs.getPhenomenonTime().getId());

		DateTimeFormatter format = ISODateTimeFormat.dateTime();
		assertEquals("2005-01-11T16:22:25.000+01:00",
				format.print(obs.getPhenomenonTime().getDateTime()).toString());

		// test resultTime
		assertEquals("#ot1t", obs.getResultTime().getHref());

		// test validTime (optional parameter)

		// test procedure
		assertEquals("http://www.example.org/register/process/scales34.xml",
				obs.getProcedure().toString());

		// test observedProperty
		assertEquals("urn:ogc:def:phenomenon:OGC:temperature", obs
				.getObservedProperty().toString());

		// test featureOfInterest
		assertEquals("SamplingPoint1", obs.getFeatureOfInterest()
				.getIdentifier());

		Point shape = (Point) obs.getFeatureOfInterest().getShape();
		assertEquals("Point", shape.getGeometryType());
		assertEquals(52.87, shape.getX());
		assertEquals(7.78, shape.getY());
		assertEquals(4326, shape.getSRID());

		// test resultQuality (optional parameter)
		DQ_UncertaintyResult uncertainty =  obs.getResultQuality()[0];
		IUncertainty uValue = uncertainty.getValues()[0];
		assertEquals("degC",uncertainty.getValueUnit().getIdentifier());
		assertEquals("org.uncertml.distribution.continuous.GaussianDistribution",uValue.getClass().getName());
		assertEquals(29.564,((GaussianDistribution)uValue).getMean().get(0));
		assertEquals(7.45,((GaussianDistribution)uValue).getVariance().get(0));
		
		// test result
		assertEquals("degC",
				((MeasureResult) obs.getResult()).getUnitOfMeasurement());
		assertEquals(36.0, ((MeasureResult) obs.getResult()).getMeasureValue());

	}
	
	private void point_TimeInstant_Uncertainty() throws Exception {

		// read XML example file
		String xmlString;
		try {
			 xmlString = readXmlFile(pathToExamples
					+ "//Obs_Point_TimeInstant_uncertainty.xml");
			}
			catch (IOException ioe){
				xmlString = readXmlFile(localPath + pathToExamples
						+ "/Obs_Point_TimeInstant_uncertainty.xml");
			}

		XBObservationParser parser = new XBObservationParser();
		AbstractObservation obs = parser.parseObservation(xmlString);

		// test id;
		assertEquals("obsTest1", obs.getIdentifier());

		// test boundedBy (optional parameter)

		// test phenomenonTime
		assertEquals("ot1t", obs.getPhenomenonTime().getId());

		DateTimeFormatter format = ISODateTimeFormat.dateTime();
		assertEquals("2005-01-11T16:22:25.000+01:00",
				format.print(obs.getPhenomenonTime().getDateTime()).toString());

		// test resultTime
		assertEquals("#ot1t", obs.getResultTime().getHref());

		// test validTime (optional parameter)

		// test procedure
		assertEquals("http://www.example.org/register/process/scales34.xml",
				obs.getProcedure().toString());

		// test observedProperty
		assertEquals("urn:ogc:def:phenomenon:OGC:mass", obs
				.getObservedProperty().toString());

		// test featureOfInterest
		assertEquals("SamplingPoint1", obs.getFeatureOfInterest()
				.getIdentifier());

		Point shape = (Point) obs.getFeatureOfInterest().getShape();
		assertEquals("Point", shape.getGeometryType());
		assertEquals(52.87, shape.getX());
		assertEquals(7.78, shape.getY());
		assertEquals(4326, shape.getSRID());

		// test result which contains probability
		Object uncertainty = ((UncertaintyResult)obs.getResult()).getUncertaintyValue();
		assertEquals("org.uncertml.statistic.Probability",uncertainty.getClass().getName());
		assertEquals("GREATER_THAN", ((Probability)uncertainty).getConstraints().get(0).getType().name());
		assertEquals(35.0, ((Probability)uncertainty).getConstraints().get(0).getValue());
		assertEquals(0.25, ((Probability)uncertainty).getValues().get(0).doubleValue());
		
	}	
	
	private void point_TimeInstant_FOIref() throws Exception{
		// read XML example file
		String xmlString;
		try {
			 xmlString = readXmlFile(pathToExamples
					+ "//Obs_Point_TimeInstant_double_SFref.xml");
			}
			catch (IOException ioe){
				xmlString = readXmlFile(localPath + pathToExamples
						+ "/Obs_Point_TimeInstant_double_SFref.xml");
			}

		XBObservationParser parser = new XBObservationParser();
		AbstractObservation obs = parser.parseObservation(xmlString);

		// test id;
		assertEquals("http://localhost:8080/sta-wps/SamplingPoint.xml", obs.getFeatureOfInterest().getHref());

	}

	private String readXmlFile(String filePath) throws IOException {
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

	public void setUp() {

	}

	public void tearDown() {

	}
}
