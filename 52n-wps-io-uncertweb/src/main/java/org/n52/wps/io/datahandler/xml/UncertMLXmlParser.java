package org.n52.wps.io.datahandler.xml;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertMLData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.OMDataBinding;
import org.n52.wps.io.data.binding.complex.UncertMLDataBinding;
import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.XMLParser;

/**
 * parser for UncertML uncertainties
 * 
 * @author staschc
 *
 */
public class UncertMLXmlParser extends AbstractXMLParser{
	
	
	private static Logger LOGGER = Logger.getLogger(UncertMLXmlParser.class);
	
	
	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		Class<?>[] supportedClasses = { OMDataBinding.class };
		return supportedClasses;
	}

	@Override
	public IData parse(InputStream stream, String mimeType) {
		if (!mimeType.equals(UncertWebDataConstants.MIMETYPE_TYPE_UNCERTML)){
			throw new RuntimeException("MimeType "+mimeType+" is not supported by UncertMLXMLParser!");
		}
		else {
			return parseXML(stream);
		}
	}

	@Override
	public IData parseXML(String arg0) {
		UncertMLDataBinding result = null;
		XMLParser parser = new XMLParser();
		try {
			IUncertainty uncertainty = parser.parse(arg0);
			result = new UncertMLDataBinding(new UncertMLData(uncertainty));
		} catch (UncertaintyParserException e) {
			String message = "Error while parsing UncertML input: "+e.getMessage();
			LOGGER.debug(message);
			throw new RuntimeException(message, e);
		}
		return result;
	}

	@Override
	public IData parseXML(InputStream stream) {
		UncertMLDataBinding result = null;
		XMLParser parser = new XMLParser();
		try {
			IUncertainty uncertainty = parser.parse(stream);
			result = new UncertMLDataBinding(new UncertMLData(uncertainty));
		} catch (UncertaintyParserException e) {
			String message = "Error while parsing UncertML input: "+e.getMessage();
			LOGGER.debug(message);
			throw new RuntimeException(message, e);
		}
		return result;
	}
}
