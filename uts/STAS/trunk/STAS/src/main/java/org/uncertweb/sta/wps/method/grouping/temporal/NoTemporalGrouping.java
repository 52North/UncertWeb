package org.uncertweb.sta.wps.method.grouping.temporal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationTime;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.IProcessInput;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;


public class NoTemporalGrouping extends TemporalGrouping {

	private class MappingIterator implements Iterator<ObservationMapping<ObservationTime>> {

		private Iterator<LinkedList<Observation>> i = null;
		
		public MappingIterator() {
			LinkedList<LinkedList<Observation>>	list = new LinkedList<LinkedList<Observation>>();
			for (Observation o : getObservations()) {
				/* due some mystery in HashMap we have to do it this way... */
				LinkedList<Observation> toAddTo = null;
				for (LinkedList<Observation> lo : list) {
					if (lo.element().getObservationTime().hashCode() == o.getObservationTime().hashCode()) {
						toAddTo = lo;
					}
				}
				if (toAddTo == null) {
					toAddTo = new LinkedList<Observation>();
					list.add(toAddTo);
				}
				toAddTo.add(o);
			}
			log.info("Got {} distinct ObservationTimes.", list.size());
			i = list.iterator();
		}
		
		@Override
		public boolean hasNext() {
			return i.hasNext();
		}

		@Override
		public ObservationMapping<ObservationTime> next() {
			LinkedList<Observation> list = i.next();
			return new ObservationMapping<ObservationTime>(list.element().getObservationTime(), list);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Iterator<ObservationMapping<ObservationTime>> iterator() {
		return new MappingIterator();
	}
	
	@Override
	public Set<IProcessInput<?>> getAdditionalInputDeclarations() {
		return Utils.set();
	}
	
	public NoTemporalGrouping(){}
	
	public NoTemporalGrouping(List<Observation> obs) {
		setInputs(obs, null);
	}

}
