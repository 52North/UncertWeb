/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24,
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
	private String visId;
	private double minValue;
	private double maxValue;
	private String uom;

	@Transient
	private StyledLayerDescriptorDocument sld;
	
	@Transient
	private Set<GridCoverage> coverages = Utils.set();

	public Visualization() {
	}

	public Visualization(UUID uuid, String id, Visualizer creator,
			JSONObject parameters, double min, double max, String uom, GridCoverage coverage) {
		this(uuid, id, creator, parameters, min, max, uom, Utils.set(coverage));
	}

	public Visualization(UUID uuid, String id, Visualizer creator,
			JSONObject parameters, double min, double max,
			String uom, Set<GridCoverage> coverages) {
		setUuid(uuid);
		setCreator(creator);
		setParameters(parameters);
		setVisId(id);
		setMinValue(min);
		setMaxValue(max);
		setUom(uom);
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

	public String getUom() {
		return uom;
	}

	public void setUom(String uom) {
		this.uom = uom;
	}

}
