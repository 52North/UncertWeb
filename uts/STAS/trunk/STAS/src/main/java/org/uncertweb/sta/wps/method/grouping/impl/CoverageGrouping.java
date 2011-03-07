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
package org.uncertweb.sta.wps.method.grouping.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.server.AlgorithmParameterException;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.SamplingSurface;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.method.grouping.SpatialGrouping;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A {@code CoverageGrouping} takes an {@link FeatureCollection} and groups all
 * {@link Observation}s that are located in one {@link Feature} together.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class CoverageGrouping extends SpatialGrouping {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AbstractProcessInput<?>> getAdditionalInputDeclarations() {
		HashSet<AbstractProcessInput<?>> set = new HashSet<AbstractProcessInput<?>>();
		set.add(Constants.Process.Inputs.FEATURE_COLLECTION_INPUT);
		return set;
	}

	/**
	 * Iterator that iterates over the {@code FeatureCollection} and creates for
	 * every {@code Feature} a {@code ObservationMapping}.
	 */
	protected class LazyMappingIterator implements
			Iterator<ObservationMapping<ISamplingFeature>> {

		/**
		 * The iterator of the {@code Feature}s.
		 */
		private FeatureIterator<?> iterator;

		/**
		 * Creates a new {@code LazyMappingIterator}.
		 */
		public LazyMappingIterator() {
			FeatureCollection<?, ?> features = (FeatureCollection<?, ?>) getInputs()
					.get(Constants.Process.Inputs.FEATURE_COLLECTION_INPUT);
			if (features == null) {
				throw new AlgorithmParameterException("No FeatureCollection found.");
			}
			this.iterator = features.features();
			log.info("Grouping {} Observations with {} Features.",
					getObservations().size(), features.size());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ObservationMapping<ISamplingFeature> next() {
			return map(iterator.next());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<ObservationMapping<ISamplingFeature>> iterator() {
		return new LazyMappingIterator();
	}

	/**
	 * Gets all {@link Observation}s that are located in the given
	 * {@link Feature} and creates a {@link ObservationMapping}.
	 * 
	 * @param feature
	 *            the feature
	 * @return the mapping
	 */
	protected ObservationMapping<ISamplingFeature> map(Feature feature) {
		SimpleFeature f = (SimpleFeature) feature;
		if (f.getDefaultGeometry() == null) {
			throw new NullPointerException(
					"defaultGeometry is null in feature with Id: " + f.getID());
		} else if (!(f.getDefaultGeometry() instanceof Geometry)) {
			// if the parser failed, it will be a string...
			log.warn("Can not handle Geometry of class {}.", f
					.getDefaultGeometry().getClass().getCanonicalName());
			return null;
		} else {
			Geometry geom = (Geometry) f.getDefaultGeometry();
			LinkedList<Observation> result = new LinkedList<Observation>();
			if (!this.getObservations().isEmpty()) {
				int srid = getObservations().get(0).getSRID();
				geom.setSRID(srid); /* FIXME enable SRID parsing in WPS Parser class */
				for (Observation o : getObservations()) {
					log.debug("{}: Observation Geom: {}",
							geom.contains(o.getFeatureOfInterest().getLocation()),
							o.getFeatureOfInterest().getLocation());
	
					if (geom.contains(o.getFeatureOfInterest().getLocation())) {
						result.add(o);
					}
				}
			}
			log.info("Feature-SRID: {}; Observation-SRID: {}", geom.getSRID(),
					getObservations().get(0).getSRID());
			log.info("{} Observations for Feature: {}", result.size(), geom);
			return new ObservationMapping<ISamplingFeature>(
					new SamplingSurface((Geometry) f.getDefaultGeometry(), null,
							f.getID(), (f.getName() == null) ? f.getID() : f
									.getName().getLocalPart()), result);
		}
	}
}