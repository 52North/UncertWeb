package org.uncertweb.viss.core.wcs;

import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.visualizer.Visualization;
import org.uncertweb.viss.core.visualizer.VisualizationReference;

public interface WCSAdapter {

	public boolean rm(Resource uuid) throws VissError;

	public VisualizationReference add(Visualization vis) throws VissError;

}
