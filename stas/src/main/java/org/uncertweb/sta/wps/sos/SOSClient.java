/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software 
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24, 
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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

/**
 * TODO JavaDoc
 * TODO Asynchronously Feeding
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class SOSClient {

	protected static final Logger log = LoggerFactory
			.getLogger(SOSClient.class);

	public GetObservationRequestBinding registerAggregatedObservations(
			List<Observation> obs, String url, String process,
			Map<String, Object> meta) throws IOException {
		RegisterSensorDocument regSenDoc = RegisterSensorBuilder.getInstance()
				.build(process, obs, meta);
		log.info("Sending RegisterSensor request:\n{}", regSenDoc
				.xmlText(Namespace.defaultOptions()));
		try {
			sendPostRequests(url, regSenDoc);
		} catch (Throwable e) {
			log.warn("Can not register Sensor.");
			throw new RuntimeException(e);
		}
		boolean printed = false;
		log.info("Sending RegisterSensor requests.");
		for (Observation o : obs) {
			InsertObservationDocument insObsDoc = InsertObservationBuilder
					.getInstance().build(o);
			if (!printed) {
				printed = true;
				log.debug("InstertObservation:\n{}", insObsDoc
						.xmlText(Namespace.defaultOptions()));
			}
			try {
				sendPostRequests(url, insObsDoc);
			} catch (Throwable e) {
				log.warn("Can not insert Observation.");
				throw new RuntimeException(e);
			}
		}
		log.info("Generating GetObservation request.");
		GetObservationDocument getObsDoc = GetObservationBuilder.getInstance()
				.build(process, obs);
		return new GetObservationRequestBinding(getObsDoc);
	}

	protected void sendPostRequests(String url, XmlObject doc)
			throws IOException {
		try {
			XmlObject xml = XmlObject.Factory.parse(Utils
					.sendPostRequest(url, doc.xmlText()));

			if (xml instanceof RegisterSensorResponseDocument) {
				log.info("RegisterSensor successfull: {}", ((RegisterSensorResponseDocument) xml)
						.getRegisterSensorResponse().getAssignedSensorId());
			} else if (xml instanceof InsertObservationResponseDocument) {
				log.info("InsertObservation successfull: {}", ((InsertObservationResponseDocument) xml)
						.getInsertObservationResponse()
						.getAssignedObservationId());
			} else if (xml instanceof ExceptionReportDocument) {
				ExceptionReportDocument ex = (ExceptionReportDocument) xml;
				String errorKey = ex.getExceptionReport().getExceptionArray(0)
						.getExceptionCode();
				String message = ex.getExceptionReport().getExceptionArray(0)
						.getExceptionTextArray(0);
				throw new RuntimeException(new ExceptionReport(message,
						errorKey));
			}
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
	}

}
