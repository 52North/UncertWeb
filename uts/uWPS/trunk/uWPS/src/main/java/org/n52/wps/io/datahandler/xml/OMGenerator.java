package org.n52.wps.io.datahandler.xml;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.OMData;
import org.n52.wps.io.data.binding.complex.OMDataBinding;
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
public class OMGenerator extends AbstractXMLGenerator {

	private static Logger log = Logger.getLogger(OMGenerator.class);
	private XBObservationEncoder encoder = new XBObservationEncoder();

	/**
	 * generates an observation or collection DOM Node
	 * @param dataBinding
	 * @param arg
	 * @return an ObservationDocument's or an ObservationCollectionDocument's Node
	 */
	@Override
	public Node generateXML(IData dataBinding, String arg) {

		if (dataBinding instanceof OMDataBinding) {

			XmlObject doc = generateXMLDocument(dataBinding, arg);

			if (doc != null) {
			
				Node node = doc.getDomNode();
				return node;
			}
		}
		return null;
	}
	
	/**
	 * generates an observation or collection document 
	 * @param dataBinding
	 * @param arg
	 * @return an ObservationDocument or an ObservationCollectionDocument
	 */
	public XmlObject generateXMLDocument(IData dataBinding, String arg) {
		
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
				throw new Exception("The data contained neither an observation nor an observation collection.");
			}
		} catch (Exception e) {
			log.error("Unable to encode observation: " + e.getMessage());
		}
		
		return doc;
	}

	@Override
	public OutputStream generate(IData arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class[] getSupportedInternalInputDataType() {
		Class<?>[] supportedClasses = { OMDataBinding.class };
		return supportedClasses;
	}

}
