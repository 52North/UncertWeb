package org.uncertweb.viss.core.web;

import org.codehaus.jettison.json.JSONObject;

public class VisualizationRequest {

	private JSONObject parameters;
	private String visualizer;

	public VisualizationRequest(JSONObject parameters, String visualizer) {
		this.parameters = parameters;
		this.visualizer = visualizer;
	}

	public JSONObject getParameters() {
		return parameters;
	}

	public String getVisualizer() {
		return visualizer;
	}

}
