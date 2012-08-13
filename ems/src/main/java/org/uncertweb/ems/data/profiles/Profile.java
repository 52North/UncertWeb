package org.uncertweb.ems.data.profiles;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.CSVEncoder;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.CategoryObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.sample.ContinuousRealisation;

import au.com.bytecode.opencsv.CSVWriter;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Stores information on ST positions, ME location and activity of individuals
 * @author Lydia Gerharz
 *
 */
public class Profile {
	
	private String uncertWebPhenPrefix = "http://www.uncertweb.org/phenomenon/";
	private String uom = "ug/m3";
//	private Map<String,String> personalInformation = new HashMap<String,String>();; // stores information about housing etc.	
	private IObservationCollection obsColl;	//stores OM representation of the input profile
	
	// variables to store attributes: microenvironment, microenvironmentDetail, activity, noOfPersons, smoker, windowOpen (raw data, remarks)
	private Map<String, Map<String, String>> attributesList;
	private ArrayList<String> dateList;
	
	// variables for modelled concentration realisations
	private HashMap<String, double[]> outConc, inConc, cospUncert;//, outinConc, inSourcConc;
		
	public Profile(IObservationCollection profileObservationCollection){
		OM2Profile(profileObservationCollection);
		outConc = new HashMap<String, double[]>();
		inConc = new HashMap<String, double[]>();
		cospUncert = new HashMap<String, double[]>();
	}	
	
	
	/**
	 *  Method to fill profile object with OM collection
	 */
	private void OM2Profile(IObservationCollection iobs){
		obsColl = new CategoryObservationCollection();
		attributesList = new HashMap<String, Map<String,String>>();
		dateList = new ArrayList<String>();
		
		// only add new spatial sampling feature
		DateTime lastTime = null;
		
		for (AbstractObservation obs : iobs.getObservations()) {  				
			// get time and add SpatialSamplingFeature if it is a new timestep
			DateTime time = obs.getResultTime().getDateTime();
			if(!time.equals(lastTime)){
				dateList.add(time.toString(ISODateTimeFormat.dateTime()));
				attributesList.put(time.toString(ISODateTimeFormat.dateTime()),  new HashMap<String,String>());				
				obsColl.addObservation(obs);
				lastTime = time;
			}
			
			// extract observation values (location, activity)
			String[] obsProp = obs.getObservedProperty().toString().split("/");
			attributesList.get(time.toString(ISODateTimeFormat.dateTime())).put(obsProp[obsProp.length-1], obs.getResult().getValue().toString());		
		}	
	
		// test attributes
//		for(int i=0; i<dateList.size(); i++){
//			String me = attributesList.get(dateList.get(i)).get("microenvironment");
//			System.out.println(dateList.get(i)+": "+me);
//				if(me==null){
//					System.out.println("Problem with attributes!");
//				}
//		}
	}

	public void aggregateProfile(int minutesResolution){
		// set start to first interval
		DateTime start = ISODateTimeFormat.dateTime().parseDateTime(dateList.get(0));
		start = start.plusMinutes(minutesResolution);
		
		// aggregate dateList, attributesList, outConc, cospUncert (not inConc because this is done beforehand)
		ArrayList<String> newDateList = new ArrayList<String>();
		HashMap<String, double[]> newOutConc = new HashMap<String, double[]>();
		ArrayList<double[]> outTemp = new ArrayList<double[]>();
//		int maxLength = 0;
//		int minLength = 10000;
		HashMap<String, double[]> newCOSPUncert = new HashMap<String, double[]>();
		ArrayList<double[]> cospTemp = new ArrayList<double[]>();
		Map<String, Map<String, String>> newAttributesList = new HashMap<String, Map<String, String>>();
		
		// aggregate observation Collection
		IObservationCollection newObsColl = new CategoryObservationCollection();
		
		// loop through original observations
		for(int i=0; i<obsColl.getObservations().size(); i++){
			DateTime time = obsColl.getObservations().get(i).getResultTime().getDateTime();
			
			// if time is within this interval, collect outdoor concentration
			if((time.isBefore(start)||time.isEqual(start))&&i<(obsColl.getObservations().size()-1)){
				outTemp.add(outConc.get(dateList.get(i)));		
				if(cospUncert.containsKey(dateList.get(i)))
					cospTemp.add(cospUncert.get(dateList.get(i)));
			}
			
			// if time falls into next interval
			else{
				AbstractObservation obs = obsColl.getObservations().get(i-1);
				obs.setResultTime(new TimeObject(start));
				obs.setPhenomenonTime(new TimeObject(start));
				newObsColl.addObservation(obs);
				newDateList.add(start.toString());
				
				// for attributes only add the last attribute
				newAttributesList.put(start.toString(),attributesList.get(dateList.get(i-1)));
				
				// for outConc: add average
				double[] outAv = new double[outTemp.get(0).length];						
				for(int j=0; j<outTemp.size(); j++){
					for(int k=0; k<outAv.length; k++){
						outAv[k] = outAv[k] + outTemp.get(j)[k]*1/outTemp.size();
					}
				}
				newOutConc.put(start.toString(), outAv);
				
				// add cosp average if available
				if(cospTemp.size()>0){
					double[] cospAv = new double[cospTemp.get(0).length]; 
					
					for(int j=0; j<cospTemp.size(); j++){
						for(int k=0; k<cospAv.length; k++){
							cospAv[k] = cospAv[k] + cospTemp.get(j)[k]*1/cospTemp.size();
						}
					}
					newCOSPUncert.put(start.toString(), cospAv);
				}
							
				// move to next time interval
				outTemp = new ArrayList<double[]>();
				outTemp.add(outConc.get(dateList.get(i)));
				cospTemp = new ArrayList<double[]>();
				if(cospUncert.containsKey(dateList.get(i)))
					cospTemp.add(cospUncert.get(dateList.get(i)));
				
				start = start.plusMinutes(minutesResolution);
			}			
		}
		
		// replace old lists with new aggregated lists
		dateList = newDateList;
		attributesList = newAttributesList;
		outConc = newOutConc;
		cospUncert = newCOSPUncert;
		obsColl = newObsColl;
		
	}
	
