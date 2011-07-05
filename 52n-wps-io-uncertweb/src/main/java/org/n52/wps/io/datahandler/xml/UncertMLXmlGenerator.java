package org.n52.wps.io.datahandler.xml;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertMLData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.UncertMLDataBinding;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.uncertml.IUncertainty;
import org.uncertml.io.XMLEncoder;
import org.w3c.dom.Node;

/**
 * XMLGenerator for UncertML uncertainties
 * 
 * @author staschc
 *
 */
public class UncertMLXmlGenerator extends AbstractXMLGenerator implements IStreamableGenerator{

	private static Logger LOGGER = Logger.getLogger(UncertMLXmlGenerator.class);
	
	@Override
	public OutputStream generate(IData output) {
		LargeBufferStream outputStream = new LargeBufferStream();
		UncertMLData outputList = (UncertMLData) ((UncertMLDataBinding)output).getPayload();
		IUncertainty uncertainties = outputList.getUncertainties();
		XMLEncoder encoder = new XMLEncoder();
		try {
			encoder.encode(uncertainties,outputStream);
		} catch (Exception e) {
			String message = "Error while encoding UncertML uncertainties: " +e.getMessage();
			LOGGER.info(message);
			throw new RuntimeException(message,e);
		} 
		return outputStream;
	}

	@Override
	public Class<?>[] getSupportedInternalInputDataType() {
		Class<?>[] supportedClasses = { UncertMLDataBinding.class };
		return supportedClasses;
	}

	@Override
	public Node generateXML(IData output, String mimeType) {
		if (!mimeType.equals(UncertWebDataConstants.MIME_TYPE_UNCERTML)){
			throw new RuntimeException("MimeType "+mimeType+" is not supported by UncertMLXMLGenerator!");
		}
		else {
			UncertMLData outputList = (UncertMLData) ((UncertMLDataBinding)output).getPayload();
			IUncertainty uncertainties = outputList.getUncertainties();
			XMLEncoder encoder = new XMLEncoder();
			String encodedUncertainty;
			XmlObject xb_unc=null;
			try {
				encodedUncertainty = encoder.encode(uncertainties);
				xb_unc = XmlObject.Factory.parse(encodedUncertainty);
			} catch (Exception e) {
				String message = "Error while encoding UncertML uncertainties: " +e.getMessage();
				LOGGER.info(message);
				throw new RuntimeException(message,e);
			} 
			return xb_unc.getDomNode();
		}
	}

	@Override
	public void writeToStream(IData outputData, OutputStream os) {
		UncertMLData outputList = (UncertMLData) ((UncertMLDataBinding)outputData).getPayload();
		IUncertainty uncertainties = outputList.getUncertainties();
		XMLEncoder encoder = new XMLEncoder();
		try {
			encoder.encode(uncertainties,os);
		} catch (Exception e) {
			String message = "Error while encoding UncertML uncertainties: " +e.getMessage();
			LOGGER.info(message);
			throw new RuntimeException(message,e);
		} 
	}

}
