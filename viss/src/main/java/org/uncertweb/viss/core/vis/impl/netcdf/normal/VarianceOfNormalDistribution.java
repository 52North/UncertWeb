package org.uncertweb.viss.core.vis.impl.netcdf.normal;

import org.apache.commons.math.distribution.NormalDistribution;

public class VarianceOfNormalDistribution extends
		AbstractNormalDistributionVisualizer {

	@Override
	protected double evaluate(NormalDistribution nd) {
		return nd.getStandardDeviation();
	}

}
