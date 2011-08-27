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

public class TemporalInstant implements TemporalExtent {

	static final String INSTANT_JSON_KEY = "instant";

	private DateTime instant;

	public TemporalInstant() {
	}

	public TemporalInstant(DateTime instant) {
		setInstant(instant);
	}

	public DateTime getInstant() {
		return instant;
	}

	public void setInstant(DateTime instant) {
		this.instant = instant;
	}

	@Override
	public JSONObject toJson() throws JSONException {
		return new JSONObject().put(INSTANT_JSON_KEY, getInstant());
	}

	@Override
	public Interval toInterval() {
		return new Interval(getInstant(), getInstant());
	}
	
}
