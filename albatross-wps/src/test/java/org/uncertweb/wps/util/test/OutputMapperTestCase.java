package org.uncertweb.wps.util.test;

import junit.framework.TestCase;

import org.uncertweb.wps.util.OutputMapper;

/**
 * TestCase for CSV Observation Encoder
 * 
 * @author staschc
 *
 */
public class OutputMapperTestCase extends TestCase {
	
	private String pathToExamples = "src/test/resources";
	
	public void testCSVEncoder() throws Exception {
		//read XML Observation Collection
		OutputMapper mapper = new OutputMapper();
		mapper.encodeODMatrix(pathToExamples+"/OUT_odmatrix.csv");
	}
}
