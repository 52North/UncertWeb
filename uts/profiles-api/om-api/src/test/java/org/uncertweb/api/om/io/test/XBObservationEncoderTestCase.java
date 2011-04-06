package org.uncertweb.api.om.io.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;
import net.opengis.om.x20.OMObservationDocument;

import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.gml.geometry.GmlGeometryFactory;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.JSONObservationParser;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Point;

/**
 * JUnit tests for O&M encoding
 * 
 * @author Kiesow, staschc
 * 
 */
public class XBObservationEncoderTestCase extends TestCase {

	private String localPath = "D:/IfGI/Projekte/UncertWeb/Implementations/uw_workspace/profiles-api";
	private String pathToExamples = "/om-api/src/test/resources";
	
	public void testObservationEncoder() throws Exception {

		point_TimeInstant_DoubleTest();
		obsCol_Point_TimeInstant_Double();
		encodeObsTP();
		encode_Point_TimeInstant_FOIref();
	}

	/**
	 * gests encoding of observation collection containing measurements
	 * 
	 * @throws Exception
	 */
	private void obsCol_Point_TimeInstant_Double() throws Exception {

		// read XML example file
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

		// parse XML example file
		// necessary as long as some xml objects are copied without parsing
		// (DQUncertaintyResult values)
		XBObservationParser parser = new XBObservationParser();
		IObservationCollection obsCol = parser
				.parseObservationCollection(xmlString);

		AbstractObservation obs1 = (AbstractObservation) obsCol.getObservations()
				.toArray()[0];
		

		// encode XML example file
		XBObservationEncoder encoder = new XBObservationEncoder();
		String obsColString = encoder
				.encodeObservationCollection(obsCol);
		System.out.println(obsColString);
		
		IObservationCollection obsCol2 = parser.parseObservationCollection(obsColString);
		AbstractObservation obs2 = (AbstractObservation) obsCol2.getObservations()
		.toArray()[0];
		
		System.out.println(encoder.encodeObservation(obs1));
		System.out.println(new JSONObservationEncoder().encodeObservation(obs1));
		String jsonObsColString = new JSONObservationEncoder().encodeObservationCollection(obsCol2);
		System.out.println(jsonObsColString);
		

		// test collection
		// test id
		assertEquals(obs1.getIdentifier(), obs2.getIdentifier());

		// test boundedBy (optional parameter)

		// test phenomenonTime
		
		assertEquals(obs1.getPhenomenonTime().getDateTime().toString(),
				obs2.getPhenomenonTime().getDateTime().toString());


		// test procedure
		assertEquals(obs1.getProcedure().toString(), obs2.getProcedure()
				.toString());

		// test observedProperty
		assertEquals(obs1.getObservedProperty().toString(), obs2.getObservedProperty().toString());

		// test featureOfInterest
		assertEquals(obs1.getFeatureOfInterest().getIdentifier(), obs2.getFeatureOfInterest().getIdentifier());

		Point shape = (Point) obs1.getFeatureOfInterest().getShape();
		Point shape2 = (Point)obs2.getFeatureOfInterest().getShape();

		assertEquals(shape.getX() + " " + shape.getY(), shape2.getX()+" "+shape2.getY());
		assertEquals(shape.getSRID(), shape2.getSRID());

		// test result
		assertEquals(((MeasureResult) obs1.getResult()).getUnitOfMeasurement(),
				((MeasureResult) obs2.getResult()).getUnitOfMeasurement());
		assertEquals(((MeasureResult) obs1.getResult()).getMeasureValue(),
				((MeasureResult) obs2.getResult()).getMeasureValue());

		// test resultQuality
		assertEquals(obs1.getResultQuality()[0].getUom(),
				obs2.getResultQuality()[0].getUom());

	}

