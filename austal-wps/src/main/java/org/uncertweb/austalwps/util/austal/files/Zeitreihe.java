package org.uncertweb.austalwps.util.austal.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.uncertweb.austalwps.util.austal.geometry.EmissionSource;
import org.uncertweb.austalwps.util.austal.timeseries.EmissionTimeSeries;
import org.uncertweb.austalwps.util.austal.timeseries.MeteorologyTimeSeries;

/**
 * class to read and write the zeitreihe.dmna file
 */

public class Zeitreihe implements Serializable{
	
	private static final long serialVersionUID = -7019593949581207831L;
	private final String SEPERATOR = System.getProperty("line.separator");	
	private static Logger LOGGER = Logger.getLogger(Zeitreihe.class);
	
	// Parameters in the zeitreihe.dmna file	
	private List<String> forms = new ArrayList<String>();
	private String locl = "\"C\"";
	private String mode = "\"text\"";	
	private String artp = "\"ZA\"";
	private String sequ = "\"i\"";
	private String dims = "1";
	private String lowb = "1";
	private String ha = "4.2\t5.2\t7.1\t9.0\t11.5\t16.3\t21.9\t26.3\t30.1";
	private String size;
	private String hghb;
	private String z0;
	private String d0; //6*z0
	
	// Strings to identify values in the timeseries
	// "ra%5.0f" wind direction
	// "ua%5.1f" wind speed
	// "lm%7.1f" stability class
	private String[] meteoIdentifiers = {"\"ra%5.0f\"","\"ua%5.1f\"","\"lm%7.1f\""};
	// "te%20lt" timestamp 
	private String timeStampIdentifier = "\"te%20lt\"";
	private DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd.HH:mm:ss").withZone(DateTimeZone.UTC.forOffsetHours(1));		
	
	//private List<TimeStamp> timestamps = new ArrayList<TimeStamp>();
	private List<EmissionTimeSeries> emisList = new ArrayList<EmissionTimeSeries>();
	private MeteorologyTimeSeries metList = new MeteorologyTimeSeries();
	
	/**
	 * constructor for manual set-up
	 */
	public Zeitreihe(MeteorologyTimeSeries meteoTS, List<EmissionSource> emissions, String z0){
		this.z0 = z0;
		this.metList = meteoTS;
		createEmissionTS(emissions);
	}
	
	/**
	 * Extracts emission time series from emission source list 
	 * @param emissions
	 */
	private void createEmissionTS(List<EmissionSource> emissions){
		for(EmissionSource source : emissions){
			if(source.isDynamic()){
				emisList.add(source.getEmissionList());
			}
		}
	}

	// constructor to create zeitreihe object from zeitreihe.dmna file
	public Zeitreihe(File zeitreiheFile){
		this.parseFile(zeitreiheFile, false);
	}
	
	// constructor to create zeitreihe object from zeitreihe.dmna file
	public Zeitreihe(InputStream in){
		this.parseFile(in, false);
	}
	
