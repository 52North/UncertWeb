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
package org.uncertweb.viss.vis.sample;

import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertml.IUncertainty;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.util.JSONSchema;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer.Description;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer.Id;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer.Type;
import org.uncertweb.viss.vis.netcdf.UncertaintyVariable;

@Id("Realisation")
@Description("Visualizers a Realisation")
@Type(UncertaintyType.CONTINUOUS_REALISATION)
public class RealisationVisualizer extends
		AbstractAnnotatedUncertaintyViusalizer {

	public static final String REALISATION_PARAMETER = "realisation";
	public static final String REALISATION_PARAMETER_DESCRIPTION = "The Realisation to visualize";
	
	@Override
	protected double evaluate(IUncertainty u) {
		return ((ContinuousRealisation) u).getValues().get(getIndex());
	}

	@Override
	public Map<String, JSONObject> getOptions() {
		Map<String, JSONObject> j = super.getOptions();
		try {
			j.put(REALISATION_PARAMETER,
					new JSONObject()
							.put(JSONSchema.Key.TYPE, JSONSchema.Type.INTEGER)
							.put(JSONSchema.Key.MINIMUM, 0)
							.put(JSONSchema.Key.DESCRIPTION, REALISATION_PARAMETER_DESCRIPTION));
			return j;
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}
	
	@Override
	public Map<String, JSONObject> getOptionsForDataSet(IDataSet r) {
		try {
			Map<String, JSONObject> options = super.getOptionsForDataSet(r);
			Object o = r.getContent();
			if (o instanceof UncertaintyVariable) {
				ContinuousRealisation real = (ContinuousRealisation) ((UncertaintyVariable) r
						.getContent()).iterator().next().getValue();
				options.get(REALISATION_PARAMETER)
				.put(JSONSchema.Key.MAXIMUM,
						real.getValues().size()-1);
			} else {
				// geotiff... (just one realisation)
			}
			return options;
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
		
	}

	protected int getIndex() {
		try {
			return getParams().getInt(REALISATION_PARAMETER);
		} catch (JSONException e) {
			throw VissError.invalidParameter(REALISATION_PARAMETER);
		}
	}

}
