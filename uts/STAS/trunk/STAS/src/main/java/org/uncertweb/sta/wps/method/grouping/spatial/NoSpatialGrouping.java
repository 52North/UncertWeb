package org.uncertweb.sta.wps.method.grouping.spatial;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.IProcessInput;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;

public class NoSpatialGrouping extends SpatialGrouping {

	private class MappingIterator implements
			Iterator<ObservationMapping<ISamplingFeature>> {
		class Pair {
			ISamplingFeature f;
			LinkedList<Observation> obs = new LinkedList<Observation>();

			public Pair(ISamplingFeature f, Observation o) {
				this.f = f;
				this.obs.add(o);
			}
		}

		Iterator<Pair> iterator;

		public MappingIterator() {
			HashMap<String, Pair> map = new HashMap<String, Pair>();
			for (Observation o : getObservations()) {
				String id = o.getFeatureOfInterest().getId();
				Pair p = map.get(id);
				if (p == null) {
					map.put(id, new Pair(o.getFeatureOfInterest(), o));
				} else {
					p.obs.add(o);
				}
			}
			iterator = map.values().iterator();
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public ObservationMapping<ISamplingFeature> next() {
			Pair p = iterator.next();
			return new ObservationMapping<ISamplingFeature>(p.f, p.obs);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Iterator<ObservationMapping<ISamplingFeature>> iterator() {
		return new MappingIterator();
	}

	@Override
	public Set<IProcessInput<?>> getAdditionalInputDeclarations() {
		return Utils.set();
	}

	public NoSpatialGrouping(){}
	public NoSpatialGrouping(List<Observation> obs) {
		setInputs(obs, null);
	}
	
}
