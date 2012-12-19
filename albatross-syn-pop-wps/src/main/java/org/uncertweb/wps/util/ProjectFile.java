package org.uncertweb.wps.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

/**
 * To run the synthetic population exe a project file is mandatory. This project file contains the parameter and paths to additional files, which contain data like postcode areas.
 * Thus, the order of arguments is important. The file is plain text.
 * 
 * @author s_voss13
 *
 */
public class ProjectFile {
	
	private String genpopHouseholds;
	private String rwdataHouseholds;
	private String municipalities;
	private String zones;
	private String postcodeAreas;
	private Boolean modelUncertainty;
	
	private String fileLocation;
	private String dataLocation;
	
	private String projectFileName;
	
	private File projectFile;
	
	protected static Logger log = Logger.getLogger(ProjectFile.class);
	
	/**
	 * Creates a new ProjectFile. It can be accessed by {@link ProjectFile#getProjectFile()}
	 * 
	 * @param projectFileName name of the project file
	 * @param fileLocation the location of the project file (should be next to the *.exe)
	 * @param dataLocation location of the data (the data referenced inside the project file)
	 * @param genpopHouseholds
	 * @param rwdataHouseholds
	 * @param municipalities
	 * @param zones
	 * @param postcodeAreas
	 */
	public ProjectFile(String projectFileName, String fileLocation, String dataLocation, String genpopHouseholds, String rwdataHouseholds, String municipalities, String zones, String postcodeAreas, Boolean modelUncertainty) {
		
		this.fileLocation = fileLocation;
		this.dataLocation = dataLocation+File.separator;
				
		this.genpopHouseholds = genpopHouseholds;
		this.rwdataHouseholds = rwdataHouseholds;
		this.municipalities = municipalities;
		this.zones = zones;
		this.postcodeAreas = postcodeAreas;
		this.modelUncertainty = modelUncertainty;
		
		this.projectFileName = projectFileName;
		
		//in our case datalocation and file location are the same 
		this.dataLocation = fileLocation+File.separator;
		
		
		projectFile = new File(this.fileLocation+File.separator+projectFileName);
		
		
		try {
			projectFile.createNewFile();
			this.fillProjectFile(projectFile);
		} catch (IOException e) {
			log.info("Error while creating new project file: "+e.getLocalizedMessage());
			throw new RuntimeException("Error while creating new project file: "+e.getLocalizedMessage());
		}
		
	}

	public String getProjectFileName() {
		return projectFileName;
	}
	
	/**
	 * This methods works only if the format of the original format is name.xyz
	 * @return
	 */
	public String getProjectFileNameAfterSampleDrawRun(){
		
		return projectFileName.substring(0, projectFileName.length()-4) + "-0" + projectFileName.substring(projectFileName.length()-4,projectFileName.length());
	}

	/**
	 * Returns the ProjectFile.
	 * @return
	 * @throws IOException
	 */
	public File getProjectFile(){
		
		return this.projectFile;
	}
	
	private void fillProjectFile(File f) throws IOException{

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));  

		try {
			/**
			 * (just model uncertainty first line=0, or just input uncertainty (first line=not 0
			 */
			
			if (modelUncertainty){
				 out.println("0");
			}
			else{
				out.println("1");
			}
			
			out.println(genpopHouseholds);
			out.println(rwdataHouseholds);
			out.println(postcodeAreas);
			out.println(zones);
			out.println(municipalities);
			
			out.println(dataLocation+"locindex+.bin");
			out.println(dataLocation+"afst-nl-car.bin");
			out.println(dataLocation+"afst-nl-slow.bin");
			out.println(dataLocation+"tijd-nl-car.bin");
			out.println(dataLocation+"reach-alt-nl-car.bin");
			out.println(dataLocation+"zonedist-ext-Base.bin");
			out.println(dataLocation+"locs-nl.bin");
			out.println(dataLocation+"times-nl.bin");
			out.println(dataLocation+"exportBin.bin");
			out.println(dataLocation+"test.prd");
			out.println(dataLocation+"Rott-2000_syn1.txt");
			out.println(dataLocation+"relmat2004.dat");
			out.println(dataLocation+"wrkmat2004.dat");
			out.println(dataLocation+"sampleBa00.dat");
			out.println(dataLocation+"dtrees-NL.dta");
			out.println(dataLocation+"Syspars_test.txt");
			out.println(dataLocation+"PADTdata.bin");
			
		}catch(Exception e){
			log.info("Error while filling project file: "+e.getLocalizedMessage());
			throw new RuntimeException("Error while filling project file: "+e.getLocalizedMessage());
		}
		finally{
			out.flush();
			out.close();
		}
	}
	
	public static void newInputDrawProjectFile(String projectFileName, String fileLocation, String noCases, String noCasesNew){
		
		File projectFile = new File(fileLocation+File.separator+projectFileName);
		
		PrintWriter out = null;
		
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(projectFile)));
			out.println(noCases);
			out.println(noCasesNew);	
		} catch (IOException e) {
			log.info("error while creating new InputDraw project file: "+e.getLocalizedMessage());
			throw new RuntimeException("error while creating new InputDraw project file: "+e.getLocalizedMessage());
		}
		finally{
			out.flush();
			out.close();
		}
		
		
	}
	
	public static void newSysParProjectFile(String projectFileName, String fileLocation, String householdFraction){
		File projectFile = new File(fileLocation+File.separator+projectFileName);
		
		PrintWriter out = null;
		
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(projectFile)));
			out.println("1");
			out.println("1");
			out.println("base setting\tscenario label");
			out.println("100.0\tCar costs - Off peak hours");
			out.println("100.0\tCar costs - Peak hours");
			out.println("100.0\train costs - Before 9 am");
			out.println("100.0\tTrain costs - After 9 am");
			out.println("100.0\tBTM costs - Younger 65 years");
			out.println("100.0\tBTM costs - 65 years or older");
			out.println("100.0\tCar travel time - Off peak hours");
			out.println("100.0\tCar travel time - Peak hours");
			out.println("100.0\tPublic transport travel time");
			out.println(householdFraction);
			out.println("0.324\tRatio living in females of total");
			out.println("0.172\tRatio single females of total");
			out.println("");
			out.println("1");
			out.println("1");
			out.println("base setting\tscenario label");
			out.println("-0.02310\t-0.00252\tSelect work");
			out.println("-0.05690\t-0.00411\tSelect fixed act");
			out.println("-0.06000\t-0.00654\tChain fixed act and work");
			out.println("-0.00434\t-0.00047\tWork location same as previous");
			out.println("-0.03170\t-0.00345\tWork location, municipality");
			out.println("-0.06259\t-0.00453\tSelect flexible activity");
				
		} catch (IOException e) {
			log.info("error while creating new InputDraw project file: "+e.getLocalizedMessage());
			throw new RuntimeException("error while creating new InputDraw project file: "+e.getLocalizedMessage());
		}
		finally{
			out.flush();
			out.close();
		}
		
	}
}
