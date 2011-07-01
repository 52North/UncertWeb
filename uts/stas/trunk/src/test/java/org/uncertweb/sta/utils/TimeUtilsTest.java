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
package org.uncertweb.sta.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Test;
import org.uncertweb.intamap.utils.TimeUtils;

public class TimeUtilsTest {

	private static final String TIME1 = "2008-02-01T09:00:00.00+0000";
	private static final String TIME2 = "2008-02-02T09:00:00.00+0000";
	private static final String TIME3 = "2008-02-03T09:00:00.00+0000";
	private static final String PERIOD_LONG = "P1Y3M5W2DT10H18M41.630S";
	private static final String PERIOD_SHORT = "P1DT1H";

	@Test
	public void testPeriodParsing() {
		Period p = TimeUtils.parsePeriod(PERIOD_LONG).toPeriod();
		assertEquals(1, p.getYears());
		assertEquals(3, p.getMonths());
		assertEquals(5, p.getWeeks());
		assertEquals(2, p.getDays());
		assertEquals(10, p.getHours());
		assertEquals(18, p.getMinutes());
		assertEquals(41, p.getSeconds());
		assertEquals(630, p.getMillis());
	}

	@Test
	public void testPeriodFormatting() {
		assertEquals(PERIOD_LONG, TimeUtils.format(TimeUtils
				.parsePeriod(PERIOD_LONG)));
	}

	@Test
	public void testWithinRange() {
		DateTime d1 = TimeUtils.parseDateTime(TIME1);
		DateTime d2 = TimeUtils.parseDateTime(TIME2);
		DateTime d3 = TimeUtils.parseDateTime(TIME3);
		Period p = TimeUtils.parsePeriod(PERIOD_SHORT).toPeriod();
		assertTrue(TimeUtils.withinRange(d1, d3, d2));
		assertTrue(TimeUtils.withinRange(d1, d3, d1));
		assertTrue(TimeUtils.withinRange(d1, d3, d3));
		assertTrue(TimeUtils.withinRange(d1, d1, d1));
		assertFalse(TimeUtils.withinRange(d1, d2, d3));
		assertFalse(TimeUtils.withinRange(d1, d2, d3));
		assertTrue(TimeUtils.withinRange(d1, p, d2));
	}
}
