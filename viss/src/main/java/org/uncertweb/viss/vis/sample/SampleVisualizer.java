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
import org.uncertml.io.JSONEncoder;
import org.uncertml.sample.AbstractRealisation;
import org.uncertml.sample.AbstractSample;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.RandomSample;
import org.uncertweb.netcdf.NcUwVariableWithDimensions;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.util.JSONSchema;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer.Description;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer.Id;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer.Type;

@Id("Sample")
@Description("Visualizes a Sample")
@Type({
	UncertaintyType.UNKNOWN_SAMPLE,
	UncertaintyType.SYSTEMATIC_SAMPLE,
	UncertaintyType.RANDOM_SAMPLE
})
public class SampleVisualizer extends
		AbstractAnnotatedUncertaintyViusalizer {

	public static final String REALISATION_PARAMETER = "realisation";
	public static final String REALISATION_PARAMETER_DESCRIPTION = "The Realisation to visualize";

	public static final String SAMPLE_PARAMETER = "sample";
	public static final String SAMPLE_PARAMETER_DESCRIPTION = "The sample to visualize";
	
	@Override
	protected double evaluate(IUncertainty u) {
		return valueAt(u, getSampleIndex(), getRealisationIndex());
	}
	
	protected double valueAt(IUncertainty u, int sample, int realisation) {
		AbstractSample as = (AbstractSample) u;
		AbstractRealisation ar = as.getRealisations().get(sample);
		ContinuousRealisation cr = (ContinuousRealisation) ar;
		return cr.getValues().get(realisation).doubleValue();
	}

	@Override
	public Map<String, JSONObject> getOptions() {
		Map<String, JSONObject> j = super.getOptions();
		try {
			j.put(SAMPLE_PARAMETER, 
					new JSONObject()
							.put(JSONSchema.Key.TYPE, JSONSchema.Type.INTEGER)
							.put(JSONSchema.Key.MINIMUM, 0)
							.put(JSONSchema.Key.DESCRIPTION, SAMPLE_PARAMETER_DESCRIPTION));
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
			NcUwVariableWithDimensions uv = ((NcUwVariableWithDimensions) r.getContent());
			AbstractSample as = (AbstractSample) uv.iterator().next().getResult().getValue();
			AbstractRealisation ar = as.getRealisations().get(0);
			ContinuousRealisation cr = (ContinuousRealisation) ar;
			
			options.get(SAMPLE_PARAMETER).put(JSONSchema.Key.MAXIMUM, as.getRealisations().size() - 1);
			options.get(REALISATION_PARAMETER).put(JSONSchema.Key.MAXIMUM, cr.getValues().size() - 1);
			
			return options;
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
		
	}

	protected int getSampleIndex() {
		try {
			return getParams().getInt(SAMPLE_PARAMETER);
		} catch (JSONException e) {
			throw VissError.invalidParameter(SAMPLE_PARAMETER);
		}
	}
	
	protected int getRealisationIndex() {
		try {
			return getParams().getInt(REALISATION_PARAMETER);
		} catch (JSONException e) {
			throw VissError.invalidParameter(REALISATION_PARAMETER);
		}
	}

	public static void main(String[] args) {
	IUncertainty u = new RandomSample(new AbstractRealisation[] { 
			new ContinuousRealisation(new double[] { 1.1, 1.2, 1.3 }),
			new ContinuousRealisation(new double[] { 1.0, 1.1, 1.2 })
	});
	System.out.println(new JSONEncoder().encode(u));
	}
	
}
