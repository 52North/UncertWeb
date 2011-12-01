package org.n52.wps.io.datahandler.binary;

import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_NETCDF;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_NETCDFX;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_NETCDF_U;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.InputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.io.datahandler.AbstractUwParser;
import org.uncertweb.utils.UwCollectionUtils;

public class UncertWebBinaryParser extends AbstractUwParser{
	
	public UncertWebBinaryParser() {
		super(
			set(SCHEMA_NETCDF_U), 
			null,
			set(MIME_TYPE_NETCDFX, MIME_TYPE_NETCDF), 
			UwCollectionUtils.<Class<?>>set(UncertWebDataBinding.class)
		);
	}
	

	@Override
	public IData parse(InputStream primaryFile, String mimeType) {
		UncertWebData uData = new UncertWebData(primaryFile, mimeType);
		return new UncertWebDataBinding(uData);
	}

}
