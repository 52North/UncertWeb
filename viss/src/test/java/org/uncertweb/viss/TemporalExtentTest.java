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
package org.uncertweb.viss;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.resource.time.AbstractTemporalExtent;
import org.uncertweb.viss.core.resource.time.IrregularTemporalInstants;
import org.uncertweb.viss.core.resource.time.IrregularTemporalIntervals;
import org.uncertweb.viss.core.resource.time.MixedTemporalExtent;
import org.uncertweb.viss.core.resource.time.RegularTemporalInstants;
import org.uncertweb.viss.core.resource.time.RegularTemporalIntervals;
import org.uncertweb.viss.core.resource.time.TemporalInstant;
import org.uncertweb.viss.core.resource.time.TemporalInterval;

public class TemporalExtentTest {

	@Test
	public void test() {
		DateTime begin = new DateTime();
		Set<DateTime> instants = UwCollectionUtils.set(begin);
		for (int i = 0; i <= 1000; ++i) {
			instants.add(begin.plusHours(i));
		}
		assertEquals(RegularTemporalInstants.class, AbstractTemporalExtent.getExtent(instants, null).getClass());
		instants.add(begin.plusSeconds(1));
		assertEquals(IrregularTemporalInstants.class, AbstractTemporalExtent.getExtent(instants, null).getClass());

		Duration d = new Duration(1000 * 60 * 60);
		Set<Interval> intervals = UwCollectionUtils.set();
		for (int i = 0; i < 200; ++i) {
			DateTime s = begin;
			for (int j = 0; j <= i; ++j) {
				s = s.plus(d);
			}
			intervals.add(new Interval(s, d));
		}
		assertEquals(RegularTemporalIntervals.class, AbstractTemporalExtent.getExtent(null, intervals).getClass());
		intervals.add(new Interval(begin, d.plus(1)));
		assertEquals(IrregularTemporalIntervals.class, AbstractTemporalExtent.getExtent(null, intervals).getClass());
		assertEquals(AbstractTemporalExtent.NO_TEMPORAL_EXTENT, AbstractTemporalExtent.getExtent(null, null));
		assertEquals(AbstractTemporalExtent.NO_TEMPORAL_EXTENT, AbstractTemporalExtent.getExtent(UwCollectionUtils.<DateTime>set(), UwCollectionUtils.<Interval>set()));
		assertEquals(MixedTemporalExtent.class, AbstractTemporalExtent.getExtent(instants, intervals).getClass());
		assertEquals(TemporalInstant.class, AbstractTemporalExtent.getExtent(UwCollectionUtils.set(new DateTime()), null).getClass());
		assertEquals(TemporalInterval.class, AbstractTemporalExtent.getExtent(null, UwCollectionUtils.set(new Interval(begin, begin.plus(d))))	.getClass());
	}
}
