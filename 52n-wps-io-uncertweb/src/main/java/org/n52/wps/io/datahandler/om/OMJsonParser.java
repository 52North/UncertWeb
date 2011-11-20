package org.n52.wps.io.datahandler.om;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_OMX_JSON;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OM_V2;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.datahandler.AbstractUwParser;
import org.uncertweb.api.om.io.JSONObservationParser;
import org.uncertweb.utils.UwCollectionUtils;

public class OMJsonParser extends AbstractUwParser {

	public OMJsonParser() {
		super(
			set(SCHEMA_OM_V2), 
			set(ENCODING_UTF_8),
			set(MIME_TYPE_OMX_JSON), 
			UwCollectionUtils.<Class<?>>set(OMBinding.class)
		);
	}

	@Override
	protected IData parse(InputStream is, String mimeType) throws Exception {
		return new OMBinding(new JSONObservationParser().parse(IOUtils.toString(is)));
	}

}
