package org.uncertweb.api.om.io.test;

import java.io.File;

import org.uncertweb.api.om.io.CSVEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

import junit.framework.TestCase;

/**
 * TestCase for CSV Observation Encoder
 * 
 * @author staschc
 *
 */
public class CSVEncoderTestCase extends TestCase {
	
	private String pathToExamples = "src/test/resources";
	
	public void testCSVEncoder() throws Exception {
		//read XML Observation Collection
		XBObservationParser parser = new XBObservationParser();
		IObservationCollection obsCol = parser.parseObservationCollection(TestUtils.readXmlFile(pathToExamples+"/DEBB0211.xml"));
		CSVEncoder encoder = new CSVEncoder();
		File file = new File(pathToExamples+"/csvOutput.txt");
		encoder.encodeObservationCollection(obsCol,file);
	}
}
