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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.uncertweb.austalwps.util.austal.geometry.EmissionSource;
import org.uncertweb.austalwps.util.austal.geometry.ReceptorPoint;
import org.uncertweb.austalwps.util.austal.geometry.StudyArea;

public class Austal2000Txt implements Serializable {

	/**
	 * Class to read, write and manage austal2000.txt
	 */
	private static final long serialVersionUID = 1639886246212812488L;
	private final String SEPARATOR = System.getProperty("line.separator");		
	private static Logger LOGGER = Logger.getLogger(Austal2000Txt.class);
	
	// Austal parameters 
	private StudyArea studyArea;
	private List<ReceptorPoint> receptorPoints;	
	private List<EmissionSource> emissionSources;
	private int qs;	// parameter to define number of particles used in the model run
	private String os = "\"NOSTANDARD;Kmax=1;Average=1;Interval=3600\""; // special options; here to force Austal to write hourly grid outputs
	

	// constructor to create austal object from file
	public Austal2000Txt(File austalFile){
		parseFile(austalFile);
	}

	/**
	 * constructor for manual set-up
	 */
	public Austal2000Txt(StudyArea sa, List<EmissionSource> es){
		studyArea = sa;
		emissionSources = es;
	}	
	
	// constructor to create austal object from file
	public Austal2000Txt(InputStream in){
		parseFile(in);
		LOGGER.debug("Parsing InputStream");
	}
	
	public Austal2000Txt(Austal2000Txt austalTemplate) {
		this.studyArea = austalTemplate.studyArea;
		this.receptorPoints = copyReceptorPoints(austalTemplate.receptorPoints);
		this.emissionSources = copySources(austalTemplate.emissionSources);
	}
	
	// ***** MODIFICATION *****
	public List<EmissionSource> getEmissionSources(){
		return emissionSources;
	}
	
	public void setEmissionSources(List<EmissionSource> emisSources){
		emissionSources = emisSources;
	}
		
	// modify receptor points
	public void setReceptorPoints(List<ReceptorPoint> pointList){
		this.receptorPoints = pointList;
	}
				
