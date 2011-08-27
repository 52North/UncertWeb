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
	private VisualizationReference reference;
	private String visId;
	private double minValue;
	private double maxValue;
	private String uom;

	@Transient
	private StyledLayerDescriptorDocument sld;

	@Transient
	private Set<GridCoverage> coverages = Utils.set();

	/**
	 * creates an empty visualization
	 */
	public Visualization() {
	}

	/**
	 * Creates a Visualization
	 * 
	 * @param uuid
	 *            the UUID of the resource
	 * @param id
	 *            the id of the visualization
	 * @param creator
	 *            the visualizer that created this visualization
	 * @param parameters
	 *            the used parameters
	 * @param min
	 *            the minimal value of the generated values
	 * @param max
	 *            the maximal value of the generated values
	 * @param uom
	 *            the unit of measurement of the visualization
	 * @param coverage
	 *            the generated coverage
	 */
	public Visualization(UUID uuid, String id, Visualizer creator,
			JSONObject parameters, double min, double max, String uom,
			GridCoverage coverage) {
		this(uuid, id, creator, parameters, min, max, uom, Utils.set(coverage));
	}

	/**
	 * Creates a Visualization
	 * 
	 * @param uuid
	 *            the UUID of the resource
	 * @param id
	 *            the id of the visualization
	 * @param creator
	 *            the visualizer that created this visualization
	 * @param parameters
	 *            the used parameters
	 * @param min
	 *            the minimal value of the generated values
	 * @param max
	 *            the maximal value of the generated values
	 * @param uom
	 *            the unit of measurement of the visualization
	 * @param coverages
	 *            the generated coverages
	 */
	public Visualization(UUID uuid, String id, Visualizer creator,
			JSONObject parameters, double min, double max, String uom,
			Set<GridCoverage> coverages) {
		setUuid(uuid);
		setCreator(creator);
		setParameters(parameters);
		setVisId(id);
		setMinValue(min);
		setMaxValue(max);
		setUom(uom);
		setCoverages(coverages);
	}

	/**
	 * @return the uuid
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the creator
	 */
	public Visualizer getCreator() {
		return creator;
	}

	/**
	 * @param creator the creator to set
	 */
	public void setCreator(Visualizer creator) {
		this.creator = creator;
	}

	/**
	 * @return the parameters
	 */
	public JSONObject getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(JSONObject parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the reference
	 */
	public VisualizationReference getReference() {
		return reference;
	}

	/**
	 * @param reference the reference to set
	 */
	public void setReference(VisualizationReference reference) {
		this.reference = reference;
	}

	/**
	 * @return the visId
	 */
	public String getVisId() {
		return visId;
	}

	/**
	 * @param visId the visId to set
	 */
	public void setVisId(String visId) {
		this.visId = visId;
	}

	/**
	 * @return the minValue
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * @param minValue the minValue to set
	 */
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	/**
	 * @return the maxValue
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * @return the uom
	 */
	public String getUom() {
		return uom;
	}

	/**
	 * @param uom the uom to set
	 */
	public void setUom(String uom) {
		this.uom = uom;
	}

	/**
	 * @return the sld
	 */
	public StyledLayerDescriptorDocument getSld() {
		return sld;
	}

	/**
	 * @param sld the sld to set
	 */
	public void setSld(StyledLayerDescriptorDocument sld) {
		this.sld = sld;
	}

	/**
	 * @return the coverages
	 */
	public Set<GridCoverage> getCoverages() {
		return coverages;
	}

	/**
	 * @param coverages the coverages to set
	 */
	public void setCoverages(Set<GridCoverage> coverages) {
		this.coverages = coverages;
	}



}
