package org.uncertweb.viss.core.util;

import static javax.ws.rs.core.MediaType.valueOf;
import javax.ws.rs.core.MediaType;

public class MediaTypes {
	private static final String PRE = "application/vnd.org.uncertweb.viss.";
	private static final String RESOURCE = PRE + "resource";
	private static final String RESOURCE_LIST = PRE + "resource.list";
	private static final String VISUALIZER = PRE + "visualizer";
	private static final String VISUALIZER_LIST = PRE + "visualizer.list";
	private static final String VISUALIZATION = PRE + "visualization";
	private static final String VISUALIZATION_LIST = PRE + "visualization.list";
	private static final String CREATE = PRE + "create";
	private static final String REQUEST = PRE + "request";

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

	public static final String JSON_VISUALIZATION = VISUALIZATION + "+json";
	public static final MediaType JSON_VISUALIZATION_TYPE = valueOf(JSON_VISUALIZATION);
	
	public static final String XML_VISUALIZATION = VISUALIZATION + "+xml";
	public static final MediaType XML_VISUALIZATION_TYPE = valueOf(XML_VISUALIZATION);

	public static final String JSON_VISUALIZATION_LIST = VISUALIZATION_LIST + "+json";
	public static final MediaType JSON_VISUALIZATION_LIST_TYPE = valueOf(JSON_VISUALIZATION_LIST);
	
	public static final String XML_VISUALIZATION_LIST = VISUALIZATION_LIST + "+xml";
	public static final MediaType XML_VISUALIZATION_LIST_TYPE = valueOf(XML_VISUALIZATION_LIST);

	public static final String JSON_CREATE = CREATE + "+json";
	public static final MediaType JSON_CREATE_TYPE = valueOf(JSON_CREATE);
	
	public static final String XML_CREATE = CREATE + "+xml";
	public static final MediaType XML_CREATE_TYPE = valueOf(XML_CREATE);

	public static final String JSON_REQUEST = REQUEST + "+json";
	public static final MediaType JSON_REQUEST_TYPE = valueOf(JSON_REQUEST);
	
	public static final String XML_REQUEST = REQUEST + "+xml";
	public static final MediaType XML_REQUEST_TYPE = valueOf(XML_REQUEST);

	public static final String NETCDF = "application/netcdf";
	public static final MediaType NETCDF_TYPE = valueOf(NETCDF);

	public static final String X_NETCDF = "application/x-netcdf";
	public static final MediaType X_NETCDF_TYPE = valueOf(X_NETCDF);

	public static final String GEOTIFF = "image/geotiff";
	public static final MediaType GEOTIFF_TYPE = valueOf(GEOTIFF);

	public static final String OM_2 = "application/vnd.ogc.om+xml";
	public static final MediaType OM_2_TYPE = valueOf(OM_2);

	public static final String STYLED_LAYER_DESCRIPTOR = "application/vnd.ogc.sld+xml";
	public static final MediaType STYLED_LAYER_DESCRIPTOR_TYPE = valueOf(STYLED_LAYER_DESCRIPTOR);

}
