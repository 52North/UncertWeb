package org.n52.wps.io.datahandler.xml;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_UNCERTML_XML;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_UNCERTML;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.InputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.io.datahandler.AbstractUwParser;
import org.uncertweb.utils.UwCollectionUtils;


public class UncertMLParser extends AbstractUwParser {

	
	
	public UncertMLParser() {
		super(set(SCHEMA_UNCERTML), set(ENCODING_UTF_8),
				set(MIME_TYPE_UNCERTML_XML), UwCollectionUtils
						.<Class<?>> set(UncertWebDataBinding.class));
	}
	


	@Override
	public IData parse(InputStream primaryFile, String mimeType) {
		UncertWebData uData = new UncertWebData(primaryFile, mimeType);
		return new UncertWebDataBinding(uData);
	}

}
