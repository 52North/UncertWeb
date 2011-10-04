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
package org.uncertweb.viss.vis.distribution;

import org.apache.commons.math.distribution.WeibullDistributionImpl;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.WeibullDistribution;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer.Type;

@Type(UncertaintyType.WEIBULL_DISTRIBUTION)
public abstract class AbstractWeibullDistributionVisualizer extends
    AbstractDistributionVisualizer {
	@Override
	public double evaluate(IUncertainty u) {
		WeibullDistribution d = (WeibullDistribution) u;
		return evaluate(new WeibullDistributionImpl(d.getShape().get(0), d
		    .getScale().get(0)));
	}

	protected abstract double evaluate(WeibullDistributionImpl d);
}