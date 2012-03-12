package org.uncertweb.api.om.sampling;


import java.net.URI;

import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.gml.geometry.RectifiedGrid;
import org.uncertweb.api.om.OMConstants;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

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
	private URI href;
	
	/**identifier of feature (optional)*/
	private Identifier identifer;
	
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
	 * @param sampledFeature
	 *            sampled feature
	 * @param shape
	 *            the feature's geometry
	 * @throws Exception 
	 */
	public SpatialSamplingFeature(String sampledFeature,
			Geometry shape) throws IllegalArgumentException {

		this(null, sampledFeature, shape);
	}

	/**
	 * Constructor with mandatory attributes plus identifier
	 * 
	 * @param identifier
	 *            identifier of the feature
	 * @param sampledFeature
	 *            sampled feature
	 * @param shape
	 *            the feature's geometry
	 * @throws Exception 
	 */
	public SpatialSamplingFeature(Identifier identifier, String sampledFeature,
			Geometry shape) throws IllegalArgumentException {

		if (shape instanceof Point){
			this.featureType = OMConstants.NS_SFT + OMConstants.EN_SAMPLINGPOINT;
		}
		else if (shape instanceof LineString){
			this.featureType = OMConstants.NS_SFT + OMConstants.EN_SAMPLINGCURVE;
		}
		else if (shape instanceof Polygon){
			this.featureType = OMConstants.NS_SFT + OMConstants.EN_SAMPLINGSURFACE;
		}
		else if (shape instanceof RectifiedGrid){
			this.featureType = OMConstants.NS_SFT + OMConstants.EN_SAMPLINGGRID;
		}
		this.setIdentifier(identifier);
		this.setSampledFeature(sampledFeature);
		this.setShape(shape);
	}
	
	/**
	 * constructor for sampling features which are referenced
	 * 
	 * @param href
	 * 			reference to the SamplingFeature
	 */
	public SpatialSamplingFeature(URI href){
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
	public SpatialSamplingFeature(Identifier identifier, Envelope boundedBy,
			String sampledFeature, Geometry shape) throws Exception {
		this(identifier, sampledFeature, shape);

		this.setBoundedBy(boundedBy);
	}

	// getters and setters
	/**
	 * 
	 * @return Returns identifier of the feature
	 */
	public Identifier getIdentifier() {
		return identifer;
	}

	/**
	 *	sets the identifier of the sampling feature 
	 * 
	 * @param identifier
	 */
	public void setIdentifier(Identifier identifier) {
		this.identifer = identifier;
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
	public URI getHref() {
		return href;
	}

	/**
	 * @param href the href to set
	 */
	public void setHref(URI href) {
		this.href = href;
	}

	/**
	 * @return the featureType
	 */
	public String getFeatureType() {
		return featureType;
	}
}
