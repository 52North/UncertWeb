package org.uncertweb.viss.core;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public interface JSON {

	public static final int DEFAULT_INTENDATION = 2;

	public JSONObject toJSON() throws JSONException;

	public String toJSONString(boolean format) throws JSONException;
}
