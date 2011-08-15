package org.uncertweb.viss.core.vis.impl.om;

import org.uncertweb.viss.core.vis.impl.netcdf.normal.VarianceOfNormalDistribution;

public class VarianceOfNormalDistributionOfMultiCoverages extends
		AbstractOMVisualizer {

	public VarianceOfNormalDistributionOfMultiCoverages() {
		super(new VarianceOfNormalDistribution());
	}

}
