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
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.util.Utils;

public class MixedTemporalExtent extends AbstractIrregularTemporalExtent {

	private List<ITemporalExtent> extents;

	public MixedTemporalExtent() {}

	public MixedTemporalExtent(List<ITemporalExtent> l) {
		setExtents(l);
	}

	public void setExtents(List<ITemporalExtent> extents) {
		setInterval(findOverallInterval(extents));
		this.extents = extents;
	}

	public List<ITemporalExtent> getExtents() {
		return this.extents;
	}

	@Override
	public JSONObject toJson() throws JSONException {
		List<JSONObject> instants = Utils.list();
		List<JSONObject> intervals = Utils.list();
		for (ITemporalExtent te : getExtents()) {
			if (te instanceof TemporalInstant) {
				instants.add(te.toJson());
			} else if (te instanceof TemporalInterval) {
				intervals.add(te.toJson());
			}
		}
		return super
		    .toJson()
		    .put(IrregularTemporalInstants.INSTANTS_JSON_KEY,
		        new JSONArray(instants))
		    .put(IrregularTemporalIntervals.INTERVALS_JSON_KEY,
		        new JSONArray(intervals));
	}

}
