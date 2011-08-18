package org.n52.wps.io.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.OMData;
import org.n52.wps.io.datahandler.xml.OMParser;
import org.uncertml.distribution.multivariate.MultivariateNormalDistribution;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;

import com.vividsolutions.jts.geom.MultiLineString;

public class OMParserTestCase extends TestCase {

	private String localPath = "D:/unc_Profiles/";
	private String pathToExamples = "uWPS/src/main/resources";
	
	
	public void testObservationParser() throws Exception {
		
		obsColTest();
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
		
		assertEquals(binding.getPayload().getClass(), OMData.class);
		
		if (binding.getPayload() instanceof OMData && ((OMData) binding.getPayload()).getObservationCollection() != null) {
			
			IObservationCollection obsCol = ((OMData) binding.getPayload()).getObservationCollection();
			
			assertEquals(obsCol.getObservations().size(), 4);
			
			if (obsCol.getObservations().size() > 0 && obsCol.getObservations().get(0) != null) {
				
				// Observation 0
				AbstractObservation obs_0 = obsCol.getObservations().get(0);
				
				// PhenomenonTime
				assertEquals(obs_0.getPhenomenonTime().getHref().getFragment(), "t0");
				assertEquals(obs_0.getPhenomenonTime().getDateTime().toString(), "2010-01-22T14:55:02.000+02:00");
				
				// ResultTime which was referenced as and so has to be the same as the PhenomenonTime TimeInstant
				assertEquals(obs_0.getResultTime().getHref().getFragment(), "t0");
				assertEquals(obs_0.getResultTime().getDateTime().toString(), "2010-01-22T14:55:02.000+02:00");
				
				// Procedure
				assertEquals(obs_0.getProcedure().toString(), "http://www.uncertweb.org/phenomenon/PM10_total");
				
				// ObservedProperty
				assertEquals(obs_0.getObservedProperty().toString(), "http://www.uncertweb.org/phenomenon/PM10_total");
				
				// Feature of Interest
				assertEquals(obs_0.getFeatureOfInterest().getShape().getClass(), MultiLineString.class);
				assertEquals(obs_0.getFeatureOfInterest().getShape().getCoordinates().length, 51);
				assertEquals(obs_0.getFeatureOfInterest().getShape().getCoordinates()[0].x, 3397499.2250925214);
				
				// Result
				assertEquals(obs_0.getResult().getClass(), UncertaintyResult.class);
				assertEquals(obs_0.getResult().getValue().getClass(), MultivariateNormalDistribution.class);
				assertEquals(((MultivariateNormalDistribution) obs_0.getResult().getValue()).getMean().get(0), 132.52);
				assertEquals(((MultivariateNormalDistribution) obs_0.getResult().getValue()).getCovarianceMatrix().getValues().get(0), 66.26);
				
				// Observation 3
				AbstractObservation obs_3 = obsCol.getObservations().get(3);
				
				// PhenomenonTime
				assertEquals(obs_3.getPhenomenonTime().getHref().getFragment(), "t3");
				assertEquals(obs_3.getPhenomenonTime().getDateTime().toString(), "2010-01-22T17:55:02.000+02:00");
				
				// ResultTime which was referenced as and so has to be the same as the PhenomenonTime TimeInstant
				assertEquals(obs_3.getResultTime().getHref().getFragment(), "t3");
				assertEquals(obs_3.getResultTime().getDateTime().toString(), "2010-01-22T17:55:02.000+02:00");
				
				// Procedure
				assertEquals(obs_3.getProcedure().toString(), "http://www.uncertweb.org/phenomenon/PM10_total");
				
				// ObservedProperty
				assertEquals(obs_3.getObservedProperty().toString(), "http://www.uncertweb.org/phenomenon/PM10_total");
				
				// Feature of Interest
				assertEquals(obs_3.getFeatureOfInterest().getShape().getClass(), MultiLineString.class);
				assertEquals(obs_3.getFeatureOfInterest().getShape().getCoordinates().length, 21);
				assertEquals(obs_3.getFeatureOfInterest().getShape().getCoordinates()[20].y, 5752050.40729385);
				
				// Result
				assertEquals(obs_3.getResult().getClass(), UncertaintyResult.class);
				assertEquals(obs_3.getResult().getValue().getClass(), MultivariateNormalDistribution.class);
				assertEquals(((MultivariateNormalDistribution) obs_3.getResult().getValue()).getMean().get(1), 384.23);
				assertEquals(((MultivariateNormalDistribution) obs_3.getResult().getValue()).getCovarianceMatrix().getValues().get(3), 232.2);
			}
		}
		
		
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
