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
package org.uncertweb.viss.vis.distribution.normal;

import java.util.Map;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.util.JSONSchema;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.vis.AbstractNormalDistributionVisualizer;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer.Description;

@Description("Returns P(min <= X <= max).")
public class ProbabilityForInterval extends
    AbstractNormalDistributionVisualizer {
	private static final String MIN_DESCRIPTION = "the (inclusive) lower bound";
	private static final String MAX_DESCRIPTION = "the (inclusive) upper bound";
	private static final String MIN_PARAMETER = "min";
	private static final String MAX_PARAMETER = "max";

	private static JSONObject createMinOption() throws JSONException {
		return new JSONObject().put(JSONSchema.Key.DESCRIPTION, MIN_DESCRIPTION)
		    .put(JSONSchema.Key.TYPE, JSONSchema.Type.NUMBER)
		    .put(JSONSchema.Key.REQUIRED, true);
	}

	private static JSONObject createMaxOption() throws JSONException {

		return new JSONObject().put(JSONSchema.Key.DESCRIPTION, MAX_DESCRIPTION)
		    .put(JSONSchema.Key.TYPE, JSONSchema.Type.NUMBER)
		    .put(JSONSchema.Key.REQUIRED, true);
	}

	@Override
	public String getUom() {
		return "%";
	}

	@Override
	public Map<String, JSONObject> getOptionsForResource(IResource r) {
		try {
			Map<String, JSONObject> options = Utils.map();
			double[] minmax = getRange(r);
			options.put(
			    MIN_PARAMETER,
			    createMinOption().put(JSONSchema.Key.MINIMUM, minmax[0]).put(
			        JSONSchema.Key.MAXIMUM, minmax[1]));
			options.put(
			    MAX_PARAMETER,
			    createMaxOption().put(JSONSchema.Key.MINIMUM, minmax[0]).put(
			        JSONSchema.Key.MAXIMUM, minmax[1]));
			return options;
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

	private double getMin() {
		return getMin(getParams());
	}

	private double getMin(JSONObject j) {
		try {
			return j.getDouble(MIN_PARAMETER);
		} catch (JSONException e) {
			throw VissError.invalidParameter(MIN_PARAMETER);
		}
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
	protected double evaluate(NormalDistributionImpl nd) {
		try {
			return nd.cumulativeProbability(getMin(), getMax());
		} catch (MathException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public String getId(JSONObject params) {
		return Utils.join("-", getShortName(), MIN_PARAMETER,
		    String.valueOf(getMin(params)).replace('.', '-'), MAX_PARAMETER, String
		        .valueOf(getMax(params)).replace('.', '-'));
	}

	@Override
	public Map<String, JSONObject> getOptions() {
		try {
			Map<String, JSONObject> options = Utils.map();
			options.put(MIN_PARAMETER, createMinOption());
			options.put(MAX_PARAMETER, createMaxOption());
			return options;
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

}