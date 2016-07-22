package org.n52.sos.uncertainty.resp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.namespace.QName;

import net.opengis.om.x20.OMMeasurementCollectionDocument;

import org.apache.xmlbeans.XmlCursor;
import org.n52.sos.SosXmlUtilities;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.resp.ISosResponse;
import org.n52.sos.uncertainty.decode.impl.OM2Constants;

/**
 * Implementation of the ISosResponse interface for a response to a
 * getObservation request, returning uncertainty enabled O&M 2 Measurement
 * Observations.
 * 
 * @author Christoph Stasch, Martin Kiesow
 */
public class MeasurementObservationResponse extends AbstractUncertaintyResponse
		implements ISosResponse {

	/** The OM2 observation document */
	private OMMeasurementCollectionDocument obsColDoc;

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
	public MeasurementObservationResponse(
			OMMeasurementCollectionDocument obsColDoc, boolean applyZipCompression) {
		this.obsColDoc = obsColDoc;
		XmlCursor cursor = this.obsColDoc.newCursor();
		if (cursor.toFirstChild()) {

			String schemaLocation = OM2Constants.NS_OM2 + " "
					+ OMConstants.SCHEMA_LOCATION_OM + " " + OM2Constants.NS_SF
					+ " " + OM2Constants.SCHEMA_LOCATION_SF
					+ OM2Constants.NS_SAMS + " "
					+ OM2Constants.SCHEMA_LOCATION_SAMS;
			cursor.setAttributeText(new QName(
					"http://www.w3.org/2001/XMLSchema-instance",
					"schemaLocation"), schemaLocation);
		}
		cursor.dispose();
		super.setApplyGzipCompression(applyZipCompression);
	}

	/**
	 * Creates an ObservationResponse from a ByteArray
	 * 
	 * @param obsDoc
	 *            the response doc
	 * @param applyZipCompression
	 *            indicates if zip compression should be applied
	 */
	public MeasurementObservationResponse(
			ByteArrayOutputStream docOutputStream, boolean applyZipCompression) {
		this.outputStream = docOutputStream;
		super.setApplyGzipCompression(applyZipCompression);
	}

	/**
	 * @return Returns the response as byte[]
	 * @throws IOException
	 *             if the transformation of the OM document into a byte[] failed
	 */
	public byte[] getByteArray() throws IOException {

		if (obsColDoc != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			this.obsColDoc.save(baos, SosXmlUtilities.getInstance()
					.getXmlOptions());
			return baos.toByteArray();
		}
		return this.outputStream.toByteArray();
	}
}
