package org.n52.wps.io.datahandler.uncertml;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_UNCERTML_JSON;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_UNCERTML;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.InputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.datahandler.AbstractUwParser;
import org.uncertml.io.JSONParser;
import org.uncertweb.utils.UwCollectionUtils;

public class UncertMLJsonParser extends AbstractUwParser {

	public UncertMLJsonParser() {
		super(
			set(SCHEMA_UNCERTML), 
			set(ENCODING_UTF_8),
			set(MIME_TYPE_UNCERTML_JSON), 
			UwCollectionUtils.<Class<?>>set(UncertMLBinding.class)
		);
	}

	@Override
	protected IData parse(InputStream is, String mimeType) throws Exception {
		return new UncertMLBinding(new JSONParser().parse(is));		
	}

}
