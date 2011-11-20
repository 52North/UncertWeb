package org.n52.wps.io.datahandler.uncertml;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_UNCERTML_XML;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_UNCERTML;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.datahandler.AbstractUwParser;
import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.XMLParser;
import org.uncertweb.utils.UwCollectionUtils;

/**
 * parser for UncertML uncertainties
 * 
 * @author staschc
 *
 */
public class UncertMLXmlParser extends AbstractUwParser {
	
	
	public UncertMLXmlParser() {
		super(
			set(SCHEMA_UNCERTML), 
			set(ENCODING_UTF_8),
			set(MIME_TYPE_UNCERTML_XML), 
			UwCollectionUtils.<Class<?>>set(UncertMLBinding.class)
		);
	}

	private static Logger LOGGER = Logger.getLogger(UncertMLXmlParser.class);
	
	
	@Override
	public IData parse(InputStream stream, String mimeType) {
		UncertMLBinding result = null;
		XMLParser parser = new XMLParser();
		try {
			IUncertainty uncertainty = parser.parse(stream);
			result = new UncertMLBinding(uncertainty);
		} catch (UncertaintyParserException e) {
			String message = "Error while parsing UncertML input: "+e.getMessage();
			LOGGER.debug(message);
			throw new RuntimeException(message, e);
		}
		return result;
	}

}
