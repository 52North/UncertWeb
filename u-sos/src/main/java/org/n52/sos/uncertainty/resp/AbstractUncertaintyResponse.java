package org.n52.sos.uncertainty.resp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.n52.sos.SosConstants;
import org.n52.sos.SosXmlUtilities;
import org.n52.sos.resp.ISosResponse;
import org.n52.sos.uncertainty.SosUncConstants;

public abstract class AbstractUncertaintyResponse implements ISosResponse {

	/** indicator for compression usage */
	private boolean applyZipCompression;

	/**
	 * @return Returns the content type of this response. The returned value is
	 *         the constant CONTENT_TYPE_
	 */
	public String getContentType() {
		if (applyZipCompression) {
			return SosConstants.CONTENT_TYPE_ZIP;
		}
		return SosUncConstants.CONTENT_TYPE_OM2;
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
