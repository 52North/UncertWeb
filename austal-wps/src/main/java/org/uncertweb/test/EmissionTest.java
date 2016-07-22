package org.uncertweb.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;

public class EmissionTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws OMParsingException 
	 * @throws XmlException 
	 */
	public static void main(String[] args) throws IOException, OMParsingException, XmlException {
		
//		XBObservationEncoder encoder = new XBObservationEncoder();
//		
//		encoder.
		
		BufferedReader bread = new BufferedReader(new FileReader(new File("C:\\UncertWeb\\src\\src\\main\\resources\\xml\\output_om\\Streets1.xml")));
		
		String xmlString = "";
		
		String line = bread.readLine();
		
		xmlString = xmlString.concat(line);
		
		while((line = bread.readLine()) != null){
			xmlString = xmlString.concat(line);
		}

//		XmlObject xbDoc = XmlObject.Factory.parse(xmlString);
		
		XBObservationParser parser = new XBObservationParser();
		
		IObservationCollection coll = parser.parseObservationCollection(xmlString);
		
		SpatialSamplingFeature spsam = coll.getObservations().get(0).getFeatureOfInterest();
		
		for (AbstractObservation abstractObservation : coll.getObservations()) {
			
			System.out.println((spsam.equals(abstractObservation.getFeatureOfInterest())));
			
//			if(abstractObservation.getFeatureOfInterest().getShape() instanceof MultiLineString){
//				
//				System.out.println(abstractObservation.getFeatureOfInterest().getShape());
//			}else if(abstractObservation.getFeatureOfInterest().getShape() instanceof MultiPolygon){
//				System.out.println(abstractObservation.getFeatureOfInterest().getShape());
//			}
			
		}
		

	}

}
