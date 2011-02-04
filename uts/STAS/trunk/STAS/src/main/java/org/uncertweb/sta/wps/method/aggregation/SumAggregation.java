package org.uncertweb.sta.wps.method.aggregation;

import java.util.List;

import org.uncertweb.intamap.om.Observation;

/**
 * @author Christian Autermann
 */
public class SumAggregation implements AggregationMethod {

	@Override
	public double aggregate(List<Observation> oc) {
		if (oc.isEmpty()) 
			throw new RuntimeException("Can not aggregate empty ObservationCollection.");
		double result = 0;
		for (Observation o : oc) {
			result += o.getResult();
		}
		return result;
	}
}
