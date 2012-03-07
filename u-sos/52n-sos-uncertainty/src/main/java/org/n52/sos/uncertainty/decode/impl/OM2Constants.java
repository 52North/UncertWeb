package org.n52.sos.uncertainty.decode.impl;

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
	
	// observation collection type constants
	public static final String OBS_COL_TYPE_BOOLEAN = "OM_BooleanCollection";
	public static final String OBS_COL_TYPE_DISCNUM = "OM_DiscreteNumericObservationCollection";
	public static final String OBS_COL_TYPE_MEASUREMENT = "OM_MeasurementCollection";
	public static final String OBS_COL_TYPE_REFERENCE = "OM_ReferenceObservationCollection";
	public static final String OBS_COL_TYPE_TEXT = "OM_TextObservationCollection";
	public static final String OBS_COL_TYPE_UNCERTAINTY = "OM_UncertaintyObservationCollection";
	
    // namespaces of O&M 2 documents
    public static final String NS_OM2 = "http://www.opengis.net/om/2.0";
    
    public static final String NS_OM2_PREFIX = "om";
    
    public static final String SCHEMA_LOCATION_OM2 = "";
    

    public static final String NS_SF = "http://www.opengis.net/sampling/2.0";
    
    public static final String NS_SF_PREFIX = "sf";
    
    public static final String SCHEMA_LOCATION_SF = "http://schemas.opengis.net/sampling/2.0/samplingFeature.xsd";
    

    public static final String NS_SAMS = "http://www.opengis.net/samplingSpatial/2.0";
    
    public static final String NS_SAMS_PREFIX = "sams";
    
    public static final String SCHEMA_LOCATION_SAMS = "http://schemas.opengis.net/samplingSpatial/2.0/spatialSamplingFeature.xsd";
    
}
