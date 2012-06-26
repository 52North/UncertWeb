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

import static org.uncertweb.utils.UwJsonConstants.INSTANTS_KEY;
import static org.uncertweb.utils.UwJsonConstants.INTERVALS_KEY;

import java.util.List;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.resource.time.AbstractTemporalExtent.CanBeBoth;

public class MixedTemporalExtent extends AbstractIrregularTemporalExtent implements CanBeBoth {

	private List<AbstractTemporalExtent> extents;

	public MixedTemporalExtent() {
	}

	public MixedTemporalExtent(List<AbstractTemporalExtent> l) {
		setExtents(l);
	}

	public void setExtents(List<AbstractTemporalExtent> extents) {
		setInterval(findOverallInterval(extents));
		this.extents = extents;
	}

	public List<AbstractTemporalExtent> getExtents() {
		return this.extents;
	}

	@Override
	public JSONObject toJson() throws JSONException {
		List<JSONObject> instants = UwCollectionUtils.list();
		List<JSONObject> intervals = UwCollectionUtils.list();
		for (AbstractTemporalExtent te : getExtents()) {
			if (te instanceof TemporalInstant) {
				instants.add(te.toJson());
			} else if (te instanceof AbstractTemporalInterval) {
				intervals.add(te.toJson());
			}
		}
		return super.toJson().put(INSTANTS_KEY, new JSONArray(instants))
				.put(INTERVALS_KEY, new JSONArray(intervals));
	}

	@Override
	public boolean contains(TimeObject t) {
		for (AbstractTemporalExtent e : getExtents()) {
			if (e.contains(t)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<TimeObject> toInstances() {
		Set<TimeObject> to = UwCollectionUtils.set();
		for (AbstractTemporalExtent e : getExtents()) {
			to.addAll(e.toInstances());
		}
		return to;
	}

}
