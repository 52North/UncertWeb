package org.uncertweb.ems.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

import net.opengis.om.x20.impl.OMCategoryObservationDocumentImpl;
import net.opengis.om.x20.impl.OMMeasurementCollectionDocumentImpl;
import net.opengis.om.x20.impl.OMUncertaintyObservationCollectionDocumentImpl;
import net.opengis.om.x20.impl.OMBooleanObservationCollectionDocumentImpl;
import net.opengis.om.x20.impl.OMTextObservationCollectionDocumentImpl;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

import com.vividsolutions.jts.geom.Coordinate;


public class Utils {

	private static String resultsPath = "C:/Temp/AQMS";	
	private static Logger logger = Logger.getLogger(Utils.class);
	
	public static String date4SOS(Date date){
		String sosDate = "";
		//"2009-03-08T11:00:00+01"
		SimpleDateFormat sosFormat = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss+01");
		sosFormat.setTimeZone(TimeZone.getTimeZone("GMT+01"));		
		
		return sosDate;
	}
	
	public static void writeObsCollXML(IObservationCollection obs, String filepath){		   
        // save result locally
		File file = new File(filepath);
			
		// encode
		try {
			new StaxObservationEncoder().encodeObservationCollection(obs,file);
		} catch (OMEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeObsCollJSON(IObservationCollection obs, String filepath){		   
        // save result locally
		File file = new File(filepath);
			
		// encode
		try {
			new JSONObservationEncoder().encodeObservationCollection(obs, file);
//			File jsonFile = new File(jsonFilepath);
//			// encode, store (for using in austal request later)
//			try {
//				
//			} catch (OMEncodingException e) {
//				e.printStackTrace();
//			}
		} catch (OMEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static IObservationCollection readObsColl(String filepath){
		File file = new File(filepath);
        IObservationCollection obs = null;
		try {
			InputStream in = new FileInputStream(file);
			XmlObject xml = XmlObject.Factory.parse(in);			
			XBObservationParser omParser = new XBObservationParser();
			obs = (IObservationCollection) omParser.parse(xml.xmlText());
//			  if (xml instanceof OMUncertaintyObservationCollectionDocumentImpl){
//				 obs = (IObservationCollection) omParser.parse(xml.xmlText());
//			  }	else if(xml instanceof OMMeasurementCollectionDocumentImpl){
//				  obs = (IObservationCollection) omParser.parse(xml.xmlText());
//			  }else if(xml instanceof OMBooleanObservationCollectionDocumentImpl){
//				  obs = (IObservationCollection) omParser.parse(xml.xmlText());
//			  }else if(xml instanceof OMTextObservationCollectionDocumentImpl){
//				  obs = (IObservationCollection) omParser.parse(xml.xmlText());
//			  }else if(xml instanceof OMCategoryObservationDocumentImpl){
//				  obs = (IObservationCollection) omParser.parse(xml.xmlText());
//			  }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OMParsingException e) {
			e.printStackTrace();
		}    
		
		return obs;
	}
	
	
	public static void writeCSV(HashMap<String, double[]> data, String filename){
		try {
			String filepath = resultsPath + "\\"+filename+".csv";
			 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filepath)));
			Iterator<String> keys = data.keySet().iterator();
			String date = keys.next();
			// write header
			out.write("Date");
			for(int i=0; i<data.get(date).length; i++){
				out.write(", "+i);
			}		
			out.newLine();
			
			// write each observation as one line
			while(keys.hasNext()){
				date = keys.next();
				out.write(date);			
				double[] r = data.get(date);
				for(int i=0; i<r.length; i++){
					out.write(", "+r[i]);
				}	
				out.newLine();
			}
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
