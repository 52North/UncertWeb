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

import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.sta.wps.method.aggregation.impl.ArithmeticMean;
import org.uncertweb.sta.wps.method.aggregation.impl.Median;
import org.uncertweb.sta.wps.method.aggregation.impl.Sum;
import org.uncertweb.sta.wps.method.grouping.impl.ConvexHullGrouping;
import org.uncertweb.sta.wps.method.grouping.impl.OneContainingTimeRangeGrouping;
import org.uncertweb.sta.wps.testutils.ObservationFactory;
import org.uncertweb.sta.wps.testutils.ProcessTester;

public class AggregationMethodTest {

	private ProcessTester p = null;

	@Before
	public void setUp() {
		p = new ProcessTester();
		p.selectAlgorithm(ConvexHullGrouping.class, OneContainingTimeRangeGrouping.class);
		// p.observationCollection(buildOC());
	}

	@SuppressWarnings("unused")
	private ObservationCollection buildOC() {
		LinkedList<Observation> obs = new LinkedList<Observation>();
		ObservationFactory f = ObservationFactory.getInstance();
		DateTime begin = new DateTime();
		long minute = 60 * 1000;
		obs.add(f.createObservation(PROCESS, begin.plus(minute * 1), 1, 5d + Math
				.random(), 52d + Math.random()));
		obs.add(f.createObservation(PROCESS, begin.plus(minute * 2), 2, 5d + Math
				.random(), 52d + Math.random()));
		obs.add(f.createObservation(PROCESS, begin.plus(minute * 3), 3, 5d + Math
				.random(), 52d + Math.random()));
		obs.add(f.createObservation(PROCESS, begin.plus(minute * 4), 4, 5d + Math
				.random(), 52d + Math.random()));
		obs.add(f.createObservation(PROCESS, begin.plus(minute * 5), 10, 5d + Math
				.random(), 52d + Math.random()));
		return new ObservationCollection(obs);
	}

	private static final String PROCESS = "urn:ogc:object:sensor:test";

	@Ignore
	@Test
	public void meanTest() throws XmlException {
		p.setSpatialAggregationMethod(ArithmeticMean.class);
		p.setTemporalAggregationMethod(ArithmeticMean.class);
		p.execute();
		ObservationCollection oc = p.getOutput();
		assertEquals(1, oc.size());
		assertEquals(4, oc.get(0).getResult(), Double.MIN_VALUE);
	}

	@Ignore
	@Test
	public void medianTest() throws XmlException {
		p.setSpatialAggregationMethod(Median.class);
		p.setTemporalAggregationMethod(Median.class);
		p.execute();
		ObservationCollection oc = p.getOutput();
		assertEquals(1, oc.size());
		assertEquals(3, oc.get(0).getResult(), Double.MIN_VALUE);
	}

	@Ignore
	@Test
	public void sumTest() {
		p.setSpatialAggregationMethod(Sum.class);
		p.setTemporalAggregationMethod(Sum.class);
		p.execute();
		ObservationCollection oc = p.getOutput();
		assertEquals(1, oc.size());
		assertEquals(20, oc.get(0).getResult(), Double.MIN_VALUE);
	}
}
