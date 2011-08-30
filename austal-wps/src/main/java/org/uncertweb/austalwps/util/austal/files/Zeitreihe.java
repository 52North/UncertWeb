package org.uncertweb.austalwps.util.austal.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.uncertweb.austalwps.util.austal.timeseries.EmissionTimeSeries;
import org.uncertweb.austalwps.util.austal.timeseries.MeteorologyTimeSeries;

/**
 * class to read and write the zeitreihe.dmna file
 */

public class Zeitreihe implements Serializable{
	private static final long serialVersionUID = -7019593949581207831L;
	private final String SEPERATOR = System.getProperty("line.separator");	
	
	// Parameters in the zeitreihe.dmna file	
	private List<String> forms = new ArrayList<String>();
	private String locl;
	private String mode;
	private String ha;
	private String z0;
	private String d0;
	private String artp;
	private String sequ;
	private String dims;
	private String size;
	private String lowb;
	private String hghb;
	// Strings to identify values in the timeseries
	// "ra%5.0f" wind direction
	// "ua%5.1f" wind speed
	// "lm%7.1f" stability class
	private String[] meteoIdentifiers = {"\"ra%5.0f\"","\"ua%5.1f\"","\"lm%7.1f\""};
	// "te%20lt" timestamp 
	private String timeStampIdentifier = "\"te%20lt\"";
	
	//private List<TimeStamp> timestamps = new ArrayList<TimeStamp>();
	private List<EmissionTimeSeries> emisList = new ArrayList<EmissionTimeSeries>();
	private MeteorologyTimeSeries metList = new MeteorologyTimeSeries();
	
	public Zeitreihe(Zeitreihe zeitreihe) {
		this.forms = zeitreihe.forms;
		this.locl = zeitreihe.locl;
		this.mode = zeitreihe.mode;
		this.ha = zeitreihe.ha;
		this.z0 = zeitreihe.z0;
		this.d0 = zeitreihe.d0;
		this.artp = zeitreihe.artp;
		this.sequ = zeitreihe.sequ;
		this.dims = zeitreihe.dims;
		this.size = zeitreihe.size;
		this.lowb = zeitreihe.lowb;
		
		//in the first modelRun we have to guarantee, that there are at least 24 values or austal won't work
	//	if(modelRun == 0) optimizationInterval = 24;
		
//		this.timestamps = zeitreihe.timestamps;//copyTimeStamps(zeitreihe, modelRun, optimizationInterval);
		// set number of timestamps in the time series
		this.hghb = "hghb\t"+(this.metList.getSize());
	}

	// constructor to create zeitreihe object from zeitreihe.dmna file
	public Zeitreihe(File zeitreiheFile){
		this.parseFile(zeitreiheFile, false);
	}
	
	// constructor with meteorology and emissions as inputs, header is parsed from file
	public Zeitreihe(File zeitreiheFile, MeteorologyTimeSeries meteoTS, List<EmissionTimeSeries> emisListTS){
		this.metList = meteoTS;
		this.emisList = emisListTS;
		
		//parse only header from zeitreihe.dmna file
		this.parseFile(zeitreiheFile, true);
		this.hghb = "hghb\t"+(this.metList.getSize());
	}
	
	public String getTSlength(){
		return hghb;
	}
	
