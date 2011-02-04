package org.uncertweb.sta.wps.method.grouping.temporal;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationTime;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.ProcessInput;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;

public class NoTemporalGrouping extends TemporalGrouping {

	private class MappingIterator implements Iterator<ObservationMapping<ObservationTime>> {

		private Iterator<Observation> observations = getObservations().iterator();

		@Override
		public boolean hasNext() {
			return observations.hasNext();
		}

		@Override
		public ObservationMapping<ObservationTime> next() {
			Observation o = observations.next();
			return new ObservationMapping<ObservationTime>(o.getObservationTime(),
												Utils.mutableSingletonList(o));
		}

		@Override
		public void remove() {
			observations.remove();
		}

	}

	@Override
	public Iterator<ObservationMapping<ObservationTime>> iterator() {
		return new MappingIterator();
	}
	
	@Override
	public Set<ProcessInput> getAdditionalInputDeclarations() {
		return Utils.set();
	}
	
	public NoTemporalGrouping(){}
	public NoTemporalGrouping(List<Observation> obs) {
		setInputs(obs, null);
	}

}
