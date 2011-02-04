package org.uncertweb.sta.wps.method.grouping.temporal;

import java.util.Comparator;

import org.joda.time.DateTime;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationTimeInstant;
import org.uncertweb.intamap.om.ObservationTimeInterval;

public class ObservationTimeComparator implements Comparator<Observation> {
	
	@Override
	public int compare(Observation o1, Observation o2) {
		DateTime a, b;
		a = (o1.getObservationTime() instanceof ObservationTimeInstant) ? 
			((ObservationTimeInstant) o1.getObservationTime()).getDateTime() : 
			((ObservationTimeInterval) o1.getObservationTime()).getStart();
		b = (o2.getObservationTime() instanceof ObservationTimeInstant) ? 
			((ObservationTimeInstant) o2.getObservationTime()).getDateTime() :
			((ObservationTimeInterval) o2.getObservationTime()).getStart();
		return a.compareTo(b);
	}
}