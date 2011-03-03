package org.uncertweb.sta.wps.sos;

import java.util.HashSet;
import java.util.List;

import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.sos.x10.GetObservationDocument.GetObservation;
import net.opengis.sos.x10.ResponseModeType;

import org.uncertweb.intamap.om.Observation;
import org.uncertweb.sta.utils.Constants;

public class GetObservationDocumentBuilder {

	private static GetObservationDocumentBuilder singleton;

	public static GetObservationDocumentBuilder getInstance() {
		if (singleton == null) {
			singleton = new GetObservationDocumentBuilder();
		}
		return singleton;
	}
	
	private GetObservationDocumentBuilder(){}
	
	public GetObservationDocument build(String process, List<Observation> obs) {
		GetObservationDocument getObsDoc = GetObservationDocument.Factory.newInstance();
		GetObservation getObs = getObsDoc.addNewGetObservation();
		getObs.setService(Constants.Sos.SERVICE_NAME);
		getObs.setVersion(Constants.Sos.SERVICE_VERSION);
		getObs.setResultModel(Constants.Sos.MEASUREMENT_RESULT_MODEL);
		getObs.setOffering(Constants.Sos.AGGREGATION_OFFERING_ID);
		getObs.setResponseFormat(Constants.Sos.OBSERVATION_OUTPUT_FORMAT);
		getObs.setResponseMode(ResponseModeType.INLINE);
		getObs.addNewProcedure().setStringValue(process);
		
		HashSet<String> obsProps = new HashSet<String>();
		for (Observation o : obs) {
			obsProps.add(o.getObservedProperty());
		}
		
		for (String s : obsProps) {
			getObs.addNewObservedProperty().setStringValue(s);
		}
		
		return getObsDoc;
	}
}
