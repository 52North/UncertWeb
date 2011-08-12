package org.uncertweb.viss.core;

import java.util.UUID;
import java.util.concurrent.Callable;
import org.uncertweb.viss.core.visualizer.Visualization;

public abstract class VisualizationCreationTask implements
		Callable<Visualization> {

	private UUID uuid;

	public UUID getUUID() {
		return this.uuid;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public final Visualization call() throws Exception {
		return this.createVisualization();
	}

	public abstract Visualization createVisualization();

}
