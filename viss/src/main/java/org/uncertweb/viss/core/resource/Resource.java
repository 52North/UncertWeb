package org.uncertweb.viss.core.resource;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.uncertweb.viss.core.visualizer.Visualization;

public interface Resource {

	public abstract UUID getUUID();

	public abstract MediaType getMediaType();

	public abstract Object getResource();

	public abstract void suspend();

	public Set<Visualization> getVisualizations();

	public void load() throws IOException;

	public boolean isLoaded();
}