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
package org.uncertweb.viss.mongo.resource;

import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.resource.time.ITemporalExtent;
import org.uncertweb.viss.core.resource.time.IrregularTemporalInstants;
import org.uncertweb.viss.core.resource.time.IrregularTemporalIntervals;
import org.uncertweb.viss.core.resource.time.MixedTemporalExtent;
import org.uncertweb.viss.core.resource.time.RegularTemporalInstants;
import org.uncertweb.viss.core.resource.time.RegularTemporalIntervals;
import org.uncertweb.viss.core.resource.time.TemporalInstant;
import org.uncertweb.viss.core.resource.time.TemporalInterval;
import org.uncertweb.viss.core.vis.IVisualization;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Polymorphic;
import com.google.code.morphia.annotations.Reference;
import com.google.code.morphia.annotations.Transient;

@Polymorphic
@Entity("datasets")
public abstract class AbstractMongoDataSet<T> implements IDataSet {

	@Id
	private ObjectId oid;
	
	@Reference
	private IResource resource;
	
	@Embedded
	private Set<IVisualization> visualizations = UwCollectionUtils.set();
	
	@Embedded
	private ITemporalExtent temporalExtent;

	@Transient
	private T content;
	
	public AbstractMongoDataSet(IResource resource) {
		setResource(resource);
	}

	public AbstractMongoDataSet() {
	}
	
	@Override
	public void setResource(IResource r) {
		this.resource = r;
	}

	@Override
	public ObjectId getId() {
		return this.oid;
	}

	public void setId(Object o) {
		this.oid = (ObjectId) o;
	}

	@Override
	public IResource getResource() {
		return this.resource;
	}
	
	@Override
	public Set<IVisualization> getVisualizations() {
		return this.visualizations;
	}

	@Override
	public void addVisualization(IVisualization v) {
		this.visualizations.add(v);
	}

	@Override
	public void removeVisualization(IVisualization v) {
		this.visualizations.remove(v);
	}

	@Override
	public ITemporalExtent getTemporalExtent() {
		if (this.temporalExtent == null) {
			this.temporalExtent = loadTemporalExtent();
		}
		return this.temporalExtent;
	}

	@Override
	public T getContent() {
		if (this.content == null) {
			setContent(loadContent());
		}
		return this.content;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setContent(Object c) {
		this.content = (T) c;
	}

	protected abstract T loadContent();
	protected abstract ITemporalExtent loadTemporalExtent();
	

	public static ITemporalExtent getExtent(Iterable<? extends TimeObject> times) {
		if (times == null) {
			return ITemporalExtent.NO_TEMPORAL_EXTENT;
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
	
	public static ITemporalExtent getExtent(Set<DateTime> instants,
			Set<Interval> intervals) {
		if (instants == null || instants.isEmpty()) {
			if (intervals == null || intervals.isEmpty()) {
				return ITemporalExtent.NO_TEMPORAL_EXTENT;
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
				List<ITemporalExtent> l = UwCollectionUtils.list();
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
