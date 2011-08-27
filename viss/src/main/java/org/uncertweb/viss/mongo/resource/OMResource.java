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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.ReferenceObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.ReferenceResult;
import org.uncertweb.viss.core.VissConfig;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.resource.time.TemporalExtent;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.util.Utils;

import com.google.code.morphia.annotations.Polymorphic;
import com.google.code.morphia.annotations.PostLoad;
import com.google.code.morphia.annotations.PrePersist;
import com.google.code.morphia.annotations.Transient;

@Polymorphic
public class OMResource extends AbstractMongoResource<IObservationCollection>{

	public OMResource() {
		super(Constants.OM_2_TYPE);
	}
	
	private Set<ResourceFile> savedResourceFiles = Utils.set();

	@Transient
	private Set<AbstractMongoResource<?>> resources = null;
	@Transient
	private Map<String, File> resourceFiles = Utils.map();

	@PostLoad
	public void postLoad() {
		for (ResourceFile r : savedResourceFiles) {
			resourceFiles.put(r.getHref(),r.getFile());
		}
	}
	
	@PrePersist
	public void prePersist() {
		for (Entry<String, File> e : resourceFiles.entrySet()) {
			savedResourceFiles.add(new ResourceFile(e.getValue(), e.getKey()));
		}
	}
	
	
	@Override
	public void load() throws IOException {
		log.debug("Loading OM resource.");
		IObservationCollection col = parseCollection();
		setContent(col);

		if (resources == null)
			resources = Utils.set();
		if (resourceFiles == null)
			resourceFiles = Utils.map();

		for (AbstractObservation ao : col.getObservations()) {
			if (ao instanceof ReferenceObservation) {
				processReference((ReferenceResult) ao.getResult());
			}
			log.debug("{}: {}: {}", new Object[] {
					ao.getClass().getSimpleName(),
					ao.getResult().getClass().getSimpleName(),
					ao.getResult().getValue() });
		}
		for (Resource r : resources) {
			r.load();
		}
	}

	private void processReference(ReferenceResult rr) throws IOException {
		log.debug("Processing ReferenceResult");
		if (rr.getValue() != null) {
			log.debug("ReferenceResult already loaded");
			/* reference is already resolved */
			if (!resources.contains(rr.getValue())) {
				resources.add((AbstractMongoResource<?>) rr.getValue());
			}
			return;
		}

		MongoResourceStore mrs = (MongoResourceStore) VissConfig.getInstance()
				.getResourceStore();
		MediaType mt = MediaType.valueOf(rr.getRole());
		log.debug("Creating resource for {}", mt);
		/* check whether we can resolve the reference */
		AbstractMongoResource<?> r = mrs.getResourceForMediaType(mt);

		/* check whether we have loaded the referenced file */
		File f = resourceFiles.get(rr.getHref());
		if (f == null) {
			log.debug("Fetching referenced resource from {}", rr.getHref());
			/* fetch the referenced file */
			URL url = new URL(rr.getHref());
			f = mrs.createResourceFile(getUUID(), mt);
			Utils.saveToFile(f, url);
			log.debug("Resource saved to {}", f.getAbsolutePath());
			resourceFiles.put(rr.getHref(), f);
		} else {
			log.debug("Referenced resource is already saved");
		}
		r.setFile(f);
		r.setUUID(getUUID());
		r.setLastUsage(getLastUsage());
		rr.setValue(r);
		if (r != null && rr.getValue() == null) {
			throw VissError.internal("WTF!?!?!");
		}
		resources.add(r);
	}

	private IObservationCollection parseCollection() throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(getFile());
			return new XBObservationParser().parse(IOUtils.toString(is));
		} catch (OMParsingException e) {
			throw VissError.internal(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	@Override
	protected String getPhenomenonForResource() {
		String phen = null;
		for (AbstractObservation ao : getContent().getObservations()) {
			if (phen == null) {
				phen = ao.getObservedProperty().toString();
			} else if (!phen.equals(ao.getObservedProperty().toString())) {
				phen = "UNKNOWN";
			}
		}
		return phen == null ? "UNKNOWN" : phen;
	}
	
	@Override
	protected TemporalExtent getTemporalExtentForResource() {
		Set<DateTime> instants = Utils.set();
		Set<Interval> intervals = Utils.set();
		for (AbstractObservation ao : getContent().getObservations()) {
			TimeObject to = ao.getPhenomenonTime();
			if (to.getInterval() != null) {
				intervals.add(to.getInterval());
			} else if (to.getDateTime() != null) {
				instants.add(to.getDateTime());
			}
		}
		return getExtent(instants, intervals);
	}
	
}
