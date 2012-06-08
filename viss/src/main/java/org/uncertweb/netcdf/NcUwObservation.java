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

import org.opengis.coverage.grid.GridCoordinates;
import org.uncertml.IUncertainty;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.utils.UwConstants;

import com.vividsolutions.jts.geom.Geometry;

public class NcUwObservation extends UncertaintyObservation {
	private static final URI MISSING = UwConstants.URL.MISSING.uri;

	private final GridCoordinates gridCoordinates;

	public NcUwObservation(TimeObject t, URI p, URI op,
			SpatialSamplingFeature foi, GridCoordinates gc, IUncertainty r,
			String uom) {
		super((t == null) ? new TimeObject(MISSING) : t,
				(t == null) ? new TimeObject(MISSING) : t,
				(p == null) ? MISSING : p, (op == null) ? MISSING : op,
				(foi == null) ? new SpatialSamplingFeature(MISSING) : foi,
				new UncertaintyResult(r, uom));
		this.gridCoordinates = gc;
	}

	public NcUwObservation(TimeObject t, URI p, URI op,
			SpatialSamplingFeature foi, GridCoordinates gc, IUncertainty r) {
		this(t, p, op, foi, gc, r, null);
	}

	public NcUwObservation(TimeObject t, URI p, URI op, Geometry foi,
			GridCoordinates gc, IUncertainty r, String uom) {
		this(t, p, op, (foi == null) ? null : new SpatialSamplingFeature(
				NcUwConstants.DEFAULT_SAMPLING_FEATURE, foi), gc, r, uom);
	}

	public NcUwObservation(TimeObject t, URI p, URI op, Geometry foi,
			GridCoordinates gc, IUncertainty r) {
		this(t, p, op, (foi == null) ? null : new SpatialSamplingFeature(
				NcUwConstants.DEFAULT_SAMPLING_FEATURE, foi), gc, r, null);
	}

	public GridCoordinates getGridCoordinates() {
		return this.gridCoordinates;
	}
}