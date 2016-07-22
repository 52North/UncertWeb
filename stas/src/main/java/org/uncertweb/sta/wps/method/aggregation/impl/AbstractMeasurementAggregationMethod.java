package org.uncertweb.sta.wps.method.aggregation.impl;

import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;

public abstract class AbstractMeasurementAggregationMethod implements
		AggregationMethod {

	@Override
	@SuppressWarnings("unchecked")
	public IResult aggregate(List<? extends AbstractObservation> oc) {
		String uom = null;
		for (AbstractObservation ao : oc) {
			if (!(ao instanceof Measurement)) {
				throw new IllegalArgumentException(
						"Only compatible with Measurements.");
			}
			String unit = ((Measurement) ao).getResult().getUnitOfMeasurement();
			if (unit != null) {
				if (uom == null) {
					uom = unit;
				} else if (!uom.equals(unit)) {
					throw new RuntimeException("Not matching UoM: " + uom
							+ " vs.  " + unit + ".");
				}
			}
		}
		return new MeasureResult(aggregate1((List<Measurement>) oc), uom);
	}

	protected abstract double aggregate1(List<Measurement> oc);

}
