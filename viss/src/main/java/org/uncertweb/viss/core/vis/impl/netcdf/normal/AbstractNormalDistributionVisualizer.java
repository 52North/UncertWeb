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
package org.uncertweb.viss.core.vis.impl.netcdf.normal;

import java.util.Set;

import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.util.FastMath;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.impl.netcdf.AbstractNetCDFVisualizer;
import org.uncertweb.viss.core.vis.impl.netcdf.UncertaintyNetCDF.UncertaintyType;
import org.uncertweb.viss.core.vis.impl.netcdf.UncertaintyNetCDF.Value;

public abstract class AbstractNormalDistributionVisualizer extends
		AbstractNetCDFVisualizer {

	private static final int TIMES_STANDARD_DEVIATION = 3;

	@Override
	protected double evaluate(IUncertainty u) {
		NormalDistribution nd = (NormalDistribution) u;
		return evaluate(new NormalDistributionImpl(nd.getMean().get(0),
				FastMath.sqrt(nd.getVariance().get(0))));
	}

	@Override
	protected Set<UncertaintyType> getSupportedUncertainties() {
		return Utils.set(UncertaintyType.NORMAL_DISTRIBUTION);
	}

	protected double[] getRange(Resource r) {
		return getRange(r, TIMES_STANDARD_DEVIATION);
	}

	protected double[] getRange(Resource r, int tsd) {

		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for (Value val : getNetCDF(r)) {
			if (val.getValue() != null) {
				NormalDistribution nd = (NormalDistribution) val.getValue();
				double m = nd.getMean().get(0);
				double sd = FastMath.sqrt(nd.getVariance().get(0));
				min = FastMath.min(min, m - tsd * sd);
				max = FastMath.max(max, m + tsd * sd);
			}
		}

		return new double[] { min, max };
	}

	protected abstract double evaluate(
			org.apache.commons.math.distribution.NormalDistribution nd);

}
