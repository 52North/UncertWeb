package org.n52.wps.io.datahandler.netcdf;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_BINARY;
import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_NETCDF;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_NETCDFX;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.datahandler.AbstractUwGenerator;
import org.uncertweb.utils.UwCollectionUtils;

/**
 * Generator for NetCDF-U outputs
 *
 * @author staschc
 *
 */
public class NetCDFGenerator extends AbstractUwGenerator {

	private static Logger log = Logger.getLogger(NetCDFGenerator.class);

	public NetCDFGenerator() {
		super(
			new HashSet<String>(),
			set(ENCODING_BINARY,ENCODING_UTF_8),
			set(MIME_TYPE_NETCDFX, MIME_TYPE_NETCDF),
			UwCollectionUtils.<Class<?>>set(NetCDFBinding.class)
		);
	}

	public void writeToStream(IData outputData, OutputStream os) {
		if (outputData instanceof NetCDFBinding) {
			InputStream inputStream = ((NetCDFBinding) outputData)
					.getInputStream();
			try {
				IOUtils.copy(inputStream, os);
				inputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(
						"Output cannot be written with NetCDFGenerator!"
								+ e.getMessage(), e);
			}
		} else {
			throw new RuntimeException(
					"Output cannot be written with NetCDFGenerator!");
		}
		log.debug("Created ouput stream in NetCDFGenerator!");
	}

}
