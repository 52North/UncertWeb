package org.uncertweb.api.om.io.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import junit.framework.TestCase;

import net.opengis.om.x20.OMObservationDocument;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.uncertweb.api.gml.geometry.GmlPoint;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * JUnit tests for O&M encoding
 * 
 * @author Kiesow, staschc
 * 
 */
public class XBObservationEncoderTestCase extends TestCase {

	private String localPath = "D:/IfGI/Projekte/UncertWeb/Implementations/uw_workspace/profiles-api";
	private String pathToExamples = "om-api/src/test/resources";

	public void testObservationEncoder() throws Exception {

		point_TimeInstant_DoubleTest();
//		obsCol_Point_TimeInstant_Double();
		encodeObsTP();
		encode_Point_TimeInstant_FOIref();
	}

//	private void obsCol_Point_TimeInstant_Double() throws Exception {
//
//		// read XML example file
//		// read XML example file
//		String xmlString;
//		try {
//		 xmlString = readXmlFile(pathToExamples
//				+ "/ObsCol_Point_TimeInstant_double.xml");
//		}
//		catch (IOException ioe){
//			xmlString = readXmlFile(localPath + pathToExamples
//					+ "/ObsCol_Point_TimeInstant_double.xml");
//		}
//
//		// parse XML example file
//		// necessary as long as some xml objects are copied without parsing
//		// (DQUncertaintyResult values)
//		XBObservationParser parser = new XBObservationParser();
//		ObservationCollection obsCol = parser
//				.parseObservationCollection(xmlString);
//
//		AbstractObservation obs1 = (AbstractObservation) obsCol.getMembers()
//				.toArray()[0];
//		
//
//		// encode XML example file
//		XBObservationEncoder encoder = new XBObservationEncoder();
//		String obsColString = encoder
//				.encodeObservationCollection(obsCol);
//		
//		ObservationCollection obsCol2 = parser.parseObservationCollection(obsColString);
//		AbstractObservation obs2 = (AbstractObservation) obsCol2.getMembers()
//		.toArray()[0];
//
//		// test collection
//		// test id
//		assertEquals(obs1.getGmlId(), obs2.getGmlId());
//
//		// test boundedBy (optional parameter)
//
//		// test phenomenonTime
//		assertEquals(obs1.getPhenomenonTime().getId(),obs2.getPhenomenonTime().getId());
//
//		assertEquals(obs1.getPhenomenonTime().getDateTime().toString(),
//				obs2.getPhenomenonTime().getDateTime().toString());
//
//		// test resultTime
//		// in this case resultTime references phenomenonTime
//		assertEquals(obs1.getResultTime().getHref(), obs2.getResultTime()
//				.getHref());
//
//		// test validTime (optional parameter)
//
//		// test procedure
//		assertEquals(obs1.getProcedure().toString(), obs2.getProcedure()
//				.toString());
//
//		// test observedProperty
//		assertEquals(obs1.getObservedProperty().toString(), obs2.getObservedProperty().toString());
//
//		// test featureOfInterest
//		assertEquals(obs1.getFeatureOfInterest().getGmlId(), obs2.getFeatureOfInterest().getGmlId());
//
//		Point shape = (Point) obs1.getFeatureOfInterest().getShape();
//		Point shape2 = (Point)obs2.getFeatureOfInterest().getShape();
//
//		assertEquals(shape.getX() + " " + shape.getY(), shape2.getX()+" "+shape2.getY());
//		assertEquals(shape.getSRID(), shape2.getSRID());
//
//		// test result
//		assertEquals(((MeasureResult) obs1.getResult()).getUnitOfMeasurement(),
//				((MeasureResult) obs2.getResult()).getUnitOfMeasurement());
//		assertEquals(((MeasureResult) obs1.getResult()).getMeasureValue(),
//				((MeasureResult) obs2.getResult()).getMeasureValue());
//
//		// test resultQuality
//		assertEquals(obs1.getResultQuality()[0].getValueUnit().getIdentifier(),
//				obs2.getResultQuality()[0].getValueUnit().getIdentifier());
//
//	}

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
		
		AbstractObservation obs2 = parser.parseObservation(xmlString);

		// test id
		assertEquals(obs.getGmlId(), obs2.getGmlId());

		// test boundedBy (optional parameter)

		// test phenomenonTime
		assertEquals(obs.getPhenomenonTime().getId(),obs2.getPhenomenonTime().getId());

		DateTimeFormatter format = ISODateTimeFormat.dateTime();
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
		assertEquals(obs.getFeatureOfInterest().getGmlId(), obs2.getFeatureOfInterest().getGmlId());

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
		assertEquals(obs.getResultQuality()[0].getValueUnit().getIdentifier(),
				obs2.getResultQuality()[0].getValueUnit().getIdentifier());
	}
	
	private void encode_Point_TimeInstant_FOIref() throws Exception{
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
		XBObservationEncoder encoder = new XBObservationEncoder();
		OMObservationDocument encodedObs = encoder.encodeObservationDocument(obs);
		System.out.println(encoder.encodeObservation(obs));
		// test id;
		assertEquals(obs.getFeatureOfInterest().getHref(), encodedObs.getOMObservation().getFeatureOfInterest().getHref());

	}
	
	public void encodeObsTP() throws Exception{
		Interval phenTime = new Interval(new Date().getTime(),new Date().getTime());
		GeometryFactory geomFac = new GeometryFactory();
		Coordinate[] c = {new Coordinate(1,2)};
		GmlPoint p = new GmlPoint(geomFac.getCoordinateSequenceFactory().create(c),geomFac, "point1");
		p.setSRID(4326);
		SpatialSamplingFeature sf = new SpatialSamplingFeature("sf1","Muenster",p);
		TimeObject ti = new TimeObject(new DateTime(new Date().getTime()));
		try {
			Measurement meas = new Measurement("o_1",null,new TimeObject(phenTime),ti,new TimeObject(phenTime),new URI("sensor1"),new URI("phen1"),sf,null,new MeasureResult(2.45,"cm"));
			XBObservationEncoder ecnoder = new XBObservationEncoder();
			System.out.println(ecnoder.encodeObservation(meas));
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
