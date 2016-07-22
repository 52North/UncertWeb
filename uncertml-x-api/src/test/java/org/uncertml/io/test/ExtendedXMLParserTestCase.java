package org.uncertml.io.test;

import java.io.File;

import junit.framework.TestCase;

import org.uncertml.IUncertainty;
import org.uncertml.io.ExtendedXMLParser;
import org.uncertweb.utils.UwFileUtils;

public class ExtendedXMLParserTestCase extends TestCase {

	private String localPath = "D:/uncertwebWorkspace/uncertml-x-api/";
	private String pathToExamples = "src/test/resources";
	
	public void testParser() throws Exception{
		File folder = new File(localPath+pathToExamples);
		File[] fileArray = folder.listFiles();
		if (fileArray!=null){
			for (int i=0;i<fileArray.length;i++){
				String path = fileArray[i].getAbsolutePath();
				
				//parse xmlFile
				if (!path.contains("svn")){
					String xmlString = UwFileUtils.readXmlFile(path);
					ExtendedXMLParser parser = new ExtendedXMLParser();
					IUncertainty result = parser.parse(xmlString);
					System.out.println("-----XMLfile read from path " + path);
					System.out.print(xmlString);
					System.out.print("Parser object: "+result.getClass());
				}
			}
		}
	}

}
