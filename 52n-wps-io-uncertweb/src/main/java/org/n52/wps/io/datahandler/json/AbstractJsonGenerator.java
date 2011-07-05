package org.n52.wps.io.datahandler.json;

import java.io.OutputStream;
import java.util.List;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.data.IData;

public abstract class AbstractJsonGenerator implements IGenerator{
	
	protected List<String> supportedFormats;
	protected List<String> supportedEncodings;
	protected Property[] properties;

	@Override
	public String[] getSupportedEncodings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSupportedFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSupportedSchemas() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSupportedEncoding(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSupportedFormat(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSupportedSchema(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
