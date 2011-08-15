package org.uncertweb.viss.core.vis.impl.om;

import org.uncertweb.viss.core.vis.impl.netcdf.normal.MeanOfNormalDistribution;

public class MeanOfNormalDistributionOfMultiCoverages extends
		AbstractOMVisualizer {

	public MeanOfNormalDistributionOfMultiCoverages() {
		super(new MeanOfNormalDistribution());
	}

}
