package org.uncertweb.sta.wps.method.aggregation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.uncertweb.intamap.om.Observation;

/**
 * @author Christian Autermann
 */
public class MedianAggregation implements AggregationMethod {

	@Override
	public double aggregate(List<Observation> oc) {
		if (oc.isEmpty())
			throw new RuntimeException("Can not aggregate empty ObservationCollection.");
		Collections.sort(oc, new Comparator<Observation>() {
			@Override
			public int compare(Observation o1, Observation o2) {
				return Double.compare(o1.getResult(), o2.getResult());
			}
		});
		int size = oc.size();
		if (size % 2 == 0) {
			return 0.5 * (oc.get((size / 2) - 1).getResult() + oc.get(
					((size / 2) + 1) - 1).getResult());
		} else {
			return oc.get(((size + 1) / 2) - 1).getResult();
		}
	}
}
