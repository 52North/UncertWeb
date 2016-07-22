package org.uncertweb.metadata.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;
import org.uncertweb.metadata.ServiceMetadata;


import junit.framework.TestCase;

public class ServiceMetadataTestCase extends TestCase {
	
	private String localPath = "D:/uncertwebWorkspace/uncertweb-service-metadata/";
	private String pathToExamples = "src/test/resources";
	
	
	@Test
	public void testPeriodParsing() {
		Properties p = new Properties();
		File f;
		
		try {
			 f = new File(pathToExamples+"/metadataTest.props");
		}catch (Exception fe){
				f = new File(localPath+pathToExamples+"/metadataTest.props");
		}
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
			p.load(fis);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServiceMetadata md = new ServiceMetadata(p);
		assertEquals("1km by 1km to 10km by 10km", md.getSpatialResolutions());
		System.out.println(md.serialize());
	}
}
