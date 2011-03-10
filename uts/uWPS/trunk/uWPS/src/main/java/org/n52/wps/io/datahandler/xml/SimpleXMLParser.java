package org.n52.wps.io.datahandler.xml;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.PlainStringBinding;

public class SimpleXMLParser extends AbstractXMLParser{

	@Override
	public IData parseXML(String arg0) {	
		return new PlainStringBinding(arg0);
	}

	@Override
	public IData parseXML(InputStream arg0) {
		
		try{
		
		BufferedReader bRead = new BufferedReader(new InputStreamReader(arg0));
		
		String payload = "";
		
		String line = "";
		
		while((line = bRead.readLine()) != null){
			payload = payload.concat(line + "\n");
		}
		
		return new PlainStringBinding(payload);
		
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		Class<?>[] supportedClasses = {PlainStringBinding.class};
		return supportedClasses;
	}

	@Override
	public IData parse(InputStream arg0, String arg1) {
		try{
			
			BufferedReader bRead = new BufferedReader(new InputStreamReader(arg0));
			
			String payload = "";
			
			String line = "";
			
			while((line = bRead.readLine()) != null){
				payload = payload.concat(line + "\n");
			}
			
			return new PlainStringBinding(payload);
			
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
	}

}
