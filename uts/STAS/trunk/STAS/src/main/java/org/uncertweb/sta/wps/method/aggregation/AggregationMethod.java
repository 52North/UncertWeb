package org.uncertweb.sta.wps.method.aggregation;

import java.util.List;

import org.uncertweb.intamap.om.Observation;

/**
 * @author Christian Autermann
 */
public interface AggregationMethod {

	public abstract double aggregate(List<Observation> oc);

}
