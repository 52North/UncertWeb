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

import static javax.ws.rs.core.MediaType.valueOf;

import javax.ws.rs.core.MediaType;

public class MediaTypes {
	
	private static final String PRE = "application/vnd.org.uncertweb.viss.";
	private static final String RESOURCE = PRE + "resource";
	private static final String RESOURCE_LIST = RESOURCE + ".list";
	private static final String VISUALIZER = PRE + "visualizer";
	private static final String VISUALIZER_LIST = VISUALIZER + ".list";
	private static final String DATASET = PRE + "dataset";
	private static final String DATASET_LIST = DATASET + ".list";
	private static final String VISUALIZATION = PRE + "visualization";
	private static final String VISUALIZATION_LIST = VISUALIZATION + ".list";
	private static final String VISUALIZATION_STYLE = PRE + "visualization-style";
	private static final String VISUALIZATION_STYLE_LIST = VISUALIZATION_STYLE + ".list";
	private static final String CREATE = PRE + "create";
	private static final String REQUEST = PRE + "request";
	private static final String VALUE_REQUEST = PRE + "value-request";
	private static final String UNCERTAINTY_COLLECTION = PRE + "uncertainty-collection";
	
	public static final String JSON_RESOURCE = RESOURCE + "+json";
	public static final MediaType JSON_RESOURCE_TYPE = valueOf(JSON_RESOURCE);
	public static final String XML_RESOURCE = RESOURCE + "+xml";
	public static final MediaType XML_RESOURCE_TYPE = valueOf(XML_RESOURCE);

	public static final String JSON_RESOURCE_LIST = RESOURCE_LIST + "+json";
	public static final MediaType JSON_RESOURCE_LIST_TYPE = valueOf(JSON_RESOURCE_LIST);
	public static final String XML_RESOURCE_LIST = RESOURCE_LIST + "+xml";
	public static final MediaType XML_RESOURCE_LIST_TYPE = valueOf(XML_RESOURCE_LIST);

	public static final String JSON_VISUALIZER = VISUALIZER + "+json";
	public static final MediaType JSON_VISUALIZER_TYPE = valueOf(JSON_VISUALIZER);
	public static final String XML_VISUALIZER = VISUALIZER + "+xml";
	public static final MediaType XML_VISUALIZER_TYPE = valueOf(XML_VISUALIZER);

	public static final String JSON_VISUALIZER_LIST = VISUALIZER_LIST + "+json";
	public static final MediaType JSON_VISUALIZER_LIST_TYPE = valueOf(JSON_VISUALIZER_LIST);
	public static final String XML_VISUALIZER_LIST = VISUALIZER_LIST + "+xml";
	public static final MediaType XML_VISUALIZER_LIST_TYPE = valueOf(XML_VISUALIZER_LIST);
	
	public static final String JSON_DATASET = DATASET + "+json";
	public static final MediaType JSON_DATASET_TYPE = valueOf(JSON_DATASET);
	public static final String XML_DATASET = DATASET + "+xml";
	public static final MediaType XML_DATASET_TYPE = valueOf(XML_DATASET);

	public static final String JSON_DATASET_LIST = DATASET_LIST + "+json";
	public static final MediaType JSON_DATASET_LIST_TYPE = valueOf(JSON_DATASET_LIST);
	public static final String XML_DATASET_LIST = DATASET_LIST + "+xml";
	public static final MediaType XML_DATASET_LIST_TYPE = valueOf(XML_DATASET_LIST);

	public static final String JSON_VISUALIZATION = VISUALIZATION + "+json";
	public static final MediaType JSON_VISUALIZATION_TYPE = valueOf(JSON_VISUALIZATION);
	public static final String XML_VISUALIZATION = VISUALIZATION + "+xml";
	public static final MediaType XML_VISUALIZATION_TYPE = valueOf(XML_VISUALIZATION);

	public static final String JSON_VISUALIZATION_LIST = VISUALIZATION_LIST + "+json";
	public static final MediaType JSON_VISUALIZATION_LIST_TYPE = valueOf(JSON_VISUALIZATION_LIST);
	public static final String XML_VISUALIZATION_LIST = VISUALIZATION_LIST + "+xml";
	public static final MediaType XML_VISUALIZATION_LIST_TYPE = valueOf(XML_VISUALIZATION_LIST);
	
