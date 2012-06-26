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
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.nc2.Variable;

public enum NcUwDimension {
	
	/** the x dimension */ X {
		@Override
		public boolean is(Variable v) {
			final String standardName = getStandardName(v);
			final String longName = getLongName(v);
			if (NcUwConstants.UNIT_LONGITUDE.equalsIgnoreCase(v.getUnitsString())) {
				log.debug("{} is unprojected X coordinate", v.getName());
				return true;
			}
			if (NcUwConstants.StandardNames.LONGITUDE.equalsIgnoreCase(standardName)) {
				log.debug("{} is unprojected X coordinate", v.getName());
				return true;
			}
			if (NcUwConstants.StandardNames.X_COORDINATE.equalsIgnoreCase(standardName)) {
				log.debug("{} is projected X coordinate", v.getName());
				return true;
			}
			if (NcUwConstants.LongNames.LONGITUDE.equalsIgnoreCase(longName)) {
				log.debug("{} is unprojected X coordinate", v.getName());
				return true;
			}
			if (NcUwConstants.LongNames.X_COORDINATE.equalsIgnoreCase(longName)) {
				log.debug("{} is projected X coordinate", v.getName());
				return true;
			}
			if (NcUwConstants.Axis.X.equalsIgnoreCase(getAxis(v))) {
				log.debug("{} is projected X coordinate", v.getName());
				return true;
			}
			return false;
		}
	}, 
	/** the y dimension */ Y {
		@Override
		public boolean is(Variable v) {
			final String standardName = getStandardName(v);
			final String longName = getLongName(v);
			if (NcUwConstants.UNIT_LATITUDE.equalsIgnoreCase(v.getUnitsString())) {
				log.debug("{} is unprojected Y coordinate", v.getName());
				return true;
			}
			if (NcUwConstants.StandardNames.LATITUDE.equalsIgnoreCase(standardName)) {
				log.debug("{} is unprojected Y coordinate", v.getName());
				return true;
			}
			if (NcUwConstants.StandardNames.Y_COORDINATE.equalsIgnoreCase(standardName)) {
				log.debug("{} is projected Y coordinate", v.getName());
				return true;
			}
			if (NcUwConstants.LongNames.LATITUDE.equalsIgnoreCase(longName)) {
				log.debug("{} is unprojected Y coordinate", v.getName());
				return true;
			}
			if (NcUwConstants.LongNames.Y_COORDINATE.equalsIgnoreCase(longName)) {
				log.debug("{} is unprojected Y coordinate", v.getName());
				return true;
			}
			if (NcUwConstants.Axis.Y.equalsIgnoreCase(getAxis(v))) {
				log.debug("{} is projected Y coordinate", v.getName());
				return true;
			}

			return false;
		}
	}, 
	/** the height dimension */ Z {
		@Override
		public boolean is(Variable v) {
			final String standardName = getStandardName(v);
			if (NcUwConstants.Axis.Z.equalsIgnoreCase(getAxis(v))) {
				return true;
			}
			if (NcUwConstants.LongNames.Z_COORDINATE.equalsIgnoreCase(getLongName(v))) {
				return true;
			}
			if (NcUwConstants.StandardNames.HEIGHT.equalsIgnoreCase(standardName)) {
				return true;
			}
			if (NcUwConstants.StandardNames.PRESSURE.equalsIgnoreCase(standardName)) {
				return true;
			}
			return false;
		}
	}, 
	/** the time dimension */ T {
		@Override
		public boolean is(Variable v) {
			if (NcUwConstants.Axis.T.equalsIgnoreCase(getAxis(v))) {
				return true;
			}
			if (NcUwConstants.StandardNames.TIME.equalsIgnoreCase(getStandardName(v))) {
				return true;
			}
			return false;
		}
	}, 
	/** the sample dimension */ S {
		@Override
		public boolean is(Variable v) {
			if (NcUwConstants.StandardNames.SAMPLE_DIMENSION
					.equalsIgnoreCase(getStandardName(v))) {
				return true;
			}
			if (NcUwConstants.LongNames.SAMPLE_DIMENSION
					.equalsIgnoreCase(getLongName(v))) {
				return true;
			}
			String ref = NcUwHelper.getStringAttribute(v,
					NcUwConstants.Attributes.REF);
			if (ref != null) {
				try {
					NcUwUncertaintyType type = NcUwUncertaintyType
							.fromUri(new URI(ref));
					if (type == NcUwUncertaintyType.CONTINUOUS_REALISATION
							|| type == NcUwUncertaintyType.CATEGORICAL_REALISATION) {
						return true;
					}
				} catch (URISyntaxException e) {
					new NcUwException(e);
				}
			}
			return false;
		}
	},
	/* the level dimension */ L {
		@Override
		public boolean is(Variable v) {
			if (NcUwConstants.StandardNames.LEVEL_DIMENSION
					.equalsIgnoreCase(getStandardName(v))) {
				return true;
			}
			if (NcUwConstants.LongNames.LEVEL_DIMENSION
					.equalsIgnoreCase(getLongName(v))) {
				return true;
			}
			return false;
		}
	};
	
	private static final Logger log = LoggerFactory.getLogger(NcUwDimension.class);
	
	public abstract boolean is(Variable v);
	
	public static NcUwDimension fromVariable(Variable v) {
		if (v.isCoordinateVariable()) {
			for (NcUwDimension d : values()) {
				if (d.is(v)) {
					return d;
				}
			}
		}
		return null;
	}
	
	protected static String getLongName(Variable v) {
		return NcUwHelper.getStringAttribute(v, NcUwConstants.Attributes.LONG_NAME);
	}

	protected static String getStandardName(Variable v) {
		return NcUwHelper.getStringAttribute(v, NcUwConstants.Attributes.STANDARD_NAME);
	}

	protected static String getAxis(Variable v) {
		return NcUwHelper.getStringAttribute(v, NcUwConstants.Attributes.AXIS);
	}

}