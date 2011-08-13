package org.uncertweb.viss.visualizer;


import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.visualizer.Visualizer.ShortName;

@ShortName("MeanVisualizer")
public class MeanVisualizer extends AbstractNetCDFVisualizer {
	
	@Override
	public JSONObject getOptions() {
		return new JSONObject();
	}

	@Override
	protected String getCoverageName() {
		return "Mean";
	}

	@Override
	protected Set<URI> hasToHaveOneOf() {
		return Utils.set(Constants.NORMAL_DISTRIBUTION_MEAN);
	}

	@Override
	protected Set<URI> hasToHaveAll() {
		return Utils.set(Constants.NORMAL_DISTRIBUTION_MEAN);
	}
	
	@Override
	protected Set<URI> getRelevantURIs() {
		return Utils.set(Constants.NORMAL_DISTRIBUTION_MEAN);
	}

	@Override
	protected double evaluate(Map<URI, Double> values) {
		return values.get(Constants.NORMAL_DISTRIBUTION_MEAN).doubleValue();
	}

}
