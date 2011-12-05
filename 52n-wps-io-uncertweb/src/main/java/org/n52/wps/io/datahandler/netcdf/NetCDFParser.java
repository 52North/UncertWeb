package org.n52.wps.io.datahandler.netcdf;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_BINARY;
import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_NETCDF;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_NETCDFX;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.datahandler.AbstractUwParser;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.utils.UwIOUtils;

import ucar.nc2.NetcdfFile;

/**
 * parser for NetCDF-U inputs
 * 
 * @author staschc
 *
 */
public class NetCDFParser extends AbstractUwParser {
	private static Logger log = Logger.getLogger(NetCDFParser.class);
	
	public NetCDFParser() {
		super(
			new HashSet<String>(), 
			set(ENCODING_BINARY,ENCODING_UTF_8),
			set(MIME_TYPE_NETCDFX, MIME_TYPE_NETCDF), 
			UwCollectionUtils.<Class<?>>set(NetCDFBinding.class)
		);
	}

	@Override
	protected IData parse(InputStream is, String mimeType) throws Exception {
		try {
			File tmp = File.createTempFile("ncInput", ".nc");
			UwIOUtils.saveToFile(tmp, is);
			tmp.deleteOnExit();
			NetcdfFile ncdf = NetcdfFile.open(tmp.getAbsolutePath());
			return new NetCDFBinding(new NetcdfUWFile(ncdf));
		} catch (Exception e) {
			String message = "Error while parsing NetCDF-U file";
			log.warn(message, e);
			throw new RuntimeException(message, e);
		}
	}

}
