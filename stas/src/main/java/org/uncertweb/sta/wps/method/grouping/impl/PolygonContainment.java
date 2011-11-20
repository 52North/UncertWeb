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

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import net.opengis.wfs.GetFeatureDocument;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GetFeatureRequestBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AlgorithmParameterException;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.observation.Observation;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.FeatureCollectionInputHandler;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.CompositeProcessInput;
import org.uncertweb.sta.wps.api.SingleProcessInput;
import org.uncertweb.sta.wps.api.annotation.SpatialPartitioningPredicate;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.method.grouping.SpatialGrouping;
import org.uncertweb.utils.UwConstants;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A {@code CoverageGrouping} takes an {@link FeatureCollection} and groups all
 * {@link Observation}s that are located in one {@link Feature} together.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
@SpatialPartitioningPredicate(Constants.MethodNames.Grouping.Spatial.POLYGON_CONTAINMENT)
public class PolygonContainment extends SpatialGrouping {

	private static final URI DEFAULT_CODE_SPACE = UwConstants.URL.INAPPLICABLE.uri;

	/**
	 * The {@link FeatureCollection} which will be merged with the
	 * {@code FeatureCollection} fetched from {@link #WFS_URL}.
	 * 
	 * @see PolygonContainment
	 */
	public static final SingleProcessInput<FeatureCollection<FeatureType, Feature>> FEATURE_COLLECTION = new SingleProcessInput<FeatureCollection<FeatureType, Feature>>(
			Constants.Process.Inputs.FEATURE_COLLECTION_ID,
			GTVectorDataBinding.class, 0, 1, null, null);

	/**
	 * The URL of the WFS from which the {@link FeatureCollection} will be
	 * fetched. Can also be a GET request.
	 * 
	 * @see PolygonContainment
	 */
	public static final SingleProcessInput<String> WFS_URL = new SingleProcessInput<String>(
			Constants.Process.Inputs.WFS_URL_ID, LiteralStringBinding.class, 0,
			1, null, null);

	/**
	 * The request which will be posted to {@link #WFS_URL}.
	 * 
	 * @see PolygonContainment
	 */
	public static final SingleProcessInput<GetFeatureDocument> WFS_REQUEST = new SingleProcessInput<GetFeatureDocument>(
			Constants.Process.Inputs.WFS_REQUEST_ID,
			GetFeatureRequestBinding.class, 0, 1, null, null);
	/**
	 * {@link CompositeProcessInput} to combine {@link #FEATURE_COLLECTION},
	 * {@link #WFS_URL} and {@link #WFS_REQUEST}.
	 * 
	 * @see FeatureCollectionInputHandler
	 * @see PolygonContainment
	 */
	public static final AbstractProcessInput<FeatureCollection<FeatureType, Feature>> FEATURE_COLLECTION_INPUT = new CompositeProcessInput<FeatureCollection<FeatureType, Feature>>(
			Constants.Process.Inputs.FEATURE_COLLECTION_INPUT_ID,
			new FeatureCollectionInputHandler(FEATURE_COLLECTION, WFS_URL,
					WFS_REQUEST));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AbstractProcessInput<?>> getAdditionalInputDeclarations() {
		HashSet<AbstractProcessInput<?>> set = new HashSet<AbstractProcessInput<?>>();
		set.add(FEATURE_COLLECTION_INPUT);
		return set;
	}

	/**
	 * Iterator that iterates over the {@code FeatureCollection} and creates for
	 * every {@code Feature} a {@code ObservationMapping}.
	 */
	protected class LazyMappingIterator implements
			Iterator<ObservationMapping<SpatialSamplingFeature>> {

		/**
		 * The iterator of the {@code Feature}s.
		 */
		private FeatureIterator<?> iterator;

		/**
		 * Creates a new {@code LazyMappingIterator}.
		 */
		public LazyMappingIterator() {
			FeatureCollection<?, ?> features = (FeatureCollection<?, ?>) getInputs()
					.get(FEATURE_COLLECTION_INPUT);
			if (features == null) {
				throw new AlgorithmParameterException(
						"No FeatureCollection found.");
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
		public ObservationMapping<SpatialSamplingFeature> next() {
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
	public Iterator<ObservationMapping<SpatialSamplingFeature>> iterator() {
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
	protected ObservationMapping<SpatialSamplingFeature> map(Feature feature) {
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
			LinkedList<AbstractObservation> result = new LinkedList<AbstractObservation>();
			if (!this.getObservations().isEmpty()) {
				int srid = getObservations().get(0).getFeatureOfInterest()
						.getShape().getSRID();
				geom.setSRID(srid); /*
									 * FIXME enable SRID parsing in WPS Parser
									 * class
									 */
				for (AbstractObservation o : getObservations()) {
					log.debug("{}: Observation Geom: {}",
							geom.contains(o.getFeatureOfInterest().getShape()),
							o.getFeatureOfInterest().getShape());

					if (geom.contains(o.getFeatureOfInterest().getShape())) {
						result.add(o);
					}
				}
			}
			log.info("Feature-SRID: {}; Observation-SRID: {}", geom.getSRID(),
					getObservations().get(0).getFeatureOfInterest().getShape()
							.getSRID());
			log.info("{} Observations for Feature: {}", result.size(), geom);
			return new ObservationMapping<SpatialSamplingFeature>(
					new SpatialSamplingFeature(new Identifier(
							DEFAULT_CODE_SPACE, f.getID()), null,
							(Geometry) f.getDefaultGeometry()), result);
		}
	}
}