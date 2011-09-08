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
package org.uncertweb.viss.core.vis.impl.netcdf;

import static org.uncertweb.viss.core.util.Constants.NETCDF_TYPE;
import static org.uncertweb.viss.core.util.Constants.X_NETCDF_TYPE;

import java.awt.geom.Point2D;
import java.net.URI;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.IUncertainty;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.Visualization;
import org.uncertweb.viss.core.vis.Visualizer;
import org.uncertweb.viss.core.vis.WriteableGridCoverage;
import org.uncertweb.viss.core.vis.impl.netcdf.UncertaintyNetCDF.UncertaintyType;
import org.uncertweb.viss.core.vis.impl.netcdf.UncertaintyNetCDF.Value;

public abstract class AbstractNetCDFVisualizer implements Visualizer {

	protected static final Logger log = LoggerFactory
			.getLogger(AbstractNetCDFVisualizer.class);

	private JSONObject params;
	private Resource resource;

	public Visualization visualize(Resource r, JSONObject params) {
		this.params = params;
		this.resource = r;
		WriteableGridCoverage wgc = getNetCDF().getCoverage(getCoverageName(), getUom());
		Double min = null, max = null;
		for (Value nv : getNetCDF()) {
			Double value = null;
			if (nv.getValue() != null) {
				
				double v = evaluate(nv.getValue());
				if (!Double.isNaN(v) && !Double.isInfinite(v)) {
					value = Double.valueOf(v);
					if (min == null || min.doubleValue() > v) { min = Double.valueOf(v); }
					if (max == null || max.doubleValue() < v) { max = Double.valueOf(v); }
				}
			}
			Point2D.Double location = new Point2D.Double(nv.getLocation().getX(), nv.getLocation().getY());
			wgc.setValueAtPos(location, value);
		}
		log.debug("min: {}; max: {}", min, max);
		return new Visualization(r.getUUID(), getId(params), this, params,
				min.doubleValue(), max.doubleValue(), getUom(), wgc.getGridCoverage());
		
		
	}

	@Override
	public JSONObject getOptionsForResource(Resource r) {
		return getOptions();
	}

	@Override
	public boolean isCompatible(Resource r) {
		return getSupportedURI().contains(getNetCDF(r).getPrimaryURI());
	}
	
	@Override
	public String getId(JSONObject params) {
		return this.getShortName();
	}

	@Override
	public JSONObject getOptions() {
		return new JSONObject();
	}

	@Override
	public String getShortName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public Set<MediaType> getCompatibleMediaTypes() {
		return Utils.set(NETCDF_TYPE, X_NETCDF_TYPE);
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public Resource getResource() {
		return this.resource;
	}

	@Override
	public void setResource(Resource r) {
		this.resource = r;
	}

	protected String getCoverageName() {
		return this.getId(getParams());
	}

	protected JSONObject getParams() {
		return this.params;
	}

	protected String getUom() {
		return this.getNetCDF().getUnitAsString();
	}


	protected UncertaintyNetCDF getNetCDF() {
		return getNetCDF(this.resource);
	}
	
	protected UncertaintyNetCDF getNetCDF(Resource r) {
		return (UncertaintyNetCDF) r.getResource();
	}

	protected Set<URI> getSupportedURI() {
		Set<URI> uris = Utils.set();
		for (UncertaintyType c : getSupportedUncertainties()) {
			uris.add(c.getURI());
		}
		return uris;
	}
	
	protected abstract Set<UncertaintyType> getSupportedUncertainties();

	protected abstract double evaluate(IUncertainty u);

}
