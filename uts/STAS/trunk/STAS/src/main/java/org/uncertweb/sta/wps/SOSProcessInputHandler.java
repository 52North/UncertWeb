package org.uncertweb.sta.wps;

import java.util.List;
import java.util.Map;

import net.opengis.sos.x10.GetObservationDocument;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.AlgorithmParameterException;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.sos.GetObservationRequestCache;

public class SOSProcessInputHandler extends ProcessInputHandler<ObservationCollection> {

	@Override
	protected ObservationCollection processInputs(Map<String, List<IData>> inputs) {
		
		String sosUrl = Constants.Process.Inputs.Common.SOS_URL.handle(inputs);
		
		GetObservationDocument sosReq = Constants.Process.Inputs.Common.SOS_REQUEST.handle(inputs);
		
		if (sosUrl == null) {
			throw new AlgorithmParameterException("No Source SOS Url.");
		}
		
		return GetObservationRequestCache.getInstance().getObservationCollection(sosUrl, sosReq, false);
	}

}
