package org.uncertweb.sta.wps.method.grouping.temporal;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AlgorithmParameterException;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationTime;
import org.uncertweb.intamap.om.ObservationTimeInstant;
import org.uncertweb.intamap.om.ObservationTimeInterval;
import org.uncertweb.intamap.utils.TimeUtils;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.ProcessInput;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;

public class TimeRangeGrouping extends TemporalGrouping {

//	private static final Logger log = LoggerFactory.getLogger(TimeRangeGrouping.class);
	
	protected static final ProcessInput TIME_RANGE = new ProcessInput(
			Constants.TIME_RANGE_INPUT_ID, Constants.TIME_RANGE_INPUT_TITLE,
			Constants.TIME_RANGE_INPUT_DESCRIPTION, LiteralStringBinding.class, 1, 1);

	private class TimeRangeMappingIterator implements Iterator<ObservationMapping<ObservationTime>> {

		private Iterator<Observation> iter;
		private Observation o;
		private Interval ci;
		
		public TimeRangeMappingIterator() {
			List<Observation> observations = getObservations();
			Collections.sort(observations, new ObservationTimeComparator());
			String  parameter = (String) getAdditionalInput(TIME_RANGE); 

			if (parameter == null || parameter.trim().isEmpty()) {
				throw new AlgorithmParameterException("Parameter \"" + TIME_RANGE.getIdentifier() + "\" not found.");
			}
			
			iter = observations.iterator();
			if (iter.hasNext()) { 
				o = iter.next();
				DateTime begin = null;
				if (o.getObservationTime() instanceof ObservationTimeInterval) {
					begin = ((ObservationTimeInterval) o.getObservationTime()).getStart();
				} else {
					begin = ((ObservationTimeInstant) o.getObservationTime()).getDateTime();
				}
				Period p = TimeUtils.parsePeriod(parameter).toPeriod();
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
	public Set<ProcessInput> getAdditionalInputDeclarations() {
		return Utils.set(TIME_RANGE);
	}

}
