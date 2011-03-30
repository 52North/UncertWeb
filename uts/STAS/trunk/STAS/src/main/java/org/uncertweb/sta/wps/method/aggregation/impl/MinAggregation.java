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

import java.util.List;

import org.uncertweb.intamap.om.Observation;
import org.uncertweb.sta.utils.Constants.MethodNames.Aggregation;
import org.uncertweb.sta.wps.api.annotation.SpatialAggregationFunction;
import org.uncertweb.sta.wps.api.annotation.TemporalAggregationFunction;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;

/**
 * Method which uses the minimal result value to aggregate {@link Observation}s.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
@SpatialAggregationFunction(Aggregation.Spatial.MINIMUM)
@TemporalAggregationFunction(Aggregation.Temporal.MINIMUM)
public class MinAggregation implements AggregationMethod {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double aggregate(List<Observation> oc) {
		double min = Double.POSITIVE_INFINITY;
		for (Observation o : oc) {
			if (o.getResult() > min) {
				min = o.getResult();
			}
		}
		return min;
	}

}