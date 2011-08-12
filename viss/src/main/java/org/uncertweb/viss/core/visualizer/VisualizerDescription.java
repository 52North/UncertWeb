package org.uncertweb.viss.core.visualizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.JSON;

public class VisualizerDescription extends HashMap<String, JSONObject>
		implements JSON {
	private static final long serialVersionUID = 4098947043165834052L;
	private String description;

	/**
	 * @param options
	 * @param description
	 */
	public VisualizerDescription(Map<String, JSONObject> options,
			String description) {
		this.putAll(options);
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject j = new JSONObject().putOpt("description", getDescription());
		for (Entry<String, JSONObject> e : entrySet()) {
			j.append("options",
					new JSONObject().putOpt(e.getKey(), e.getValue()));
		}
		return j;
	}

	@Override
	public String toJSONString(boolean format) throws JSONException {
		return format ? toJSON().toString(DEFAULT_INTENDATION) : toJSON()
				.toString();
	}
}
