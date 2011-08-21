package org.uncertweb.viss.core.vis.impl.netcdf.normal;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Utils;

public class ProbabilityForIntervalOfNormalDistribution extends
		AbstractNormalDistributionVisualizer {

	private static final String DESCRIPTION = "Returns P(min <= X <= max).";
	private static final String MIN_DESCRIPTION = "the (inclusive) lower bound";
	private static final String MAX_DESCRIPTION = "the (inclusive) upper bound";
	private static final String MIN_PARAMETER = "min";
	private static final String MAX_PARAMETER = "max";
	private static final JSONObject OPTIONS;
	
	static {
		JSONObject j = null;
		try {
			j = new JSONObject()
			.put(MIN_PARAMETER, new JSONObject()
				.put(JSON_KEY_DESCRIPTION, MIN_DESCRIPTION)
				.put(JSON_KEY_TYPE, JSON_TYPE_NUMBER)
				.put(JSON_KEY_REQUIRED, true))
			.put(MAX_PARAMETER, new JSONObject()
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
	public JSONObject getOptions() {
		return OPTIONS;
	}
	
	@Override
	public String getDescription() {
		return DESCRIPTION;
	}
	
	@Override
	public String getUom() {
		return "%";
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
	protected double evaluate(NormalDistribution nd) {
		try {
			return nd.cumulativeProbability(getMin(), getMax());
		} catch (MathException e) {
			throw VissError.internal(e);
		}
	}
	
	@Override
	public String getId(JSONObject params) {
		return Utils.join("-", getShortName(), 
				MIN_PARAMETER, String.valueOf(getMin(params)).replace('.', '-'), 
				MAX_PARAMETER, String.valueOf(getMax(params)).replace('.', '-'));
	}

}
