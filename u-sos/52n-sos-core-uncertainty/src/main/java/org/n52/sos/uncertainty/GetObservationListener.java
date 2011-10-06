package org.n52.sos.uncertainty;

import org.n52.sos.ISosRequestListener;
import org.n52.sos.request.AbstractSosRequest;
import org.n52.sos.resp.ISosResponse;

/**
 * class parses and validates the GetObservation requests and forwards them to
 * the GetObservationDAO; after query of Database, class encodes the
 * ObservationResponse (thru using the OMEncoder)
 * 
 * @author Christoph Stasch, Martin Kiesow
 * 
 */
public class GetObservationListener extends org.n52.sos.GetObservationListener implements ISosRequestListener {
	
	//TODO check comments 

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
    	return null;
    }
	
}
