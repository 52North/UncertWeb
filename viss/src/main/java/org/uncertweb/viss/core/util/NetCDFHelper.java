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
package org.uncertweb.viss.core.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.geotools.coverage.grid.GridCoverageBuilder;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.vis.WriteableGridCoverage;

import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFHelper {
	private static final Logger log = LoggerFactory.getLogger(NetCDFHelper.class);
	private static final String MISSING_VALUE_ATTRIBUTE = "missing_value";
	private static final String PRIMARY_VARIABLES_ATTRIBUTE = "primary_variables";
	private static final String UNITS_ATTRIBUTE = "units";
	private static final String REF_ATTRIBUTE = "ref";
	private static final String ANCIALLARY_VARIABLES_ATTRIBUTE = "ancillary_variables";
	private static final String CONVENTIONS_ATTRIBUTE = "Conventions";
	private static final String UW_CONVENTION = "UW-1.0";
	public static final CoordinateReferenceSystem EPSG4326;
	
	static {
		try {
			EPSG4326 = CRS.getAuthorityFactory(true).createCoordinateReferenceSystem("EPSG:4326");
		} catch (NoSuchAuthorityCodeException e) {
			throw VissError.internal(e);
		} catch (FactoryRegistryException e) {
			throw VissError.internal(e);
		} catch (FactoryException e) {
			throw VissError.internal(e);
		}
	}
	
	public static void checkForUWConvention(NetcdfFile f) {
		Attribute a = f.findGlobalAttribute(CONVENTIONS_ATTRIBUTE);
		if (a == null) {
			throw VissError.internal("No conventions attribute");
		}
		for (String s : a.getStringValue().split(" ")) {
			if (s.equals(UW_CONVENTION))
				return;
		}
		throw VissError.internal("NetCDF file is not " + UW_CONVENTION
				+ " compliant.");
	}
	
	public static Set<URI> getURIs(NetcdfFile f) {
		Set<URI> set = Utils.set();
		for (Variable v : f.getVariables()) {
			URI uri = getURI(v);
			if (uri != null) {
				set.add(uri);
			}
		}
		return set;
	}

	public static Integer getMissingValue(Variable v) {
		Attribute a = v.findAttribute(MISSING_VALUE_ATTRIBUTE);
		if (a == null) {
			return Integer.MIN_VALUE;
		} else {
			return a.getNumericValue().intValue();
		}
	}

	public static double getMissingValue(NetcdfFile f, URI uri) {
		Variable v = getVariables(f, Utils.set(uri)).get(uri);
		if (v == null) {
			return Double.NaN;
		} else {
			return getMissingValue(v);
		}
	}

	public static Unit<? extends Quantity> getUnit(NetcdfFile f) {
		return getUnit(getPrimaryVariable(f));
	}
	
	public static String getUnitAsString(NetcdfFile f) {
		return getUnitAsString(getPrimaryVariable(f));
	}

	public static Variable getPrimaryVariable(NetcdfFile f) {
		Attribute a = f.findGlobalAttribute(PRIMARY_VARIABLES_ATTRIBUTE);
		if (a.getLength() == 1) {
			return getNotNullVariable(f, a.getStringValue(0));
		} else {
			throw VissError
					.internal("Only a single primary value is currently supported");
		}
	}
	
	public static String getUnitAsString(Variable v) {
		Attribute a = v.findAttribute(UNITS_ATTRIBUTE);
		if (a!=null)return a.getStringValue();
		return null;
	}

	public static Unit<? extends Quantity> getUnit(Variable v) {
		return getUnit(getUnitAsString(v));
	}
	
	public static Unit<? extends Quantity> getUnit(String uom) {
		if (uom != null) {
			try {
				return Unit.valueOf(uom);
			} catch (IllegalArgumentException e) {
				return Unit.ONE;
			}
		} else {
			return Unit.ONE;
		}
	}

	public static Unit<? extends Quantity> getUnit(NetcdfFile f, URI uri) {
		Variable v = getVariables(f, Utils.set(uri)).get(uri);
		if (v == null) {
			return Unit.ONE;
		}
		return getUnit(v);
	}

	public static Variable getNotNullVariable(NetcdfFile f, String name) {
		Variable v = f.findVariable(name);
		if (v == null) {
			throw VissError.internal("Variable with name \"" + name
					+ "\" could not be found.");
		}
		return v;
	}

	public static Variable getLongitude(NetcdfFile f) {
		return getNotNullVariable(f, "lon");
	}

	public static Variable getLatitude(NetcdfFile f) {
		return getNotNullVariable(f, "lat");
	}

	public static Envelope getEnvelope(NetcdfFile f) {
		Variable lon = getLongitude(f);
		Variable lat = getLatitude(f);
		try {
			//X=LON,Y=LAT!!!
			int lonSize = lon.getShape()[0];
			int latSize = lat.getShape()[0];
			double lonMin = lon.read(Utils.list(new Range(0, 0))).getDouble(0);
			double lonMax = lon.read(Utils.list(new Range(lonSize - 1, lonSize - 1))).getDouble(0);
			double latMin = lat.read(Utils.list(new Range(0, 0))).getDouble(0);
			double latMax = lat.read(Utils.list(new Range(latSize - 1, latSize - 1))).getDouble(0);
			return new Envelope2D(EPSG4326, lonMin, latMin, lonMax - lonMin, latMax - latMin);
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	public static WriteableGridCoverage getCoverage(NetcdfFile f, String layerName) {
		return getCoverage(f, layerName, getPrimaryVariable(f), null);
	}
	
	public static WriteableGridCoverage getCoverage(NetcdfFile f, String layerName, String uom) {
		return getCoverage(f, layerName, getPrimaryVariable(f), uom);
	}

	public static URI getPrimaryURI(NetcdfFile f) {
		return getURI(getPrimaryVariable(f));
	}

	public static WriteableGridCoverage getCoverage(NetcdfFile f, String layerName, Variable v, String unit) {
		Attribute a = v.findAttribute(ANCIALLARY_VARIABLES_ATTRIBUTE);
		if (a == null)
			throw VissError.internal("Can not determine shape of variable: no \""
						+ ANCIALLARY_VARIABLES_ATTRIBUTE + "\" attribute.");
		
		int missingValue = -999;
		for (String s  : a.getStringValue().split(" ")) {
			Attribute mv = f.findVariable(s).findAttribute(MISSING_VALUE_ATTRIBUTE);
			if (mv != null) {
				missingValue = mv.getNumericValue().intValue();
				break;
			}
		}
	
		
		int latSize = getLatitude(f).getShape()[0];
		int lonSize = getLongitude(f).getShape()[0];

		final GridCoverageBuilder b = new GridCoverageBuilder();
		b.setCoordinateReferenceSystem(EPSG4326);
		log.debug("ImageSize: {}x{}", lonSize, latSize);
		//FIXME this removes the cross, but thats somewhat ugly
		b.setImageSize(lonSize-1, latSize-1);
		b.setEnvelope(getEnvelope(f));
		GridCoverageBuilder.Variable var;
		if (unit == null) {
			var = b.newVariable(layerName, getUnit(v));
		} else {
			var = b.newVariable(layerName, getUnit(unit));
		}
		var.setLinearTransform(1, 0);
		log.info("MissingValue: {}", missingValue);
		var.addNodataValue("UNKNOWN", missingValue);
		return new WriteableGridCoverage(b.getGridCoverage2D());
	}
	
	public static URI getURI(Variable v) {
		Attribute ref = v.findAttribute(REF_ATTRIBUTE);
		if (ref != null) {
			try {
				return new URI(ref.getStringValue());
			} catch (URISyntaxException e) {
				VissError.internal(e);
			}
		}
		return null;
	}

	public static Map<URI, Variable> getVariables(NetcdfFile f, Set<URI> wanted) {
		if (f == null || wanted == null || wanted.isEmpty()) {
			return Collections.emptyMap();
		} else {
			Map<URI, Variable> variables = Utils.map();
			for (Variable v : f.getVariables()) {
				URI uri = getURI(v);
				if (wanted.contains(uri)) {
					variables.put(uri, v);
				}
			}
			return variables;
		}
	}
}
