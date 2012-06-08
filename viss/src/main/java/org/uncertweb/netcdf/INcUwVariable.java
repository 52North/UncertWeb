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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.netcdf.util.WriteableGridCoverage;
import org.uncertweb.viss.core.UncertaintyType;

import ucar.nc2.Variable;

import com.vividsolutions.jts.geom.Point;

public interface INcUwVariable extends Iterable<NcUwObservation> {
	public abstract NcUwFile getFile();
	public abstract List<TimeObject> getTimes();
	public abstract NcUwCoordinate getIndex(TimeObject t);
	public abstract NcUwCoordinate getIndex(Point p);
	public abstract NcUwCoordinate getIndex(DirectPosition p);
	public abstract boolean contains(Point p);
	public abstract boolean contains(DirectPosition p);
	public abstract boolean hasDimension(NcUwDimension... ds);
	public abstract boolean isUncertaintyVariable();
	public abstract Set<NcUwDimension> getDimensions();
	public abstract int getSize(NcUwDimension d);
	public abstract Envelope getEnvelope();
	public abstract CoordinateReferenceSystem getCRS();
	public abstract WriteableGridCoverage getCoverage();
	public abstract Set<? extends INcUwVariable> getAncillaryVariables();
	public abstract UncertaintyType getType();
	public abstract URI getObservedProperty();
	public abstract URI getProcedure();
	public abstract String getName();
	public abstract String getUnit();
	public abstract NcUwObservation getObservation(NcUwCoordinate c);
	public abstract Iterator<NcUwObservation> iterator();
	public abstract Iterable<NcUwObservation> getTimeLayer(TimeObject t);
	
	public String getStringAttribute(String name, boolean failIfNotExisting);
	public Variable getVariable(String name, boolean failIfNotExisting);
	public Variable getVariable(String name);
	public Number getNumberAttribute(String name, boolean failIfNotExisting); 
	public String getStringAttribute(String name);
	public Number getNumberAttribute(String name);

}