package org.n52.wps.io.datahandler.uncertml;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_JSON;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_UNCERTML_JSON;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_UNCERTML;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.OutputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.datahandler.AbstractUwGenerator;
import org.uncertml.exception.UncertaintyEncoderException;
import org.uncertml.exception.UnsupportedUncertaintyTypeException;
import org.uncertml.io.JSONEncoder;
import org.uncertweb.utils.UwCollectionUtils;

public class UncertMLJsonGenerator extends AbstractUwGenerator {

	public UncertMLJsonGenerator() {
		super(
			set(SCHEMA_UNCERTML), 
			set(ENCODING_UTF_8),
			set(MIME_TYPE_UNCERTML_JSON, MIME_TYPE_JSON), 
			UwCollectionUtils.<Class<?>>set(UncertMLBinding.class)
		);
	}

	@Override
	protected void writeToStream(IData data, OutputStream out) {
		try {
			new JSONEncoder().encode(((UncertMLBinding) data).getPayload(), out);
		} catch (UnsupportedUncertaintyTypeException e) {
			throw new RuntimeException(e);
		} catch (UncertaintyEncoderException e) {
			throw new RuntimeException(e);
		}
	}

}
