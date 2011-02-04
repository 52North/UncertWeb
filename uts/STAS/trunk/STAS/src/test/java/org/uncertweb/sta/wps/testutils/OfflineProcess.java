package org.uncertweb.sta.wps.testutils;

import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.sta.wps.GenericObservationAggregationProcess;
import org.uncertweb.sta.wps.method.grouping.spatial.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.TemporalGrouping;


public class OfflineProcess extends GenericObservationAggregationProcess {

	public static final String OC_ID = "ObservationCollection";
	
	public OfflineProcess(String identifier, String title, Class<? extends SpatialGrouping> sg, Class<? extends TemporalGrouping> tg) {
		super(identifier, title, sg, tg);
	}

	private static final String SOS_URL = "http://novalidaddress.org:8080/SOS/sos?asdf";
	
	@Override
	protected ObservationCollection getObservationCollection(Map<String, List<IData>> inputs) {
		return ProcessTester.getInstance().getObservationCollection();
	}
	
	protected String getSOSUrl(Map<String, List<IData>> inputs) {
		return SOS_URL;
	}
	
}
