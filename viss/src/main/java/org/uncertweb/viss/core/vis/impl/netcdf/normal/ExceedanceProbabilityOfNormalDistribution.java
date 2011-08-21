package org.uncertweb.viss.core.vis.impl.netcdf.normal;

import org.apache.commons.math.distribution.NormalDistribution;

public class ExceedanceProbabilityOfNormalDistribution extends
		ProbabilityOfNormalDistribution {

	private static final String DESCRIPTION = "Returns 1-P(X <= max).";
	
	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	protected double evaluate(NormalDistribution nd) {
		return 1 - super.evaluate(nd);
	}

}
