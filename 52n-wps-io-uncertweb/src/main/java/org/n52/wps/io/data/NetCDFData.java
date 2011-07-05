package org.n52.wps.io.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;

import ucar.nc2.NetcdfFile;


/**
 * wrapper class for NetCDF-U files 
 * 
 * @author staschc
 *
 */
public class NetCDFData {
	
	private static Logger LOGGER = Logger.getLogger(NetCDFData.class);
	
	/**
	 * NetCDF file following the UncertWeb conventions
	 */
	private NetcdfUWFile file;
	
	/**
	 * constructor
	 * 
	 * @param filep
	 * 			NetCDF file following the UncertWeb conventions
	 */
	public NetCDFData(NetcdfUWFile filep){
		this.file=filep;
	}
	
	/**
	 *
	 * @return Returns NetCDF file following the UncertWeb conventions
	 */
	public NetcdfUWFile getNetcdfUWFile(){
		return this.file;
	}
	
	/**
	 * constructor with inputstream and mimeType
	 * 
	 * @param stream
	 * @param mimeType
	 */
	public NetCDFData(InputStream stream){
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String fileSeparator = System.getProperty("file.separator");			
		String fileName = tmpDirPath + fileSeparator + "ncInput"+System.currentTimeMillis()+".nc";
		try {
			FileOutputStream fOut = new FileOutputStream(fileName);
			int i = stream.read();
		
			while(i != -1){			
			fOut.write(i);
			i = stream.read();
			}
		
			stream.close();
			fOut.flush();
			fOut.close();
		
			NetcdfFile ncdf = NetcdfFile.open(fileName);
			this.file = new NetcdfUWFile(ncdf);
		} catch (FileNotFoundException e) {
			String message = "Error while parsing NetCDF-U file " + e.getMessage();
			LOGGER.info(message);
		} catch (IOException e) {
			String message = "Error while parsing NetCDF-U file " + e.getMessage();
			LOGGER.info(message);
			e.printStackTrace();
		} catch (NetcdfUWException e) {
			String message = "Error while parsing NetCDF-U file " + e.getMessage();
			LOGGER.info(message);
		}
	}
	
	/**
	 * method for retrieving input stream for netcdf file
	 * 
	 * @return Returns input stream for netcdf file
	 */
	public InputStream getInputStream(){
		File javaFile = new File(this.file.getNetcdfFile().getLocation());
		InputStream is = null;
		try {
			is = new FileInputStream(javaFile);
		} catch (FileNotFoundException e) {
			LOGGER.info("Temporary NetCDF file could not be loaded: "+e.getMessage());
		}
		return is;
	}
	
	@Override
	public void finalize(){
		if (this.file!=null){
			try {
				this.file.getNetcdfFile().close();
			} catch (IOException e) {
				LOGGER.info("NetCDF file could not be deleted: "+e.getMessage());
			}
		}
	}
	
	
	
}
