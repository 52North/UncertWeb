package org.uncertweb.viss.core.resource;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.uncertweb.viss.core.vis.Visualization;

public interface ResourceStore {

	public Resource get(UUID uuid);

	public void deleteResource(Resource resource);

	public Resource addResource(InputStream is, MediaType mt);

	public Set<Resource> getAllResources();

	public Set<Resource> getResourcesUsedBefore(DateTime dt);

	public void saveResource(Resource r);

	public void deleteVisualizationForResource(Resource r, Visualization v);

}