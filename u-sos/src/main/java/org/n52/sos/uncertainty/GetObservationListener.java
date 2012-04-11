package org.n52.sos.uncertainty;

import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x20.OMMeasurementCollectionDocument;
import net.opengis.om.x20.OMUncertaintyObservationCollectionDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ISosRequestListener;
import org.n52.sos.Sos1Constants;
import org.n52.sos.SosConfigurator;
import org.n52.sos.SosConstants;
import org.n52.sos.Util4Listeners;
import org.n52.sos.Sos1Constants.GetObservationParams;
import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionLevel;
import org.n52.sos.request.AbstractSosRequest;
import org.n52.sos.request.SosGetObservationRequest;
import org.n52.sos.resp.ExceptionResp;
import org.n52.sos.resp.ISosResponse;
import org.n52.sos.resp.ObservationResponse;
import org.n52.sos.uncertainty.ds.pgsql.PGSQLGetObservationDAO;
import org.n52.sos.uncertainty.resp.MeasurementObservationResponse;
import org.n52.sos.uncertainty.resp.UncertaintyObservationResponse;
import org.uncertml.exception.UncertaintyEncoderException;
import org.uncertml.exception.UnsupportedUncertaintyTypeException;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

/**
 * class parses and validates the GetObservation requests and forwards them to
 * the GetObservationDAO; after query of Database, class encodes the
 * ObservationResponse; this subclass also queries uncertainties and encodes O&M
 * 2 Observations
 * 
 * @author Christoph Stasch, Martin Kiesow
 * 
 */