	// ***** PARSER *****
	private void parseFile(InputStream in){
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while((line = br.readLine())!= null){
				this.parseLine(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}	
	
	// ***** PARSER *****
	private void parseFile(File austalFile){
		try {
			FileReader fr = new FileReader(austalFile);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while((line = br.readLine())!= null){
				this.parseLine(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// method to parse each row
	private void parseLine(String line){
		if (line.length() ==0) return;
		
		// if current line is a comment, look for source ids or skip this line
		if(line.startsWith("'")){
			if(line.contains("sourceid")){
				// get single entries for this line
				int startID = Integer.parseInt(line.split(" ")[1].split("-")[0].trim());
				int endID = Integer.parseInt(line.split(":")[0].split(" ")[1].split("-")[1].trim());
				String type = line.split(":")[1].trim();
				
				// assign source type to the sources within the specified range
				for(int i=0; i<emissionSources.size(); i++){
					int id = emissionSources.get(i).getDynamicSourceID();
					if(id>=startID&&id<=endID){
						emissionSources.get(i).setSourceType(type);
					}
				}
				return;
			}else return;
		}
		
		// check if current line includes a comment at a later position
		int commentPosition = line.indexOf("'");
		String lineNoComment = line;
		if (!(commentPosition == -1)){
			lineNoComment = line.substring(0, commentPosition);
		}
		while(lineNoComment.charAt(lineNoComment.length()-1)== ' '||lineNoComment.charAt(lineNoComment.length()-1)== '\t'){
			lineNoComment = lineNoComment.substring(0,lineNoComment.length()-1);
		}
		
		// get single entries for this line
		String[] lineTokens = lineNoComment.split(" ");
		
		// first entry is the header
		String lineHeader = lineTokens[0];
		if (!(lineHeader == null)){
			if(lineHeader.equalsIgnoreCase("qs")){
				if(lineTokens.length<2) return;
				qs = Integer.parseInt(lineTokens[1]);
			}
			if(lineHeader.equalsIgnoreCase("os")){
				if(lineTokens.length<2) return;
				os = lineTokens[1];
			}
			if(lineHeader.equalsIgnoreCase("gx")||lineHeader.equalsIgnoreCase("gy")||lineHeader.equalsIgnoreCase("dd")||lineHeader.equalsIgnoreCase("nx")||lineHeader.equalsIgnoreCase("ny")||lineHeader.equalsIgnoreCase("x0")||lineHeader.equalsIgnoreCase("y0")){
				if(lineTokens.length<2) return;
				this.createStudyArea(lineTokens);
				return;
			}
			if(lineHeader.equalsIgnoreCase("xp")||lineHeader.equalsIgnoreCase("yp")||lineHeader.equalsIgnoreCase("hp")){
				if(lineTokens.length<2) return;			
				createReceptorPoints(lineTokens);
				return;
			}
			if(lineHeader.equalsIgnoreCase("xq")||lineHeader.equalsIgnoreCase("yq")||lineHeader.equalsIgnoreCase("hq")||lineHeader.equalsIgnoreCase("pm-2")||lineHeader.equalsIgnoreCase("wq")||lineHeader.equalsIgnoreCase("bq")||lineHeader.equalsIgnoreCase("aq")||lineHeader.equalsIgnoreCase("cq")){
				if(lineTokens.length<2) return;
				createEmissionSources(lineTokens);
				return;
			}
		}
	}
	
	// ***** PARSER DETAILS *****
	// add data to study area 
	private void createStudyArea(String[] lineTokens){
		// for the first line, create study area
		if(studyArea==null)
			studyArea = new StudyArea();
			
		// use header to identify which parameter to add
		String lineHeader = lineTokens[0];
		if(lineHeader.equalsIgnoreCase("gx")){
			studyArea.setGx(Integer.parseInt(lineTokens[1]));
		}
		if(lineHeader.equalsIgnoreCase("gy")){
			studyArea.setGy(Integer.parseInt(lineTokens[1]));
		}
		if(lineHeader.equalsIgnoreCase("dd")){
			studyArea.setDd(Integer.parseInt(lineTokens[1]));
		}
		if(lineHeader.equalsIgnoreCase("nx")){
			studyArea.setNx(Integer.parseInt(lineTokens[1]));
		}
		if(lineHeader.equalsIgnoreCase("ny")){
			studyArea.setNy(Integer.parseInt(lineTokens[1]));
		}
		if(lineHeader.equalsIgnoreCase("x0")){
			studyArea.setX0(Integer.parseInt(lineTokens[1]));
		}
		if(lineHeader.equalsIgnoreCase("y0")){
			studyArea.setY0(Integer.parseInt(lineTokens[1]));
		}
	}
	
	// add data to receptor points
	private void createReceptorPoints(String[] lineTokens) {
		// for the first line, fill list of receptor points
		if(receptorPoints==null){
			receptorPoints = new ArrayList<ReceptorPoint>();
			for(int i = 1; i<lineTokens.length; i++){
				receptorPoints.add(new ReceptorPoint());
			}
		}
		
		// for further lines add information to receptorPoints
		if(lineTokens[0].equalsIgnoreCase("xp")){
			for(int i = 1; i<lineTokens.length; i++){
				receptorPoints.get(i-1).setXp(lineTokens[i]);
			}
		}
		if(lineTokens[0].equalsIgnoreCase("yp")){
			for(int i = 1; i<lineTokens.length; i++){
				receptorPoints.get(i-1).setYp(lineTokens[i]);
			}
		}
		if(lineTokens[0].equalsIgnoreCase("hp")){
			for(int i = 1; i<lineTokens.length; i++){
				receptorPoints.get(i-1).setHp(lineTokens[i]);
			}
		}
		
	}

	// add data to emission sources
	private void createEmissionSources(String[] lineTokens) {
		// for the first line, fill list of emission sources
		if(emissionSources==null){
			emissionSources = new ArrayList<EmissionSource>();
			for(int i = 1; i<lineTokens.length; i++){
				emissionSources.add(new EmissionSource());
			}
		}
		
		// for further lines add information to emission sources
		if(lineTokens[0].equalsIgnoreCase("pm-2")){
			// distinguish between static and dynamic sources here
			int dynamicID = 1;
			for(int i = 1; i<lineTokens.length; i++){
				if(lineTokens[i].contains("?")){
					emissionSources.get(i-1).setDynamicSourceID(dynamicID);
					dynamicID++;
				}
				else{
					emissionSources.get(i-1).setStaticStrength(lineTokens[i]);
				}
				
			}
		}
		if(lineTokens[0].equalsIgnoreCase("xq")){
			for(int i = 1; i<lineTokens.length; i++){
				emissionSources.get(i-1).setXq(lineTokens[i]);
			}
		}
		if(lineTokens[0].equalsIgnoreCase("yq")){
			for(int i = 1; i<lineTokens.length; i++){
				emissionSources.get(i-1).setYq(lineTokens[i]);
			}
		}
		if(lineTokens[0].equalsIgnoreCase("hq")){
			for(int i = 1; i<lineTokens.length; i++){
				emissionSources.get(i-1).setHq(lineTokens[i]);
			}
		}
		if(lineTokens[0].equalsIgnoreCase("wq")){
			for(int i = 1; i<lineTokens.length; i++){
				emissionSources.get(i-1).setWq(lineTokens[i]);
			}
		}
		if(lineTokens[0].equalsIgnoreCase("bq")){
			for(int i = 1; i<lineTokens.length; i++){
				emissionSources.get(i-1).setBq(lineTokens[i]);
			}
		}
		if(lineTokens[0].equalsIgnoreCase("aq")){
			for(int i = 1; i<lineTokens.length; i++){
				emissionSources.get(i-1).setAq(lineTokens[i]);
			}
		}
		if(lineTokens[0].equalsIgnoreCase("cq")){
			for(int i = 1; i<lineTokens.length; i++){
				emissionSources.get(i-1).setCq(lineTokens[i]);
			}
		}
	}


	// ***** WRITER *****
	public void writeFile(File targetFile){
		try {
			FileWriter fw = new FileWriter(targetFile);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write("gx "+studyArea.getGx()+SEPARATOR);
			bw.write("gy "+studyArea.getGy()+SEPARATOR);
			bw.write(SEPARATOR);
			bw.write("dd "+studyArea.getDd()+SEPARATOR);
			bw.write("nx "+studyArea.getNx()+SEPARATOR);
			bw.write("ny "+studyArea.getNy()+SEPARATOR);
			bw.write("x0 "+studyArea.getX0()+SEPARATOR);
			bw.write("y0 "+studyArea.getY0()+SEPARATOR);
			//if(z0!=null) bw.write("z0 "+z0+SEPERATOR);
			bw.write(SEPARATOR);
			bw.write("qs "+qs+SEPARATOR);
			bw.write(SEPARATOR);
			if(os!=null&&!os.equals("")) bw.write("os "+os+SEPARATOR);
			bw.write(SEPARATOR);
			bw.write("xp "+parseReceptorPointToString("xp")+SEPARATOR);
			bw.write("yp "+parseReceptorPointToString("yp")+SEPARATOR);
			bw.write("hp "+parseReceptorPointToString("hp")+SEPARATOR);
			bw.write(SEPARATOR);
			bw.write("xq "+parseEmissionSourceToString("xq")+SEPARATOR);
			bw.write(SEPARATOR);
			bw.write("yq "+parseEmissionSourceToString("yq")+SEPARATOR);
			bw.write(SEPARATOR);
			bw.write("hq "+parseEmissionSourceToString("hq")+SEPARATOR);
			bw.write(SEPARATOR);
			bw.write("pm-2 "+parseEmissionSourceToString("pm-2")+SEPARATOR);
			bw.write(SEPARATOR);
			bw.write("wq "+parseEmissionSourceToString("wq")+SEPARATOR);
			bw.write(SEPARATOR);
			bw.write("bq "+parseEmissionSourceToString("bq")+SEPARATOR);
			bw.write(SEPARATOR);
			bw.write("aq "+parseEmissionSourceToString("aq")+SEPARATOR);
			bw.write(SEPARATOR);
			bw.write("cq "+parseEmissionSourceToString("cq")+SEPARATOR);
			bw.close();
		} catch (IOException e) {
			LOGGER.error(e);
		}		
	}
	
	private String parseReceptorPointToString(String header) {
		String result = "";
		if(header.equalsIgnoreCase("xp")){
			for(ReceptorPoint ap : receptorPoints){
				result = result + Math.round(ap.getXp()) + " ";
			}
		}
		if(header.equalsIgnoreCase("yp")){
			for(ReceptorPoint ap : receptorPoints){
				result = result + Math.round(ap.getYp()) + " ";
			}
		}
		if(header.equalsIgnoreCase("hp")){
			for(ReceptorPoint ap : receptorPoints){
				result = result + Math.round(ap.getHp()) + " ";
			}
		}
		return result;
	}

	private String parseEmissionSourceToString(String header) {
		String result = "";
		if(header.equalsIgnoreCase("xq")){
			for(EmissionSource as : emissionSources){
				result = result + Math.round(as.getXq()) + " ";
			}
		}
		if(header.equalsIgnoreCase("yq")){
			for(EmissionSource as : emissionSources){
				result = result + Math.round(as.getYq()) + " ";
			}
		}
		if(header.equalsIgnoreCase("hq")){
			for(EmissionSource as : emissionSources){
				result = result + Math.round(as.getHq()) + " ";
			}
		}
		if(header.equalsIgnoreCase("pm-2")){
			for(EmissionSource as : emissionSources){
				result = result + as.getPm2() + " ";
			}
		}
		if(header.equalsIgnoreCase("wq")){
			for(EmissionSource as : emissionSources){
				result = result + Math.round(as.getWq()) + " ";
			}
		}
		if(header.equalsIgnoreCase("bq")){
			for(EmissionSource as : emissionSources){
				result = result + Math.round(as.getBq()) + " ";
			}
		}
		if(header.equalsIgnoreCase("aq")){
			for(EmissionSource as : emissionSources){
				result = result + Math.round(as.getAq()) + " ";
			}
		}
		if(header.equalsIgnoreCase("cq")){
			for(EmissionSource as : emissionSources){
				result = result + Math.round(as.getCq()) + " ";
			}
		}
		return result;
	}
	
	// ***** UTILITIES *****
	
	public int getQs(){
		return qs;
	}
	
	public void setQs(int qs){
		this.qs = qs;
	}
	
	public String getOs(){
		return os;
	}
	
	public void setOs(String os){
		this.os = os;
	}
	
	private List<ReceptorPoint> copyReceptorPoints(
			List<ReceptorPoint> observationPoints) {
		List<ReceptorPoint> copy = new ArrayList<ReceptorPoint>();
		for(ReceptorPoint rp : observationPoints){
			copy.add(rp.getCopy());
		}
		return copy;
	}

	private List<EmissionSource> copySources(List<EmissionSource> sources) {
		List<EmissionSource> copy = new ArrayList<EmissionSource>();
		for(EmissionSource es : sources){
			copy.add(es.getCopy());
		}
		return copy;
	}

	public StudyArea getStudyArea() {
		return studyArea;
	}

//	public void multiplyPM2(double value){  
//		DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
//		df.applyPattern("0.000E00");
//		
//		for(AustalSource as : sources){
//			String s = as.getPm2();
//			if (!s.equals("?")){
//				Double d = new Double(s);
//				d = d*value;
//				as.setPm2(df.format(d.doubleValue()));
//			}
//			
//		}
//	}
//	
//	public void addPM2(double value){  
//		DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
//		df.applyPattern("0.000E00");
//		
//		for(AustalSource as : sources){
//			String s = as.getPm2();
//			if (!s.equals("?")){
//				Double d = new Double(s);
//				d = d+value;
//				as.setPm2(df.format(d.doubleValue()));
//			}
//			
//		}
//	}
	
}
