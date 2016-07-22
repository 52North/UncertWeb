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

import org.uncertweb.utils.UwConstants;


public abstract class NcUwConstants {

	public enum Origin {
		UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT;
	}

	public enum Order {
		ASCENDING, DESCENDING;
	}

	public static abstract class Fragments {
		public static final String ALPHA = "alpha";
		public static final String BETA = "beta";
		public static final String LOCATION = "location";
		public static final String SCALE = "scale";
		public static final String SHAPE = "shape";
		public static final String MEAN = "mean";
		public static final String VARIANCE = "variance";
		public static final String DEGREES_OF_FREEDOM = "degreesOfFreedom";
		public static final String MINIMUM = "minimum";
		public static final String MAXIMUM = "maximum";
		public static final String UPPER = "upper";
		public static final String LOWER = "lower";
		public static final String LEVEL = "level";
		public static final String RATE = "rate";
		public static final String VALUE = "value";
		public static final String ORDER = "order";
		public static final String LOG_SCALE = "logScale";
		public static final String DENOMINATOR = "denominator";
		public static final String NUMERATOR = "numerator";
		public static final String VARIANCE_SCALING = "varianceScaling";
	}

	public static abstract class Attributes {
		public static final String MISSING_VALUE = "missing_value";
		public static final String COORDINATES = "coordinates";
		public static final String ANCILLARY_VARIABLES = "ancillary_variables";
		public static final String REF = "ref";
		public static final String STANDARD_NAME = "standard_name";
		public static final String LONG_NAME = "long_name";
		public static final String SHAPE = "shape";
		public static final String PRIMARY_VARIABLES = "primary_variables";
		public static final String GRID_MAPPING = "grid_mapping";
		public static final String CONVENTIONS = "conventions";
		public static final String AXIS = "axis";
		public static final String PROJ4 = "proj4string";
		public static final String WKT = "wkt";
		public static final String EPSG_CODE = "epsg_code";
		public static final String PROCEDURE = "procedure";
		public static final String OBSERVED_PROPERTY = "observed_property";
		public static final String UNITS = "units";
		public static final String Z_ORIENTATION = "positive";
	}

	public static abstract class Axis {
		public static final String X = "x";
		public static final String Y = "y";
		public static final String Z = "z";
		public static final String T = "t";
	}

	public static abstract class StandardNames {
		public static final String LONGITUDE = "longitude";
		public static final String LATITUDE = "latitude";
		public static final String TIME = "time";
		public static final String HEIGHT = "height";
		public static final String PRESSURE = "pressure";
		public static final String X_COORDINATE = "projection_x_coordinate";
		public static final String Y_COORDINATE = "projection_y_coordinate";
		public static final String SAMPLE_DIMENSION = "realization";
		public static final String LEVEL_DIMENSION = "level";
	}

	public static abstract class LongNames {
		public static final String Z_COORDINATE = "z";
		public static final String Y_COORDINATE = "y";
		public static final String X_COORDINATE = "x";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";
		public static final String SAMPLE_DIMENSION = "realization";
		public static final String LEVEL_DIMENSION = "level";
	}

	public static final String UW_CONVENTION = "UW-1.0";
	public static final String CF_CONVENTION = "CF-1.5";

	/* TODO south/west? */
	public static final String UNIT_LONGITUDE = "degrees_east";
	public static final String UNIT_LATITUDE = "degrees_north";

	public static final String ORIENTATION_UP = "up";
	public static final String ORIENTATION_DOWN = "down";

	public static final URI DEFAULT_PROCEDURE = UwConstants.URL.MISSING.uri;
	public static final URI DEFAULT_OBSERVED_PROPERTY = UwConstants.URL.UNKNOWN.uri;
	public static final String DEFAULT_SAMPLING_FEATURE = UwConstants.URL.INAPPLICABLE.value;
}
