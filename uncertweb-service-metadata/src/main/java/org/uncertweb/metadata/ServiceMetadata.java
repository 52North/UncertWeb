package org.uncertweb.metadata;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

/**
 * class represents service metadata as defined in UncertWeb project 
 * (see <a href="https://wiki.aston.ac.uk/foswiki/bin/view/UncertWeb/ProcessMetadata">UncertWeb Process Metadata</a>)
 * 
 * @author staschc
 *
 */
public class ServiceMetadata {
	
	/**spatial resolution*/
	private String spatialResolutions;
	
	/**spatial support type*/
	private String spatialSupportTypes;
	
	/**
	 * spatial reference system; EPSG code (e.g. EPSG:4326) 
	 * or URL can be used (e.g. http://www.opengis.net/def/crs/EPSG/0/4326) can be used
	 */
	private String srs;

	/**
	 * spatial geometry types
	 */
	private String spatialGeometryTypes;
	
	
	/**
	 * spatial domain encoded as 'minx miny to maxx maxy' e.g. '42N 7W to 52N 8E) 
	 */
	private String spatialDomain;
	
	
	/**temporal resolution*/
	private String temporalResolutions;
	
	/**temporal support types*/
	private String temporalSupportTypes;
	
	/**
	 * spatial domain encoded as 'minx miny to maxx maxy' e.g. '42N 7W to 52N 8E) 
	 */
	private String temporalDomain;
	
	/**
	 * variable identifier - would be the observedProperty URI in case of O&M data and the variable ID in NetCDF 
	 */
	private String variablePhenomena;
	
	/**
	 * uncertainty type - UncertML type of uncertainty information
	 */
	private String variableUncertaintyTypes;
	
	/**
	 * units of measure of the variable 
	 */
	private String variableUOMs;
	
	/**
	 * any additional information about the inputs we need to encode
	 */
	private String misc;
	
	/**
	 * properties file representing the service metadata
	 */
	private Properties props;
	
	/**
	 * constructor
	 * 
	 * @param props
	 * 			{@link Properties} containing the ServiceMetadata 
	 */
	public ServiceMetadata(Properties props){
		this.spatialResolutions = props.getProperty("@spatial-resolutions");
		this.spatialSupportTypes = props.getProperty("@spatial-support-types");
		this.srs = props.getProperty("@spatial-crss");
		this.spatialGeometryTypes = props.getProperty("@spatial-geometry-types");
		this.spatialDomain = props.getProperty("@spatial-domain");
		this.temporalResolutions = props.getProperty("@temporal-resolutions");
		this.temporalSupportTypes = props.getProperty("@temporal-support-types");
		this.temporalDomain = props.getProperty("@temporal-domain");
		this.variablePhenomena = props.getProperty("@variable-phenomena");
		this.variableUncertaintyTypes = props.getProperty("@variable-uncertainty-types");
		this.variableUOMs = props.getProperty("@variable-units-of-measure");
		this.misc = props.getProperty("@misc");
		this.props = props;
	}
	
	/**
	 * 
	 * 
	 * @param props
	 */
	public ServiceMetadata(String propertiesString){
		props = new Properties();
		try {
			props.load(new StringReader(propertiesString));
		} catch (IOException e) {
			throw new RuntimeException("Error while reading string containing the UncertWeb metadata properties!");
		}
		this.spatialResolutions = props.getProperty("@spatial-resolutions");
		this.spatialSupportTypes = props.getProperty("@spatial-support-types");
		this.srs = props.getProperty("@spatial-crss");
		this.spatialGeometryTypes = props.getProperty("@spatial-geometry-types");
		this.spatialDomain = props.getProperty("@spatial-domain");
		this.temporalResolutions = props.getProperty("@temporal-resolutions");
		this.temporalSupportTypes = props.getProperty("@temporal-support-types");
		this.temporalDomain = props.getProperty("@temporal-domain");
		this.variablePhenomena = props.getProperty("@variable-phenomena");
		this.variableUncertaintyTypes = props.getProperty("@variable-uncertainty-types");
		this.variableUOMs = props.getProperty("@variable-units-of-measure");
		this.misc = props.getProperty("@misc");
	}
	
	/**
	 * serializes the metadata to a string
	 * 
	 * @return {@link String} containing the serialized service metadata
	 */
	public String serialize() {
		String result = props.toString();
		result = result.replace("{", "");
		result = result.replace(",", "");
		return result;
	}

	/**
	 * @return the spatialResolutions
	 */
	public String getSpatialResolutions() {
		return spatialResolutions;
	}

	/**
	 * @return the spatialSupportType
	 */
	public String getSpatialSupportType() {
		return spatialSupportTypes;
	}

	/**
	 * @return the srs
	 */
	public String getSrs() {
		return srs;
	}

	/**
	 * @return the spatialGeometryTypes
	 */
	public String getSpatialGeometryTypes() {
		return spatialGeometryTypes;
	}

	/**
	 * @return the spatialDomain
	 */
	public String getSpatialDomain() {
		return spatialDomain;
	}

	/**
	 * @return the temporalResolutions
	 */
	public String getTemporalResolutions() {
		return temporalResolutions;
	}

	/**
	 * @return the temporalSupportType
	 */
	public String getTemporalSupportType() {
		return temporalSupportTypes;
	}

	/**
	 * @return the temporalDomain
	 */
	public String getTemporalDomain() {
		return temporalDomain;
	}

	/**
	 * @return the variablePhenomena
	 */
	public String getVariablePhenomena() {
		return variablePhenomena;
	}

	/**
	 * @return the variableUncertaintyTypes
	 */
	public String getVariableUncertaintyTypes() {
		return variableUncertaintyTypes;
	}

	/**
	 * @return the variableUOMs
	 */
	public String getVariableUOMs() {
		return variableUOMs;
	}

	/**
	 * @return the misc
	 */
	public String getMisc() {
		return misc;
	}
}