	/**
	 * tests encoding of observation with double result and time instants in time properties
	 * 
	 * @throws Exception
	 * 			if encoding fails
	 */
	private void point_TimeInstant_DoubleTest() throws Exception {

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


		// parse XML example file
		// necessary as long as some xml objects are copied without parsing
		// (DQUncertaintyResult values)
		XBObservationParser parser = new XBObservationParser();
		AbstractObservation obs = parser.parseObservation(xmlString);

		// encode XML example file
		XBObservationEncoder encoder = new XBObservationEncoder();
		String obsString = encoder.encodeObservation(obs);
		System.out.println(obsString);
		JSONObservationEncoder jobsEnc = new JSONObservationEncoder();
		String jobsString = jobsEnc.encodeObservation(obs);
		System.out.println(jobsString);
		AbstractObservation obs3 = new JSONObservationParser().parseObservation(jobsString);
		System.out.println(jobsEnc.encodeObservation(obs3));
		
		AbstractObservation obs2 = parser.parseObservation(xmlString);

		// test id
		assertEquals(obs.getIdentifier().getIdentifier(), obs2.getIdentifier().getIdentifier());

		// test boundedBy (optional parameter)

		// test phenomenonTime
		
		assertEquals(obs.getPhenomenonTime().getDateTime(),
				obs2.getPhenomenonTime().getDateTime());

		// test resultTime
		// in this case resultTime references phenomenonTime
		assertEquals(obs.getResultTime().getHref(), obs2.getResultTime()
				.getHref());

		// test validTime (optional parameter)

		// test procedure
		assertEquals(obs.getProcedure().toString(), obs.getProcedure()
				.toString());

		// test observedProperty
		assertEquals(obs.getObservedProperty().toString(), obs2.getObservedProperty().toString());

		// test featureOfInterest
		assertEquals(obs.getFeatureOfInterest().getIdentifier(), obs2.getFeatureOfInterest().getIdentifier());

		Point shape = (Point) obs.getFeatureOfInterest().getShape();
		Point shape2 = (Point)obs2.getFeatureOfInterest().getShape();

		assertEquals(shape.getX() + " " + shape.getY(), shape2.getX()+" "+shape2.getY());
		assertEquals(shape.getSRID(), shape2.getSRID());

		// test result
		assertEquals(((MeasureResult) obs.getResult()).getUnitOfMeasurement(),
				((MeasureResult) obs2.getResult()).getUnitOfMeasurement());
		assertEquals(((MeasureResult) obs.getResult()).getMeasureValue(),
				((MeasureResult) obs2.getResult()).getMeasureValue());

		// test resultQuality
		assertEquals(obs.getResultQuality()[0].getUom(),
				obs2.getResultQuality()[0].getUom());
	}
	
	private void encode_Point_TimeInstant_FOIref() throws Exception{
		// read XML example file
		String xmlString;
		try {
			 xmlString = readXmlFile(pathToExamples
					+ "/Obs_Point_TimeInstant_double_SFref.xml");
			}
			catch (IOException ioe){
				xmlString = readXmlFile(localPath + pathToExamples
						+ "/Obs_Point_TimeInstant_double_SFref.xml");
			}

		XBObservationParser parser = new XBObservationParser();
		AbstractObservation obs = parser.parseObservation(xmlString);
		XBObservationEncoder encoder = new XBObservationEncoder();
		OMObservationDocument encodedObs = encoder.encodeObservationDocument(obs);
		System.out.println(encoder.encodeObservation(obs));
		// test id;
		assertEquals(obs.getFeatureOfInterest().getHref().toASCIIString(), encodedObs.getOMObservation().getFeatureOfInterest().getHref());

	}
	
	public void encodeObsTP() throws Exception{
		try {
		//create temporal elements
		TimeObject phenomenonTime = new TimeObject("2005-01-11T16:22:25.000+01:00","2005-01-12T16:22:25.000+01:00");
		TimeObject resultTime = new TimeObject("2005-01-12T16:22:25.000+01:00");
		TimeObject validTime = new TimeObject("2005-01-13T16:22:25.000+01:00","2005-01-14T16:22:25.000+01:00");
		
		//create spatial feature
		Point p = new GmlGeometryFactory().createPoint(52.72, 8.72, 4326);
		SpatialSamplingFeature sf = new SpatialSamplingFeature("Muenster",p);
		
		//optional identifier
		Identifier id = new Identifier(new URI("http://www.uncertweb.org"),"o_1");
		Measurement meas = new Measurement(id,null,phenomenonTime,resultTime,validTime,new URI("sensor1"),new URI("phen1"),sf,null,new MeasureResult(2.45,"cm"));
		XBObservationEncoder encoder = new XBObservationEncoder();
		System.out.println(encoder.encodeObservation(meas));
		System.out.println(new JSONObservationEncoder().encodeObservation(meas));

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setUp() {

	}

	public void tearDown() {

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
}
