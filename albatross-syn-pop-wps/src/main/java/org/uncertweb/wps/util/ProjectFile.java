/**
 * 
 */
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
	
	public ProjectFile(String projectFileName,String fileLocation, String dataLocation, String genpopHouseholds, String rwdataHouseholds, String municipalities, String zones, String postcodeAreas) {
		
		this.fileLocation = fileLocation;
		this.dataLocation = dataLocation+File.separator;
		this.projectFileName = projectFileName;
		
		this.genpopHouseholds = genpopHouseholds;
		this.rwdataHouseholds = rwdataHouseholds;
		this.municipalities = municipalities;
		this.zones = zones;
		this.postcodeAreas = postcodeAreas;
		
		//in our case datalocation and file location are the same 
		
		this.dataLocation = fileLocation+File.separator;
	}

	public File getProjectFile() throws IOException{
		
		File projectFile = new File(this.fileLocation+File.separator+projectFileName);
	
		projectFile.createNewFile();
		
		this.fillProjectFile(projectFile);
		
		return projectFile;

	}
	
	private void fillProjectFile(File f) throws IOException{

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));  

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
		out.println(dataLocation+"test.obs");
		out.println(dataLocation+"test.prd");
		out.println(dataLocation+"test_syn.txt");
		out.println(dataLocation+"relmat2004.dat");
		out.println(dataLocation+"wrkmat2004.dat");
		out.println(dataLocation+"sampleBa00.dat");
		out.println(dataLocation+"dtrees-NL.dta");
		out.println(dataLocation+"Syspars_test.txt");
		out.println(dataLocation+"PADTdata.bin");
		
		out.flush();
	}
}
