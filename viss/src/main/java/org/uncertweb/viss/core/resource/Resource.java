package org.uncertweb.viss.core.resource;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.uncertweb.viss.core.vis.Visualization;

public interface Resource {

	public UUID getUUID();

	public MediaType getMediaType();

	public Object getResource();

	public Set<Visualization> getVisualizations();

	public void addVisualization(Visualization v);

	public String getPhenomenon();

	public void load() throws IOException;

	public void suspend();

	public boolean isLoaded();
}