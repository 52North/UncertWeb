package org.n52.sos.uncertainty.resp;

import java.io.IOException;

import org.n52.sos.SosConstants;
import org.n52.sos.resp.ISosResponse;

public abstract class AbstractUncertaintyResponse implements ISosResponse {

	/** indicator for compression usage */
	private boolean applyZipCompression;
	
	/** content type for different types than text/xml;subtype="om/2.0.0" */
	private String contentType;

	/**
	 * @return Returns the content type of this response. The returned value is
	 *         the constant CONTENT_TYPE_
	 */
	public String getContentType() {
		if (applyZipCompression) {
			return SosConstants.CONTENT_TYPE_ZIP;
		}
		if (contentType != null) {
			return contentType;
		}
		return SosConstants.CONTENT_TYPE_OM_2;
	}
	
	/** Sets a content type */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return Returns the the length of the content in bytes.
	 * @throws IOException
	 *             if the transformation of the OM document into a byte[] failed
	 */
	public int getContentLength() throws IOException {
		return getByteArray().length;
	}

	/**
	 * @return Returns the response as byte[]
	 * @throws IOException
	 *             if the transformation of the OM document into a byte[] failed
	 */
	public abstract byte[] getByteArray() throws IOException;

	/**
	 * @return Returns true if the response should compressed using zip,
	 *         otherwise false
	 */
	public boolean getApplyGzipCompression() {
		return applyZipCompression;
	}

	/**
	 * Sets true if the response should compressed using zip, otherwise false
	 */
	public void setApplyGzipCompression(boolean applyZipCompression) {
		this.applyZipCompression = applyZipCompression;
	}
}
