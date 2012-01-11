







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
//		String examplesPath = pathToExamples+"/EEAInput";
//		File directory = new File(examplesPath);
//		String[] files = directory.list();
//		IObservationCollection obsCol = null;
//		for (String file:files){
//			if (!file.contains("svn")){
//				if (obsCol==null){
//					obsCol = parser.parseObservationCollection(TestUtils.readXmlFile(examplesPath+"/"+file));
//				}
//				else {
//					obsCol.addObservationCollection(parser.parseObservationCollection(TestUtils.readXmlFile(examplesPath+"/"+file)));
//				}
//			}
//		}
//		CSVEncoder encoder = new CSVEncoder();
//		File file = new File(pathToExamples+"/csvOutput.txt");
//		encoder.encodeObservationCollection(obsCol,file);
	}
}
