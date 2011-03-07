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
package org.uncertweb.sta.wps.testutils;

import static org.uncertweb.intamap.utils.Namespace.defaultOptions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.sos.x10.InsertObservationDocument;
import net.opengis.sos.x10.InsertObservationDocument.InsertObservation;
import net.opengis.sos.x10.InsertObservationResponseDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.intamap.om.ObservationTimeInstant;
import org.uncertweb.intamap.om.SamplingPoint;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.RandomStringGenerator;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.xml.binding.ObservationCollectionBinding;
import org.uncertweb.sta.wps.xml.io.enc.ObservationCollectionGenerator;
import org.uncertweb.sta.wps.xml.io.enc.ObservationGenerator;

/**
 * TODO JavaDoc
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ObservationFactory {
	private static final String SAMPLED_FEATURE = Constants.NULL_URN;
	private static final String OBSERVATION_ID_PREFIX = "o_";
	private static final String FEATURE_OF_INTEREST_PREFIX = "foi_";
	private static final String OBSERVED_PROPERTY = "urn:ogc:def:phenomenon:ME:test";
	private static final int SRID = 4326;
	private static final String UOM = "m";
	private static ObservationFactory singleton;

	public static ObservationFactory getInstance() {
		if (singleton == null)
			singleton = new ObservationFactory();
		return singleton;
	}

	private ObservationFactory() {}

	private int observationCount = 0;

	public Observation createObservation(String process, String obsProp, DateTime time, double result, double lat, double lon) {
		String id = OBSERVATION_ID_PREFIX + String.valueOf(observationCount);
		String foiId = FEATURE_OF_INTEREST_PREFIX + String.valueOf(observationCount);
		ISamplingFeature f = new SamplingPoint(lat, lon, SAMPLED_FEATURE, foiId, foiId);
		f.getLocation().setSRID(SRID);
		observationCount++;
		return new Observation(id, result, f, null, obsProp, process, new ObservationTimeInstant(time), UOM);
	}
	
	public Observation createObservation(String process, DateTime time, double result, double lat, double lon) {
		return createObservation(process, OBSERVED_PROPERTY, time, result, lat, lon);
	}
	
	public Observation createObservation(String process, DateTime time, double lat, double lon) {
		return createObservation(process, time, Math.random(), lat, lon);
	}
	
	public Observation createObservation(String process, DateTime time) {
		return createObservation(process, time, 5d + Math.random(), 52d + Math.random());
	}
	
	public ObservationCollectionDocument toXml(List<Observation> obs) {
		ObservationCollectionGenerator g = new ObservationCollectionGenerator();
		ObservationCollectionBinding b = new ObservationCollectionBinding(new ObservationCollection(obs));
		return g.generateXML(b);
	}
	
	public ObservationCollection buildCollection(String url, int fois, int obsPerFoi, double latMin, double latMax, double lonMin, double lonMax) {
		String uniqueString = RandomStringGenerator.getInstance().generate(20);
		LinkedList<Observation> obs = new LinkedList<Observation>();
		DateTime begin = new DateTime();
		int obsId = 0;
		String process = "urn:ogc:object:sensor:test:" + uniqueString;
		String obsProp = "urn:ogc:def:phenomenon:ME:test:" + uniqueString;
		String foiPrefix = "foi_" + uniqueString + "_";

		for (int foi = 0; foi <= fois; foi++) {
			String foiId = foiPrefix + String.valueOf(foi);
			ISamplingFeature f = new SamplingPoint(Utils.randomBetween(latMin, latMax), Utils.randomBetween(lonMin, lonMax), Constants.NULL_URN, foiId, foiId);
			f.getLocation().setSRID(SRID);
			for (int o = 0; o < obsPerFoi; o++) {
				obs.add(new Observation("o_" + obsId++, Utils.randomBetween(0.0, 100.0), f, null, obsProp, process,
						new ObservationTimeInstant(begin.plusMinutes(o)), "m"));
			}
		}
		
//		RegisterSensorDocument regSenDoc = RegisterSensorDocument.Factory.newInstance();
		//TODO
		ObservationGenerator obsGen = new ObservationGenerator();
		Logger log = LoggerFactory.getLogger(this.getClass());
		for (Observation o : obs) {
			InsertObservationDocument insObsDoc = InsertObservationDocument.Factory.newInstance();
			InsertObservation insObs = insObsDoc.addNewInsertObservation();
			insObs.setAssignedSensorId(process);
			insObs.setObservation(obsGen.generateXML(o).getObservation());
			try {
				XmlObject xo = XmlObject.Factory.parse(Utils.sendPostRequest(url, insObsDoc.xmlText(defaultOptions())));
				if (!(xo instanceof InsertObservationResponseDocument)) {
					throw new RuntimeException(xo.xmlText(defaultOptions()));
				}
				log.info("Inserted Observation {}.", o.getId());
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (XmlException e) {
				throw new RuntimeException(e);
			}
		}
		
		
		return new ObservationCollection(obs);
	}
	
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		ObservationCollectionBinding obs = new ObservationCollectionBinding(
				ObservationFactory.getInstance().buildCollection(
						"http://localhost:8080/sos/sos", 10, 2000, 52.0D,
						53.0D, 5.0D, 6.0D));		
		
		
		System.out.println("Generated and written "+ obs.getPayload().size()+" Observations in "+Utils.timeElapsed(start));
	}
	
}
