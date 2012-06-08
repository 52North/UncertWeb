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
package org.uncertweb.netcdf.util;

import java.util.Comparator;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.utils.IntervalRelation;

public class TimeObjectComparator implements Comparator<TimeObject> {

	@Override
	public int compare(TimeObject o1, TimeObject o2) {
		if (o1.isInstant() && o2.isInterval()) {
			Interval i1 = o1.getInterval();
			DateTime d2 = o2.getDateTime();
			if (i1.isAfter(d2)) {
				return 1;
			}
			if (i1.isBefore(d2)) {
				return -1;
			}
			return 0;

		}
		if (o1.isInterval() && o2.isInstant()) {
			return compare(o2, o1);
		}
		if (o1.isInstant() && o2.isInstant()) {
			return o1.getDateTime().compareTo(o2.getDateTime());
		}
		if (o1.isInterval() && o2.isInterval()) {
			Set<IntervalRelation> rels = IntervalRelation.getRelations(
					o1.getInterval(), o2.getInterval());
			if (rels.contains(IntervalRelation.EQUALS))
				return 0;
			if (rels.contains(IntervalRelation.PRECEDES)
					|| rels.contains(IntervalRelation.MEETS))
				return -1;
			if (rels.contains(IntervalRelation.PRECEDES_BY)
					|| rels.contains(IntervalRelation.MET_BY))
				return 1;
			if (rels.contains(IntervalRelation.STARTS)
					|| rels.contains(IntervalRelation.FINISHED_BY))
				return -1;
			if (rels.contains(IntervalRelation.FINISHES)
					|| rels.contains(IntervalRelation.STARTED_BY))
				return 1;
			if (rels.contains(IntervalRelation.OVERLAPS))
				return -1;
			if (rels.contains(IntervalRelation.OVERLAPPED_BY))
				return 1;
			if (rels.contains(IntervalRelation.CONTAINS)
					|| rels.contains(IntervalRelation.DURING))
				return 0;
		}
		return 0;
	}
}