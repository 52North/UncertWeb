package org.n52.wps.io.data;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Wrapper class for all data types supported in UncertWeb. 
 * These are O&M UncertWeb profile, NetCDF-U and UncertML.
 * 
 * @author staschc
 *
 */
public class UncertWebIOData {
	
	
	private static Logger LOGGER = Logger.getLogger(UncertWebIOData.class);
	
	/**mime type of the data*/
	private String mimeType;
	
	/**
	 * object holding the data that is parsed or should be encoded
	 * 
	 */
	private Object data;
	
	/**
	 * constructor
	 * 
	 * @param data
	 */
	public UncertWebIOData(Object data)throws IOException{
		if (data instanceof OMData){
			this.data=data;
			this.mimeType=UncertWebDataConstants.MIME_TYPE_OMX;
		}
		else if (data instanceof NetCDFData){
			this.data=data;
			this.mimeType=UncertWebDataConstants.MIME_TYPE_NETCDFX;
		}
		else if (data instanceof UncertMLData){
			this.data=data;
			this.mimeType=UncertWebDataConstants.MIME_TYPE_UNCERTML;
		}
		else {
			String message="Error: The datatype for class " + data.getClass().getName() + "is not supported by this extension!!";
			LOGGER.info(message);
			throw new IOException(message);
		}
	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}
	
	
}
