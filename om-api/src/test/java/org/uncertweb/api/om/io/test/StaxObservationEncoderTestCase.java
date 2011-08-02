package org.uncertweb.api.om.io.test;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.MeasureResult;

import com.vividsolutions.jts.geom.Point;

import junit.framework.TestCase;


public class StaxObservationEncoderTestCase extends TestCase {

	private String localPath = "D:/IfGI/Projekte/UncertWeb/Implementations/uw_workspace/profiles-api/om-api/";
	private String pathToExamples = "src/test/resources";
	
	
	public void testObservationEncoder() throws Exception {

		obsCol_Point_TimeInstant_Double();
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
		 xmlString = TestUtils.readXmlFile(pathToExamples
				+ "/ObsCol_Measurements.xml");
		}
		catch (IOException ioe){
			xmlString = TestUtils.readXmlFile(localPath + pathToExamples
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
		StaxObservationEncoder encoder = new StaxObservationEncoder();
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
	
	
}
