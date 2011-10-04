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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.resource.time.ITemporalExtent;
import org.uncertweb.viss.core.resource.time.IrregularTemporalInstants;
import org.uncertweb.viss.core.resource.time.IrregularTemporalIntervals;
import org.uncertweb.viss.core.resource.time.MixedTemporalExtent;
import org.uncertweb.viss.core.resource.time.RegularTemporalInstants;
import org.uncertweb.viss.core.resource.time.RegularTemporalIntervals;
import org.uncertweb.viss.core.resource.time.TemporalInstant;
import org.uncertweb.viss.core.resource.time.TemporalInterval;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.IVisualization;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Polymorphic;
import com.google.code.morphia.annotations.PostLoad;
import com.google.code.morphia.annotations.PrePersist;
import com.google.code.morphia.annotations.Property;
import com.google.code.morphia.annotations.Transient;

@Polymorphic
@Entity("resources")
public abstract class AbstractMongoResource<T> implements IResource {

	protected static final Logger log = LoggerFactory
	    .getLogger(AbstractMongoResource.class);

	public static final String TIME_PROPERTY = "lastUsage";
	public static final String CHECKSUM_PROPERTY = "checksum";
	public static final String MEDIA_TYPE_PROPERTY = "mediaType";

	@Id
	private UUID uuid;
	@Property(MEDIA_TYPE_PROPERTY)
	private MediaType mediaType;
	@Indexed
	@Property(TIME_PROPERTY)
	private DateTime lastUsage;
	private File file;
	@Indexed
	@Property(CHECKSUM_PROPERTY)
	private long checksum;
	private String phenomenon;
	private ITemporalExtent temporalExtent;

	@Transient
	private T content;

	@Embedded
	private Set<IVisualization> visualizations = Utils.set();;

	public AbstractMongoResource(MediaType mt) {
		this.mediaType = mt;
	}

	@Override
	public boolean isLoaded() {
		return this.content != null;
	}

	@Override
	public UUID getUUID() {
		return this.uuid;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public MediaType getMediaType() {
		return this.mediaType;
	}

	@Override
	public Object getResource() {
		return this.content;
	}

	@Override
	public String getPhenomenon() {
		if (this.phenomenon == null) {
			if (!isLoaded()) {
				try {
					load();
				} catch (IOException e) {
					throw VissError.internal(e);
				}
			}
			this.phenomenon = getPhenomenonForResource();
		}
		return this.phenomenon;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public DateTime getLastUsage() {
		return lastUsage;
	}

	public void setLastUsage(DateTime lastUsage) {
		this.lastUsage = lastUsage;
	}

	public T getContent() {
		return content;
	}

	public void setContent(T content) {
		this.content = content;
	}

	@Override
	public void addVisualization(IVisualization v) {
		this.visualizations.add(v);
	}

	public void removeVisualization(IVisualization v) {
		this.visualizations.remove(v);
	}

	public Set<IVisualization> getVisualizations() {
		return this.visualizations;
	}

	public long getChecksum() {
		return checksum;
	}

	public void setChecksum(long checksum) {
		this.checksum = checksum;
	}

	@PostLoad
	@PrePersist
	public void setTime() {
		setLastUsage(new DateTime());
	}

	@PostLoad
	public void setPhenomenonAndExtent() {
		getPhenomenon();
		getTemporalExtent();
	}

	@Override
	public ITemporalExtent getTemporalExtent() {
		if (this.temporalExtent == null) {
			if (!isLoaded()) {
				try {
					load();
				} catch (IOException e) {
					throw VissError.internal(e);
				}
			}
			this.temporalExtent = getTemporalExtentForResource();
		}
		return this.temporalExtent;
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
							} else if (i.toDurationMillis() != duration.longValue()) {
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
						return new IrregularTemporalIntervals(Utils.asList(intervals));
					} else {
						return new RegularTemporalIntervals(begin, end, new Duration(
						    duration.longValue()));
					}
				}
			}
		} else {
			if (intervals == null || intervals.isEmpty()) {
				if (instants.size() == 1) {
					return new TemporalInstant(instants.iterator().next());
				} else {
					DateTime[] dts = Utils.sort(instants.toArray(new DateTime[instants
					    .size()]));
					Duration duration = new Duration(dts[0], dts[1]);
					boolean irregular = false;
					for (int i = 2; i < dts.length && !irregular; ++i) {
						if (!duration.isEqual(new Duration(dts[i - 1], dts[i]))) {
							irregular = true;
						}
					}
					if (irregular) {
						return new IrregularTemporalInstants(Utils.asList(instants));
					} else {
						return new RegularTemporalInstants(dts[0], dts[dts.length - 1],
						    duration);
					}
				}
			} else {
				List<ITemporalExtent> l = Utils.list();
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

	protected abstract ITemporalExtent getTemporalExtentForResource();

	protected abstract String getPhenomenonForResource();

}
