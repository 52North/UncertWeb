package org.uncertweb.wps.util.test;

import org.junit.Test;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.wps.io.outputmapper.AlbatrossOutputMapper;
import org.uncertweb.wps.io.outputmapper.OutputMapper;
import org.uncertweb.wps.util.AlbatrossOutputParser;

/**
 * TestCase for CSV Observation Encoder
 * 
 * @author staschc
 *
 */
public class OutputMapperTestCase {
	
	private String pathToExamples = "src/test/resources";
	
	@Test
	public void testOutputMapper() throws Exception {
		//read XML Observation Collection
		OutputMapper mapper = new OutputMapper();
		XBObservationEncoder obsEncoder = new XBObservationEncoder();
		
		IObservationCollection obsCol = mapper.encodeODMatrix(pathToExamples+"/OUT_odmatrix.csv");
		System.out.print(obsEncoder.encodeObservationCollection(obsCol));
		obsCol = mapper.encodeIndicators(pathToExamples+"/OUT_indicators.csv");
		System.out.print(obsEncoder.encodeObservationCollection(obsCol));
	}
	
	@Test
	public void testAlbatrossOutputMapper() throws Exception{
		
		IObservationCollection col = AlbatrossOutputMapper.encodeAlbatrossOutput(AlbatrossOutputParser.parse(pathToExamples+"/export.txt"));
		
		XBObservationEncoder encoder = new XBObservationEncoder();
		
		System.out.println(encoder.encodeObservationCollection(col));
	}
	
}
