package org.uncertweb.om.io.v1;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;

import org.junit.Test;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

public class ParserTest {

	@Test
	public void testMeasurements() throws Exception {
		testFile("/measurement.xml",false,false);
	}

	@Test
	public void testObservations() throws Exception {
		testFile("/observation.xml",false,false);
	}

	@Test
	public void testSurface() throws Exception {
		testFile("/samplingsurface.xml",false,true);
	}


	private static void testFile(String file, boolean print, boolean printXml) throws Exception {
		InputStream in = ParserTest.class.getResourceAsStream(file);
		if (in == null) {
			fail();
		}
		testInputStream(in,print,printXml);
	}

	private static void testInputStream(InputStream in, boolean print, boolean printXml) throws Exception {
		OMDecoder dec = new OMDecoder();
		IObservationCollection ioc = dec.parse(in);

		for (AbstractObservation ao : ioc.getObservations()) {
			validateObservation(ao);
			if (print) {
				printObservation(ao);
			}
			if (printXml) {
				printObservationAsXml(ao);
			}
		}
	}

	private static void printObservationAsXml(AbstractObservation ao) throws OMEncodingException {
		System.out.println(new XBObservationEncoder().encodeObservation(ao));
	}

	private static void printObservation(AbstractObservation ao) {
		System.out.println("########################################### Observation:");
		System.out.println("Identifier: " + ao.getIdentifier().toIdentifierString());
		System.out.println("FOI: " + ao.getFeatureOfInterest().getShape());
		System.out.println("Result: " + ao.getResult().getValue());
		System.out.println("Procedure: " + ao.getProcedure());
		System.out.println("ObservedProperty: " + ao.getObservedProperty());
		System.out.println("Time: " + ao.getPhenomenonTime());
		System.out.println("########################################################");
		System.out.println();
	}

	private static void validateObservation(AbstractObservation ao) {
		assertNotNull(ao);
		assertNotNull(ao.getIdentifier());
		assertNotNull(ao.getResult());
		assertNotNull(ao.getResult().getValue());
		assertNotNull(ao.getFeatureOfInterest());
		assertNotNull(ao.getFeatureOfInterest().getShape());
		assertNotNull(ao.getObservedProperty());
		assertNotNull(ao.getProcedure());
		assertNotNull(ao.getPhenomenonTime());
		assertTrue(ao.getPhenomenonTime().isInstant() || ao.getPhenomenonTime().isInterval());
	}

}
