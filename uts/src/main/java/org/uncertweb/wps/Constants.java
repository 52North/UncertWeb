package org.uncertweb.wps;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.LocalAlgorithmRepository;

public class Constants {
	
	private static Constants instance;

	private String tmpDir;
	
	public static Constants getInstance() {
		if (instance == null) {

			instance = new Constants();
		}
		return instance;
	}
	
	private Constants(){
		
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(LocalAlgorithmRepository.class.getCanonicalName());
		for(Property property : propertyArray){
			if(property.getName().equalsIgnoreCase("tmpDir")){
				tmpDir = property.getStringValue();
			}
		}
		
	}
	
	public String getTmpDir(){
		return tmpDir;
	}
	
}
