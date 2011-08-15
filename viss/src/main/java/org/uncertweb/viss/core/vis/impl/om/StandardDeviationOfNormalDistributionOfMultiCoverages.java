package org.uncertweb.viss.core.vis.impl.om;

import org.uncertweb.viss.core.vis.impl.netcdf.normal.StandardDeviationOfNormalDistribution;

public class StandardDeviationOfNormalDistributionOfMultiCoverages extends
		AbstractOMVisualizer {

	public StandardDeviationOfNormalDistributionOfMultiCoverages() {
		super(new StandardDeviationOfNormalDistribution());
	}

}
