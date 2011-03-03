package org.uncertweb.sta.wps.method.grouping.spatial;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.server.AlgorithmParameterException;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.SamplingSurface;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.IProcessInput;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Christian Autermann
 */
public class CoverageGrouping extends SpatialGrouping {
	protected static final Logger log = LoggerFactory.getLogger(CoverageGrouping.class);
	
	@Override
	public Set<IProcessInput<?>> getAdditionalInputDeclarations() {
		HashSet<IProcessInput<?>> set = new HashSet<IProcessInput<?>>();
		set.add(Constants.Process.Inputs.FEATURE_COLLECTION_INPUT);
		return set;
	}
	
	protected class LazyMappingIterator implements Iterator<ObservationMapping<ISamplingFeature>> {
		private FeatureIterator<?> iterator;
		public LazyMappingIterator(FeatureCollection<?, ?> features) { this.iterator = features.features(); }
		@Override public boolean hasNext() { return iterator.hasNext(); }
		@Override public ObservationMapping<ISamplingFeature> next() { return map(iterator.next()); }
		@Override public void remove() { throw new UnsupportedOperationException(); }
	}

	@Override
	public Iterator<ObservationMapping<ISamplingFeature>> iterator() {
		FeatureCollection<?, ?> features = (FeatureCollection<?, ?>) getInputs().get(Constants.Process.Inputs.FEATURE_COLLECTION_INPUT);
		if (features == null) {
			throw new AlgorithmParameterException("No FeatureCollection found.");
		}
		
		log.info("Grouping {} Observations.",this.getObservations().size());
		return new LazyMappingIterator(features);
	}

	protected ObservationMapping<ISamplingFeature> map(Feature feature) {
		SimpleFeature f = (SimpleFeature) feature;
		if (f.getDefaultGeometry() == null) {
			throw new NullPointerException("defaultGeometry is null in feature with Id: " + f.getID());
		}
		if (!(f.getDefaultGeometry() instanceof Geometry)) {
			// if the parser failed, it will be a string...
			log.warn("Can not handle Geometry of class {}.", f.getDefaultGeometry().getClass().getCanonicalName());
			return null;
		}
		Geometry geom = (Geometry) f.getDefaultGeometry();
		LinkedList<Observation> result = new LinkedList<Observation>();
		if (!this.getObservations().isEmpty()) {
			int srid = getObservations().get(0).getSRID();
			geom.setSRID(srid); /* FIXME enable SRID parsing in WPS Parser class */
			for (Observation o : getObservations()) {
				log.debug("{}: Observation Geom: {}",geom.contains(o.getFeatureOfInterest().getLocation()), o.getFeatureOfInterest().getLocation());
				
				if (geom.contains(o.getFeatureOfInterest().getLocation())) {
					result.add(o);
				}
			}
		}
		log.info("Feature-SRID: {}; Observation-SRID: {}", geom.getSRID(),getObservations().get(0).getSRID());
		log.info("{} Observations for Feature: {}", result.size(), geom);
		return new ObservationMapping<ISamplingFeature>(
				new SamplingSurface((Geometry) f.getDefaultGeometry(), null,
						f.getID(), (f.getName() == null) ? f.getID() : f
								.getName().getLocalPart()), result);
	}
}