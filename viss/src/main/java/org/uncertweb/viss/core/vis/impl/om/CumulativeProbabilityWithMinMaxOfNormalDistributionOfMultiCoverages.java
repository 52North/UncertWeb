package org.uncertweb.viss.core.vis.impl.om;

import org.uncertweb.viss.core.vis.impl.netcdf.normal.CumulativeProbabilityWithMinMaxOfNormalDistribution;

public class CumulativeProbabilityWithMinMaxOfNormalDistributionOfMultiCoverages
		extends AbstractOMVisualizer {

	public CumulativeProbabilityWithMinMaxOfNormalDistributionOfMultiCoverages() {
		super(new CumulativeProbabilityWithMinMaxOfNormalDistribution());
	}

}
