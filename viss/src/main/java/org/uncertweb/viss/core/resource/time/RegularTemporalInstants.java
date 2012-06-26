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

import static org.uncertweb.utils.UwJsonConstants.SEPERATOR_KEY;

import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.resource.time.AbstractTemporalExtent.CanBeInstant;

public class RegularTemporalInstants extends AbstractRegularTemporalExtent
		implements CanBeInstant {

	public RegularTemporalInstants() {
	}

	public RegularTemporalInstants(DateTime begin, DateTime end,
			Duration seperator) {
		super(begin, end, seperator);
	}

	public Duration getSeperator() {
		return getSep();
	}

	public void setSeperator(Duration seperator) {
		setSep(seperator);
	}

	@Override
	public JSONObject toJson() throws JSONException {
		return super.toJson().put(SEPERATOR_KEY, getSeperator().getMillis());
	}

	@Override
	public boolean contains(DateTime t) {
		if (t.isBefore(getBegin()) || t.isAfter(getEnd())) {
			return false;
		}
		return ((t.getMillis() - getBegin().getMillis()) % getSeperator().getMillis()) == 0;
	}

	@Override
	public boolean contains(Interval i) {
		if (i.getStartMillis() == i.getEndMillis()) {
			return contains(i.getStart());
		}else {
			return false;
		}
	}

	@Override
	public Set<TimeObject> toInstances() {
		DateTime dt = getBegin();
		Set<TimeObject> to = UwCollectionUtils.set(new TimeObject(dt));
		while (dt.getMillis() <= getEnd().getMillis()) {
			to.add(new TimeObject(dt = dt.plus(getSeperator())));
		}
		return to;
	}
}
