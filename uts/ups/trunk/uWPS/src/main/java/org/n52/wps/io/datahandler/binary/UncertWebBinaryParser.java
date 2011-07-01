package org.n52.wps.io.datahandler.binary;

import java.io.InputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;

public class UncertWebBinaryParser extends AbstractBinaryParser {

	@Override
	public Class[] getSupportedInternalOutputDataType() {
		Class[] supportedClasses = {UncertWebDataBinding.class};
		return supportedClasses;
	}

	@Override
	public IData parse(InputStream primaryFile, String mimeType) {
		UncertWebData uData = new UncertWebData(primaryFile, mimeType);
		return new UncertWebDataBinding(uData);
	}

}
