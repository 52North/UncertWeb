package org.uncertweb.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.austalwps.util.AustalOutputReader;
import org.uncertweb.austalwps.util.Point;
import org.uncertweb.austalwps.util.Value;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

public class OMTest {

	/**
	 * @param args
	 * @throws URISyntaxException 
	 * @throws OMEncodingException 
	 */
	public static void main(String[] args) throws URISyntaxException, OMEncodingException {
				
		try {

			MeasurementCollection mcoll = new MeasurementCollection();			
//			UncertaintyObservationCollection mcoll = new UncertaintyObservationCollection();			
			
			AustalOutputReader austal = new AustalOutputReader();
			
			ArrayList<Point[]> points = austal.createPoints("C:/UncertWeb/workspace/AustalWPS/src/test/resources", true);
			
			URI procedure = new URI("http://www.uncertweb.org/models/austal2000");
			URI observedProperty = new URI("http://www.uncertweb.org/phenomenon/pm10");
			
			URI codeSpace = new URI("");
			
			for (int j = 0; j < points.size(); j++) {
				
				Point[] p = points.get(j);
				
				for (int i = 0; i < p.length; i++) {
					
					ArrayList<Value> vals = p[i].values();				
					double[] coords = p[i].coordinates();
					
					// get coordinates and create point
					Coordinate coord = new Coordinate(coords[0], coords[1]);								
					
					PrecisionModel pMod = new PrecisionModel(PrecisionModel.FLOATING);
					
					GeometryFactory geomFac = new GeometryFactory(pMod, 31467);
					SpatialSamplingFeature featureOfInterest = new SpatialSamplingFeature(null, geomFac.createPoint(coord));
					featureOfInterest.setIdentifier(new Identifier(codeSpace, "point" + i));
					Measurement m1 = null;
//					UncertaintyObservation m1 = null;
					
					for (int k = 0; k < vals.size(); k++) {	
						Identifier identifier = new Identifier(codeSpace, "m" + k);
						
						String timeStamp = vals.get(k).TimeStamp().trim();
						
						timeStamp = timeStamp.replace(" ", "T");
						
						TimeObject phenomenonTime = new TimeObject(timeStamp);						
//						UncertaintyResult result = new UncertaintyResult(new Realisation(new double[]{vals.get(k).PM10val()}), "");
						MeasureResult result = new MeasureResult(vals.get(k).PM10val(), "");
						
//						if(m1 == null){
//						m1 = new UncertaintyObservation(identifier, null, phenomenonTime, phenomenonTime, null, procedure, observedProperty, featureOfInterest, null, result);
						m1 = new Measurement(identifier, null, phenomenonTime, phenomenonTime, null, procedure, observedProperty, featureOfInterest, null, result);
						mcoll.addObservation(m1);
//						}else{							
//							Measurement m2 = new Measurement(identifier, null, phenomenonTime, phenomenonTime, null, procedure, observedProperty, m1.getFeatureOfInterest(), null, result);
//							mcoll.addObservation(m2);
//						}
					}					
				}
			}
			
			XBObservationEncoder encoder = new XBObservationEncoder();
//			
			System.out.println(encoder.encodeObservationCollection(mcoll));
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		
//		URI codeSpace = new URI("");
//		Identifier identifier = new Identifier(codeSpace, "m1");
//		Envelope boundedBy = null;
//		TimeObject phenomenonTime = new TimeObject("2009-01-06T01:00:00.000+00:00");
//		
//		
//		TimeObject resultTime;
//		TimeObject validTime;
//		URI procedure = new URI("http://www.uncertweb.org/models/austal2000");
//		URI observedProperty = new URI("http://www.uncertweb.org/phenomenon/pm10");
//		
//		PrecisionModel pMod = new PrecisionModel(PrecisionModel.FLOATING);
//		
//		GeometryFactory geomFac = new GeometryFactory(pMod, 31467);
//		
//		DateTime dateTime = null;
//
//		DateTimeFormatter dtf = ISODateTimeFormat.dateTimeParser();
//		dateTime = dtf.withOffsetParsed().parseDateTime("2009-01-06T01:00:00.000");
//		
//		SpatialSamplingFeature featureOfInterest = new SpatialSamplingFeature("sampledFeature", geomFac.createPoint(new Coordinate(3405148.7,5760096.3)));
//		DQ_UncertaintyResult[] resultQuality = null;
//		MeasureResult result = new MeasureResult(13.1, "");		
//		
//		Measurement m1 = new Measurement(identifier, boundedBy, phenomenonTime, phenomenonTime, null, procedure, observedProperty, featureOfInterest, resultQuality, result);
//
//		System.out.println(m1);
		
//		OMData omd = new OMData(m1);
		
//		Node n = new OMGenerator().generateXML(new OMDataBinding(omd), "text/xml");

	}

}
