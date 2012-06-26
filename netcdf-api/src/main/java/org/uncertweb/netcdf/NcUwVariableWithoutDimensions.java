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
package org.uncertweb.netcdf;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.geotools.geometry.Envelope2D;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.geometry.DirectPosition;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.netcdf.util.WriteableGridCoverage;
import org.uncertweb.utils.MultivaluedHashMap;
import org.uncertweb.utils.MultivaluedMap;

import ucar.nc2.Variable;

import com.vividsolutions.jts.geom.Point;

public class NcUwVariableWithoutDimensions extends AbstractNcUwVariable {

	private NcUwVariableWithDimensions variableWithDimensions;

	public NcUwVariableWithoutDimensions(NcUwFile file, Variable variable,
			NcUwArrayCache cache, AbstractNcUwVariable parent) {
		super(file, variable, cache, parent);
		this.variableWithDimensions = findVariableWithDimensions(this);
	}
	
	private NcUwVariableWithDimensions findVariableWithDimensions(INcUwVariable v) {
		if (v instanceof NcUwVariableWithDimensions) {
			return (NcUwVariableWithDimensions) v;
		} else {
			for (INcUwVariable i : v.getAncillaryVariables()) {
				NcUwVariableWithDimensions found = findVariableWithDimensions(i);
				if (found != null)
					return found;
			}
			return null;
		}
	}

	@Override
	public List<TimeObject> getTimes() {
		return this.variableWithDimensions.getTimes();
	}

	@Override
	public NcUwCoordinate getIndex(TimeObject t) {
		return this.variableWithDimensions.getIndex(t);
	}

	@Override
	public NcUwCoordinate getIndex(Point p) {
		return this.variableWithDimensions.getIndex(p);
	}

	@Override
	public NcUwCoordinate getIndex(DirectPosition p) {
		return this.variableWithDimensions.getIndex(p);
	}

	@Override
	public boolean contains(Point p) {
		return this.variableWithDimensions.contains(p);
	}

	@Override
	public boolean contains(DirectPosition p) {
		return this.variableWithDimensions.contains(p);
	}

	@Override
	public boolean hasDimension(NcUwDimension... ds) {
		return this.variableWithDimensions.hasDimension(ds);
	}

	@Override
	public Set<NcUwDimension> getDimensions() {
		return this.variableWithDimensions.getDimensions();
	}

	@Override
	public int getSize(NcUwDimension d) {
		return this.variableWithDimensions.getSize(d);
	}

	@Override
	public Envelope2D getEnvelope() {
		return this.variableWithDimensions.getEnvelope();
	}

	@Override
	public WriteableGridCoverage getCoverage() {
		return this.variableWithDimensions.getCoverage();
	}

	@Override
	protected GridCoordinates getGridCoordinates(NcUwCoordinate c) {
		return this.variableWithDimensions.getGridCoordinates(c);
	}

	@Override
	protected SpatialSamplingFeature getFeature(NcUwCoordinate c) {
		return this.variableWithDimensions.getFeature(c);
	}

	@Override
	protected TimeObject getTime(NcUwCoordinate c) {
		return this.variableWithDimensions.getTime(c);
	}

	@Override
	protected MultivaluedMap<URI, Object> getValueMap(NcUwCoordinate c) {
		final MultivaluedMap<URI, Object> map = MultivaluedHashMap.create();
		for (final INcUwVariable v : getAncillaryVariables()) {
			final MultivaluedMap<URI, Object> submap = ((AbstractNcUwVariable) v).getValueMap(c);
			if (submap == null) {
				return null;
			}
			map.addAll(submap);
		}
		return map;
	}
	
}