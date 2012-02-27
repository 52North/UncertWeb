package org.uncertml.distribution.randomvariable;

import java.net.URL;

/**
 * class represents a reference to optional samples of a field; currently either
 * U-O&M or NetCDF-U are supported
 * 
 * @author staschc
 * 
 */
public class SampleReference {

	/** mimeType; has to be one of the mimetypes for U-O&M or NetCDF-U */
	private String mimeType;

	/** reference to samples */
	private URL reference;

	/**
	 * constructor
	 * 
	 * @param mimeType
	 * 			mimeType of samples
	 * @param reference
	 * 			reference to samples
	 */
	public SampleReference(String mimeType, URL reference) {
		checkMimeType(mimeType);
		this.mimeType = mimeType;
		this.reference = reference;
	}

	/**
	 * helper method checks whether mimeType is supported (true) or not (false)
	 * 
	 * @param mimeType2
	 * 			mimeType that should be checked
	 * @return true, if mimeType is supported, false if not.
	 */
	private boolean checkMimeType(String mimeType2) {
		return (mimeType.equals("application/x-netcdf")
				|| mimeType.equals("application/netcdf")
				|| mimeType.equals("application/x-om-u")
				|| mimeType.equals("application/x-om-u+json") || mimeType
				.equals("application/x-om-u+xml"));

	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType
	 *            the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * @return the reference
	 */
	public URL getReference() {
		return reference;
	}

	/**
	 * @param reference
	 *            the reference to set
	 */
	public void setReference(URL reference) {
		this.reference = reference;
	}

}
