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
package org.uncertweb.viss.core.resource.time;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.uncertweb.api.om.TimeObject;

public abstract class AbstractIrregularTemporalExtent extends AbstractTemporalInterval {

	/**
	 *
	 * @param instants
	 *          a list containing DateTime, TemporalInstant, TemporalInterval or
	 *          Interval
	 * @return
	 */
	protected static Interval findOverallInterval(
	    List<? extends AbstractTemporalExtent> instants) {
		DateTime start = null, end = null;
		for (AbstractTemporalExtent o : instants) {
			Interval i = o.toInterval();
			if (i != null) {
				if (i.getStart() != null
				    && (start == null || i.getStart().isBefore(start))) {
					start = i.getStart();
				}
				if (i.getEnd() != null && (end == null || i.getEnd().isAfter(end))) {
					end = i.getEnd();
				}
			}
		}
		return new Interval(start, end);
	}

	protected JSONArray toJSONArray(List<? extends AbstractTemporalExtent> values)
	    throws JSONException {
		JSONArray a = new JSONArray();
		for (AbstractTemporalExtent o : values) {
			a.put(o.toJson());
		}
		return a;
	}
	@Override
	public abstract boolean contains(TimeObject t);

	@Override
	public boolean contains(DateTime t) {
		return contains(new TimeObject(t));
	}

	@Override
	public boolean contains(Interval i) {
		return contains(new TimeObject(i));
	}

}