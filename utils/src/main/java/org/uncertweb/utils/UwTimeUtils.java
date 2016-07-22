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
package org.uncertweb.utils;

import java.security.InvalidParameterException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePeriod;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;


/**
 * @author Christian Autermann
 */
public class UwTimeUtils {
	private static final PeriodFormatter ISO8601_PERIOD_FORMAT = ISOPeriodFormat.standard();
	private static final DateTimeFormatter ISO8601_TIME_FORMAT = ISODateTimeFormat.dateTime();

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	public static DateTime parseDateTime(String isoInstant) {
		return parseInstant(isoInstant).toInstant().toDateTime();
	}

	public static DateTime parseInstant(String isoInstant) {
		try {
			return ISO8601_TIME_FORMAT.parseDateTime(isoInstant);
		} catch (Exception e) {
			throw new InvalidParameterException("Not an ISO time: "+isoInstant+"\n"+e.getMessage());
		}

	}

	public static Period parsePeriod(String isoPeriod) {
		try {
			return ISO8601_PERIOD_FORMAT.parsePeriod(isoPeriod).toPeriod();
		} catch (Exception e) {
			throw new InvalidParameterException("Not an ISO Period: " + isoPeriod+"\n"+e.getMessage());
		}
	}

	public static String format(Period p) {
		return ISO8601_PERIOD_FORMAT.print(p);
	}

	public static String format(DateTime i) {
		return ISO8601_TIME_FORMAT.print(i);
	}

	public static final DateTime getEndDate(ReadableInstant begin, ReadablePeriod period) {
		return begin.toInstant().plus(period.toPeriod().toDurationFrom(begin)).toDateTime();
	}

	public static final boolean withinRange(ReadableInstant begin, ReadablePeriod period, ReadableInstant time) {
		return withinRange(begin, getEndDate(begin, period), time);
	}

	public static final boolean withinRange(ReadableInstant begin, ReadableInstant end, ReadableInstant time) {
		return (time.isAfter(begin) || time.isEqual(begin)) && (time.isBefore(end) || time.isEqual(end));
	}

}
