package org.uncertweb.sta.wps.method.grouping.temporal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationTime;
import org.uncertweb.intamap.om.ObservationTimeInstant;
import org.uncertweb.intamap.om.ObservationTimeInterval;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.IProcessInput;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;

public class TimeRangeGrouping extends TemporalGrouping {

	protected static final Logger log = LoggerFactory.getLogger(TimeRangeGrouping.class);
	
	private class TimeRangeMappingIterator implements Iterator<ObservationMapping<ObservationTime>> {

		private Iterator<Observation> iter;
		private Observation o;
		private Interval ci;
		
		public TimeRangeMappingIterator() {
			List<Observation> observations = getObservations();
			Collections.sort(observations, new ObservationTimeComparator());
			Period p = (Period) getInputs().get(Constants.Process.Inputs.TIME_RANGE);

			iter = observations.iterator();
			if (iter.hasNext()) { 
				o = iter.next();
				DateTime begin = null;
				if (o.getObservationTime() instanceof ObservationTimeInterval) {
					begin = ((ObservationTimeInterval) o.getObservationTime()).getStart();
				} else {
					begin = ((ObservationTimeInstant) o.getObservationTime()).getDateTime();
				}
				ci = new Interval(begin, p);
			}
		}
		
		@Override
		public boolean hasNext() {
			return (o != null);
		}

		@Override
		public ObservationMapping<ObservationTime> next() {
			LinkedList<Observation> obs = new LinkedList<Observation>();
			while (hasNext() && isInRange(ci, o)) {
				obs.add(o);
				if (iter.hasNext()) 
					o = iter.next();
				else 
					o = null;
			}
			ObservationMapping<ObservationTime> mapping = 
				new ObservationMapping<ObservationTime>(new ObservationTimeInterval(ci), obs);
			ci = new Interval(ci.getEnd(), ci.toPeriod());			
			return mapping;
		}
		
		private boolean isInRange(Interval time, Observation test) {
			if (test.getObservationTime() instanceof ObservationTimeInterval) {
				return time.contains(((ObservationTimeInterval) test.getObservationTime()).getInterval());
			} else {
				return time.contains((((ObservationTimeInstant) test.getObservationTime()).getDateTime()));
			}
		}

		@Override
		public void remove() { throw new UnsupportedOperationException(); }
		
	}
	
	@Override
	public Iterator<ObservationMapping<ObservationTime>> iterator() {
		return new TimeRangeMappingIterator();
	}

	
	@Override
	public Set<IProcessInput<?>> getAdditionalInputDeclarations() {
		HashSet<IProcessInput<?>> set = new HashSet<IProcessInput<?>>();
		set.add(Constants.Process.Inputs.TIME_RANGE);
		return set;
	}
}
