package org.uncertweb.sta.wps.sos;

import net.opengis.sos.x10.InsertObservationDocument;

import org.uncertweb.intamap.om.Observation;
import org.uncertweb.sta.wps.xml.io.enc.ObservationGenerator;

public class InsertObservationDocumentBuilder {

	private static InsertObservationDocumentBuilder singleton;

	public static InsertObservationDocumentBuilder getInstance() {
		if (singleton == null) {
			singleton = new InsertObservationDocumentBuilder();
		}
		return singleton;
	}
	
	private InsertObservationDocumentBuilder(){}
	private ObservationGenerator generator = new ObservationGenerator();
	
	public InsertObservationDocument build(Observation o) {
		InsertObservationDocument insObsDoc = InsertObservationDocument.Factory.newInstance();
		insObsDoc.addNewInsertObservation().set(generator.generateXML(o));
		insObsDoc.getInsertObservation().setAssignedSensorId(o.getSensorModel());
		return insObsDoc;
	}
}
