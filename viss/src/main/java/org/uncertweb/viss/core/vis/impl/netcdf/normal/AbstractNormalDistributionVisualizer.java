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
