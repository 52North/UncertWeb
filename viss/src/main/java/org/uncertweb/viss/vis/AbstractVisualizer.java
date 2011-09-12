package org.uncertweb.viss.vis;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.Visualization;
import org.uncertweb.viss.core.vis.Visualizer;

public abstract class AbstractVisualizer implements Visualizer {
	
	protected static final Logger log = LoggerFactory.getLogger(AbstractVisualizer.class);
	private Set<MediaType> compatibleTypes;
	private Resource resource;
	private JSONObject params;
	
	public AbstractVisualizer(MediaType... compatibleTypes) {
		this.compatibleTypes = Collections.unmodifiableSet(Utils.set(compatibleTypes));
	}
	
	@Override
	public Set<MediaType> getCompatibleMediaTypes() {
		return this.compatibleTypes;
	}

	@Override
	public String getShortName() {
		return this.getClass().getName()
				.replace(getClass().getPackage().getName() + ".", "")
				.replace('$', '.');
	}

	@Override
	public String getId(JSONObject params) {
		return getShortName();
	}

	@Override
	public void setResource(Resource r) {
		this.resource = r;
	}

	@Override
	public Resource getResource() {
		return this.resource;
	}
	
	protected JSONObject getParams() {
		return this.params;
	}
	
	protected void setParams(JSONObject params) {
		this.params = params;
	}
	
	@Override
	public Map<String, JSONObject> getOptions() {
		return Utils.map();
	}
	
	@Override
	public Map<String, JSONObject> getOptionsForResource(Resource r) {
		return Utils.map();
	}
	
	@Override
	public Visualization visualize(Resource r, JSONObject params) {
		setResource(r);
		setParams(params);
		return visualize();
	}
	
	protected String getCoverageName() {
		return this.getId(getParams());
	}

	protected abstract Visualization visualize();
}