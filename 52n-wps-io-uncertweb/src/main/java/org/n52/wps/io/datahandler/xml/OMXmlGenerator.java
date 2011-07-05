package org.n52.wps.io.datahandler.xml;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.OMData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.OMDataBinding;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.w3c.dom.Node;

/**
 * Observation generator, producing observations encoded in XML according the
 * UncertWeb O&M profile; wraps {@link XBObservationEncoder}
 * 
 * @author Kiesow
 * 
 */
public class OMXmlGenerator extends AbstractXMLGenerator {

	private static Logger LOGGER = Logger.getLogger(OMXmlGenerator.class);
	private XBObservationEncoder encoder = new XBObservationEncoder();

	/**
	 * generates an observation or collection DOM Node
	 * @param dataBinding
	 * 			data binding containing the observation output
	 * @param mimeType
	 * 			has to be mimetype of O&M UncertWeb profile
	 * @return an ObservationDocument's or an ObservationCollectionDocument's Node
	 */
	@Override
	public Node generateXML(IData dataBinding, String mimeType) {
		
		if (!mimeType.equals(UncertWebDataConstants.MIME_TYPE_OMX)){
			throw new RuntimeException("The data contained neither an observation nor an observation collection.");
		}
		
		Node node = null;
		if (dataBinding instanceof OMDataBinding) {
			XmlObject doc = generateXMLDocument(dataBinding);
			if (doc != null) {
				node = doc.getDomNode();
			}
		}
		return node;
	}
	
	/**
	 * generates an observation or collection document
	 *  
	 * @param dataBinding
	 * 			data binding containing the observation output
	 * @param mimeType
	 * 			has to be mimetype of O&M UncertWeb profile
	 * @return an ObservationDocument or an ObservationCollectionDocument
	 */
	public XmlObject generateXMLDocument(IData dataBinding, String mimeType) {
		if (!mimeType.equals(UncertWebDataConstants.MIME_TYPE_OMX)){
			throw new RuntimeException("The data contained neither an observation nor an observation collection.");
		}
		else {
			return generateXMLDocument(dataBinding);
		}
	}
	
	/**
	 * generates an observation or collection document
	 *  
	 * @param dataBinding
	 * 			data binding containing the observation output
	 * @return an ObservationDocument or an ObservationCollectionDocument
	 */
	public XmlObject generateXMLDocument(IData dataBinding) {
		
		OMData omData = (OMData) dataBinding.getPayload();
		XmlObject doc = null;
		try {
			if (omData.getObservation() != null) {
				doc = encoder
						.encodeObservationDocument((AbstractObservation) omData
								.getObservation());
			} else if (omData.getObservationCollection() != null) {
				doc = encoder.encodeObservationCollectionDocument(omData
						.getObservationCollection());
			} else {
				throw new RuntimeException("The data contained neither an observation nor an observation collection.");
			}
		} catch (Exception e) {
			LOGGER.error("Unable to encode observation: " + e.getMessage());
		}
		
		return doc;
	}

	@Override
	public OutputStream generate(IData outputData) {
		XmlObject xb_doc = generateXMLDocument(outputData);
		LargeBufferStream outputStream = new LargeBufferStream();
		try {
			xb_doc.save(outputStream);
		} catch (IOException e) {
			LOGGER.error("Unable to encode observation: " + e.getMessage());
		}
		return outputStream;
	}

	@Override
	public Class<?>[] getSupportedInternalInputDataType() {
		Class<?>[] supportedClasses = { OMDataBinding.class };
		return supportedClasses;
	}

}

