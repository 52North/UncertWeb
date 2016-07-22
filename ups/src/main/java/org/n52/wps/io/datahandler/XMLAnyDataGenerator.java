package org.n52.wps.io.datahandler;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.XMLAnyDataBinding;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

public class XMLAnyDataGenerator extends AbstractGenerator {

	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)
			throws IOException {
		
//		PipedInputStream pin = new PipedInputStream();
//		
//		PipedOutputStream pout = new PipedOutputStream(pin);
				
		if(data.getPayload() instanceof XmlObject){
			
//			((XmlObject)data).save(pout);
//			
//			pout.flush();
//			
//			pout.close();
//			
//			return pin;
			
			XmlOptions o = new XmlOptions();
			o.setSaveNoXmlDecl();
			return ((XmlObject)data.getPayload()).newInputStream(o);
		}
		return null;
	}
	
	@Override
	public Class<?>[] getSupportedDataBindings() {
		return new Class[]{XMLAnyDataBinding.class};
	}

}
