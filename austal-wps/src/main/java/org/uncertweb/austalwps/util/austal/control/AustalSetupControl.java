package org.uncertweb.austalwps.util.austal.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.austalwps.util.austal.files.Austal2000Txt;
import org.uncertweb.austalwps.util.austal.files.Zeitreihe;
import org.uncertweb.austalwps.util.austal.geometry.EmissionSource;
import org.uncertweb.austalwps.util.austal.timeseries.EmissionTimeSeries;
import org.uncertweb.austalwps.util.austal.timeseries.MeteorologyTimeSeries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;

// class to manage Austal Setup

public class AustalSetupControl {

	private static final String FILE_PATH="C:\\UncertWeb\\workspace\\AustalWPS\\src\\test\\resources\\";
	
	// general Austal objects
	private Austal2000Txt austal;
	private Zeitreihe ts;
	//private StudyArea studyArea;
	//private List<ReceptorPoint> receptorPoints;	
	
	// Austal objects that need to be changed
	private List<EmissionSource> emissionSources;
	private List<EmissionTimeSeries> emisList = new ArrayList<EmissionTimeSeries>();
	private MeteorologyTimeSeries metList = new MeteorologyTimeSeries();
	
	public static void main(String[] args) {
		AustalSetupControl control = new AustalSetupControl();
	}
	
