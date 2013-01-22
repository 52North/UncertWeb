package org.n52.sos.uncertainty;

import java.io.ByteArrayOutputStream;

import net.opengis.om.x10.ObservationCollectionDocument;

import org.apache.log4j.Logger;
import org.n52.sos.ISosRequestListener;
import org.n52.sos.Sos1Constants;
import org.n52.sos.Sos1Constants.GetObservationParams;
import org.n52.sos.SosConfigurator;
import org.n52.sos.SosConstants;
import org.n52.sos.Util4Listeners;
import org.n52.sos.ogc.filter.FilterConstants;
import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionLevel;
import org.n52.sos.request.AbstractSosRequest;
import org.n52.sos.request.SosGetObservationRequest;
import org.n52.sos.resp.ExceptionResp;
import org.n52.sos.resp.ISosResponse;
import org.n52.sos.resp.ObservationResponse;
import org.n52.sos.uncertainty.decode.impl.OM2Constants;
import org.n52.sos.uncertainty.ds.pgsql.PGDAOUncertaintyConstants;
import org.n52.sos.uncertainty.ds.pgsql.PGSQLGetObservationDAO;
import org.n52.sos.uncertainty.resp.MeasurementObservationResponse;
import org.n52.sos.uncertainty.resp.UncertaintyObservationResponse;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;

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

				// workaround: remove ResultFilter for a limited number of realisations
				// per sample from request
				int numOfReals = Integer.MIN_VALUE;
				if (sosRequest.getResult() != null
						&& sosRequest.getResult().getOperator().equals(FilterConstants.ComparisonOperator.PropertyIsEqualTo)
						&& sosRequest.getResult().getPropertyName().equals(PGDAOUncertaintyConstants.u_numOfReals)) {
					
					numOfReals = Integer.parseInt(sosRequest.getResult().getValue());
					
					if (numOfReals < 0) {
						OwsExceptionReport se = new OwsExceptionReport(
								ExceptionLevel.DetailedExceptions);
						LOGGER.error("A result filter limit to the number of returned realisations per sample has to be a non-negativ value.");
						se.addCodedException(
								ExceptionCode.NoApplicableCode,
								null,
								"A result filter limit to the number of returned realisations per sample has to be a non-negativ value.");
						return new ExceptionResp(se.getDocument());
					}
					
					// clear result filter
					sosRequest.setResult(null);
				}	
								
				SosObservationCollection obsCollection;

				obsCollection = getDao().getObservation(sosRequest);

				if (sosRequest.getResponseFormat().equals(
						SosConstants.CONTENT_TYPE_OM_2)) {

					// ////////////////////////////////////////////
					// XML response with uncertainties
					IObservationCollection om2obsCol = ((PGSQLGetObservationDAO) getDao())
							.getUncertainObservationCollection(obsCollection, numOfReals);

					StaxObservationEncoder xmlEncoder = new StaxObservationEncoder();
					
					ByteArrayOutputStream staxOutputStream = new ByteArrayOutputStream();
					xmlEncoder.encodeObservationCollection(om2obsCol, staxOutputStream);
					
					if (om2obsCol instanceof UncertaintyObservationCollection) {
						
						response = new UncertaintyObservationResponse (
								staxOutputStream, zipCompression);

					} else if (om2obsCol instanceof MeasurementCollection) {
						
						response = new MeasurementObservationResponse (
								staxOutputStream, zipCompression);

//					} else if (om2obsCol instanceof BooleanObservationCollection) {
//						response = new BooleanObservationResponse(staxOutputStream,
//								zipCompression);
//
//					} else if (om2obsCol instanceof TextObservationCollection) {
//						response = new TextObservationResponse(staxOutputStream,
//								zipCompression);
//
//					} else if (om2obsCol instanceof CategoryObservationCollection) {
//						response = new CategoryObservationResponse(staxOutputStream,
//								zipCompression);
//
//					} else if (om2obsCol instanceof DiscreteNumericObservationCollection) {
//						response = new DiscreteNumericObservationResponse(
//								staxOutputStream, zipCompression);
//
//					} else if (om2obsCol instanceof ReferenceObservationCollection) {
//						response = new ReferenceObservationResponse(staxOutputStream,
//								zipCompression);
//
//					} else if (om2obsCol instanceof ObservationCollection) {
//						response = new ObservationResponse(staxOutputStream,
//								zipCompression);
						
					} else {
						OwsExceptionReport se = new OwsExceptionReport(
								ExceptionLevel.DetailedExceptions);
						LOGGER.error("Received observation collection is not of a supported observation collection type!");
						se.addCodedException(
								ExceptionCode.NoApplicableCode,
								null,
								"Received observation collection is not of a supported observation collection type!");
						return new ExceptionResp(se.getDocument());
					}

				} else if (sosRequest.getResponseFormat().equals(
						SosUncConstants.CONTENT_TYPE_JSON_OM2)) {
					// ////////////////////////////////////////////
					// JSON response with uncertainties
					IObservationCollection om2obsCol = ((PGSQLGetObservationDAO) getDao())
							.getUncertainObservationCollection(obsCollection, numOfReals);

					JSONObservationEncoder jsonEncoder = new JSONObservationEncoder();
					ByteArrayOutputStream jsonOutputStream = new ByteArrayOutputStream();

					if (om2obsCol != null) {
						jsonEncoder.encodeObservationCollection(om2obsCol,
							jsonOutputStream);
					} else {
						return response;
					}

					// create response
					if (om2obsCol.getTypeName().equals(OM2Constants.OBS_COL_TYPE_MEASUREMENT)) {
						
						response = new MeasurementObservationResponse(
								jsonOutputStream, zipCompression);
						((MeasurementObservationResponse) response)
						.setContentType(SosUncConstants.CONTENT_TYPE_JSON_OM2);

					} else if (om2obsCol.getTypeName().equals(OM2Constants.OBS_COL_TYPE_UNCERTAINTY)) {
						
						response = new UncertaintyObservationResponse(
								jsonOutputStream, zipCompression);
						((UncertaintyObservationResponse) response)
								.setContentType(SosUncConstants.CONTENT_TYPE_JSON_OM2);

					} // TODO add further observation types here

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
					// ////////////////////////////////////////////
					// response without uncertainties
					ObservationCollectionDocument xb_obsCol;

					xb_obsCol = (ObservationCollectionDocument) SosConfigurator
								.getInstance().getOmEncoder()
								.createObservationCollection(obsCollection);
					
					response = new ObservationResponse(xb_obsCol,
							zipCompression, Sos1Constants.SERVICEVERSION);
				}

			} catch (OwsExceptionReport se) {
				return new ExceptionResp(se.getDocument());
			} catch (IllegalArgumentException iae) {
				return new ExceptionResp(
						new OwsExceptionReport(iae).getDocument());
			} catch (OMEncodingException omee) {
				return new ExceptionResp(
						new OwsExceptionReport(omee).getDocument());
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
						.equalsIgnoreCase(SosConstants.CONTENT_TYPE_OM_2)
				|| responseFormat
						.equalsIgnoreCase(SosUncConstants.CONTENT_TYPE_JSON_OM2)) {
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
							+ "', '" + SosConstants.CONTENT_TYPE_OM_2 + "', '"
							+ SosUncConstants.CONTENT_TYPE_JSON_OM2 + "' or '"
							+ SosConstants.CONTENT_TYPE_ZIP
							+ "'. Delivered value was: " + responseFormat);
			LOGGER.error("The responseFormat parameter is incorrect!", se);
			throw se;
		}
	}

}
