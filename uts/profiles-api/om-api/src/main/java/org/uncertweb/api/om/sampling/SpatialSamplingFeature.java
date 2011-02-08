package org.uncertweb.api.om.sampling;

import org.uncertweb.api.gml.geometry.GmlLineString;
import org.uncertweb.api.gml.geometry.GmlPoint;
import org.uncertweb.api.gml.geometry.GmlPolygon;
import org.uncertweb.api.gml.geometry.IGmlGeometry;
import org.uncertweb.api.gml.geometry.RectifiedGrid;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Class representing a spatial sampling feature primarily used for features of
 * interest; feature might be either be provided as reference (href member variable is set) 
 * or provided with complete content (href isn't set, but rest of attributes are set (boundedBy is optional) 
 * 
 * @author Kiesow, staschc
 * 
 */
public class SpatialSamplingFeature {

	/**reference to sampling feature*/
	private String href;
	
	/**gml ID of feature*/
	private String gmlId;
	
	/**envelope of the sampling feature*/
	private Envelope boundedBy;
	
	/** reference to the sampled feature (might be a lake for example)*/
	private String sampledFeature;
	
	/**indicates the type of the sampling feature*/
	private String featureType;
	
	/**geometry of the sampling feature*/
	private Geometry shape;

	/**
	 * Constructor with mandatory attributes
	 * 
	 * @param gmlId
	 *            gml id
	 * @param sampledFeature
	 *            sampled feature
	 * @param shape
	 *            the feature's geometry
	 * @throws Exception 
	 */
	public SpatialSamplingFeature(String gmlId, String sampledFeature,
			Geometry shape) throws Exception {

		if (!(shape instanceof IGmlGeometry)){
			throw new Exception("geometry of shape has to implement IGmlGeometry!!");
		}
		if (shape instanceof GmlPoint){
			this.featureType = "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint";
		}
		else if (shape instanceof GmlLineString){
			this.featureType="http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingCurve";
		}
		else if (shape instanceof GmlPolygon){
			this.featureType = "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingSurface";
		}
		else if (shape instanceof RectifiedGrid){
			this.featureType = "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingGrid";
		}
		this.setGmlId(gmlId);
		this.setSampledFeature(sampledFeature);
		this.setShape(shape);
	}
	
	/**
	 * constructor for sampling features which are referenced
	 * 
	 * @param href
	 * 			reference to the SamplingFeature
	 */
	public SpatialSamplingFeature(String href){
		this.href = href;
	}

	/**
	 * Constructor
	 * 
	 * @param gmlId
	 *            gml id
	 * @param boundedBy
	 *            (optional) spatial and temporal extent
	 * @param sampledFeature
	 *            sampled feature
	 * @param shape
	 *            shape
	 * @throws Exception 
	 */
	public SpatialSamplingFeature(String gmlId, Envelope boundedBy,
			String sampledFeature, Geometry shape) throws Exception {
		this(gmlId, sampledFeature, shape);

		this.setBoundedBy(boundedBy);
	}

	// getters and setters
	/**
	 * 
	 * @return Returns gml id of the feature
	 */
	public String getGmlId() {
		return gmlId;
	}

	
	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public Envelope getBoundedBy() {
		return boundedBy;
	}

	public void setBoundedBy(Envelope boundedBy) {
		this.boundedBy = boundedBy;
	}

	public String getSampledFeature() {
		return sampledFeature;
	}

	public void setSampledFeature(String sampledFeature) {
		this.sampledFeature = sampledFeature;
	}

	public Geometry getShape() {
		return shape;
	}

	public void setShape(Geometry shape) {
		this.shape = shape;
	}

	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}

	/**
	 * @param href the href to set
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * @return the featureType
	 */
	public String getFeatureType() {
		return featureType;
	}
}
