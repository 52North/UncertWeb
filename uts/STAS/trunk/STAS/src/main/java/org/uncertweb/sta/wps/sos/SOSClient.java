package org.uncertweb.sta.wps.sos;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.sos.x10.InsertObservationDocument;
import net.opengis.sos.x10.InsertObservationResponseDocument;
import net.opengis.sos.x10.RegisterSensorDocument;
import net.opengis.sos.x10.RegisterSensorResponseDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.server.ExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.xml.binding.GetObservationRequestBinding;

public class SOSClient {
	
	protected static final Logger log = LoggerFactory.getLogger(SOSClient.class);
	
	public GetObservationRequestBinding registerAggregatedObservations(List<Observation> obs, String url, String process, Map<String,String> meta) throws IOException {
		RegisterSensorDocument regSenDoc = RegisterSensorDocumentBuilder.getInstance().build(process, obs, meta);
//		log.debug("RegisterSensor Request:\n{}", regSenDoc.xmlText(defaultOptions()));
		log.info("Sending RegisterSensor request.");
		try {
			sendPostRequests(url, regSenDoc);
		} catch(Throwable e) {
			log.warn("Can not register Sensor.");
			throw new RuntimeException(e);
		}
		boolean printed =false;
		log.info("Sending RegisterSensor requests.");
		for (Observation o : obs) {
			InsertObservationDocument insObsDoc = InsertObservationDocumentBuilder.getInstance().build(o);
			if (!printed) {
				printed = true;
				log.debug("InstertObservation:\n{}", insObsDoc.xmlText(Namespace.defaultOptions()));
			}
			try {
				sendPostRequests(url, insObsDoc);
			} catch(Throwable e) {
				log.warn("Can not insert Observation.");
				throw new RuntimeException(e);
			}
		}
		log.info("Generating GetObservation request.");
		GetObservationDocument getObsDoc = GetObservationDocumentBuilder.getInstance().build(process, obs);
//		log.debug("GetObservation Request:\n{}", getObsDoc.xmlText(Namespace.defaultOptions()));
		return new GetObservationRequestBinding(getObsDoc);
	}

	protected void sendPostRequests(String url, XmlObject doc) throws IOException {
		try {
			XmlObject xml = XmlObject.Factory.parse(Utils.sendPostRequest(url, doc.xmlText()));

			if (xml instanceof RegisterSensorResponseDocument) {
				log.info("RegisterSensor successfull: {}", 
						((RegisterSensorResponseDocument) xml).getRegisterSensorResponse().getAssignedSensorId());
			} else if (xml instanceof InsertObservationResponseDocument) {
				log.info("InsertObservation successfull: {}", 
						((InsertObservationResponseDocument) xml).getInsertObservationResponse().getAssignedObservationId());
			} else if (xml instanceof ExceptionReportDocument) {
				ExceptionReportDocument ex = (ExceptionReportDocument) xml;
				String errorKey = ex.getExceptionReport().getExceptionArray(0).getExceptionCode();
				String message = ex.getExceptionReport().getExceptionArray(0).getExceptionTextArray(0);
				throw new RuntimeException(new ExceptionReport(message, errorKey));
			}
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
	}
	
}
