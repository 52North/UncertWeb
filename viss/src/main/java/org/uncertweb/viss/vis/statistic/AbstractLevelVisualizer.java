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
package org.uncertweb.viss.vis.statistic;

import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertml.IUncertainty;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.util.JSONSchema;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyVisualizer;

public abstract class AbstractLevelVisualizer extends AbstractAnnotatedUncertaintyVisualizer {

	public static final String LEVEL_PARAMETER = "level";
	public static final String LEVEL_PARAMETER_DESCRIPTION = "the level";
	
	protected double evaluate(IUncertainty u) {
		try {
			return evaluate(u, getParams().getDouble(LEVEL_PARAMETER));
		} catch (JSONException e) {
			throw VissError.invalidParameter(LEVEL_PARAMETER);
		}
	}
	
	@Override
	public Map<String, JSONObject> getOptions() {
		Map<String, JSONObject> options = super.getOptions();
		try {
			options.put(LEVEL_PARAMETER, new JSONObject()
				.put(JSONSchema.Key.TYPE, JSONSchema.Type.ARRAY)
				.put(JSONSchema.Key.ITEMS, JSONSchema.Type.NUMBER)
				.put(JSONSchema.Key.ENUM, new JSONArray(getLevels()))
				.put(JSONSchema.Key.DESCRIPTION, LEVEL_PARAMETER_DESCRIPTION));
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
		return options;
	}
	
	@Override
	public Map<String, JSONObject> getOptionsForDataSet(IDataSet r) {
		Map<String, JSONObject> options = super.getOptionsForDataSet(r);
		try {
			options.get(LEVEL_PARAMETER).put(JSONSchema.Key.ENUM, getLevels());
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
		return options;
	}
	
	protected abstract Set<Double> getLevels();
	protected abstract double evaluate(IUncertainty u, double level);

}
