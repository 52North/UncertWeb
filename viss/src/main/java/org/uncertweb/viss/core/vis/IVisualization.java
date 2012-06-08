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

import org.codehaus.jettison.json.JSONObject;
import org.opengis.coverage.grid.GridCoverage;
import org.uncertweb.viss.core.resource.IDataSet;

public interface IVisualization {

	/**
	 * @return the dataSet
	 */
	public IDataSet getDataSet();

	/**
	 * @param dataSet
	 *            the dataSet to set
	 */
	public void setDataSet(IDataSet dataSet);

	/**
	 * @return the creator
	 */
	public IVisualizer getCreator();

	/**
	 * @param creator
	 *            the creator to set
	 */
	public void setCreator(IVisualizer creator);

	/**
	 * @return the parameters
	 */
	public JSONObject getParameters();

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(JSONObject parameters);

	/**
	 * @return the reference
	 */
	public IVisualizationReference getReference();

	/**
	 * @param reference
	 *            the reference to set
	 */
	public void setReference(IVisualizationReference reference);

	/**
	 * @return the visId
	 */
	public String getId();

	/**
	 * @param visId
	 *            the visId to set
	 */
	public void setVisId(String visId);

	/**
	 * @return the minValue
	 */
	public Double getMinValue();

	/**
	 * @param minValue
	 *            the minValue to set
	 */
	public void setMinValue(Double minValue);

	/**
	 * @return the maxValue
	 */
	public Double getMaxValue();

	/**
	 * @param maxValue
	 *            the maxValue to set
	 */
	public void setMaxValue(Double maxValue);

	/**
	 * @return the uom
	 */
	public String getUom();

	/**
	 * @param uom
	 *            the uom to set
	 */
	public void setUom(String uom);

	/**
	 * @return the coverages
	 */
	public Set<GridCoverage> getCoverages();

	/**
	 * @param coverages
	 *            the coverages to set
	 */
	public void setCoverages(Set<GridCoverage> coverages);

	public void addStyle(VisualizationStyle style);

	public void removeStyle(VisualizationStyle style);

	public Set<VisualizationStyle> getStyles();

}