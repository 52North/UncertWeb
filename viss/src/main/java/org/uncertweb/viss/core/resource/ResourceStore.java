package org.uncertweb.viss.core.resource;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.uncertweb.viss.core.visualizer.Visualization;

public interface ResourceStore {

	public Resource get(UUID uuid);

	public void rm(Resource resource);

	public Resource add(InputStream is, MediaType mt);

	public Set<Resource> getAll();

	public Set<Resource> getResourcesUsedBefore(DateTime dt);

	public void saveVisualizationForResource(Resource r, Visualization v);

	void deleteVisualizationForResource(Resource r, Visualization v);

}