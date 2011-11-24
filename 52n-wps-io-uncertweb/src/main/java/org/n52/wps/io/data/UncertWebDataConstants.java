package org.n52.wps.io.data;

/**
 * class contains UncertWeb data constants
 * 
 * @author staschc
 *
 */
public class UncertWebDataConstants {

	/**mime type of NetCDF-U files*/
	public static final String MIME_TYPE_NETCDFX = "application/x-netcdf";
	public static final String MIME_TYPE_NETCDF = "application/netcdf";
	
	/**mime type of O&M Uncertainty files*/
	public static final String MIME_TYPE_OMX = "application/x-om-u";
	public static final String MIME_TYPE_OMX_JSON = "application/x-om-u+json";
	public static final String MIME_TYPE_OMX_XML = "application/x-om-u+xml";
	

	/**mime type of UncertML in/outputs*/
	public static final String MIME_TYPE_UNCERTML = "application/x-uncertml";
	public static final String MIME_TYPE_UNCERTML_JSON = "application/x-uncertml+json";
	public static final String MIME_TYPE_UNCERTML_XML = "application/x-uncertml+xml";
	
	/**mime type of O&M v1 files*/
	public static final String MIME_TYPE_TEXT_XML = "text/xml";
	public static final String ENCODING_UTF_8 = "UTF-8";
	public static final String ENCODING_BINARY = "binary";
	
	
	public static final String SCHEMA_OM_V2 = "http://schemas.opengis.net/om/2.0/observation.xsd";
	public static final String SCHEMA_OM_V1 = "http://schemas.opengis.net/om/1.0.0/om.xsd";
	public static final String SCHEMA_UNCERTML = "http://uncertml.org/uncertml.xsd";
	public static final String SCHEMA_NETCDF_U = ""; //TODO
}
