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

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.IUncertainty;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.utils.MultivaluedMap;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.UncertaintyType;

import ucar.nc2.Variable;

public abstract class AbstractNcUwVariable implements INcUwVariable {
	protected static final Logger log = LoggerFactory
			.getLogger(AbstractNcUwVariable.class);
	private final NcUwFile file;
	private final Variable variable;
	private final NcUwArrayCache cache;
	private CoordinateReferenceSystem crs = null;
	private URI observedProperty = null;
	private URI procedure = null;
	private Set<INcUwVariable> ancillaryVariables;
	private final AbstractNcUwVariable parent;

	public AbstractNcUwVariable(NcUwFile file, Variable v, NcUwArrayCache cache, AbstractNcUwVariable parent) {
		this.file = file;
		this.variable = v;
		this.cache = cache;
		this.parent = parent;
	}

	@Override
	public NcUwFile getFile() {
		return this.file;
	}

	public Variable getVariable() {
		return this.variable;
	}

	public NcUwArrayCache getCache() {
		return this.cache;
	}

	@Override
	public String getName() {
		return getVariable().getName();
	}

	@Override
	public String getUnit() {
		return getVariable().getUnitsString();
	}

	@Override
	public URI getObservedProperty() {
		if (this.observedProperty == null) {
			final String op = NcUwHelper.getStringAttribute(getVariable(),
					NcUwConstants.Attributes.OBSERVED_PROPERTY, false);
			if (op != null) {
				this.observedProperty = NcUwHelper.toUri(op,
						NcUwConstants.DEFAULT_OBSERVED_PROPERTY, null);
			} else {
				this.observedProperty = NcUwHelper.toUri(getName(),
						NcUwConstants.DEFAULT_OBSERVED_PROPERTY, null);
			}
		}
		return this.observedProperty;
	}

	@Override
	public URI getProcedure() {
		if (this.procedure == null) {
			final String p = NcUwHelper.getStringAttribute(getVariable(),
					NcUwConstants.Attributes.PROCEDURE, false);
			this.procedure = NcUwHelper.toUri(p,
					NcUwConstants.DEFAULT_OBSERVED_PROPERTY, null);
		}
		return this.procedure;

	}

	@Override
	public UncertaintyType getType() {
		return UncertaintyType.fromURI(getRef());
	}

	protected URI getRef() {
		return NcUwHelper.getRef(getVariable());
	}

	@Override
	public boolean isUncertaintyVariable() {
		return getRef() != null;
	}

	@Override
	public CoordinateReferenceSystem getCRS() {
		if (getParent() != null && getParent().getCRS() != null) {
			return getParent().getCRS();
		} else if (crs == null) {
			crs = _createCrs(this);
		}
		return crs;
	}

	public static INcUwVariable findVariableWithCRS(
			INcUwVariable v) {
		String gm = v.getStringAttribute(NcUwConstants.Attributes.GRID_MAPPING);
		if (gm != null) {
			log.debug("Found CRS reference in {}", v.getName());
			return v;
		} else {
			log.debug("No grid_mapping in {}", v.getName());
			for (INcUwVariable av : v.getAncillaryVariables()) {
				INcUwVariable found = findVariableWithCRS(av);
				if (found != null) {
					log.debug("Found CRS reference in {}", found.getName());
					return found;
				}
			}
		}
		return null;
	}

	private static CoordinateReferenceSystem _createCrs(AbstractNcUwVariable orig) {
		INcUwVariable v = findVariableWithCRS(orig);
		if (v == null) {
			log.debug("No Variable with CRS found");
			return DefaultGeographicCRS.WGS84;
		}
		final String gm = v
				.getStringAttribute(NcUwConstants.Attributes.GRID_MAPPING);
		final Variable var = v.getVariable(gm, true);
		final String proj4 = NcUwHelper.getStringAttribute(var,
				NcUwConstants.Attributes.PROJ4, false);
		final String wkt = NcUwHelper.getStringAttribute(var,
				NcUwConstants.Attributes.WKT, false);
		final String epsg = NcUwHelper.getStringAttribute(var,
				NcUwConstants.Attributes.EPSG_CODE, false);

		try {
			if (epsg != null) {
				log.debug("Decoding EPSG Code {}", epsg);
				return NcUwHelper.decodeEpsgCode(Integer.valueOf(epsg.trim()));
			} else if (wkt != null) {
				log.debug("Decoding WKT string {}", wkt);
				return NcUwHelper.decodeWkt(wkt.trim());
			} else if (proj4 != null) {
				log.debug("Decoding PROJ4 string {}", proj4);
				return NcUwHelper.decodeProj4(proj4.trim());
			} else {
				log.warn("Could not decode CRS of variable {}", v.getName());
				return null;
			}
		} catch (final NumberFormatException e) {
			throw new NcUwException(e);
		} catch (final FactoryException e) {
			throw new NcUwException(e);
		} catch (final IOException e) {
			throw new NcUwException(e);
		}
	}

