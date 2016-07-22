/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24,
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.sta.wps.method.aggregation.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.sta.utils.Constants.MethodNames.Aggregation;
import org.uncertweb.sta.wps.api.annotation.SpatialAggregationFunction;
import org.uncertweb.sta.wps.api.annotation.TemporalAggregationFunction;

/**
 * Method which uses the median to aggregate {@link Observation}s.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
@SpatialAggregationFunction(Aggregation.Spatial.MEDIAN)
@TemporalAggregationFunction(Aggregation.Temporal.MEDIAN)
public class Median extends AbstractMeasurementAggregationMethod {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected double aggregate1(List<Measurement> oc) {
		if (oc.isEmpty())
			throw new RuntimeException(
					"Can not aggregate empty ObservationCollection.");
		Collections.sort(oc, new Comparator<Measurement>() {
			@Override
			public int compare(Measurement o1, Measurement o2) {
				return Double.compare(o1.getResult().getMeasureValue(), o2.getResult().getMeasureValue());
			}
		});
		int size = oc.size();
		if (size % 2 == 0) {
			return 0.5 * (oc.get((size / 2) - 1).getResult().getMeasureValue() + oc.get(
					((size / 2) + 1) - 1).getResult().getMeasureValue());
		} else {
			return oc.get(((size + 1) / 2) - 1).getResult().getMeasureValue();
		}
	}
}
