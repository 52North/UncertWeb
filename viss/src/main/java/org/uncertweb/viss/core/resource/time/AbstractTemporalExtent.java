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
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.utils.UwCollectionUtils;

public abstract class AbstractTemporalExtent {
	public static interface CanBeInterval {}
	public static interface CanBeInstant {}
	public static interface CanBeBoth {}

	public static final AbstractTemporalExtent NO_TEMPORAL_EXTENT = new NoTemporalExtent();

	public boolean contains(TimeObject t) {
		if (t.isInstant()) {
			return contains(t.getDateTime());
		} else if (t.isInterval()) {
			return contains(t.getInterval());
		} else {
			return false;
		}
	}
	
	public abstract JSONObject toJson() throws JSONException;
	public abstract Interval toInterval();
	public abstract boolean contains(DateTime t);
	public abstract boolean contains(Interval i);
	public abstract Set<TimeObject> toInstances();
		
	public static AbstractTemporalExtent getExtent(Iterable<? extends TimeObject> times) {
		if (times == null) {
			return AbstractTemporalExtent.NO_TEMPORAL_EXTENT;
		}
		Set<DateTime> instants = UwCollectionUtils.set();
		Set<Interval> intervals = UwCollectionUtils.set();
		for (TimeObject time : times) {
			if (time.isInstant()) {
				instants.add(time.getDateTime());
			} else { 
				intervals.add(time.getInterval()); 
			}	
		}
		return getExtent(instants, intervals);
	}
	
	public static AbstractTemporalExtent getExtent(Set<DateTime> instants, Set<Interval> intervals) {
		if (instants == null || instants.isEmpty()) {
			if (intervals == null || intervals.isEmpty()) {
				return AbstractTemporalExtent.NO_TEMPORAL_EXTENT;
			} else {
				if (intervals.size() == 1) {
					return new TemporalInterval(intervals.iterator().next());
				} else {
					Long duration = null;
					boolean irregular = false;
					DateTime begin = null, end = null;
					for (Interval i : intervals) {
						if (!irregular) {
							if (duration == null) {
								duration = Long.valueOf(i.toDurationMillis());
							} else if (i.toDurationMillis() != duration
									.longValue()) {
								irregular = true;
								break;
							}
						}
						if (begin == null || i.getStart().isBefore(begin)) {
							begin = i.getStart();
						}
						if (end == null || i.getEnd().isAfter(end)) {
							end = i.getEnd();
						}
					}
					if (irregular) {
						return new IrregularTemporalIntervals(
								UwCollectionUtils.asList(intervals));
					} else {
						return new RegularTemporalIntervals(begin, end,
								new Duration(duration.longValue()));
					}
				}
			}
		} else {
			if (intervals == null || intervals.isEmpty()) {
				if (instants.size() == 1) {
					return new TemporalInstant(instants.iterator().next());
				} else {
					DateTime[] dts = UwCollectionUtils.sort(instants
							.toArray(new DateTime[instants.size()]));
					Duration duration = new Duration(dts[0], dts[1]);
					boolean irregular = false;
					for (int i = 2; i < dts.length && !irregular; ++i) {
						if (!duration.isEqual(new Duration(dts[i - 1], dts[i]))) {
							irregular = true;
						}
					}
					if (irregular) {
						return new IrregularTemporalInstants(
								UwCollectionUtils.asList(instants));
					} else {
						return new RegularTemporalInstants(dts[0],
								dts[dts.length - 1], duration);
					}
				}
			} else {
				List<AbstractTemporalExtent> l = UwCollectionUtils.list();
				for (DateTime t : instants) {
					l.add(new TemporalInstant(t));
				}
				for (Interval i : intervals) {
					l.add(new TemporalInterval(i));
				}
				return new MixedTemporalExtent(l);
			}
		}
	}

}
