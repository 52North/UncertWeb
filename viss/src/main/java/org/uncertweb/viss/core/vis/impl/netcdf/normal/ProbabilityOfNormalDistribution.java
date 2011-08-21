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

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Utils;

public class ProbabilityOfNormalDistribution extends
		AbstractNormalDistributionVisualizer {

	public static final String DESCRIPTION = "Returns P(X <= max).";
	private static final String MAX_DESCRIPTION = "the (inclusive) upper bound";
	private static final String MAX_PARAMETER = "max";
	private static final JSONObject OPTIONS;

	static {
		JSONObject j = null;
		try {
			j = new JSONObject().put(MAX_PARAMETER, new JSONObject()
				.put(JSON_KEY_DESCRIPTION, MAX_DESCRIPTION)
				.put(JSON_KEY_TYPE, JSON_TYPE_NUMBER)
				.put(JSON_KEY_REQUIRED, true));
		} catch (JSONException e) {
			VissError.internal(e);
		} finally {
			OPTIONS = j;
		}
	}
	
	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public JSONObject getOptions() {
		return OPTIONS;
	}
	
	@Override
	protected String getUom() {
		return "%";
	}

	private double getMax() {
		return getMax(getParams());
	}

	private double getMax(JSONObject j) {
		try {
			return j.getDouble(MAX_PARAMETER);
		} catch (JSONException e) {
			throw VissError.invalidParameter(MAX_PARAMETER);
		}
	}

	@Override
	protected double evaluate(NormalDistribution nd) {
		try {
			return nd.cumulativeProbability(getMax());
		} catch (MathException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public String getId(JSONObject params) {
		return Utils.join("-", getShortName(), MAX_PARAMETER,
				String.valueOf(getMax(params)).replace('.', '-'));
	}

}
