package org.uncertweb.api.om.io.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.statistic.Probability;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.JSONObservationParser;
import org.uncertweb.api.om.io.StaxObservationEncoder;
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

	private String localPath = "D:/IfGI/Projekte/UncertWeb/Implementations/uw_workspace/profiles-api/om-api/";
	private String pathToExamples = "src/test/resources";


	public void testObservationParser() throws Exception {
//		point_TimeInstant_Double();
		obsCol_Measurement();
//		obsCol_generic();
//		obsCol_yield();
//		point_TimeInstant_Uncertainty();
//		point_TimeInstant_FOIref();
//		testJSON();
	}


	public void testJSON() throws Exception{
		File folder = new File(localPath);
		if (!folder.exists()) {
			folder = new File(localPath+pathToExamples);
		}
		File[] fileArray = folder.listFiles();
		if (fileArray!=null){
			for (int i=0;i<fileArray.length;i++){
				String path = fileArray[i].getAbsolutePath();

				//parse xmlFile
				if (!path.contains("svn")){
					String xmlString = Utils.readXmlFile(path);
					XBObservationParser parser = new XBObservationParser();
					IObservationCollection obsCol = parser.parse(xmlString);
					System.out.println("-----XMLfile read from path " + path);
					System.out.print(xmlString);

					//Encode Json FIle
					JSONObservationEncoder jEncoder = new JSONObservationEncoder();
					String jsonString = jEncoder.encodeObservationCollection(obsCol);
					System.out.println("-----JSONEncodedfile");
					System.out.println(jsonString);

					JSONObservationParser jParser = new JSONObservationParser();
					System.out.println("-----JSONParsedFile");
					IObservationCollection jObs = jParser.parse(jsonString);
					System.out.println(jEncoder.encodeObservationCollection(jObs));
				}
			}
		}
	}

	private void obsCol_yield() throws Exception {

		// read XML example file
		FileInputStream fis = null;
		try {
		 fis = new FileInputStream(pathToExamples
				+ "/yield_om_anglia.xml");
		}
		catch (IOException ioe){
			fis = new FileInputStream(localPath + pathToExamples
					+ "/yield_om_anglia.xml");
		}
		XBObservationParser parser = new XBObservationParser();
		IObservationCollection oc = parser.parseObservationCollection(fis);
		fis.close();
		StaxObservationEncoder encoder = new StaxObservationEncoder();
		System.out.println(encoder.encodeObservationCollection(oc));
	}

	private void obsCol_Measurement() throws Exception {

		// read XML example file
		String xmlString;
		try {
		 xmlString = Utils.readXmlFile(pathToExamples
				+ "/ObsCol_Measurements.xml");
		}
		catch (IOException ioe){
			xmlString = Utils.readXmlFile(localPath + pathToExamples
					+ "/ObsCol_Measurements.xml");
		}
		XBObservationParser parser = new XBObservationParser();
		IObservationCollection oc = parser.parse(xmlString);
		XBObservationEncoder encoder = new XBObservationEncoder();
		System.out.println(encoder.encodeObservationCollection(oc));
	}

	private void obsCol_generic() throws Exception {

		// read XML example file
		String xmlString;
		try {
		 xmlString = Utils.readXmlFile(pathToExamples
				+ "/Obs_albatross_output.xml");
		}
		catch (IOException ioe){
			xmlString = Utils.readXmlFile(localPath + pathToExamples
					+ "/Obs_albatross_output.xml");
		}
		XBObservationParser parser = new XBObservationParser();
		IObservationCollection oc = parser.parse(xmlString);
		XBObservationEncoder encoder = new XBObservationEncoder();
		System.out.println(encoder.encodeObservationCollection(oc));
	}

	private void obsCol_Austal() throws Exception {

		// read XML example file
		String xmlString;
		try {
		 xmlString = Utils.readXmlFile(pathToExamples
				+ "/obsCol_austal.xml");
		}
		catch (IOException ioe){
			xmlString = Utils.readXmlFile(localPath + pathToExamples
					+ "/obsCol_austal.xml");
		}
		XBObservationParser parser = new XBObservationParser();
		IObservationCollection oc = parser.parse(xmlString);
		XBObservationEncoder encoder = new XBObservationEncoder();
		System.out.println(encoder.encodeObservationCollection(oc));
	}


	private void point_TimeInstant_Double() throws Exception {

		// read XML example file
		String xmlString;
		try {
		 xmlString = Utils.readXmlFile(pathToExamples
				+ "/Obs_Point_TimeInstant_double.xml");
		}
		catch (IOException ioe){
			xmlString = Utils.readXmlFile(localPath + pathToExamples
					+ "/Obs_Point_TimeInstant_double.xml");
		}

		XBObservationParser parser = new XBObservationParser();
		AbstractObservation obs = parser.parse(xmlString).getObservations().get(0);

		// test id;
		assertEquals("obsTest1", obs.getIdentifier().getIdentifier());

		// test boundedBy (optional parameter)

		// test phenomenonTime

		DateTimeFormatter format = ISODateTimeFormat.dateTime();
		assertEquals("2005-01-11T16:22:25.000+01:00",
				format.print(obs.getPhenomenonTime().getDateTime()).toString());

//		// test resultTime
//		assertEquals("#ot1t", obs.getResultTime().getHref());

		// test validTime (optional parameter)

		// test procedure
		assertEquals("http://www.example.org/register/process/scales34.xml",
				obs.getProcedure().toString());

		// test observedProperty
		assertEquals("urn:ogc:def:phenomenon:OGC:temperature", obs
				.getObservedProperty().toString());

		// test featureOfInterest

		Point shape = (Point) obs.getFeatureOfInterest().getShape();
		assertEquals("Point", shape.getGeometryType());
		assertEquals(52.87, shape.getX());
		assertEquals(7.78, shape.getY());
		assertEquals(4326, shape.getSRID());

		// test resultQuality (optional parameter)
		DQ_UncertaintyResult uncertainty =  obs.getResultQuality()[0];
		IUncertainty uValue = uncertainty.getValues()[0];
		assertEquals("degC",uncertainty.getUom());
		assertEquals("org.uncertml.distribution.continuous.GaussianDistribution",uValue.getClass().getName());
		assertEquals(29.564,((NormalDistribution)uValue).getMean().get(0));
		assertEquals(7.45,((NormalDistribution)uValue).getVariance().get(0));

		// test result
		assertEquals("degC",
				((MeasureResult) obs.getResult()).getUnitOfMeasurement());
		assertEquals(36.0, ((MeasureResult) obs.getResult()).getMeasureValue());

	}

	private void point_TimeInstant_Uncertainty() throws Exception {

		// read XML example file
		String xmlString;
		try {
			 xmlString = Utils.readXmlFile(pathToExamples
					+ "//Obs_Point_TimeInstant_uncertainty.xml");
			}
			catch (IOException ioe){
				xmlString = Utils.readXmlFile(localPath + pathToExamples
						+ "/Obs_Point_TimeInstant_uncertainty.xml");
			}

		XBObservationParser parser = new XBObservationParser();
		AbstractObservation obs = parser.parseObservation(xmlString);

		// test id;
		assertEquals("obsTest1", obs.getIdentifier().getIdentifier());

		// test boundedBy (optional parameter)

		// test phenomenonTime
		DateTimeFormatter format = ISODateTimeFormat.dateTime();
		assertEquals("2005-01-11T16:22:25.000+01:00",
				format.print(obs.getPhenomenonTime().getDateTime()).toString());

		// test resultTime
//		assertEquals("#ot1t", obs.getResultTime().getHref());

		// test validTime (optional parameter)

		// test procedure
		assertEquals("http://www.example.org/register/process/scales34.xml",
				obs.getProcedure().toString());

		// test observedProperty
		assertEquals("urn:ogc:def:phenomenon:OGC:mass", obs
				.getObservedProperty().toString());

		// test featureOfInterest
		assertEquals("SamplingPoint1", obs.getFeatureOfInterest()
				.getIdentifier().getIdentifier());

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
		System.out.println(new JSONObservationEncoder().encodeObservation(obs));
	}

	private void point_TimeInstant_FOIref() throws Exception{
		// read XML example file
		String xmlString;
		try {
			 xmlString = Utils.readXmlFile(pathToExamples
					+ "//Obs_Point_TimeInstant_double_SFref.xml");
			}
			catch (IOException ioe){
				xmlString = Utils.readXmlFile(localPath + pathToExamples
						+ "/Obs_Point_TimeInstant_double_SFref.xml");
			}

		XBObservationParser parser = new XBObservationParser();
		AbstractObservation obs = parser.parseObservation(xmlString);

		// test id;
		assertEquals("http://v-mars.uni-muenster.de/uncertweb/schema/Profiles/Sampling/Examples/SamplingPoint.xml", obs.getFeatureOfInterest().getHref().toASCIIString());

	}


	public void setUp() {

	}

	public void tearDown() {

	}
}
