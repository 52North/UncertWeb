package org.n52.wps.io.datahandler.xml;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.UncertainInputDataBinding;
import org.uncertweb.UncertainInputDocument;
import org.uncertweb.UncertainInputType;

public class UncertainInputParser extends AbstractXMLParser {

	@Override
	public IData parseXML(String arg0) {
		UncertainInputDocument type2 = null;
		UncertainInputType type = null;
		try {
			type2 = UncertainInputDocument.Factory.parse(arg0);
			
			type = type2.getUncertainInput();
			return new UncertainInputDataBinding(type);
		} catch (XmlException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public IData parseXML(InputStream arg0) {
		UncertainInputDocument type2 = null;
		UncertainInputType type = null;
		try {
			type2 = UncertainInputDocument.Factory.parse(arg0);
			
			type = type2.getUncertainInput();
			return new UncertainInputDataBinding(type);
		} catch (XmlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		Class<?>[] supportedClasses = {UncertainInputDataBinding.class};
		return supportedClasses;
	}

	@Override
	public IData parse(InputStream arg0, String arg1) {
		UncertainInputDocument type2 = null;
		UncertainInputType type = null;
		try {
			type2 = UncertainInputDocument.Factory.parse(arg0);
			
			type = type2.getUncertainInput();
			return new UncertainInputDataBinding(type);
		} catch (XmlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
