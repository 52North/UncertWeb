package org.uncertweb.viss.core.vis;

import java.util.Set;
import java.util.UUID;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.codehaus.jettison.json.JSONObject;
import org.opengis.coverage.grid.GridCoverage;
import org.uncertweb.viss.core.util.Utils;

import com.google.code.morphia.annotations.Transient;

public class Visualization {

	private UUID uuid;
	private Visualizer creator;
	private JSONObject parameters;
	private VisualizationReference ref;
	private StyledLayerDescriptorDocument sld;
	private String visId;
	private double minValue;
	private double maxValue;

	@Transient
	private Set<GridCoverage> coverages = Utils.set();

	public Visualization() {
	}

	public Visualization(UUID uuid, String id, Visualizer creator,
			JSONObject parameters, double min, double max, GridCoverage coverage) {
		this(uuid, id, creator, parameters, min, max, Utils.set(coverage));
	}

	public Visualization(UUID uuid, String id, Visualizer creator,
			JSONObject parameters, double min, double max,
			Set<GridCoverage> coverages) {
		setUuid(uuid);
		setCreator(creator);
		setParameters(parameters);
		setVisId(id);
		setMinValue(min);
		setMaxValue(max);
		this.coverages = coverages;
	}

	public Visualizer getCreator() {
		return creator;
	}

	public void setCreator(Visualizer creator) {
		this.creator = creator;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getVisId() {
		return this.visId;
	}

	public Set<GridCoverage> getCoverages() {
		return coverages;
	}

	public VisualizationReference getReference() {
		return ref;
	}

	public void setReference(VisualizationReference ref) {
		this.ref = ref;
	}

	public JSONObject getParameters() {
		return parameters;
	}

	public void setParameters(JSONObject parameters) {
		this.parameters = parameters;
	}

	public StyledLayerDescriptorDocument getSld() {
		return sld;
	}

	public void setSld(StyledLayerDescriptorDocument sld) {
		this.sld = sld;
	}

	public void setVisId(String visId) {
		this.visId = visId;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

}