	public static final String JSON_VISUALIZATION_STYLE = VISUALIZATION_STYLE + "+json";
	public static final MediaType JSON_VISUALIZATION_STYLE_TYPE = valueOf(JSON_VISUALIZATION_STYLE);
	public static final String XML_VISUALIZATION_STYLE = VISUALIZATION_STYLE + "+xml";
	public static final MediaType XML_VISUALIZATION_STYLE_TYPE = valueOf(XML_VISUALIZATION_STYLE);
	
	public static final String JSON_VISUALIZATION_STYLE_LIST = VISUALIZATION_STYLE_LIST + "+json";
	public static final MediaType JSON_VISUALIZATION_STYLE_LIST_TYPE = valueOf(JSON_VISUALIZATION_STYLE_LIST);
	public static final String XML_VISUALIZATION_STYLE_LIST = VISUALIZATION_STYLE_LIST + "+xml";
	public static final MediaType XML_VISUALIZATION_STYLE_LIST_TYPE = valueOf(XML_VISUALIZATION_STYLE_LIST);

	public static final String JSON_CREATE = CREATE + "+json";
	public static final MediaType JSON_CREATE_TYPE = valueOf(JSON_CREATE);
	public static final String XML_CREATE = CREATE + "+xml";
	public static final MediaType XML_CREATE_TYPE = valueOf(XML_CREATE);

	public static final String JSON_REQUEST = REQUEST + "+json";
	public static final MediaType JSON_REQUEST_TYPE = valueOf(JSON_REQUEST);
	public static final String XML_REQUEST = REQUEST + "+xml";
	public static final MediaType XML_REQUEST_TYPE = valueOf(XML_REQUEST);
	
	public static final String JSON_VALUE_REQUEST = VALUE_REQUEST + "+json";
	public static final MediaType JSON_VALUE_REQUEST_TYPE = valueOf(JSON_VALUE_REQUEST);
	public static final String XML_VALUE_REQUEST = VALUE_REQUEST + "+xml";
	public static final MediaType XML_VALUE_REQUEST_TYPE = valueOf(XML_VALUE_REQUEST);

	public static final String NETCDF = "application/netcdf";
	public static final MediaType NETCDF_TYPE = valueOf(NETCDF);
	public static final String X_NETCDF = "application/x-netcdf";
	public static final MediaType X_NETCDF_TYPE = valueOf(X_NETCDF);

	public static final String GEOTIFF = "image/geotiff";
	public static final MediaType GEOTIFF_TYPE = valueOf(GEOTIFF);

	public static final String OM_2 = "application/vnd.ogc.om+xml";
	public static final MediaType OM_2_TYPE = valueOf(OM_2);
	
	public static final String JSON_OBSERVATION_COLLECTION = "application/vnd.ogc.om+json";
	public static final MediaType JSON_OBSERVATION_COLLECTION_TYPE = valueOf(JSON_OBSERVATION_COLLECTION);

	public static final String STYLED_LAYER_DESCRIPTOR = "application/vnd.ogc.sld+xml";
	public static final MediaType STYLED_LAYER_DESCRIPTOR_TYPE = valueOf(STYLED_LAYER_DESCRIPTOR);
	
	public static final String JSON_SCHEMA = "application/schema+json";
	public static final MediaType JSON_SCHEMA_TYPE = valueOf(JSON_SCHEMA);
	
	public static final String JSON_UNCERTAINTY_COLLECTION = UNCERTAINTY_COLLECTION + "+json";
	public static final MediaType JSON_UNCERTAINTY_COLLECTION_TYPE = valueOf(JSON_UNCERTAINTY_COLLECTION);
	public static final String XML_UNCERTAINTY_COLLECTION = UNCERTAINTY_COLLECTION + "+xml";
	public static final MediaType XML_UNCERTAINTY_COLLECTION_TYPE = valueOf(XML_UNCERTAINTY_COLLECTION);
	
}
