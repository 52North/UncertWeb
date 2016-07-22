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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertml.IUncertainty;
import org.uncertml.statistic.ContinuousStatistic;
import org.uncertml.statistic.IStatistic;
import org.uncertml.statistic.StatisticCollection;
import org.uncertweb.netcdf.NcUwObservation;
import org.uncertweb.netcdf.NcUwUncertaintyType;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.util.JSONSchema;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyVisualizer;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyVisualizer.Description;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyVisualizer.Id;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyVisualizer.Type;

@Id("StatisticCollectionVisualizer")
@Type(NcUwUncertaintyType.STATISTIC_COLLECTION)
@Description("Visualizes a Statistic Collection")
public class StatisticCollectionVisualizer extends AbstractAnnotatedUncertaintyVisualizer {

	public static final String STATISTIC_PARAMETER = "statistic";
	public static final String STATISTIC_PARAMETER_DESCRIPTION = "The statistic to visualize.";

	@Override
	protected double evaluate(IUncertainty u) {
		String selected;
		try {
			selected = getParams().getString(STATISTIC_PARAMETER);
		} catch (JSONException e) {
			throw VissError.invalidParameter(STATISTIC_PARAMETER);
		}
		StatisticCollection col = (StatisticCollection) u;
		ContinuousStatistic tovis = null;
		for (IStatistic s : col) {
			if (s instanceof ContinuousStatistic && s.getClass().getName().equals(selected)) {
				tovis = (ContinuousStatistic) s;
				break;
			}
		}
		return tovis.getValues().get(0);
	}

	private Set<String> getStatistics(IDataSet r) {
		/* find a valid statistics collection */
		Iterator<NcUwObservation> i = getIteratorForDataSet(r);
		NcUwObservation o = null;
		while (o == null && i.hasNext()) {
			NcUwObservation candidate = i.next();
			if (candidate.hasValue()) {
				o = candidate;
			}
		}

		if (o == null) {
			throw VissError.internal("No valid Observation.");
		}

		StatisticCollection col = (StatisticCollection) o.getResult()
				.getValue();
		Set<String> statistics = UwCollectionUtils.set();
		for (IStatistic s : col) {
			if (s instanceof ContinuousStatistic) {
				statistics.add(s.getClass().getName());
			}
		}
		return statistics;
	}

	private JSONObject createStatisticOption() {
		try {
			return new JSONObject()
					.put(JSONSchema.Key.TYPE, JSONSchema.Type.STRING)
					.put(JSONSchema.Key.DESCRIPTION,
						 STATISTIC_PARAMETER_DESCRIPTION)
					.put(JSONSchema.Key.REQUIRED, true);
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public Map<String, JSONObject> getOptions() {
		Map<String, JSONObject> options = super.getOptions();
		options.put(STATISTIC_PARAMETER, createStatisticOption());
		return options;

	}

	@Override
	public Map<String, JSONObject> getOptionsForDataSet(IDataSet r) {
		try {
			Map<String, JSONObject> options = super.getOptionsForDataSet(r);
			options.put(STATISTIC_PARAMETER,
						createStatisticOption().put(JSONSchema.Key.ENUM,
													new JSONArray(getStatistics(r))));
			return options;
		} catch (JSONException e) {
			throw VissError.internal(e);
		}

	}
}
