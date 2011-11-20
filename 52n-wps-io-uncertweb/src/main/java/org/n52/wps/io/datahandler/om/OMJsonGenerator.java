package org.n52.wps.io.datahandler.om;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.JSONObservationEncoder;

public class OMJsonGenerator extends AbstractGenerator {

	public OMJsonGenerator() {
		this.supportedIDataTypes.add(OMBinding.class);
	}

	@Override
	public InputStream generateStream(IData data, String mime, String encoding)
			throws IOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			JSONObservationEncoder enc = new JSONObservationEncoder();
			OMBinding om = (OMBinding) data;
			if (om.getObservation() != null) {
				enc.encodeObservation(om.getObservation(), out);
			} else {
				enc.encodeObservationCollection(om.getObservationCollection(),
						out);
			}
			return new ByteArrayInputStream(out.toByteArray());
		} catch (OMEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
