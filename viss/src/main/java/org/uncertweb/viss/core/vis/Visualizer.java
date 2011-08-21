package org.uncertweb.viss.core.vis;

import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.resource.Resource;

public interface Visualizer {
	
	public static final String JSON_KEY_DESCRIPTION = "description";
	public static final String JSON_KEY_TYPE = "type";
	public static final String JSON_TYPE_NUMBER = "number";
	public static final String JSON_KEY_REQUIRED = "required";

	public Set<MediaType> getCompatibleMediaTypes();

	public String getShortName();

	public String getDescription();

	public String getId(JSONObject params);

	public boolean isCompatible(Resource r);

	public JSONObject getOptions();

	public Visualization visualize(Resource r, JSONObject params);
}
