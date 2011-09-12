package org.uncertweb.viss.vis.distribution;

import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer;

public abstract class AbstractDistributionVisualizer extends
		AbstractAnnotatedUncertaintyViusalizer {

	@Override
	public String getShortName() {
		return "Distribution-"
				+ getCompatibleUncertaintyTypes().iterator().next().clazz
						.getSimpleName().replace("Distribution", "-")
				+ getClass().getSimpleName();
	}
}
