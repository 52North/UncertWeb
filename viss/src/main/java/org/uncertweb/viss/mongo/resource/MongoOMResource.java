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
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.DiscreteNumericObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.ReferenceObservation;
import org.uncertweb.api.om.observation.TextObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.BooleanObservationCollection;
import org.uncertweb.api.om.observation.collections.CategoryObservationCollection;
import org.uncertweb.api.om.observation.collections.DiscreteNumericObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.ReferenceObservationCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.ReferenceResult;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.utils.UwIOUtils;
import org.uncertweb.viss.core.VissConfig;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.util.MediaTypes;

import com.google.code.morphia.annotations.NotSaved;
import com.google.code.morphia.annotations.Polymorphic;
import com.google.code.morphia.annotations.PrePersist;

@Polymorphic
public class MongoOMResource extends
		AbstractMongoResource<IObservationCollection> {

	private Set<MongoResourceFile> savedResourceFiles = UwCollectionUtils.set();

	@NotSaved
	private Set<AbstractMongoResource<?>> resources = null;

	@NotSaved
	private Map<String, File> resourceFiles = UwCollectionUtils.map();

	public MongoOMResource(File f, ObjectId oid, long checksum) {
		super(MediaTypes.OM_2_TYPE, f, oid, checksum);
	}

	public MongoOMResource() {
		super(MediaTypes.OM_2_TYPE);
	}

	@PrePersist
	public void prePersist() {
		super.prePersist();
		for (Entry<String, File> e : resourceFiles.entrySet()) {
			savedResourceFiles.add(new MongoResourceFile(e.getValue(), e
					.getKey()));
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

		/* check whether we have loaded the referenced file */
		File f = resourceFiles.get(rr.getHref());
		if (f == null) {
			log.debug("Fetching referenced resource from {}", rr.getHref());
			/* fetch the referenced file */
			URL url = new URL(rr.getHref());
			f = mrs.createResourceFile(getId(), mt);
			UwIOUtils.saveToFile(f, url);
			log.debug("Resource saved to {}", f.getAbsolutePath());
			resourceFiles.put(rr.getHref(), f);
		} else {
			log.debug("Referenced resource is already saved");
		}
		AbstractMongoResource<?> r = mrs.getResourceForMediaType(mt, f,
				getId(), -1);
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
	protected Set<IDataSet> createDataSets() {
		Set<URI>  uris = UwCollectionUtils.set();
		
		for (AbstractObservation ao : getContent().getObservations()) {
			uris.add(ao.getObservedProperty());
		}
		Set<IDataSet> dss = UwCollectionUtils.set();
		for (URI uri : uris) {
			dss.add(new MongoOMDataSet(this, uri));
		}
		return dss;
	}

	public IObservationCollection getObservationsForPhenomenon(String phen) {
		List<AbstractObservation> list = UwCollectionUtils.list();
		URI uri = URI.create(phen);
		for (AbstractObservation ao : getContent().getObservations()) {
			if (ao.getObservedProperty().equals(uri)) {
				list.add(ao);
			}
		}
		return getCollection(list);
	}
	
	private static IObservationCollection getCollection(Collection<AbstractObservation> observations) {
		AbstractObservation ao = observations.iterator().next();
		if (ao instanceof Measurement) {
			return new MeasurementCollection(MongoOMResource.<Measurement> asList(observations));
		} else if (ao instanceof BooleanObservation) {
			return new BooleanObservationCollection(MongoOMResource.<BooleanObservation> asList(observations));
		} else if (ao instanceof CategoryObservation) {
			return new CategoryObservationCollection(MongoOMResource.<CategoryObservation> asList(observations));
		} else if (ao instanceof DiscreteNumericObservation) {
			return new DiscreteNumericObservationCollection(MongoOMResource.<DiscreteNumericObservation> asList(observations));
		} else if (ao instanceof ReferenceObservation) {
			return new ReferenceObservationCollection(MongoOMResource.<ReferenceObservation> asList(observations));
		} else if (ao instanceof TextObservation) {
			return new TextObservationCollection(MongoOMResource.<TextObservation> asList(observations));
		} else {
			return new UncertaintyObservationCollection(MongoOMResource.<UncertaintyObservation> asList(observations));
		}
	}
	@SuppressWarnings("unchecked")
	
	private static <T extends AbstractObservation> List<T> asList(
			Collection<AbstractObservation> col) {
		List<T> list = new LinkedList<T>();
		for (AbstractObservation ao : col) {
			list.add((T) ao);
		}
		return list;
	}

	@Override
	protected IObservationCollection loadContent() {
		log.debug("Loading OM resource.");

		for (MongoResourceFile r : savedResourceFiles) {
			log.debug("Loading previously saved resource file {}", r.getFile()
					.toString());
			resourceFiles.put(r.getHref(), r.getFile());
		}

		IObservationCollection col;
		try {
			col = parseCollection();
		} catch (IOException e) {
			throw VissError.internal(e);
		}

		if (resources == null)
			resources = UwCollectionUtils.set();
		if (resourceFiles == null)
			resourceFiles = UwCollectionUtils.map();

		for (AbstractObservation ao : col.getObservations()) {
			if (ao instanceof ReferenceObservation) {
				try {
					processReference((ReferenceResult) ao.getResult());
				} catch (IOException e) {
					throw VissError.internal(e);
				}
			}
			log.debug("{}: {}: {}", new Object[] {
					ao.getClass().getSimpleName(),
					ao.getResult().getClass().getSimpleName(),
					ao.getResult().getValue() });
		}
		return col;
	}
}
