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
public class PostProcessingConfigFile {
	
	private File postProcessingConfigFile;
	private String exportFilePath, areaFilePath,odmFilePath,indicatorsFilePath,postProcessingPath;
	
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
			
			e.printStackTrace();
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

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));  

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
		
		out.flush();
	}

}



    