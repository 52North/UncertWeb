package org.n52.wps.io.datahandler.uncertml;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_UNCERTML_XML;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_UNCERTML;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.datahandler.AbstractUwGenerator;
import org.uncertml.IUncertainty;
import org.uncertml.io.XMLEncoder;
import org.uncertweb.utils.UwCollectionUtils;

/**
 * XMLGenerator for UncertML uncertainties
 * 
 * @author staschc
 *
 */
public class UncertMLXmlGenerator extends AbstractUwGenerator {

	private static Logger LOGGER = Logger.getLogger(UncertMLXmlGenerator.class);
	
	
	public UncertMLXmlGenerator() {
		super(
			set(SCHEMA_UNCERTML), 
			set(ENCODING_UTF_8),
			set(MIME_TYPE_UNCERTML_XML), 
			UwCollectionUtils.<Class<?>>set(UncertMLBinding.class)
		);
	}

	@Override
	public void writeToStream(IData outputData, OutputStream os) {
		IUncertainty uncertainty = ((UncertMLBinding) outputData).getPayload();
		XMLEncoder encoder = new XMLEncoder();
		try {
			encoder.encode(uncertainty,os);
		} catch (Exception e) {
			String message = "Error while encoding UncertML uncertainties: " +e.getMessage();
			LOGGER.info(message);
			throw new RuntimeException(message,e);
		} 
	}

}
