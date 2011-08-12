package org.uncertweb.viss.core.visualizer;

import java.util.HashMap;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.JSON;

public class VisualizerDescriptions extends
		HashMap<String, VisualizerDescription> implements JSON {
	private static final long serialVersionUID = -3483697236625652640L;

	@Override
	public JSONObject toJSON() throws JSONException {

		JSONObject j = new JSONObject();
		for (String v : keySet()) {
			j.append("visualizers", get(v).toJSON().put("id", v));
		}
		return j;
	}

	@Override
	public String toJSONString(boolean format) throws JSONException {
		return format ? toJSON().toString(DEFAULT_INTENDATION) : toJSON()
				.toString();
	}
}
