package org.n52.wps.io.datahandler;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.NetCDFData;
import org.n52.wps.io.data.UncertMLData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.UncertWebIOData;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.datahandler.binary.NetCDFGenerator;
import org.n52.wps.io.datahandler.binary.NetCDFParser;
import org.n52.wps.io.datahandler.xml.OMXmlParser;
import org.n52.wps.io.datahandler.xml.UncertMLXmlParser;


/**
 * generic parser for UncertWeb IO data
 * 
 * @author staschc
 *
 */
public class UncertWebIODataParser extends AbstractUncertWebDataParser{
			 
	private static Logger LOGGER = Logger.getLogger(NetCDFGenerator.class);
	
	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		Class<?>[] supportedClasses = {UncertWebIODataBinding.class};
		return supportedClasses;
	}

	@Override
	public IData parse(InputStream is, String mimeType) {
		Object data = null;
		if (mimeType.equals(UncertWebDataConstants.MIME_TYPE_NETCDFX)){
			data = new NetCDFParser().parse(is, mimeType).getPayload();
			
		}
		else if (mimeType.equals(UncertWebDataConstants.MIME_TYPE_UNCERTML)){
			data = new UncertMLXmlParser().parse(is, mimeType).getPayload();
		}
		else if (mimeType.equals(UncertWebDataConstants.MIME_TYPE_OMX)){
			data = new OMXmlParser().parse(is, mimeType).getPayload();
		}
		else {
			String message = "Error: MimeType "+  mimeType +" is not supported by the UncertWebIODataParser!";
			LOGGER.debug(message);
			throw new RuntimeException(message);
		}
		UncertWebIOData uwData;
		try {
			uwData = new UncertWebIOData(data);
		} catch (IOException e) {
			LOGGER.info(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
		return new UncertWebIODataBinding(uwData);
	}

}
