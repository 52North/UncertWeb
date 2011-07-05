package org.n52.wps.io.datahandler.binary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.NetCDFDataBinding;

/**
 * Generator for NetCDF-U outputs
 * 
 * @author staschc
 *
 */
public class NetCDFGenerator extends AbstractBinaryGenerator{

	private static Logger LOGGER = Logger.getLogger(NetCDFGenerator.class);
	private File tmpFile;
	
	@Override
	public OutputStream generate(IData outputData) {
		LargeBufferStream outputStream = new LargeBufferStream();
		if (outputData instanceof NetCDFDataBinding){
			InputStream inputStream = ((NetCDFDataBinding)outputData).getInputStream();
			try {
				IOUtils.copy(inputStream, outputStream);
				inputStream.close();
			} catch (IOException e) {
				throw new RuntimeException("Output cannot be written with NetCDFGenerator!"+e.getMessage(),e);
			}
		}
		else {
			throw new RuntimeException("Output cannot be written with NetCDFGenerator!");
		}
		LOGGER.debug("Created ouput stream in NetCDFGenerator!");
		return outputStream;
	}

	@Override
	public Class<?>[] getSupportedInternalInputDataType() {
		Class<?>[] supportedTypes = {NetCDFDataBinding.class};
		return supportedTypes;
	}

	@Override
	public File generateFile(IData outputData, String mimeType) {
		
		//check whether output data is of supported type
		if(!(outputData instanceof NetCDFDataBinding)){
			throw new RuntimeException("NetCDFBinaryGenerator does not support incoming datatype " + outputData.getClass().getName());
		}
		
		//check whether mimetype is supported
		if (mimeType.equals(UncertWebDataConstants.MIME_TYPE_NETCDFX)){
			throw new RuntimeException("NetCDFBinaryGenerator does not support incoming mimeType " + mimeType);
		}
	
		//create a tempfile for the output
		this.tmpFile = new File("tempFile"+System.currentTimeMillis()+".temp");
		try {
			InputStream inputStream = ((NetCDFDataBinding)outputData).getInputStream();
			OutputStream outputStream = new FileOutputStream(tmpFile);
			IOUtils.copy(inputStream, outputStream);
			inputStream.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Error while creating temporary output file for NetCDF File!",e);
		} catch (IOException e) {
			throw new RuntimeException("Error while creating temporary output file for NetCDF File!",e);
		}
		LOGGER.debug("Created temporary file for ouput stream in NetCDFGenerator!");
		return tmpFile;
	}
	
	@Override
	public void finalize(){
		if (this.tmpFile!=null){
			this.tmpFile.delete();
		}
	}

}
