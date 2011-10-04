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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.uncertweb.viss.core.util.Utils;

public class IrregularTemporalInstants extends AbstractIrregularTemporalExtent {

	static final String INSTANTS_JSON_KEY = "instants";

	protected static List<TemporalInstant> toTemporalInstantList(
	    List<DateTime> instants) {
		List<TemporalInstant> ti = Utils.list();
		for (DateTime i : instants) {
			ti.add(new TemporalInstant(i));
		}
		return ti;
	}

	private List<TemporalInstant> instants;

	public IrregularTemporalInstants() {}

	public IrregularTemporalInstants(List<DateTime> instants) {
		setInstants(toTemporalInstantList(instants));
	}

	public void setInstants(List<TemporalInstant> instants) {
		setInterval(findOverallInterval(instants));
		this.instants = instants;
	}

	public List<TemporalInstant> getInstants() {
		return this.instants;
	}

	@Override
	public JSONObject toJson() throws JSONException {
		return super.toJson().put(INSTANTS_JSON_KEY, toJSONArray(getInstants()));
	}
}