	// ***** PARSER *****
	/**
	 * Parses zeitreihe.dmna file
	 * @param File zeitreihe
	 */
	private void parseFile(File zeitreiheFile, boolean onlyHeader){
		try {
			FileReader fr = new FileReader(zeitreiheFile);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while(!(line = br.readLine()).equals("*")){
				this.parseHeader(line);
			}
			
			while(!onlyHeader&&!(line = br.readLine()).equals("***")){
				this.parseTimeStamps(line);
			}
			//if(metList.getSize()<1);
			br.close();
			System.out.println();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void parseTimeStamps(String line) {
		if (line.length() ==0) return;
		
		String lineNoDoubleSpaces = line;
		//delete all double spaces
		while(lineNoDoubleSpaces.indexOf("  ")!=-1){
			lineNoDoubleSpaces = lineNoDoubleSpaces.replaceAll("  ", " ");
		}
		//delete first space
			lineNoDoubleSpaces = lineNoDoubleSpaces.replaceFirst(" ", "");
		
		String[] timeStampTokens = lineNoDoubleSpaces.split(" ");
		if(timeStampTokens.length == 1){
			timeStampTokens = lineNoDoubleSpaces.split("\t");
		}
		
		// check if line has same length as forms in header
		if(timeStampTokens.length!=forms.size()){
			System.out.println("Length of header is "+forms.size()+" while lenght of line is "+timeStampTokens.length);
		}
		
		// divide into meteorology and emissions
		// timestamp 
		String timeStamp = timeStampTokens[this.getFormIndex(timeStampIdentifier)];
		
		// meteorology				
		String[] meteoVals = new String[3];
		java.lang.System.arraycopy(timeStampTokens, this.getFormIndex(meteoIdentifiers[0]), meteoVals, 0, 3);
		metList.addMeteorology(timeStamp, meteoIdentifiers, meteoVals);
				
		// emissions
		// "01.pm-2%10.3e" ...
		for(int i=0; i<emisList.size(); i++){
			emisList.get(i).addEmissionValue(timeStamp, timeStampTokens[this.getFormIndex(emisList.get(i).getSourceID())]);
		}
		
		//timestamps.add(new TimeStamp(timeStampTokens));
	}

	private void parseHeader(String line){
		if (line.length() ==0) return;
		int commentPosition = line.indexOf("'");
		String lineNoComment = line;
		if (!(commentPosition == -1)){
			lineNoComment = line.substring(0, commentPosition);
		}
		while(lineNoComment.charAt(lineNoComment.length()-1)== ' '||lineNoComment.charAt(lineNoComment.length()-1)== '\t'){
			lineNoComment = lineNoComment.substring(0,lineNoComment.length()-1);
		}
		String[] lineTokens = lineNoComment.split("\t");
		
		String lineHeader = lineTokens[0];
		if (!(lineHeader == null)){
			if(lineHeader.equalsIgnoreCase("form")){
				if(lineTokens.length<2) return;
					String[] forms = lineTokens[1].split(" ");
					for(int i = 0; i<forms.length; i++){
						this.forms.add(forms[i]);
						
						// fill emission list with sources
						if(forms[i].contains("pm-2")){
							EmissionTimeSeries emis = new EmissionTimeSeries(forms[i]);
							emisList.add(emis);
						}		
					}
				return;
			}
			if(lineHeader.equalsIgnoreCase("locl")){
				if(lineTokens.length<2) return;
				this.locl = line;
				return;
			}
			if(lineHeader.equalsIgnoreCase("mode")){
				if(lineTokens.length<2) return;
				this.mode = line;
				return;
			}
			if(lineHeader.equalsIgnoreCase("ha")){
				if(lineTokens.length<2) return;
				this.ha = line;
				return;
			}
			if(lineHeader.equalsIgnoreCase("z0")){
				if(lineTokens.length<2) return;
				this.z0 = line;
				return;
			}
			if(lineHeader.equalsIgnoreCase("d0")){
				if(lineTokens.length<2) return;
				this.d0 = line;
				return;
			}
			if(lineHeader.equalsIgnoreCase("artp")){
				if(lineTokens.length<2) return;
				this.artp = line;
				return;
			}
			if(lineHeader.equalsIgnoreCase("sequ")){
				if(lineTokens.length<2) return;
				this.sequ = line;
				return;
			}
			if(lineHeader.equalsIgnoreCase("dims")){
				if(lineTokens.length<2) return;
				this.dims = line;
				return;
			}
			if(lineHeader.equalsIgnoreCase("size")){
				if(lineTokens.length<2) return;
				this.size = line;
				return;
			}
			if(lineHeader.equalsIgnoreCase("lowb")){
				if(lineTokens.length<2) return;
				this.lowb = line;
				return;
			}
			if(lineHeader.equalsIgnoreCase("hghb")){
				if(lineTokens.length<2) return;
				this.hghb = line;
				return;
			}
		}
	}
	
	// ***** WRITER *****
	/**
	 * Writes emission and meteorology timeseries to new zeitreihe.dmna file
	 * @param targetFile
	 */
	public void writeFile(File targetFile){
		try {
			FileWriter fw = new FileWriter(targetFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("form\t"+buildFormsString()+SEPERATOR);
			bw.write(locl+SEPERATOR);
			bw.write(mode+SEPERATOR);
			bw.write(ha+SEPERATOR);
			bw.write(z0+SEPERATOR);
			bw.write(d0+SEPERATOR);
			bw.write(artp+SEPERATOR);
			bw.write(sequ+SEPERATOR);
			bw.write(dims+SEPERATOR);
			bw.write(size+SEPERATOR);
			bw.write(lowb+SEPERATOR);
			bw.write(hghb+SEPERATOR);
			bw.write("*"+SEPERATOR);
			List<String> timestamps = metList.getTimeStamps();
			for(int i=0; i<timestamps.size(); i++){
				bw.write(timestamps.get(i)+" ");
				bw.write(metList.getMeteorologyToString(i));
				String e = "";
				for(EmissionTimeSeries ts : emisList){
					e = e + " " + ts.getEmissionValue(i);
				}
				bw.write(e + SEPERATOR);
			}
			bw.write("***");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// ***** MODIFICATION *****
	// modify meteorology time series
	public void setMeteorologyTimeSeries(MeteorologyTimeSeries meteoTS){
		this.metList = meteoTS;
		this.hghb = "hghb\t"+(this.metList.getSize());
	}
	
	// modify emission time series
	public void setEmissionSourcesTimeSeries(List<EmissionTimeSeries> emisListTS){
		this.emisList = emisListTS;
	}
	
	// set start and end date
	public void setStartEndDate(String start, String end){
		
	}
	
	//****** UTILITIES ******
	// find index in the timestamp arrays for the respective parameter
	private int getFormIndex(String formID){
		// loop through forms to find respective string	
		for(int i=0; i<forms.size(); i++){
			if(formID.equalsIgnoreCase(forms.get(i)))
				return i;
		}
		return -1;
	}
	
	// creates new line from forms array
	private String buildFormsString() {
		String s = "";
		for(String s2 : forms){
			s = s+s2+" ";
		}
		return s;
	}

//	public int getColumn(String s) {
//		for(String form: forms){
//			if(form.contains(s)){
//				return forms.indexOf(form);
//			}
//		}
//		return -1;
//	}
	
}
