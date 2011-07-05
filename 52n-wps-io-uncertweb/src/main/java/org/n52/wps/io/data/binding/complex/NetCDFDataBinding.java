package org.n52.wps.io.data.binding.complex;

import java.io.InputStream;

import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.NetCDFData;

/**
 * binding class for NetCDFData containing a NetCDF-U file
 * 
 * @author staschc
 *
 */
public class NetCDFDataBinding implements IComplexData {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * payload is NetCDF-U file
	 * 
	 */
	private NetCDFData payload;
	
	/**
	 * constructor
	 * 
	 * @param data
	 */
	public NetCDFDataBinding(NetCDFData data){
		this.payload = data;
	}

	@Override
	public Object getPayload() {
		return payload;
	}

	@Override
	public Class<?> getSupportedClass() {
		return NetCDFData.class;
	}
	
	/**
	 * method for retrieving input stream for netcdf file
	 * 
	 * @return Returns input stream for netcdf file
	 */
	public InputStream getInputStream() {
		return this.payload.getInputStream();
	}

}
