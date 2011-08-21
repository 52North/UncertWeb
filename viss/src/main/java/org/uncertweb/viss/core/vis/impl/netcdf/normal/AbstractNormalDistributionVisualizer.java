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

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.util.FastMath;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.impl.netcdf.AbstractNetCDFVisualizer;

public abstract class AbstractNormalDistributionVisualizer extends
		AbstractNetCDFVisualizer {

	private static final Set<URI> NEEDED = Collections.unmodifiableSet(Utils.set(
			Constants.NORMAL_DISTRIBUTION_MEAN, Constants.NORMAL_DISTRIBUTION_VARIANCE));

	@Override
	public JSONObject getOptions() {
		return new JSONObject();
	}

	@Override
	protected String getCoverageName() {
		return "Variance";
	}
	
	@Override
	protected Set<URI> hasToHaveOneOf() {
		return Collections.emptySet();
	}
	
	@Override
	protected Set<URI> hasToHaveAll() {
		return NEEDED;
	}
	
	@Override
	protected double evaluate(Map<URI, Double> values) {
		double m = values.get(Constants.NORMAL_DISTRIBUTION_MEAN).doubleValue();
		double v = values.get(Constants.NORMAL_DISTRIBUTION_VARIANCE).doubleValue();
		return evaluate(new NormalDistributionImpl(m, FastMath.sqrt(v)));
	}

	protected abstract double evaluate(NormalDistribution nd);

}