	// ***** PARSER *****
	/**
	 * Parses zeitreihe.dmna file
	 * @param File zeitreihe
	 */
	private void parseFile(InputStream in, boolean onlyHeader){
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while(!(line = br.readLine()).equals("*")){
				this.parseHeader(line);
			}
			
			while(!onlyHeader&&!(line = br.readLine()).equals("***")){
				this.parseTimeStamps(line);
			}
			//if(metList.getSize()<1);
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private void parseTimeStamps(String line) {
		if (line.length() ==0||line.equals(" ")) return;
		
		String lineNoDoubleSpaces = line;
		//delete all double spaces
		while(lineNoDoubleSpaces.indexOf("  ")!=-1){
			lineNoDoubleSpaces = lineNoDoubleSpaces.replaceAll("  ", " ");
		}
		//delete first space
		if(lineNoDoubleSpaces.startsWith(" "))
			lineNoDoubleSpaces = lineNoDoubleSpaces.replaceFirst(" ", "");
		
		String[] timeStampTokens = lineNoDoubleSpaces.split(" ");
		if(timeStampTokens.length == 1){
			timeStampTokens = lineNoDoubleSpaces.split("\t");
		}
		
		// check if line has same length as forms in header
		if(timeStampTokens.length!=forms.size()){
			LOGGER.debug("Length of header is "+forms.size()+" while lenght of line is "+timeStampTokens.length);
			LOGGER.debug("Error for line "+ timeStampTokens[this.getFormIndex(timeStampIdentifier)].trim());
		}
		
		// divide into meteorology and emissions
		// timestamp 
		String time = timeStampTokens[this.getFormIndex(timeStampIdentifier)].trim();
		DateTime timeStamp = dateFormat.parseDateTime(time);

		//String timeStamp = timeStampTokens[this.getFormIndex(timeStampIdentifier)];
		
		// meteorology				
		String[] meteoVals = new String[3];
		java.lang.System.arraycopy(timeStampTokens, this.getFormIndex(meteoIdentifiers[0]), meteoVals, 0, 3);
		metList.addMeteorology(timeStamp, meteoIdentifiers, meteoVals);
				
		// emissions
		// "01.pm-2%10.3e" ...
		for(int i=0; i<emisList.size(); i++){
			emisList.get(i).addEmissionValue(timeStamp, timeStampTokens[this.getFormIndex(emisList.get(i).getDynamicSourceIDToken())]);
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
		DecimalFormat scientific = new DecimalFormat("0.###E000");
		try {

			List<DateTime> timestamps = metList.getTimeStamps();
			// check that list starts with hour 01:00
			int start = 0;
			//while(start<(timestamps.size()-1)&&timestamps.get(start).getHourOfDay()!=1){
			while(start<(timestamps.size()-1)&&!dateFormat.print(timestamps.get(start)).contains(".01:00:00")){
				start++;
			}
			
			// get length of time series and emission sources
			hghb = ""+(this.metList.getSize() - start);
			size = ""+(emisList.size()*4 + 20);
			d0 = ""+Double.parseDouble(this.z0)*6;
			
			FileWriter fw = new FileWriter(targetFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(buildFormsString()+SEPERATOR);
			bw.write("locl\t"+locl+SEPERATOR);
			bw.write("mode\t"+mode+SEPERATOR);
			bw.write("ha\t"+ha+SEPERATOR);
			bw.write("z0\t"+z0+SEPERATOR);
			bw.write("d0\t"+d0+SEPERATOR);
			bw.write("artp\t"+artp+SEPERATOR);
			bw.write("sequ\t"+sequ+SEPERATOR);
			bw.write("dims\t"+dims+SEPERATOR);
			bw.write("size\t"+size+SEPERATOR);
			bw.write("lowb\t"+lowb+SEPERATOR);
			bw.write("hghb\t"+hghb+SEPERATOR);
			bw.write("*"+SEPERATOR);		
			
			for(int i=start; i<timestamps.size(); i++){
				String time = dateFormat.print(timestamps.get(i));
				bw.write(time+" ");
				bw.write(metList.getMeteorologyToString(i));
				String e = "";
				for(EmissionTimeSeries ts : emisList){	
					double emis = ts.getEmissionValue(i);
					if(emis==0)
						e = e + " " + "0.000e+000";
					else
						e = e + " " + scientific.format(emis);									
				}
				String eNew = e.replace(",", ".");
				bw.write(eNew + SEPERATOR);
			}
			bw.write("***");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// ***** MODIFICATION *****
	//
	public String getZ0(){
		return z0;
	}
	
	public void setZ0(String z0){
		this.z0 = z0;
	}
	
	// modify meteorology time series
	public MeteorologyTimeSeries getMeteorologyTimeSeries(){
		return metList;
	}
	
	public void setMeteorologyTimeSeries(MeteorologyTimeSeries meteoTS){
		this.metList = meteoTS;
		this.hghb = "hghb\t"+(this.metList.getSize());
	}
	
	// modify emission time series
	public List<EmissionTimeSeries> getEmissionSourcesTimeSeries(){
		return emisList;
	}
	
	public void setEmissionSourcesTimeSeries(List<EmissionTimeSeries> emisListTS){
		this.emisList = emisListTS;
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
		String s = "form\t";
		// add time and meteo tokens
		s = s+ timeStampIdentifier + " ";
		for(String s2 : meteoIdentifiers){
			s = s+s2+" ";
		}
		// add emission id tokens
		for(EmissionTimeSeries emisTS : emisList){
			String s2 = emisTS.getDynamicSourceIDToken();
			s = s+s2+" ";
			
		}
		return s;
	}

	public String getTSlength(){
		return hghb;
	}
	
	public void setTimePeriod(DateTime start, DateTime end){
		metList.cutTimePeriod(start, end);
		for(int i=0; i<emisList.size(); i++){
			emisList.get(i).cutTimePeriod(start, end);
		}
		this.hghb = "hghb\t"+(this.metList.getSize());
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