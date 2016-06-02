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
package org.uncertweb.viss.mongo.vis;

import java.util.Set;

import org.codehaus.jettison.json.JSONObject;
import org.opengis.coverage.grid.GridCoverage;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.vis.IVisualizationReference;
import org.uncertweb.viss.core.vis.IVisualizer;
import org.uncertweb.viss.core.vis.VisualizationStyle;

import com.github.jmkgreen.morphia.annotations.PostLoad;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.annotations.Transient;


public class MongoVisualization implements IVisualization {

	private IVisualizer creator;
	private JSONObject parameters;
	private IVisualizationReference reference;
	private String visId;
	private double minValue;
	private double maxValue;
	private String uom;
	private Set<VisualizationStyle> styles = UwCollectionUtils.set();;

	@Reference
	private IDataSet dataSet;

	@Transient
	private Set<GridCoverage> coverages = UwCollectionUtils.set();

	@PostLoad
	public void postLoad() {
		for (VisualizationStyle s : getStyles()) {
			s.setVis(this);
		}
	}

	@Override
	public IVisualizer getCreator() {
		return creator;
	}

	@Override
	public void setCreator(IVisualizer creator) {
		this.creator = creator;
	}

	@Override
	public JSONObject getParameters() {
		return parameters;
	}

	@Override
	public void setParameters(JSONObject parameters) {
		this.parameters = parameters;
	}

	@Override
	public IVisualizationReference getReference() {
		return reference;
	}

	@Override
	public void setReference(IVisualizationReference reference) {
		this.reference = reference;
	}

	@Override
	public String getId() {
		return visId;
	}

	@Override
	public void setVisId(String visId) {
		this.visId = visId;
	}

	@Override
	public Double getMinValue() {
		return minValue;
	}

	@Override
	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}

	@Override
	public Double getMaxValue() {
		return maxValue;
	}

	@Override
	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public String getUom() {
		return uom;
	}

	@Override
	public void setUom(String uom) {
		this.uom = uom;
	}

	@Override
	public Set<GridCoverage> getCoverages() {
		return coverages;
	}

	@Override
	public void setCoverages(Set<GridCoverage> coverages) {
		this.coverages = coverages;
	}

	@Override
	public IDataSet getDataSet() {
		return this.dataSet;
	}

	@Override
	public void setDataSet(IDataSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public void addStyle(VisualizationStyle style) {
		this.styles.add(style);
	}

	@Override
	public void removeStyle(VisualizationStyle style) {
		this.styles.remove(style);
	}

	@Override
	public Set<VisualizationStyle> getStyles() {
		return this.styles;
	}

}
