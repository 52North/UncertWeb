package org.n52.sos.uncertainty.resp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.namespace.QName;

import net.opengis.om.x20.OMObservationDocument;

import org.apache.xmlbeans.XmlCursor;
import org.n52.sos.SosXmlUtilities;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.resp.ISosResponse;
import org.n52.sos.uncertainty.decode.impl.OM2Constants;

/**
 * Implementation of the ISosResponse interface for a response to a
 * getObservation request, returning a single uncertainty enabled O&M 2
 * Observation.
 * 
 * @author Christoph Stasch, Martin Kiesow
 */
public class SingleObservationResponse extends AbstractUncertaintyResponse implements ISosResponse {

	/** The OM2 observation document */
	private OMObservationDocument obsDoc;

	/** output stream of document */
	private ByteArrayOutputStream outputStream;

	/**
	 * Creates an ObservationResponse from a passed ObservationDoc.
	 * 
	 * @param obsDoc
	 *            the response doc
	 * @param applyZipCompression
	 *            indicates if zip compression should be applied
	 */
	public SingleObservationResponse(OMObservationDocument obsDoc,
			boolean applyZipCompression) {
		this.obsDoc = obsDoc;
		XmlCursor cursor = this.obsDoc.newCursor();
		if (cursor.toFirstChild()) {

			String schemaLocation = OM2Constants.NS_OM2 + " "
					+ OMConstants.SCHEMA_LOCATION_OM + " " + OM2Constants.NS_SF
					+ " " + OM2Constants.SCHEMA_LOCATION_SF + OM2Constants.NS_SAMS
					+ " " + OM2Constants.SCHEMA_LOCATION_SAMS;
			cursor.setAttributeText(new QName(
					"http://www.w3.org/2001/XMLSchema-instance",
					"schemaLocation"), schemaLocation);
		}
		cursor.dispose();
		super.setApplyGzipCompression(applyZipCompression);
	}

	/**
	 * Creates an ObservationResponse from a passed ObservationDoc.
	 * 
	 * @param obsDoc
	 *            the response doc
	 * @param applyZipCompression
	 *            indicates if zip compression should be applied
	 */
	public SingleObservationResponse(ByteArrayOutputStream docOutputStream,
			boolean applyZipCompression) {
		this.outputStream = docOutputStream;
		super.setApplyGzipCompression(applyZipCompression);
	}
	
	/**
	 * @return Returns the response as byte[]
	 * @throws IOException
	 *             if the transformation of the OM document into a byte[] failed
	 */
	public byte[] getByteArray() throws IOException {

		if (obsDoc != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			this.obsDoc.save(baos, SosXmlUtilities.getInstance()
					.getXmlOptions());
			return baos.toByteArray();
		}
		return this.outputStream.toByteArray();
	}
}
