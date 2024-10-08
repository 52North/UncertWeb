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
package org.uncertweb.sta.wps.method.aggregation;

import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.sta.wps.method.aggregation.impl.ArithmeticMean;
import org.uncertweb.sta.wps.method.aggregation.impl.Median;
import org.uncertweb.sta.wps.method.aggregation.impl.Sum;

/**
 * Interface for aggregations methods. For simple implementations see
 * {@link ArithmeticMean}, {@link Sum} and
 * {@link Median}.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public interface AggregationMethod {

	/**
	 * Aggregates the given {@link Observation}s.
	 *
	 * @param oc the {@code Observation}s
	 * @return the aggregated result value
	 */
	public IResult aggregate(List<? extends AbstractObservation> oc);

}
