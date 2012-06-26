package org.uncertweb.wps.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
			
			e.printStackTrace();
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
		out.println(dataLocation+"test_syn.txt");
		out.println(dataLocation+"relmat2004.dat");
		out.println(dataLocation+"wrkmat2004.dat");
		out.println(dataLocation+"sampleBa00.dat");
		out.println(dataLocation+"dtrees-NL.dta");
		out.println(dataLocation+"Syspars_test.txt");
		out.println(dataLocation+"PADTdata.bin");
		
		out.flush();
		out.close();
	}
	
	public static void newInputDrawProjectFile(String projectFileName, String fileLocation, String noCases, String noCasesNew){
		
		File projectFile = new File(fileLocation+File.separator+projectFileName);
		
		PrintWriter out = null;
		
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(projectFile)));
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		out.println(noCases);
		out.println(noCasesNew);	
		
		out.flush();
		out.close();	
		
	}
}
