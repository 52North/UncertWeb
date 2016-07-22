package org.uncertweb.ems.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.joda.time.PeriodType;
import org.joda.time.format.ISODateTimeFormat;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.ems.data.profiles.AbstractProfile;

import au.com.bytecode.opencsv.CSVWriter;

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
	
	/**
	 * 
	 * 
	 * @return
	 */
	public double[] aggregateExposureObs4Day(UncertaintyObservationCollection obsCol){
		List<UncertaintyObservation> obsList = (List<UncertaintyObservation>)obsCol.getObservations();
		int numbOfReal = ((ContinuousRealisation)obsList.get(0).getResult().getUncertaintyValue()).getValues().size();
		double[] aggregates = new double[numbOfReal];
		for (UncertaintyObservation uncertObs : obsList){
			TimeObject time = uncertObs.getPhenomenonTime();
			double weight = 1;
			if (time.isInterval()){
				weight = time.getInterval().toPeriod(PeriodType.minutes()).getMinutes();
			}
			ContinuousRealisation real = (ContinuousRealisation)uncertObs.getResult().getUncertaintyValue();
			Double[] array = (Double[])real.getValues().toArray();
			for (int i=0;i<numbOfReal;i++){
				aggregates[i]+=array[i]*weight;
			}
		}
		int n = obsList.size();
		for (int i=0;i<aggregates.length;++i){
			aggregates[i]=aggregates[i]/n;
		}
		return aggregates;
	}
	
//	public static void writeObsCollXMLStax(IObservationCollection obs, String filepath){		   
//        // save result locally
//		File file = new File(filepath);
//			
//		// encode
//		try {
//			new StaxObservationEncoder().encodeObservationCollection(obs,file);
//		} catch (OMEncodingException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void writeObsCollXML(IObservationCollection obs, String filepath){		   
//        // save result locally
//		File file = new File(filepath);
//			
//		// encode
//		try {
//			new XBObservationEncoder().encodeObservationCollection(obs,file);
//		} catch (OMEncodingException e) {
//			e.printStackTrace();
//		}
//	}
	
//	public static void writeObsCollJSON(IObservationCollection obs, String filepath){		   
//        // save result locally
//		File file = new File(filepath);
//			
//		// encode
//		try {
//			new JSONObservationEncoder().encodeObservationCollection(obs, file);
////			File jsonFile = new File(jsonFilepath);
////			// encode, store (for using in austal request later)
////			try {
////				
////			} catch (OMEncodingException e) {
////				e.printStackTrace();
////			}
//		} catch (OMEncodingException e) {
//			e.printStackTrace();
//		}
//	}
	
//	public static IObservationCollection readObsColl(String filepath) throws FileNotFoundException, XmlException, IOException, OMParsingException{
//		File file = new File(filepath);
//        
//		InputStream in = new FileInputStream(file);
//		XmlObject xml = XmlObject.Factory.parse(in);			
//		XBObservationParser omParser = new XBObservationParser();
//		IObservationCollection obs = (IObservationCollection) omParser.parse(xml.xmlText());
//		if (xml instanceof OMUncertaintyObservationCollectionDocumentImpl||
//				xml instanceof OMMeasurementCollectionDocumentImpl ||
//				xml instanceof OMBooleanObservationCollectionDocumentImpl ||
//				xml instanceof OMTextObservationCollectionDocumentImpl ||
//				xml instanceof OMCategoryObservationDocumentImpl){
//			obs = (IObservationCollection) omParser.parse(xml.xmlText());
//		}else{
//			throw new OMParsingException("Observation Collection type is not supported!");
//		}	
//		return obs;
//	}
	
	
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
	

	public void writeObsCollRealisations2csv(AbstractProfile profile, String filepath){
//		File f = new File(filepath);
//		BufferedWriter writer = null;
//		try {
//			writer = new BufferedWriter(new OutputStreamWriter(
//					new FileOutputStream(f)));
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		}
//		CSVWriter encoder = new CSVWriter(writer);
//		
//		// write header
//		ArrayList<String> currentRow  = new ArrayList<String>();		
//		currentRow .add("Time");
//		for(String att : attributesList.get(dateList.get(0)).keySet()){
//			currentRow .add(att);
//		}
//		for(int i=0; i<+outConc.get(dateList.get(0)).length; i++){
//			currentRow .add("out_"+i);
//		}
//		if(!cospUncert.isEmpty()&&cospUncert.size()>0){
//			for(int i=0; i<+cospUncert.entrySet().iterator().next().getValue().length; i++){
//				currentRow .add("cosp_"+i);
//			}
//		}		
//		for(int i=0; i<+inConc.entrySet().iterator().next().getValue().length; i++){
//			currentRow .add("in_"+i);
//		}
//		String[] row = new String[currentRow .size()];
//		currentRow .toArray(row);
//		encoder.writeNext(row);
//		
//		// loop through exposure results and write results in each line
////		ArrayList<String> currentRow = new ArrayList<String>();
//		for(String time : dateList){
//			currentRow = new ArrayList<String>();
//			
//			// create row
//			currentRow.add(time);
//			for(String att : attributesList.get(dateList.get(0)).keySet()){
//				currentRow.add(attributesList.get(time).get(att));
//			}
//			for(int i=0; i<+outConc.get(time).length; i++){
//				currentRow.add(outConc.get(time)[i]+"");
//			}
//			if(!cospUncert.isEmpty()&&cospUncert.size()>0){
//				for(int i=0; i<+cospUncert.entrySet().iterator().next().getValue().length; i++){
//					if(cospUncert.containsKey(time))
//						currentRow.add(cospUncert.get(time)[i]+"");
//					else
//						currentRow.add("");
//				}
//			}			
//			for(int i=0; i<+inConc.entrySet().iterator().next().getValue().length; i++){
//				if(inConc.containsKey(time))
//					currentRow.add(inConc.get(time)[i]+"");
//				else
//					currentRow.add("");
//			}
//			
//			// write row
//			currentRow .toArray(row);
//			encoder.writeNext(row);
//		}
//
//		try {
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	
	public static DateTime getNextHourDateTime(DateTime input){
		DateTime result = input;
		
		//input time is exactly on the beginning of and hour, e.g. 03:00:00
		//--> return unchanged input
		if (input.getMinuteOfHour()==0&&input.getSecondOfMinute()==0){
			return result;
		}
		
		//input time is somewhat between
		else {
			//add one hour
			result = result.plusHours(1);
			result = result.minusMinutes(input.getMinuteOfHour());
			result = result.minusSeconds(input.getSecondOfMinute());
			return result;
		}
	}
}
