package org.uncertweb.viss.vis;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.vis.IVisualizer;

public abstract class AbstractVisualizer implements IVisualizer {

	protected static final Logger log = LoggerFactory
	    .getLogger(AbstractVisualizer.class);
	private Set<MediaType> compatibleTypes;
	private IResource resource;
	private JSONObject params;

	public AbstractVisualizer(MediaType... compatibleTypes) {
		this.compatibleTypes = Collections.unmodifiableSet(UwCollectionUtils
		    .set(compatibleTypes));
	}

	@Override
	public Set<MediaType> getCompatibleMediaTypes() {
		return this.compatibleTypes;
	}

	@Override
	public String getShortName() {
		return this.getClass().getName()
		    .replace(getClass().getPackage().getName() + ".", "").replace('$', '.');
	}

	@Override
	public String getId(JSONObject params) {
		return getShortName();
	}

	@Override
	public void setResource(IResource r) {
		this.resource = r;
	}

	@Override
	public IResource getResource() {
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
		return UwCollectionUtils.map();
	}

	@Override
	public Map<String, JSONObject> getOptionsForResource(IResource r) {
		return UwCollectionUtils.map();
	}

	@Override
	public IVisualization visualize(IResource r, JSONObject params) {
		setResource(r);
		setParams(params);
		return visualize();
	}

	protected String getCoverageName() {
		return this.getId(getParams());
	}

	protected abstract IVisualization visualize();
}