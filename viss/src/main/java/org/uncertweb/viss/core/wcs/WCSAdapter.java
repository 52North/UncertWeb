package org.uncertweb.viss.core.wcs;

import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.visualizer.Visualization;
import org.uncertweb.viss.core.visualizer.VisualizationReference;

public interface WCSAdapter {

	public boolean deleteResource(Resource res);

	public VisualizationReference addVisualization(Visualization vis);

	public boolean deleteVisualization(Visualization vis);

	public boolean setSldForVisualization(Visualization vis);

}
