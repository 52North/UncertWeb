package org.uncertweb.api.om.io.test;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.StaxObservationParser;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

public class StaxObservationParserTestCase {
	
	private String localPath = "D:/IfGI/Projekte/UncertWeb/Implementations/uw_workspace/profiles-api/om-api/";
	private String pathToExamples = "src/test/resources";

	@Test
	public void test() {
		try {
			obsCol_Measurement();
//			obsCol_yield();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		StaxObservationParser parser = new StaxObservationParser();
		IObservationCollection oc = parser.parseObservationCollection(fis);
		StaxObservationEncoder encoder = new StaxObservationEncoder();
		System.out.println(encoder.encodeObservationCollection(oc));
	}
	
	private void obsCol_Measurement() throws Exception {

		// read XML example file
		InputStream in = null;
		try {
		 in = new FileInputStream(pathToExamples
				+ "/ObsCol_Measurements.xml");
		}
		catch (IOException ioe){
			in = new FileInputStream(localPath + pathToExamples
					+ "/ObsCol_Measurements.xml");
		}
		StaxObservationParser parser = new StaxObservationParser();
		IObservationCollection oc = parser.parseObservationCollection(in);
		in.close();
		StaxObservationEncoder encoder = new StaxObservationEncoder();
		System.out.println(encoder.encodeObservationCollection(oc));
	}

}
