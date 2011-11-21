package org.n52.sos.uncertainty;

import java.util.Collection;

import net.opengis.om.x10.ObservationCollectionDocument;

import org.apache.log4j.Logger;
import org.n52.sos.ISosRequestListener;
import org.n52.sos.SosConfigurator;
import org.n52.sos.Util4Listeners;
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
import org.omg.CORBA.NameValuePair;

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
    private static final Logger LOGGER = Logger.getLogger(GetObservationListener.class.getName());
	
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
                Util4Listeners.checkSingleVersionParameter(sosRequest.getVersion());
                checkOfferingId(sosRequest.getOffering());
                checkObservedProperties(sosRequest.getObservedProperty(), sosRequest.getOffering());
                checkSrsName(sosRequest.getSrsName());

                boolean zipCompression = checkResponseFormat(sosRequest.getResponseFormat());

                boolean mobileEnabled = sosRequest.isMobileEnabled();

                SosObservationCollection obsCollection;
                ObservationCollectionDocument xb_obsCol;

                if (mobileEnabled) {
                    obsCollection = this.dao.getObservationMobile(sosRequest);
                    
                } else {
                    obsCollection = this.dao.getObservation(sosRequest);
                }

                Collection<NameValuePair> uncCol = ((PGSQLGetObservationDAO) this.dao).getUncCol(obsCollection);

                if (mobileEnabled) {
                	xb_obsCol =
                        SosConfigurator.getInstance().getOmEncoder()
                                .createObservationCollectionMobile(obsCollection);
                } else {
                	xb_obsCol =
                        SosConfigurator.getInstance().getOmEncoder().createObservationCollection(obsCollection);
                }
                

                response = new ObservationResponse(xb_obsCol, zipCompression);
            } catch (OwsExceptionReport se) {
                return new ExceptionResp(se.getDocument());
            }
        } else {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("Received request in GetObservationListener() is not a SosGetObservationRequest!");
            se.addCodedException(ExceptionCode.NoApplicableCode, null,
                    "Received request in GetObservationListener() is not a SosGetObservationRequest!");
            return new ExceptionResp(se.getDocument());
        }

        return response;
	}

}
