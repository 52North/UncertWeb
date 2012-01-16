package org.n52.wps.io.datahandler.om;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_OMX_XML;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_TEXT_XML;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OM_V2;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OM_V1;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OMU;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.datahandler.AbstractUwGenerator;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.utils.UwCollectionUtils;

/**
 * Observation generator, producing observations encoded in XML according the
 * UncertWeb O&M profile; wraps {@link XBObservationEncoder}
 * 
 * @author Kiesow
 * 
 */
public class OMXmlGenerator extends AbstractUwGenerator {
	private static final Logger log = Logger.getLogger(OMXmlGenerator.class);
	private XBObservationEncoder encoder = new XBObservationEncoder();

	public OMXmlGenerator() {
		super(
			set(SCHEMA_OM_V2,SCHEMA_OM_V1,SCHEMA_OMU), 
			set(ENCODING_UTF_8),
			set(MIME_TYPE_OMX_XML, MIME_TYPE_TEXT_XML), 
			UwCollectionUtils.<Class<?>>set(OMBinding.class)
		);
	}

	@Override
	public void writeToStream(IData outputData, OutputStream os) {

		OMBinding omData = (OMBinding) outputData;
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
			doc.save(os);
		} catch (Exception e) {
			log.error("Unable to encode observation: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

}

