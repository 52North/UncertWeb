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
package org.uw.viss.core;

import static org.junit.Assert.assertEquals;
import static org.uncertweb.viss.mongo.resource.AbstractMongoResource.getExtent;

import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;
import org.uncertweb.viss.core.resource.time.IrregularTemporalInstants;
import org.uncertweb.viss.core.resource.time.IrregularTemporalIntervals;
import org.uncertweb.viss.core.resource.time.MixedTemporalExtent;
import org.uncertweb.viss.core.resource.time.RegularTemporalInstants;
import org.uncertweb.viss.core.resource.time.RegularTemporalIntervals;
import org.uncertweb.viss.core.resource.time.TemporalExtent;
import org.uncertweb.viss.core.resource.time.TemporalInstant;
import org.uncertweb.viss.core.resource.time.TemporalInterval;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.mongo.resource.AbstractMongoResource;

public class TemporalExtentTest {

	@Test
	public void test() {

		DateTime begin = new DateTime();
		Set<DateTime> instants = Utils.set(begin);
		for (int i = 0; i <= 1000; ++i) {
			instants.add(begin.plusHours(i));
		}
		assertEquals(RegularTemporalInstants.class, getExtent(instants, null)
				.getClass());
		instants.add(begin.plusSeconds(1));
		assertEquals(IrregularTemporalInstants.class, getExtent(instants, null)
				.getClass());

		Duration d = new Duration(1000 * 60 * 60);
		Set<Interval> intervals = Utils.set();
		for (int i = 0; i < 200; ++i) {
			DateTime s = begin;
			for (int j = 0; j <= i; ++j) {
				s = s.plus(d);
			}
			intervals.add(new Interval(s, d));
		}
		assertEquals(RegularTemporalIntervals.class, getExtent(null, intervals)
				.getClass());

		intervals.add(new Interval(begin, d.plus(1)));
		assertEquals(IrregularTemporalIntervals.class,
				getExtent(null, intervals).getClass());

		assertEquals(TemporalExtent.NO_TEMPORAL_EXTENT, getExtent(null, null));
		assertEquals(TemporalExtent.NO_TEMPORAL_EXTENT,
				getExtent(Utils.<DateTime> set(), Utils.<Interval> set()));
		assertEquals(MixedTemporalExtent.class, getExtent(instants, intervals)
				.getClass());
		assertEquals(TemporalInstant.class,
				AbstractMongoResource
						.getExtent(Utils.set(new DateTime()), null).getClass());
		assertEquals(
				TemporalInterval.class,
				AbstractMongoResource.getExtent(null,
						Utils.set(new Interval(begin, begin.plus(d))))
						.getClass());
	}
}
