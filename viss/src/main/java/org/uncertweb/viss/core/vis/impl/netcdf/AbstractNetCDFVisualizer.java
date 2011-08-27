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
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.NetCDFHelper;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.Visualization;
import org.uncertweb.viss.core.vis.Visualizer;
import org.uncertweb.viss.core.vis.WriteableGridCoverage;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public abstract class AbstractNetCDFVisualizer implements Visualizer {

	protected static final Logger log = LoggerFactory
			.getLogger(AbstractNetCDFVisualizer.class);

	private JSONObject params;
	private Set<URI> found;
	private NetcdfFile netCDF;
	private Resource resource;

	@SuppressWarnings("unchecked")
	public Visualization visualize(Resource r, JSONObject params) {
		try {
			this.params = params;
			setNetCDF(r);

			NetCDFHelper.checkForUWConvention(getNetCDF());

			Map<URI, Variable> vars = NetCDFHelper.getVariables(getNetCDF(),
					Utils.combineSets(hasToHaveAll(), hasToHaveOneOf()));
			log.debug("Found {} Variables with relevant URIs.", vars.size());

			this.found = Collections.unmodifiableSet(vars.keySet());

			Map<URI, Array> arrays = Utils.map();
			Map<URI, Index> indexes = Utils.map();
			Map<URI, Integer> missingValues = Utils.map();

			for (final Entry<URI, Variable> e : vars.entrySet()) {
				Array a;
				a = e.getValue().read();
				arrays.put(e.getKey(), a);
				indexes.put(e.getKey(), a.getIndex());
				missingValues.put(e.getKey(), Integer.valueOf(NetCDFHelper
						.getMissingValue(e.getValue())));
			}

			WriteableGridCoverage wgc = NetCDFHelper.getCoverage(getNetCDF(), getCoverageName(), getUom());

			Array lonValues = NetCDFHelper.getLongitude(getNetCDF()).read();
			Array latValues = NetCDFHelper.getLatitude(getNetCDF()).read();

			final int sizeLon = lonValues.getShape()[0];
			final int sizeLat = latValues.getShape()[0];

			Double min = null, max = null;

			for (int i = 0; i < sizeLat; ++i) {
				for (int j = 0; j < sizeLon; ++j) {
					final Map<URI, Double> values = Utils.map();
					for (final URI uri : vars.keySet()) {
						Array a = arrays.get(uri);
						Index x = indexes.get(uri);
						Double val = Double.valueOf(a.getDouble(x.set(i, j)));
						if (!Integer.valueOf(val.intValue()).equals(missingValues.get(uri))) {
							values.put(uri, val);
						}
					}
					Number value = null;
					if (!values.isEmpty()) {
						double v = evaluate(values);
						if (!Double.isNaN(v) && !Double.isInfinite(v)) {
							value = Double.valueOf(v);
							if (min == null || min.doubleValue() > v) {
								min = Double.valueOf(v);
							}
							if (max == null || max.doubleValue() < v) {
								max = Double.valueOf(v);
							}
						}
					}
					
					double lat = latValues.getDouble(i);
					double lon = lonValues.getDouble(j);
					Point2D p = new Point2D.Double(lon,lat);
					wgc.setValueAtPos(p, value);
					
				}
			}
			log.debug("min: {}; max: {}", min, max);
			return new Visualization(r.getUUID(), getId(params), this, params,
					min.doubleValue(), max.doubleValue(), getUom(), wgc.getGridCoverage());
		} catch (IOException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public JSONObject getOptionsForResource(Resource r) {
		return getOptions();
	}

	@Override
	public boolean isCompatible(Resource r) {
		Set<URI> uris = NetCDFHelper.getURIs(getNetCDF(r));
		Set<URI> all = hasToHaveAll();
		if (!all.isEmpty()) {
			if (!uris.containsAll(all)) {
				return false;
			}
		}
		Set<URI> one = hasToHaveOneOf();
		if (!one.isEmpty()) {
			for (URI uri : one) {
				if (uris.contains(uri)) {
					return true;
				}
			}
			return false;
		}
		return true;
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

	protected Set<URI> getFoundURIs() {
		return this.found;
	}

	protected JSONObject getParams() {
		return this.params;
	}

	protected String getUom() {
		return NetCDFHelper.getUnitAsString(getNetCDF());
	}

	protected NetcdfFile getNetCDF() {
		return this.netCDF;
	}

	protected void setNetCDF(NetcdfFile netCDF) {
		this.netCDF = netCDF;
	}

	protected void setNetCDF(Resource r) {
		setNetCDF(getNetCDF(r));
	}

	protected NetcdfFile getNetCDF(Resource r) {
		NetcdfUWFile netCDF = (NetcdfUWFile) r.getResource();
		return netCDF.getNetcdfFile();
	}

	protected abstract Set<URI> hasToHaveOneOf();

	protected abstract Set<URI> hasToHaveAll();

	protected abstract double evaluate(Map<URI, Double> values);

}
