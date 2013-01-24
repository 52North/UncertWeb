package org.n52.wps.io.datahandler.om;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_OMX_XML;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_TEXT_XML;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OMU;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OMU_52N;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OM_V1;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OM_V2;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.datahandler.AbstractUwGenerator;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationEncoder;
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
	private StaxObservationEncoder encoder = new StaxObservationEncoder();

	public OMXmlGenerator() {
		super(
			set(SCHEMA_OM_V2,SCHEMA_OM_V1,SCHEMA_OMU, SCHEMA_OMU_52N), 
			set(ENCODING_UTF_8),
			set(MIME_TYPE_OMX_XML, MIME_TYPE_TEXT_XML), 
			UwCollectionUtils.<Class<?>>set(OMBinding.class)
		);
	}

	@Override
	public void writeToStream(IData outputData, OutputStream os) {

		OMBinding omData = (OMBinding) outputData;
		try {
			if (omData.getObservation() != null) {
				encoder.encodeObservation(omData.getObservation(),os);
			} else if (omData.getObservationCollection() != null) {
				encoder.encodeObservationCollection(omData
						.getObservationCollection(),os);
			} else {
				throw new RuntimeException("The data contained neither an observation nor an observation collection.");
			}
		} catch (Exception e) {
			log.error("Unable to encode observation: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

}

