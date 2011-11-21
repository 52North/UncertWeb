package org.n52.sos.uncertainty.decode.impl;

import org.n52.sos.ogc.gml.GMLConstants;

/**
 * additional constants for handling O&M 2 observations and uncertainties
 * 
 * @author Kiesow
 */
public final class OM2Constants {

	// if non-static constants are added, this class will have to
	// be initialized first

	// observation type constants
	public static final String OBS_TYPE_BOOLEAN = "OM_BooleanObservation";
	public static final String OBS_TYPE_DISCNUM = "OM_DiscreteNumericObservation";
	public static final String OBS_TYPE_MEASUREMENT = "OM_Measurement";
	public static final String OBS_TYPE_REFERENCE = "OM_ReferenceObservation";
	public static final String OBS_TYPE_TEXT = "OM_TextObservation";
	public static final String OBS_TYPE_UNCERTAINTY = "OM_UncertaintyObservation";
	
    // namespaces of O&M 2 documents
    public static final String NS_OM2 = "http://www.opengis.net/om/2.0";
    
    public static final String NS_OM2_PREFIX = "om";
    
    public static final String NS_SAMS_PREFIX = "sams";
    
    public static final String EN_SAMPLINGPOINT = "SF_SamplingPoint";
    
    public static final String EN_SAMPLINGCURVE = "SF_SamplingCurve";
    
    public static final String EN_SAMPLINGSURFACE = "SF_SamplingSurface";
    
    public static final String EN_SAMPLINGGRID = "SF_SamplingGrid";
    
    
    

}
