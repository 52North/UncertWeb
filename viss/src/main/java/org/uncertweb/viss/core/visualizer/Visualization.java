package org.uncertweb.viss.core.visualizer;

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
	@Transient
	private Set<GridCoverage> coverages = Utils.set();

	public Visualization() {
	}

	public Visualization(UUID uuid, Visualizer creator, JSONObject parameters,
			GridCoverage coverage) {
		this(uuid, creator, parameters, Utils.set(coverage));
	}

	public Visualization(UUID uuid, Visualizer creator, JSONObject parameters,
			Set<GridCoverage> coverages) {
		this.uuid = uuid;
		this.creator = creator;
		this.parameters = parameters;
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
		return Utils.join("-", getUuid(),
				Visualizer.getShortName(getCreator().getClass()));
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

}
