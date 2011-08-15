package org.uncertweb.viss.core.wms;

import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.vis.Visualization;
import org.uncertweb.viss.core.vis.VisualizationReference;

public interface WMSAdapter {

	public boolean deleteResource(Resource res);

	public VisualizationReference addVisualization(Visualization vis);

	public boolean deleteVisualization(Visualization vis);

	public boolean setSldForVisualization(Visualization vis);

}