	@Override
	public Set<INcUwVariable> getAncillaryVariables() {
		if (this.ancillaryVariables == null) {

			this.ancillaryVariables = UwCollectionUtils.set();
			final String av = NcUwHelper.getStringAttribute(getVariable(),
					NcUwConstants.Attributes.ANCILLARY_VARIABLES, false);
			if (av != null) {
				for (final String s : av.trim().split(" ")) {
					this.ancillaryVariables.add(create(getFile(), getVariable(s, true), getCache(), this));
				}
			}
			checkIfAncillaryVariablesAreCompatible(this.ancillaryVariables);
		}
		return this.ancillaryVariables;
	}

	private void checkIfAncillaryVariablesAreCompatible(
			Set<INcUwVariable> anciallaryVariables) {
		// TODO check for same dimensions
	}

	public static INcUwVariable create(NcUwFile file, Variable variable, NcUwArrayCache cache, AbstractNcUwVariable parent) {
		if (variable.getDimensions().isEmpty()) {
			return new NcUwVariableWithoutDimensions(file, variable, cache, parent);
		} else {
			return new NcUwVariableWithDimensions(file, variable, cache, parent);
		}
	}

	@Override
	public String getStringAttribute(String name, boolean failIfNotExisting) {
		return NcUwHelper.getStringAttribute(getVariable(), name,
				failIfNotExisting);
	}

	@Override
	public Variable getVariable(String name, boolean failIfNotExisting) {
		return getFile().getVariable(name, failIfNotExisting);
	}

	@Override
	public Variable getVariable(String name) {
		return getVariable(name, false);
	}

	@Override
	public Number getNumberAttribute(String name, boolean failIfNotExisting) {
		return NcUwHelper.getNumberAttribute(getVariable(), name,
				failIfNotExisting);
	}

	@Override
	public String getStringAttribute(String name) {
		return getStringAttribute(name, false);
	}

	@Override
	public Number getNumberAttribute(String name) {
		return getNumberAttribute(name, false);
	}

	@Override
	public Iterator<NcUwObservation> iterator() {
		return new NcUwIterator(this);
	}

	@Override
	public Iterable<NcUwObservation> getTimeLayer(TimeObject t) {
		NcUwCoordinate c = null;
		if (t == null || (c = getIndex(t)) == null || !c.hasDimension(NcUwDimension.T))
			return new NcUwIterable(this);
		return new NcUwTemporalIterable(this, c.get(NcUwDimension.T).intValue());
	}

	@Override
	public NcUwObservation getObservation(NcUwCoordinate c) {
		IUncertainty u = getValue(c);
		if (u == null) {
			return null;
		}
		return new NcUwObservation(getTime(c), getProcedure(),
				getObservedProperty(), getFeature(c), 
				getGridCoordinates(c), u, getUnit());
	}
	
	protected IUncertainty getValue(NcUwCoordinate c) {
		MultivaluedMap<URI, Object> map = getValueMap(c);
		if (map == null) {
			return null;
		}
		return NcUwUriParser.parse(getType(), map);
	}
	
	protected AbstractNcUwVariable getParent() {
		return parent;
	}

	protected abstract MultivaluedMap<URI, Object> getValueMap(NcUwCoordinate c);
	protected abstract GridCoordinates getGridCoordinates(NcUwCoordinate c);
	protected abstract SpatialSamplingFeature getFeature(NcUwCoordinate c);
	protected abstract TimeObject getTime(NcUwCoordinate c);

	
	
}
