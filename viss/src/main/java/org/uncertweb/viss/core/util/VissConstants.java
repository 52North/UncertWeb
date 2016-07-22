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

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.uncertweb.viss.core.VissError;

public class VissConstants {
	public static final CoordinateReferenceSystem EPSG4326;

	static {
		try {
			EPSG4326 = CRS.decode("EPSG:4326");
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	public static final String CLEAN_UP_INTERVAL_DEFAULT = "PT2H";

	public static final String CLEAN_UP_INTERVAL_KEY = "cleanup.interval";

	public static final String CONFIG_FILE = "/viss.properties";

	public static final String DELETE_OLDER_THAN_PERIOD_DEFAULT = "P1D";

	public static final String DELETE_OLDER_THAN_PERIOD_KEY = "cleanup.deleteBefore";

	public static final String PRETTY_PRINT_IO_DEFAULT = "false";

	public static final String PRETTY_PRINT_IO_KEY = "prettyPrintIO";

	public static final String RESOURCE_STORE_KEY = "implementation.resourceStore";

	public static final String SEARCH_PACKAGES_KEY = "visualizerSearchPackages";

	public static final String VISUALIZATION_KEY = "implementation.visualization";

	public static final String VISUALIZATION_REFERENCE_KEY = "implementation.visualizationReference";

	public static final String WMS_ADAPTER_KEY = "implementation.wmsAdapter";

	public static final String WORKING_DIR_KEY = "workingDir";
}