public class GetObservationListener extends org.n52.sos.GetObservationListener
		implements ISosRequestListener {

	/** logger */
	private static final Logger LOGGER = Logger
			.getLogger(GetObservationListener.class.getName());

	/**
	 * method receives the GetObservation request and sends back a repsonse
	 * 
	 * @param request
	 *            the XMLObject request (which should be a
	 *            GetObservationDocument)
	 * 
	 * @return Returns the GetObservation response
	 * 
	 */
	public synchronized ISosResponse receiveRequest(AbstractSosRequest request) {

		ISosResponse response = null;

		if (request instanceof SosGetObservationRequest) {
			SosGetObservationRequest sosRequest = (SosGetObservationRequest) request;
			try {

				// check parameters with variable content
				Util4Listeners.checkServiceParameter(sosRequest.getService());
				Util4Listeners.checkSingleVersionParameter(sosRequest
						.getVersion());
				checkOfferingId(sosRequest.getOffering());
				checkObservedProperties(sosRequest.getObservedProperty(),
						sosRequest.getOffering(), Sos1Constants.SERVICEVERSION);
				checkSrsName(sosRequest.getSrsName());

				boolean zipCompression = checkResponseFormat(sosRequest
						.getResponseFormat());

				boolean mobileEnabled = sosRequest.isMobileEnabled();

				SosObservationCollection obsCollection;

				if (mobileEnabled) {
					obsCollection = getDao().getObservationMobile(sosRequest);

				} else {
					obsCollection = getDao().getObservation(sosRequest);
				}

				if (sosRequest.getResponseFormat().equals(
						SosUncConstants.CONTENT_TYPE_OM2)) {

					// response with uncertainties
					IObservationCollection om2obsCol = ((PGSQLGetObservationDAO) getDao())
							.getUncertainObservationCollection(obsCollection);

					XBObservationEncoder encoder = new XBObservationEncoder();
					XmlObject xb_obsCol;

					// encode observation collection
					// TODO add mobile enabled obsCol encoding
					if (mobileEnabled) {
						xb_obsCol = encoder
								.encodeObservationCollectionDocument(om2obsCol);
					} else {
						xb_obsCol = encoder
								.encodeObservationCollectionDocument(om2obsCol);
					}

					// create response
					if (xb_obsCol instanceof OMMeasurementCollectionDocument) {
						response = new MeasurementObservationResponse(
								(OMMeasurementCollectionDocument) xb_obsCol,
								zipCompression);
					} else if (xb_obsCol instanceof OMUncertaintyObservationCollectionDocument) {
						response = new UncertaintyObservationResponse(
								(OMUncertaintyObservationCollectionDocument) xb_obsCol,
								zipCompression);
					}
					// TODO add further uncertainty types here
					// else if (om2obsCol instanceof BooleanObservation) {
					// response = new BooleanObservationResponse(xb_obsCol,
					// zipCompression);
					// } else if (om2obsCol instanceof
					// CategoryObservationCollection) {
					// response = new CategoryObservationResponse(xb_obsCol,
					// zipCompression);
					// } else if (om2obsCol instanceof
					// DiscreteNumericObservationCollection) {
					// response = new
					// DiscreteNumericObservationResponse(xb_obsCol,
					// zipCompression);
					// } else if (om2obsCol instanceof
					// ReferenceObservationCollection) {
					// response = new ReferenceObservationResponse(xb_obsCol,
					// zipCompression);
					// } else if (om2obsCol instanceof
					// TextObservationCollection) {
					// response = new TextObservationResponse(xb_obsCol,
					// zipCompression);
					// }
					else {
						OwsExceptionReport se = new OwsExceptionReport(
								ExceptionLevel.DetailedExceptions);
						LOGGER.error("Received observation collection is not of a supported observation collection type!");
						se.addCodedException(
								ExceptionCode.NoApplicableCode,
								null,
								"Received observation collection is not of a supported observation collection type!");
						return new ExceptionResp(se.getDocument());
					}

				} else {
					// response without uncertainties
					ObservationCollectionDocument xb_obsCol;

					if (mobileEnabled) {
						xb_obsCol = (ObservationCollectionDocument) SosConfigurator
								.getInstance()
								.getOmEncoder()
								.createObservationCollectionMobile(
										obsCollection);
					} else {
						xb_obsCol = (ObservationCollectionDocument) SosConfigurator.getInstance()
								.getOmEncoder()
								.createObservationCollection(obsCollection);
					}
					response = new ObservationResponse(xb_obsCol,
							zipCompression, Sos1Constants.SERVICEVERSION);
				}

			} catch (XmlException xmle) {
				return new ExceptionResp(
						new OwsExceptionReport(xmle).getDocument());
			} catch (UncertaintyEncoderException uee) {
				return new ExceptionResp(
						new OwsExceptionReport(uee).getDocument());
			} catch (UnsupportedUncertaintyTypeException uute) {
				return new ExceptionResp(
						new OwsExceptionReport(uute).getDocument());
			} catch (OwsExceptionReport se) {
				return new ExceptionResp(se.getDocument());
			}
		} else {
			OwsExceptionReport se = new OwsExceptionReport(
					ExceptionLevel.DetailedExceptions);
			LOGGER.error("Received request in GetObservationListener() is not a SosGetObservationRequest!");
			se.addCodedException(
					ExceptionCode.NoApplicableCode,
					null,
					"Received request in GetObservationListener() is not a SosGetObservationRequest!");
			return new ExceptionResp(se.getDocument());
		}

		return response;
	}

	/**
	 * help method to check the result format parameter. If the application/zip
	 * result format is set, true is returned. If not and the value is text/xml;
	 * subtype="OM" false is returned. If neither zip nor OM is set, a
	 * ServiceException with InvalidParameterValue as its code is thrown.
	 * 
	 * @param responseFormat
	 *            String containing the value of the result format parameter
	 * @return boolean true if application/zip is the resultFormat value, false
	 *         if its value is text/xml;subtype="OM"
	 * @throws OwsExceptionReport
	 *             if the parameter value is incorrect
	 */
	protected boolean checkResponseFormat(String responseFormat)
			throws OwsExceptionReport {
		boolean isZipCompr = false;
		if (responseFormat.equalsIgnoreCase(SosConstants.CONTENT_TYPE_OM)
				|| responseFormat
						.equalsIgnoreCase(SosUncConstants.CONTENT_TYPE_OM2)) {
			return isZipCompr;
		}

		else if (responseFormat.equalsIgnoreCase(SosConstants.CONTENT_TYPE_ZIP)) {
			isZipCompr = true;
			return isZipCompr;
		}

		else {
			OwsExceptionReport se = new OwsExceptionReport();
			se.addCodedException(
					OwsExceptionReport.ExceptionCode.InvalidParameterValue,
					GetObservationParams.responseFormat.toString(),
					"The value of the parameter '"
							+ GetObservationParams.responseFormat.toString()
							+ "' must be '" + SosConstants.CONTENT_TYPE_OM
							+ "', '" + SosUncConstants.CONTENT_TYPE_OM2
							+ "' or '" + SosConstants.CONTENT_TYPE_ZIP
							+ "'. Delivered value was: " + responseFormat);
			LOGGER.error("The responseFormat parameter is incorrect!", se);
			throw se;
		}
	}

}
