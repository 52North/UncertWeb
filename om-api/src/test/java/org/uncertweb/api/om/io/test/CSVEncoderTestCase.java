

package org.uncertweb.api.om.io.test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.io.CSVEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

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
		/*XBObservationParser parser = new XBObservationParser();
		String examplesPath = pathToExamples+"/Obs_Point_TimeInstant_uncertainty.xml";
		String inputFileString = Utils.readXmlFile(examplesPath);
		AbstractObservation obs = parser.parseObservation(inputFileString);
		*/
//		AbstractObservation obs = getUncertaintyObservation();
//		CSVEncoder encoder = new CSVEncoder();
//		String outputName = pathToExamples+"/UncertaintyObs.txt";
//		File outputF = new File(outputName);
//		encoder.encodeObservation(obs,outputF);
		//+"/aggregation";
		/*File directory = new File(examplesPath);
		String[] files = directory.list();
		IObservationCollection obsCol = null;
		for (String file:files){
			if (!file.contains("svn")){
					obsCol = parser.parseObservationCollection(Utils.readXmlFile(examplesPath+"/"+file));
					CSVEncoder encoder = new CSVEncoder();
					String outputName = file.replace(".xml","");
					File outputF = new File(examplesPath+"/"+outputName+".txt");
					encoder.encodeObservationCollection(obsCol,outputF);
			}
		}*/
	}
	
	private UncertaintyObservation getUncertaintyObservation() throws URISyntaxException{
		DateTimeFormatter dtf = ISODateTimeFormat.dateTimeParser();
		TimeObject phenomenonTime = new TimeObject(dtf.parseDateTime("2011-12-31T19:00:00.000-05:00"),dtf.parseDateTime("2012-01-31T19:00:00.000-05:00"));
		Point p = new GeometryFactory().createPoint(new Coordinate(52.8,7.72));
		p.setSRID(4326);
		SpatialSamplingFeature foi = new SpatialSamplingFeature("http://myServer/Muenster", p);
		UncertaintyResult result = new UncertaintyResult(new NormalDistribution(5.52,0.2));
		UncertaintyObservation uobs = new UncertaintyObservation(phenomenonTime, phenomenonTime, new URI ("http://www.example.org/register/process/scales34.xml"), new URI("urn:ogc:def:phenomenon:OGC:temperature"), foi, result);
		return uobs;
	}
}
