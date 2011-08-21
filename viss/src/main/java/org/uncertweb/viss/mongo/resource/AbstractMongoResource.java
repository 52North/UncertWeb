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
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.Visualization;

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
public abstract class AbstractMongoResource<T> implements Resource {

	protected static final Logger log = LoggerFactory.getLogger(AbstractMongoResource.class);
	
	public static final String TIME_PROPERTY = "last_usage";
	public static final String CHECKSUM_PROPERTY = "checksum";

	@Id
	private UUID uuid;
	private MediaType mediaType;
	@Indexed
	@Property(TIME_PROPERTY)
	private DateTime lastUsage;
	private File file;
	@Transient
	private T content;
	@Indexed
	@Property(CHECKSUM_PROPERTY)
	private long checksum;
	private String phenomenon;
	
	@Embedded
	private Set<Visualization> visualizations = Utils.set();;

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
	public void suspend() {
		this.content = null;
	}
	
	@Override
	public void addVisualization(Visualization v) {
		this.visualizations.add(v);
	}

	public void removeVisualization(Visualization v) {
		this.visualizations.remove(v);
	}

	public Set<Visualization> getVisualizations() {
		return this.visualizations;
	}

	public long getChecksum() {
		return checksum;
	}

	public void setChecksum(long checksum) {
		this.checksum = checksum;
	}
	
	protected abstract String getPhenomenonForResource();
	
	@PostLoad
	@PrePersist
	public void setLastUsageTime() {
		setLastUsage(new DateTime());
	}
	
	@PostLoad
	public void setPhenomenon() {
		if (this.phenomenon == null) {
			getPhenomenon();
		}
	}

}
