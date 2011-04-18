package org.n52.wps.io.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.xml.OMGenerator;
import org.n52.wps.io.datahandler.xml.OMParser;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.w3c.dom.Node;

public class OMGeneratorTestCase extends TestCase {

	private String localPath = "D:/unc_Profiles/";
	private String pathToExamples = "uWPS/src/main/resources";
	
	
	public void testObservationGenerator() throws Exception {
		
		obsColTest();
		obsTest();
	}
	
	private void obsTest() throws Exception {
		
	}
	
	private void obsColTest() throws Exception {
		// read XML example file
		String xmlString;
		try {
		 xmlString = readXmlFile(pathToExamples
				+ "/ObsCol_UncertaintyObs.xml");
		}
		catch (IOException ioe){
			xmlString = readXmlFile(localPath + pathToExamples
					+ "/ObsCol_UncertaintyObs.xml");
		}
		OMParser parser = new OMParser();
		IData binding = parser.parseXML(xmlString);
		OMGenerator encoder = new OMGenerator();
		Node encDoc = encoder.generateXML(binding, null);
		String encString = encoder.generateXMLDocument(binding, null).toString();
		
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
