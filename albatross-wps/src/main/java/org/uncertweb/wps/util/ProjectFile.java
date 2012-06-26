package org.uncertweb.wps.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author s_voss13
 *
 */
public class ProjectFile {
	
	private String genpopHouseholds;
	private String rwdataHouseholds;
	private String municipalities;
	private String zones;
	private String postcodeAreas;
	
	private String fileLocation;
	private String dataLocation;
	
	private String projectFileName;
	
	private File projectFile;
	
	private Boolean randomNumberSeed;
	
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
	public ProjectFile(String projectFileName, String fileLocation, String dataLocation, String genpopHouseholds, String rwdataHouseholds, String municipalities, String zones, String postcodeAreas, Boolean randomNumberSeed) {
		
		this.fileLocation = fileLocation;
		this.dataLocation = dataLocation+File.separator;
				
		this.genpopHouseholds = genpopHouseholds;
		this.rwdataHouseholds = rwdataHouseholds;
		this.municipalities = municipalities;
		this.zones = zones;
		this.postcodeAreas = postcodeAreas;
		
		this.projectFileName = projectFileName;
		
		//in our case datalocation and file location are the same 
		this.dataLocation = fileLocation+File.separator;
		
		this.randomNumberSeed = randomNumberSeed;
		
		projectFile = new File(this.fileLocation+File.separator+projectFileName);
		
		
		try {
			projectFile.createNewFile();
			this.fillProjectFile(projectFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public String getProjectFileName() {
		return projectFileName;
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
		
		if (randomNumberSeed){
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
	
	public static void newInputDrawProjectFile(String projectFileName, String fileLocation, String dataLocation, String genpopHouseholds, String rwdataHouseholds, String municipalities, String zones, String postcodeAreas, Boolean isModelUncertainty){
		
		File projectFile = new File(fileLocation+File.separator+projectFileName);
		
		PrintWriter out = null;
		
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(projectFile)));
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		
		dataLocation += File.separator;
		
		/**
		 * (just model uncertainty first line=0, or just input uncertainty (first line=not 0
		 */
		
		if (isModelUncertainty){
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
		
		//this file will be written
		out.println("tijd-nl-car.bin");
		
		out.println(dataLocation+"reach-alt-nl-car.bin");
		out.println(dataLocation+"zonedist-ext-Base.bin");
		
		//this file will be written
		out.println("locs-nl.bin");
		
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
}
