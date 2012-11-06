package org.n52.wps.io.datahandler;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.XMLAnyDataBinding;
import org.n52.wps.io.datahandler.parser.AbstractParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLAnyDataParser extends AbstractParser {
	
	private static final Logger log = LoggerFactory
			.getLogger(XMLAnyDataParser.class);
	@Override
	public IData parse(InputStream input, String mimeType, String schema) {
		
		XmlObject inputObject = null;
		try {
			inputObject = XmlObject.Factory.parse(input);
		} catch (XmlException e1) {
			log.error(e1.getMessage());
			throw new RuntimeException(e1.getMessage());
		} catch (IOException e1) {
			log.error(e1.getMessage());
			throw new RuntimeException(e1.getMessage());
		}
		
		return new XMLAnyDataBinding(inputObject);
	}

	@Override
	public Class<?>[] getSupportedDataBindings() {
		return new Class[]{XMLAnyDataBinding.class};
	}
}
