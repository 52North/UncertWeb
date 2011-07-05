package org.n52.wps.io.datahandler.binary;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.NetCDFData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.NetCDFDataBinding;

/**
 * parser for NetCDF-U inputs
 * 
 * @author staschc
 *
 */
public class NetCDFParser extends AbstractBinaryParser{
	
	private static Logger LOGGER = Logger.getLogger(NetCDFParser.class);

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		Class<?>[] supportedClasses = {NetCDFDataBinding.class};
		return supportedClasses;
	}

	@Override
	public IData parse(InputStream is, String mimeType) {
		if (!mimeType.equalsIgnoreCase(UncertWebDataConstants.MIME_TYPE_NETCDFX)){
			LOGGER.error("MimeType "+mimeType+" is not supported by this parser!!");
			return null;
		}
		else {
			NetCDFData ncdfData = new NetCDFData(is);
			return new NetCDFDataBinding(ncdfData);
		}
	}

}
