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

import java.util.List;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.resource.time.AbstractTemporalExtent.CanBeInstant;

public class IrregularTemporalInstants extends AbstractIrregularTemporalExtent implements CanBeInstant{

	protected static List<TemporalInstant> toTemporalInstantList(Iterable<DateTime> instants) {
		List<TemporalInstant> ti = UwCollectionUtils.list();
		for (DateTime i : instants) {
			ti.add(new TemporalInstant(i));
		}
		return ti;
	}
	
	protected static List<TimeObject> toTimeObjects(Iterable<DateTime> instants, Iterable<Interval> intervals) {
		List<TimeObject> l = UwCollectionUtils.list();
		for (DateTime dt : instants) {
			l.add(new TimeObject(dt));
		}
		for (Interval i : intervals) {
			l.add(new TimeObject(i));
		}
		return l;
	}
	
	private List<TemporalInstant> instants;

	public IrregularTemporalInstants() {
	}

	public IrregularTemporalInstants(List<DateTime> instants) {
		setInstants(toTemporalInstantList(instants));
	}

	public void setInstants(List<TemporalInstant> instants) {
		this.instants = instants;
	}

	public List<TemporalInstant> getInstants() {
		return this.instants;
	}

	@Override
	public JSONObject toJson() throws JSONException {
		return super.toJson().put(INSTANTS_KEY, toJSONArray(getInstants()));
	}

	@Override
	public boolean contains(TimeObject t) {
		for (TemporalInstant i : getInstants()) {
			if (i.contains(t)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Set<TimeObject> toInstances() {
		Set<TimeObject> to = UwCollectionUtils.set();
		for (TemporalInstant i : instants) {
			to.add(new TimeObject(i.getInstant()));
		}
		return to;
	}

}
