package org.n52.wps.io.datahandler.xml;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.StaticInputDataBinding;
import org.uncertweb.StaticInputDocument;
import org.uncertweb.StaticInputType;

public class StaticInputParser extends AbstractXMLParser {

	@Override
	public IData parseXML(String arg0) {
		StaticInputDocument type2 = null;
		StaticInputType type = null;
		try {
			type2 = StaticInputDocument.Factory.parse(arg0);
			
			type = type2.getStaticInput();
			return new StaticInputDataBinding(type);
		} catch (XmlException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public IData parseXML(InputStream arg0) {
		StaticInputDocument type2 = null;
		StaticInputType type = null;
		try {
			type2 = StaticInputDocument.Factory.parse(arg0);
			
			type = type2.getStaticInput();
			return new StaticInputDataBinding(type);
		} catch (XmlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		Class<?>[] supportedClasses = {StaticInputDataBinding.class};
		return supportedClasses;
	}

	@Override
	public IData parse(InputStream arg0, String arg1) {
		StaticInputDocument type2 = null;
		StaticInputType type = null;
		try {
			type2 = StaticInputDocument.Factory.parse(arg0);
			
			type = type2.getStaticInput();
			return new StaticInputDataBinding(type);
		} catch (XmlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
