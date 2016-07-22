package org.n52.wps.io.datahandler.om;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_OMX_JSON;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OMU;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OMU_52N;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OM_V1;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OM_V2;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.OutputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.datahandler.AbstractUwGenerator;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.utils.UwCollectionUtils;

public class OMJsonGenerator extends AbstractUwGenerator {

	public OMJsonGenerator() {
		super(
			set(SCHEMA_OM_V2,SCHEMA_OM_V1,SCHEMA_OMU,SCHEMA_OMU_52N), 
			set(ENCODING_UTF_8), 
			set(MIME_TYPE_OMX_JSON), 
			UwCollectionUtils.<Class<?>> set(OMBinding.class)
		);
	}

	@Override
	protected void writeToStream(IData data, OutputStream out) {
		try {
			OMBinding om = (OMBinding) data;
			JSONObservationEncoder enc = new JSONObservationEncoder();
			if (om.getObservation() != null) {
				enc.encodeObservation(om.getObservation(), out);
			} else {
				enc.encodeObservationCollection(om.getObservationCollection(),
						out);
			}
		} catch (OMEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
