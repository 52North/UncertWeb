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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.Interval;

public class TemporalInterval implements ITemporalExtent {

	static final String BEGIN_JSON_KEY = "begin";
	static final String END_JSON_KEY = "end";

	private Interval interval;

	public TemporalInterval() {}

	public TemporalInterval(DateTime begin, DateTime end) {
		setInterval(begin, end);
	}

	public TemporalInterval(Interval i) {
		setInterval(i);
	}

	public void setInterval(Interval interval) {
		this.interval = interval;
	}

	public Interval getInterval() {
		return this.interval;
	}

	public void setInterval(DateTime begin, DateTime end) {
		setInterval(new Interval(begin, end));
	}

	public DateTime getBegin() {
		return getInterval().getStart();
	}

	public DateTime getEnd() {
		return getInterval().getEnd();
	}

	@Override
	public JSONObject toJson() throws JSONException {
		return new JSONObject().put(BEGIN_JSON_KEY, getBegin()).put(END_JSON_KEY,
		    getEnd());
	}

	@Override
	public Interval toInterval() {
		return getInterval();
	}

}
