package org.uncertweb.sta.wps.method.grouping.temporal;

import java.util.Iterator;
import java.util.Set;

import org.joda.time.DateTime;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationTime;
import org.uncertweb.intamap.om.ObservationTimeInstant;
import org.uncertweb.intamap.om.ObservationTimeInterval;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.ProcessInput;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;

public class IgnoreTimeGrouping extends TemporalGrouping {

	@Override
	public Iterator<ObservationMapping<ObservationTime>> iterator() {
		DateTime start = null, end = null;
		log.info("Calculating TimeRange for {} Observations.", getObservations().size());
		for (Observation o : getObservations()) {
			if (o.getObservationTime() instanceof ObservationTimeInterval) {
				ObservationTimeInterval time = (ObservationTimeInterval) o.getObservationTime();
				if (start == null || time.getStart().isBefore(start)) {
					start = time.getStart();
				}
				if (end == null || time.getEnd().isAfter(end)) {
					end = time.getEnd();
				}
			} else {
				ObservationTimeInstant time = (ObservationTimeInstant) o.getObservationTime();
				if (start == null || time.isBefore(start)) {
					start = time.getDateTime();
				}
				if (end == null || time.isAfter(end)) {
					end = time.getDateTime();
				}
			}
		}
		ObservationTime time = (start.equals(end)) ? new ObservationTimeInstant(start)
		 										   : new ObservationTimeInterval(start, end);
		return Utils.mutableSingletonList(new ObservationMapping<ObservationTime>(time, getObservations())).iterator();
	}

	@Override
	public Set<ProcessInput> getAdditionalInputDeclarations() {
		return Utils.set();
	}

}
