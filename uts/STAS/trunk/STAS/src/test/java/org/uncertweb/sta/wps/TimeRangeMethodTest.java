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
package org.uncertweb.sta.wps;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.sta.wps.method.aggregation.impl.MedianAggregation;
import org.uncertweb.sta.wps.method.grouping.impl.IgnoreSpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.impl.TimeRangeGrouping;
import org.uncertweb.sta.wps.testutils.ObservationFactory;
import org.uncertweb.sta.wps.testutils.ProcessTester;

public class TimeRangeMethodTest {

	private static final String PROCESS = "urn:ogc:object:sensor:test:timerange";
	private static final long ONE_MINUTE = 1000 * 60;
	private ProcessTester p = null;

	@Ignore
	@Before
	public void setUp() {
		p = new ProcessTester();
	}

	@Ignore
	@Test(timeout = ONE_MINUTE)
	public void bigObsColl() throws XmlException {
		p.setSpatialAggregationMethod(MedianAggregation.class);
		p.setTemporalAggregationMethod(MedianAggregation.class);
		p.selectAlgorithm(IgnoreSpatialGrouping.class, TimeRangeGrouping.class);
		p.setTimeRange("P1D");

		ObservationFactory f = ObservationFactory.getInstance();
		List<Observation> obs = new LinkedList<Observation>();
		DateTime begin = new DateTime();
		int days = 50;
		int inputCount = days * 24 * 60;
		for (int i = 0; i < inputCount; i++) {
			obs.add(f.createObservation(PROCESS, begin.plus(ONE_MINUTE * i)));
		}
		p.setObservationCollection(new ObservationCollection(obs));
		p.execute();
		ObservationCollection oc = p.getOutput();
		assertEquals(days, oc.size());
	}
}
