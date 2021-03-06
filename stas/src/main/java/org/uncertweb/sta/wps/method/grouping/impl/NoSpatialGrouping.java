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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.annotation.NotCompatibleWith;
import org.uncertweb.sta.wps.api.annotation.SpatialPartitioningPredicate;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.method.grouping.SpatialGrouping;
import org.uncertweb.utils.UwCollectionUtils;

/**
 * Groups a collection of {@code Observation}s by their {@code ISamplingFeature}
 * . Only {@code Observation}s with the same {@code ISamplingFeature} are
 * grouped together.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
@SpatialPartitioningPredicate(Constants.MethodNames.Grouping.Spatial.NO_GROUPING)
@NotCompatibleWith(NoTemporalGrouping.class)
public class NoSpatialGrouping extends SpatialGrouping {

	/**
	 * {@code Iterator} that groups the {@code Observation} by their
	 * {@code ISamplingFeature} and iterates over them.
	 */
	private class MappingIterator implements
			Iterator<ObservationMapping<SpatialSamplingFeature>> {

		/**
		 * A {@code Feature}/{@code Observation} pair.
		 */
		private class Pair {

			/**
			 * The feature.
			 */
			SpatialSamplingFeature f;

			/**
			 * The observations
			 */
			LinkedList<AbstractObservation> obs = new LinkedList<AbstractObservation>();

			/**
			 * Creates a new {@code Pair}
			 *
			 * @param f
			 * @param o
			 */
			public Pair(SpatialSamplingFeature f, AbstractObservation o) {
				this.f = f;
				this.obs.add(o);
			}
		}

		/**
		 * The iterator we delegate to.
		 */
		Iterator<Pair> iterator;

		/**
		 * Creates a new {@code Iterator}. Sorts all {@code Observation}s by
		 * their {@code ISamplingFeature}.
		 */
		public MappingIterator() {
			HashMap<String, Pair> map = new HashMap<String, Pair>();
			for (AbstractObservation o : getObservations()) {
				String id = o.getFeatureOfInterest().getIdentifier().getIdentifier();
				Pair p = map.get(id);
				if (p == null) {
					map.put(id, new Pair(o.getFeatureOfInterest(), o));
				} else {
					p.obs.add(o);
				}
			}
			iterator = map.values().iterator();
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
		public ObservationMapping<SpatialSamplingFeature> next() {
			Pair p = iterator.next();
			return new ObservationMapping<SpatialSamplingFeature>(p.f, p.obs);
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
	public Iterator<ObservationMapping<SpatialSamplingFeature>> iterator() {
		return new MappingIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AbstractProcessInput<?>> getAdditionalInputDeclarations() {
		return UwCollectionUtils.set();
	}

	/**
	 * Creates a new {@link SpatialGrouping}.
	 */
	public NoSpatialGrouping() {}

	/**
	 * Creates a new {@link SpatialGrouping}.
	 *
	 * @param obs the {@link Observation}s to group.
	 */
	public NoSpatialGrouping(List<? extends AbstractObservation> obs) {
		setInputs(obs, null);
	}

}