	public AustalSetupControl(){
		// read files to create datamodel
		this.readFiles();
		
		try {
			handleObservationCollection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Austal coordinates for line sources
//		EmissionSource line = lineGK3ToLocalCoords(3405540, 5758268, 3401540, 5758268, 3400540, 5759268);
		
		// Austal coordinates for polygon sources
		// coordinates are from lower left and upper right corner
//		EmissionSource polygon = cellPolygonGK3ToLocalCoords(3405540, 5758268, 3401540, 5758268, 3400540, 5759268);
		
		// get realisations of emissions and meteorology
		
		
//		List<EmissionSource> new_emissionList = new ArrayList<EmissionSource>();
//		new_emissionList.add(line);
		// substitute emissions and meteorology with new data
//		austal.setEmissionSources(new_emissionList );
//		ts.setEmissionSourcesTimeSeries(new_emisTSlist);
//		ts.setMeteorologyTimeSeries(new_meteoTS);

		// add new receptor points
		
		
		// run Austal
		
		
		// read Austal results
//		int tsLength = Integer.parseInt(ts.getTSlength());
		
	}
	
	private void handleObservationCollection() throws Exception {

		int gx = austal.getStudyArea().getGx();
		int gy = austal.getStudyArea().getGy();

		BufferedReader bread = new BufferedReader(
				new FileReader(
						new File(
								"C:\\UncertWeb\\src\\src\\main\\resources\\xml\\output_om\\Streets1.xml")));

		String xmlString = "";

		String line = bread.readLine();

		xmlString = xmlString.concat(line);

		while ((line = bread.readLine()) != null) {
			xmlString = xmlString.concat(line);
		}

		XBObservationParser parser = new XBObservationParser();

		IObservationCollection coll = parser
				.parseObservationCollection(xmlString);

		SpatialSamplingFeature spsam = null;

		for (AbstractObservation abstractObservation : coll.getObservations()) {

//			System.out.println((spsam.equals(abstractObservation
//					.getFeatureOfInterest())));
			if(spsam == null){
				spsam = abstractObservation.getFeatureOfInterest();
				if (abstractObservation.getFeatureOfInterest().getShape() instanceof MultiLineString) {

					MultiLineString mline = (MultiLineString) abstractObservation
							.getFeatureOfInterest().getShape();

					Coordinate[] coords = mline.getCoordinates();
					EmissionSource tmpEMS = lineGK3ToLocalCoords(gx, gy, coords[0].x, coords[0].y, coords[1].x, coords[1].y);
					System.out.println(tmpEMS);
				} else if (abstractObservation.getFeatureOfInterest()
						.getShape() instanceof MultiPolygon) {

					MultiPolygon mpoly = (MultiPolygon) abstractObservation
							.getFeatureOfInterest().getShape();
					
					Coordinate[] coords = mpoly.getCoordinates();
					
					EmissionSource tmpEMS = cellPolygonGK3ToLocalCoords(gx, gy, coords[1].x, coords[1].y, coords[3].x, coords[3].y);
					System.out.println(tmpEMS);
				}
			} else {
				if (!spsam.equals(abstractObservation.getFeatureOfInterest())) {
					if (abstractObservation.getFeatureOfInterest().getShape() instanceof MultiLineString) {

						MultiLineString mline = (MultiLineString) abstractObservation
								.getFeatureOfInterest().getShape();

						Coordinate[] coords = mline.getCoordinates();
						EmissionSource tmpEMS = lineGK3ToLocalCoords(gx, gy, coords[0].x, coords[0].y, coords[1].x, coords[1].y);
						System.out.println(tmpEMS);
					} else if (abstractObservation.getFeatureOfInterest()
							.getShape() instanceof MultiPolygon) {

						MultiPolygon mpoly = (MultiPolygon) abstractObservation
								.getFeatureOfInterest().getShape();
						
						Coordinate[] coords = mpoly.getCoordinates();
						
						EmissionSource tmpEMS = cellPolygonGK3ToLocalCoords(gx, gy, coords[1].x, coords[1].y, coords[3].x, coords[3].y);
						System.out.println(tmpEMS);
					}
				}
			}

		}
	}
	
	
	private void readFiles(){
		// read austal2000.txt
		File austalFile = new File(FILE_PATH+"//austal2000.txt");
		austal = new Austal2000Txt(austalFile);
			
		// read zeitreihe.dmna
		File tsFile = new File(FILE_PATH+"//zeitreihe.dmna");
		ts = new Zeitreihe(tsFile);
		
		// test writer
		File new_austalFile = new File(FILE_PATH+"//austal_new.txt");
		austal.writeFile(new_austalFile);
		File new_tsFile = new File(FILE_PATH+"//zeitreihe_new.dmna");
		ts.writeFile(new_tsFile);
	}
	
	// methods to calculate Gauss-Krüger-Coordinates to local austal coordinates
	private EmissionSource lineGK3ToLocalCoords(double gx, double gy, double x1, double y1, double x2, double y2){
		
		EmissionSource source = new EmissionSource();		
		double xq, yq, wq;	
		double bq = Math.sqrt(Math.pow((x1-x2), 2)+Math.pow((y1-y2), 2)); // extension in y direction = length
		double aq=0;					// extension in x direction
		double cq=1;					// extension in z direction
		double hq=0.2;					// height
		
		// find point to the right which will stay fixed
		if(x1==x2){ // easiest case
			// convert to local coordinates
			xq = x1 - gx;
			yq = y1 - gy;		
			wq = 0;					// angle						
		} else if(x1>x2){	
			// convert to local coordinates
			xq = x1 - gx;
			yq = y1 - gy;
			if(y1>y2)
				wq = 180 - Math.atan(Math.abs(x1-x2)/Math.abs(y1-y2))*180/Math.PI;
			else
				wq = Math.atan(Math.abs(x1-x2)/Math.abs(y1-y2))*180/Math.PI;
				
		}else{
			// convert to local coordinates
			xq = x2 - gx;
			yq = y2 - gy;
			if(y2>y1)
				wq = 180 - Math.atan(Math.abs(x1-x2)/Math.abs(y1-y2))*180/Math.PI;
			else
				wq = Math.atan(Math.abs(x1-x2)/Math.abs(y1-y2))*180/Math.PI;
		}
		
		source.setCoordinates(xq, yq);
		source.setExtent(aq, bq, cq, wq, hq);
		return source;
	}
	
	// method to calculate Gauss-Krüger-Coordinates to local austal coordinates
	private EmissionSource cellPolygonGK3ToLocalCoords(double gx, double gy, double x1, double y1, double x2, double y2){
			
			EmissionSource source = new EmissionSource();		
			double xq, yq;	
			double bq = Math.abs(y1-y2); 	// extension in y direction
			double aq = Math.abs(x1-x2);	// extension in x direction
			double cq=1;					// extension in z direction
			double hq=0.2;					// height
			double wq = 0;					//TODO: This is zero for our case
			
			// get lower left point which will stay fixed
			if(x1<x2)
				xq = x1 - gx;
			else
				xq = x2 - gx;
			
			if(y1<y2)
				yq = y1 - gy;
			else
				yq = y2 - gy;
		
			source.setCoordinates(xq, yq);
			source.setExtent(aq, bq, cq, wq, hq);
			return source;
		}
		
	// omParser
	private void om2EmissionSources(){
		//IObservationCollection result = new UncertaintyObservationCollection();

	}
	
	private void om2Meteorology(){
		
	}
	
	private void readAustalResults(){
		
	}

}
