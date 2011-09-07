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

import javax.ws.rs.core.MediaType;

import org.uncertml.UncertML;
import org.uncertml.distribution.continuous.NormalDistribution;

public class Constants {

	public static final URI NORMAL_DISTRIBUTION = URI.create(UncertML.getURI(NormalDistribution.class));
	public static final URI NORMAL_DISTRIBUTION_MEAN = URI.create(Utils.join("#", NORMAL_DISTRIBUTION.toString(), "mean"));
	public static final URI NORMAL_DISTRIBUTION_VARIANCE = URI.create(Utils.join("#", NORMAL_DISTRIBUTION.toString(), "variance"));

	public static final String JSON_RESOURCE = "application/vnd.org.uncertweb.viss.resource+json";
	public static final MediaType JSON_RESOURCE_TYPE = MediaType.valueOf(JSON_RESOURCE);
	
	public static final String JSON_RESOURCE_LIST = "application/vnd.org.uncertweb.viss.resource.list+json";
	public static final MediaType JSON_RESOURCE_LIST_TYPE = MediaType.valueOf(JSON_RESOURCE_LIST);
	
	public static final String JSON_VISUALIZER = "application/vnd.org.uncertweb.viss.visualizer+json";
	public static final MediaType JSON_VISUALIZER_TYPE = MediaType.valueOf(JSON_VISUALIZER);
	
	public static final String JSON_VISUALIZER_LIST = "application/vnd.org.uncertweb.viss.visualizer.list+json";
	public static final MediaType JSON_VISUALIZER_LIST_TYPE = MediaType.valueOf(JSON_VISUALIZER_LIST);
	
	public static final String JSON_VISUALIZATION = "application/vnd.org.uncertweb.viss.visualization+json";
	public static final MediaType JSON_VISUALIZATION_TYPE = MediaType.valueOf(JSON_VISUALIZATION);
	
	public static final String JSON_VISUALIZATION_LIST = "application/vnd.org.uncertweb.viss.visualization.list+json";
	public static final MediaType JSON_VISUALIZATION_LIST_TYPE = MediaType.valueOf(JSON_VISUALIZATION_LIST);
	
	public static final String JSON_CREATE = "application/vnd.org.uncertweb.viss.create+json";
	public static final MediaType JSON_CREATE_TYPE = MediaType.valueOf(JSON_CREATE);

	public static final String JSON_REQUEST = "application/vnd.org.uncertweb.viss.request+json";
	public static final MediaType JSON_REQUEST_TYPE = MediaType.valueOf(JSON_REQUEST);
	
	public static final String NETCDF = "application/netcdf";
	public static final MediaType NETCDF_TYPE = MediaType.valueOf(NETCDF);
	
	public static final String X_NETCDF = "application/x-netcdf";
	public static final MediaType X_NETCDF_TYPE = MediaType.valueOf(X_NETCDF);
	
	public static final String GEOTIFF = "image/geotiff";
	public static final MediaType GEOTIFF_TYPE = MediaType.valueOf(GEOTIFF);
	
	public static final String OM_2 = "application/vnd.ogc.om+xml";
	public static final MediaType OM_2_TYPE = MediaType.valueOf(OM_2);

	public static final String STYLED_LAYER_DESCRIPTOR = "application/vnd.ogc.sld+xml";
	public static final MediaType STYLED_LAYER_DESCRIPTOR_TYPE = MediaType.valueOf(STYLED_LAYER_DESCRIPTOR);
	
	public static final String WORKING_DIR_KEY = "workingDir";
	public static final String CLEAN_UP_INTERVAL_KEY = "cleanup.interval";
	public static final String DELETE_OLDER_THAN_PERIOD_KEY = "cleanup.deleteBefore";
	public static final String RESOURCE_STORE_KEY = "implementation.resourceStore";
	public static final String WMS_ADAPTER_KEY = "implementation.wmsAdapter";
	public static final String SEARCH_PACKAGES_KEY = "visualizerSearchPackages";
	public static final String PRETTY_PRINT_IO_KEY = "prettyPrintIO";
	public static final String CONFIG_FILE = "/viss.properties";
	public static final String PRETTY_PRINT_IO_DEFAULT = "false";
	public static final String CLEAN_UP_INTERVAL_DEFAULT = "PT2H";
	public static final String DELETE_OLDER_THAN_PERIOD_DEFAULT = "P1D";
}
