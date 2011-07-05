package org.n52.wps.io.datahandler;

import java.util.ArrayList;
import java.util.List;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IStreamableGenerator;

public abstract class AbstractUncertWebDataGenerator implements IGenerator, IStreamableGenerator{

	protected Property[] properties;
	protected List<String> supportedSchemas;
	protected List<String> supportedFormats;
	protected List<String> supportedEncodings;
	
	/**
	 * constructor; queries supported schemas, formats and encodings from definition of parser in WPS config
	 * 
	 */
	public AbstractUncertWebDataGenerator() {
		supportedSchemas = new ArrayList<String>();
		supportedFormats = new ArrayList<String>();
		supportedEncodings = new ArrayList<String>();
		properties = WPSConfig.getInstance().getPropertiesForGeneratorClass(this.getClass().getName());
		for(Property property : properties){
			if(property.getName().equalsIgnoreCase("supportedSchema")){
				String supportedSchema = property.getStringValue();
				supportedSchemas.add(supportedSchema);
			}
			if(property.getName().equalsIgnoreCase("supportedFormat")){
				String supportedFormat = property.getStringValue();
				supportedFormats.add(supportedFormat);
			}
			if(property.getName().equalsIgnoreCase("supportedEncoding")){
				String supportedEncoding = property.getStringValue();
				supportedEncodings.add(supportedEncoding);
			}
		}
	}
	
	@Override
	public boolean isSupportedFormat(String format) {
		for(String f : getSupportedFormats()) {
			if (f.equalsIgnoreCase(format)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String[] getSupportedFormats() {
		String[] resultList = new String[supportedFormats.size()];
		for(int i = 0; i<supportedFormats.size();i++){
			resultList[i] = supportedFormats.get(i);
		}
		return resultList;
		
	}
	
	@Override
	public String[] getSupportedSchemas() {
		String[] resultList = new String[supportedSchemas.size()];
		for(int i = 0; i<supportedSchemas.size();i++){
			resultList[i] = supportedSchemas.get(i);
		}
		return resultList;
	
	}


	@Override
	public boolean isSupportedSchema(String schema) {
		if (schema==null){
			return true;
		}
		else {
			for(String supportedSchema : supportedSchemas) {
				if(supportedSchema.equalsIgnoreCase(schema))
					return true;
			}
			return false;
		}
	}
	
	@Override
	public boolean isSupportedEncoding(String encoding) {
		for(String supportedEncoding : supportedEncodings) {
			if(supportedEncoding.equalsIgnoreCase(encoding))
				return true;
		}
		return false;
	}
	
	@Override
	public String[] getSupportedEncodings() {
		String[] resultList = new String[supportedEncodings.size()];
		for(int i = 0; i<supportedEncodings.size();i++){
			resultList[i] = supportedEncodings.get(i);
		}
		return resultList;
	}

}
