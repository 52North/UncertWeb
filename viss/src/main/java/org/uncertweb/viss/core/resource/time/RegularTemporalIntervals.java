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

import static org.uncertweb.utils.UwJsonConstants.INTERVAL_SIZE_KEY;

import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.resource.time.AbstractTemporalExtent.CanBeInterval;

public class RegularTemporalIntervals extends AbstractRegularTemporalExtent implements CanBeInterval{

	public RegularTemporalIntervals() {
	}

	public RegularTemporalIntervals(DateTime begin, DateTime end,
			Duration intervalSize) {
		super(begin, end, intervalSize);
	}

	public Duration getIntervalSize() {
		return getSep();
	}

	public void setIntervalSize(Duration intervalSize) {
		setSep(intervalSize);
	}

	@Override
	public JSONObject toJson() throws JSONException {
		return super.toJson().put(INTERVAL_SIZE_KEY,
				getIntervalSize().getMillis());
	}


	@Override
	public boolean contains(DateTime t) {
		return t.getMillis() >= getBegin().getMillis() && t.getMillis() <= getEnd().getMillis();
	}

	@Override
	public boolean contains(Interval i) {
		if (!contains(i.getStart()) || !contains(i.getEnd())) {
			return false;
		}
		return getBegin().getMillis() - i.getStartMillis()	% getSep().getMillis() == 0
				&& i.getStartMillis() - i.getEndMillis() == getSep().getMillis();
	}

	@Override
	public Set<TimeObject> toInstances() {
		Set<TimeObject> to = UwCollectionUtils.set();
		Duration size = getIntervalSize();
		long endMillis = getEnd().getMillis();
		DateTime begin = getBegin();
		DateTime end = null;
		while ((end = begin.plus(size)).getMillis() <= endMillis) {
			to.add(new TimeObject(begin, end));
			begin = end;
		}
		return to;
	}
}
