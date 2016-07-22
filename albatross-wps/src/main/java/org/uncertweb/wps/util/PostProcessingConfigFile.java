package org.uncertweb.wps.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

/**
 * The postprocessing requires a configuration file that includes several paths. It is basically a xml document. However, the document has to be adjusted for every 
 * specific workspace. This class is intended to build this file from  the given parameters.
 * 
 * @author s_voss13
 *
 */
public class PostProcessingConfigFile {
	
	protected static Logger log = Logger.getLogger(PostProcessingConfigFile.class);
	private File postProcessingConfigFile;
	private String exportFilePath, areaFilePath,odmFilePath,indicatorsFilePath,postProcessingPath;
	
	/**
	 * Creates a new "physical" post processing config file. As the post processing program expects only a predefined name for the file the constructor needs only the folder name.
	 * The config file will be created inside this folder. This is only the case if the file does not exist. The config file consists of 5 paths. 3 of them link to already existing files
	 * inside the workspace and two of them (indicators and odMatrix) are creating by the post processing program. The file can be determined with the {@link PostProcessingConfigFile#getPostProcessingConfigFile()}
	 * method. For later usage of the generated files find the {@link PostProcessingConfigFile#getIndicatorsPath()} and {@link PostProcessingConfigFile#getOdmPath()} methods.
	 * 
	 * @param exportFilePath
	 * @param areaFilePath
	 * @param odmFilePath
	 * @param indicatorsFilePath
	 * @param postProcessingPath
	 */
	public PostProcessingConfigFile(String exportFilePath, String areaFilePath, String odmFilePath, String indicatorsFilePath, String postProcessingPath){
		
		this.exportFilePath = exportFilePath;
		this.areaFilePath = areaFilePath;
		this.odmFilePath = odmFilePath;
		this.indicatorsFilePath = indicatorsFilePath;
		this.postProcessingPath = postProcessingPath;
		
		postProcessingConfigFile = new File(this.postProcessingPath+File.separator+"config_uw.xml");
		
		try {
			postProcessingConfigFile.createNewFile();
			fillPostProcessingConfigFile(postProcessingConfigFile);
		} catch (IOException e) {
			log.info("Error while creating config file for post processing: "+e.getLocalizedMessage());
			throw new RuntimeException("Error while creating config file for post processing: "+e.getLocalizedMessage());
		}
	}
	
	public String getIndicatorsPath(){
		
		return postProcessingPath+File.separator+indicatorsFilePath;
	}
	
	public String getOdmPath(){
		
		return postProcessingPath+File.separator+odmFilePath;
	}
	
	public File getPostProcessingConfigFile(){
		
		return this.postProcessingConfigFile;
	}
	
	private void fillPostProcessingConfigFile(File f) throws IOException{

		PrintWriter out = null;
		try {
		out = new PrintWriter(new BufferedWriter(new FileWriter(f)));  

		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + 
				"       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + 
				"       xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\">\r\n" + 
				"\r\n" + 
				"    <bean id=\"source\" class=\"uw.odmatrix.ODMatrix\">");

	   
		out.println("<property name=\"fileSchedule\" value=\""+exportFilePath+"\"/>");
		out.println("<property name=\"fileArea\" value=\""+postProcessingPath+File.separator+areaFilePath+"\"/>");
		out.println("<property name=\"fileODMtx\" value=\""+postProcessingPath+File.separator+odmFilePath+"\"/>");
		
		out.println("</bean>\r\n" + 
				"    <bean id=\"indexes\" class=\"uw.odmatrix.Indicators\">");
		
		out.println(" <property name=\"fileIndicators\" value=\""+postProcessingPath+File.separator+indicatorsFilePath+ "\"/>");
		
		out.println(" </bean> \r\n" + 
				"   \r\n" + 
				"</beans>");
		} catch (Exception e){
			log.info("Error while creating config file for post processing: "+e.getLocalizedMessage());
			throw new RuntimeException("Error while creating config file for post processing: "+e.getLocalizedMessage());
		}
		finally {
			if (out!=null){
				out.flush();
				out.close();
			}
		}
	}

}



    