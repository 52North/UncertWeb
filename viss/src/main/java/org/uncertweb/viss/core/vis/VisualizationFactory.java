package org.uncertweb.viss.core.vis;

import java.net.URL;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.codehaus.jettison.json.JSONObject;
import org.opengis.coverage.grid.GridCoverage;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.mongo.vis.MongoVisualization;

public class VisualizationFactory {

	public static class VisualizationBuilder {
		private IVisualization vis = createEmptyVisualization();
		private Set<GridCoverage> coverages = Utils.set();

		public IVisualization build() {
			this.vis.setCoverages(this.coverages);
			return validate(this.vis);
		}

		public VisualizationBuilder setUuid(UUID uuid) {
			this.vis.setUuid(uuid);
			return this;
		}

		public VisualizationBuilder setId(String id) {
			this.vis.setVisId(id);
			return this;
		}

		public VisualizationBuilder setCreator(IVisualizer creator) {
			this.vis.setCreator(creator);
			return this;
		}

		public VisualizationBuilder setParameters(JSONObject parameters) {
			this.vis.setParameters(parameters);
			return this;
		}

		public VisualizationBuilder setMin(Double min) {
			this.vis.setMinValue(min);
			return this;
		}

		public VisualizationBuilder setMax(Double max) {
			this.vis.setMaxValue(max);
			return this;
		}

		public VisualizationBuilder setUom(String uom) {
			this.vis.setUom(uom);
			return this;
		}

		public VisualizationBuilder addCoverages(Set<GridCoverage> coverages) {
			this.coverages.addAll(coverages);
			return this;
		}

		public VisualizationBuilder addCoverage(GridCoverage coverage) {
			this.coverages.add(coverage);
			return this;
		}

		public VisualizationBuilder setCoverage(GridCoverage coverage) {
			this.coverages = Utils.set(coverage);
			return this;
		}

		public VisualizationBuilder setCoverage(Set<GridCoverage> coverages) {
			Validate.notNull(coverages);
			this.coverages = coverages;
			return this;
		}
	}

	public static class VisualizationReferenceBuilder {
		private IVisualizationReference vis = createEmptyReference();
		private Set<String> layers = Utils.set();

		public IVisualizationReference build() {
			this.vis.setLayers(this.layers);
			return validate(vis);
		}

		public VisualizationReferenceBuilder setUrl(URL url) {
			this.vis.setWmsUrl(url);
			return this;
		}

		public VisualizationReferenceBuilder setLayers(Set<String> layers) {
			Validate.notNull(layers);
			this.layers = layers;
			return this;
		}

		public VisualizationReferenceBuilder addLayer(String layer) {
			this.layers.add(layer);
			return this;
		}

		public VisualizationReferenceBuilder addLayers(Set<String> layers) {
			this.layers.addAll(layers);
			return this;
		}

	}

	public static IVisualization validate(IVisualization vis) {
		Validate.notNull(vis);
		Validate.notNull(vis.getUuid());
		Validate.notNull(vis.getVisId());
		Validate.notNull(vis.getCreator());
		Validate.notNull(vis.getParameters());
		Validate.notNull(vis.getMinValue());
		Validate.notNull(vis.getMaxValue());
		Validate.notNull(vis.getUom());
		Validate.notNull(vis.getCoverages());
		Validate.notEmpty(vis.getCoverages());
		Validate.noNullElements(vis.getCoverages());
		return vis;
	}

	public static IVisualizationReference validate(IVisualizationReference ref) {
		Validate.notNull(ref);
		Validate.notNull(ref.getWmsUrl());
		Validate.notNull(ref.getLayers());
		Validate.notEmpty(ref.getLayers());
		Validate.noNullElements(ref.getLayers());
		return ref;
	}

	public static VisualizationBuilder getBuilder() {
		return new VisualizationBuilder();
	}

	protected static IVisualization createEmptyVisualization() {
		return new MongoVisualization(); // TODO
	}

	protected static IVisualizationReference createEmptyReference() {
		return new DefaultVisualizationReference(); // TODO
	}

}