	// TODO: reimplement method
	public IObservationCollection getExposureProfileObservationCollection(String statistics){		
		IObservationCollection exposureObs = new UncertaintyObservationCollection();
		
		// loop through original observation collection and add concentrations as results
		for(AbstractObservation obs : obsColl.getObservations()){			
			try {
				String time = obs.getResultTime().getDateTime().toString(ISODateTimeFormat.dateTime());
				double[] realisations = null;
				if(inConc.containsKey(time)){
					realisations = inConc.get(time);
				}else{
					// if available also add COSP
					if(cospUncert.containsKey(time)){
						realisations = addCOSP2OutConc(time);
					}else{
						realisations = outConc.get(time);
					}
				}
				UncertaintyResult res = null;
				if(statistics.equals("realisations")){
					res = new UncertaintyResult(new ContinuousRealisation(realisations));					
				}else if(statistics.equals("normal")){
					double[] mean = new double[1];
					double[] var = new double[1];
					for(int i=0; i<realisations.length; i++){
						mean[0] += realisations[i]/realisations.length;
					}
					for(int i=0; i<realisations.length; i++){
						var[0] += Math.pow((realisations[i]-mean[0]),2)/realisations.length;
					}
					res = new UncertaintyResult(new NormalDistribution(mean, var));
					
				}else if(statistics.equals("lognormal")){
					double NDmean = 0, NDsd = 0;
					for(int i=0; i<realisations.length; i++){
						NDmean += realisations[i]/realisations.length;
					}
					for(int i=0; i<realisations.length; i++){
						NDsd += Math.pow((realisations[i]-NDmean),2)/realisations.length;
					}
					NDsd = Math.sqrt(NDsd);
					double[] mean = {Math.log(Math.pow(NDmean,2) / Math.sqrt(Math.pow(NDsd,2)+Math.pow(NDmean,2)))};
					double[] var = {Math.log(Math.pow(NDsd,2)/Math.pow(NDmean,2) + 1)};
					res = new UncertaintyResult(new LogNormalDistribution(mean, var));
				}
				
				res.setUnitOfMeasurement(uom);
				AbstractObservation expoObs = new UncertaintyObservation(obs.getResultTime(), 
						obs.getResultTime(), obs.getProcedure(), new URI(uncertWebPhenPrefix+"pm10"), 
						obs.getFeatureOfInterest(), res);
				exposureObs.addObservation(expoObs);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}			
		return exposureObs;
	}
	
	
	private double[] addCOSP2OutConc(String time){
		// add COSP to outdoor concentration
		double[] outC = outConc.get(time);
		double[] cosp = cospUncert.get(time);
		double[] newC = new double[outC.length*cosp.length];
		for(int i=0; i<outC.length; i++){
			for(int j=0; j<cosp.length; j++){
				newC[i] = outC[i]+cosp[j];
			}			
		}		
		return newC;
	}
	
	
	/*
	 *  Setter methods
	 */
	// outdoor concentration
	public void setOutConcRealisations(int index, double[] outC){
		// get DateTime for index
		outConc.put(dateList.get(index),outC);
	}
	
	// COSP uncertainties
	public void setCOSPrealisations(int index, double[] cosp){	
		// get DateTime for index
		cospUncert.put(dateList.get(index),cosp);
	}		
	
	// indoor concentration
	public void setInConcRealisations(int index, double[] inC){
		// get DateTime for index
		inConc.put(dateList.get(index),inC);
	}
	
	
	
//	public void setPersonalInformationList(Map<String,String> newList){
//		personalInformation = newList;
//	}
//	
//	public void setPersonalInformation(String key, String value){
//		personalInformation.put(key, value);
//	}
	
	/*
	 *  Getter methods
	 */	

	public int getSize(){
		return attributesList.size();
	}
	
	public Map<String,String> getAttributesEntry(int index){
		return attributesList.get(dateList.get(index));
	}
	
	public int getTemporalIntervalInMinutes(int index){
		DateTime start = ISODateTimeFormat.dateTime().parseDateTime(dateList.get(index));
		DateTime end = ISODateTimeFormat.dateTime().parseDateTime(dateList.get(0)).plusDays(1);
		if(index<(dateList.size()-1)){
			end = ISODateTimeFormat.dateTime().parseDateTime(dateList.get(index+1));
		}
		
		if(Seconds.secondsBetween(start, end).getSeconds()>=60){
			return Minutes.minutesBetween(start, end).getMinutes();
		}else{
			return 0;
		}		
	}
	
	public double[] getOutConcRealisations(int index){
		// get DateTime for index
		return outConc.get(dateList.get(index));
	}
	
	public double[] getInConcRealisations(int index){
		// get DateTime for index
		return inConc.get(dateList.get(index));
	}
	
//	public Map<String,String> getPersonalInformationList(){
//		return personalInformation;
//	}
//	
//	public String getPersonalInformation(String key){
//		return personalInformation.get(key);
//	}
//	
//	public int getID(){
//		return profileID;
//	}
	

	/***
     * Method to write spatial and temporal information from the Observation Collection to csv file
     * @param filepath
     */
	public void writeObsCollGeometry2csv(String filepath){ 		
		// use CSVEncoder 
		CSVEncoder encoder = new CSVEncoder();
		try {
			encoder.encodeObservationCollection(obsColl, new File(filepath));
		} catch (OMEncodingException e) {
			e.printStackTrace();
		}	
		
    }
	
	
	public void writeObsCollRealisations2csv(String filepath){
		File f = new File(filepath);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		CSVWriter encoder = new CSVWriter(writer);
		
		// write header
		ArrayList<String> currentRow  = new ArrayList<String>();		
		currentRow .add("Time");
		for(String att : attributesList.get(dateList.get(0)).keySet()){
			currentRow .add(att);
		}
		for(int i=0; i<+outConc.get(dateList.get(0)).length; i++){
			currentRow .add("out_"+i);
		}
		if(!cospUncert.isEmpty()&&cospUncert.size()>0){
			for(int i=0; i<+cospUncert.entrySet().iterator().next().getValue().length; i++){
				currentRow .add("cosp_"+i);
			}
		}		
		for(int i=0; i<+inConc.entrySet().iterator().next().getValue().length; i++){
			currentRow .add("in_"+i);
		}
		String[] row = new String[currentRow .size()];
		currentRow .toArray(row);
		encoder.writeNext(row);
		
		// loop through exposure results and write results in each line
//		ArrayList<String> currentRow = new ArrayList<String>();
		for(String time : dateList){
			currentRow = new ArrayList<String>();
			
			// create row
			currentRow.add(time);
			for(String att : attributesList.get(dateList.get(0)).keySet()){
				currentRow.add(attributesList.get(time).get(att));
			}
			for(int i=0; i<+outConc.get(time).length; i++){
				currentRow.add(outConc.get(time)[i]+"");
			}
			if(!cospUncert.isEmpty()&&cospUncert.size()>0){
				for(int i=0; i<+cospUncert.entrySet().iterator().next().getValue().length; i++){
					if(cospUncert.containsKey(time))
						currentRow.add(cospUncert.get(time)[i]+"");
					else
						currentRow.add("");
				}
			}			
			for(int i=0; i<+inConc.entrySet().iterator().next().getValue().length; i++){
				if(inConc.containsKey(time))
					currentRow.add(inConc.get(time)[i]+"");
				else
					currentRow.add("");
			}
			
			// write row
			currentRow .toArray(row);
			encoder.writeNext(row);
		}

		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
