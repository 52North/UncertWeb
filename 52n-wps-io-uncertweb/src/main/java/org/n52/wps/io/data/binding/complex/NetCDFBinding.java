package org.n52.wps.io.data.binding.complex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.uncertweb.netcdf.NcUwFile;

/**
 * binding class for NetCDFData containing a NetCDF-U file
 *
 * @author staschc
 *
 */
public class NetCDFBinding extends UncertWebIODataBinding {

	private static final long serialVersionUID = 5465758473705293719L;
	private static final Logger log = Logger.getLogger(NetCDFBinding.class);


	/**
	 * payload is NetCDF-U file
	 */
	private NcUwFile payload;

	/**
	 * constructor
	 *
	 * @param data
	 */
	public NetCDFBinding(NcUwFile data) {
		this.payload = data;
	}

	@Override
	public NcUwFile getPayload() {
		return payload;
	}

	@Override
	public Class<?> getSupportedClass() {
		return NcUwFile.class;
	}

	/**
	 * method for retrieving input stream for netcdf file
	 *
	 * @return Returns input stream for netcdf file
	 */
	public InputStream getInputStream() {
		try {
			return new FileInputStream(new File(this.payload.getFile().getLocation()));
		} catch (FileNotFoundException e) {
			log.info("Temporary NetCDF file could not be loaded: "+e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
