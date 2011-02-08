package org.uncertweb.api.om;

/**
 * contains constants like namespace urls
 * @author Kiesow
 *
 */
public class OMConstants {

	// namespaces and prefixes
	
	public static final String NS_OM = "http://www.opengis.net/om/2.0";
	public static final String NS_OM_PREFIX = "om";
	
	public static final String NS_SAMS = "http://www.opengis.net/samplingSpatial/2.0";
	public static final String NS_SAMS_PREFIX = "sams";
	public static final String NS_SA = "http://www.opengis.net/sampling/2.0";
	public static final String NS_SA_PREFIX = "sa";
	
	public static final String NS_XLINK = "http://www.w3.org/1999/xlink";
	public static final String NS_XLINK_PREFIX = "xlink";
	
	public static final String NS_GML = "http://www.opengis.net/gml/3.2";
	public static final String NS_GML_PREFIX = "gml";
	
	
	// attribute names

    public static final String AN_ID = "id";
    
    public static final String AN_HREF = "href";
	
    
	// names of elements in O&M documents
	
    public static final String EN_OBSERVED_PROPERTY = "observedProperty";

    public static final String EN_OBSERVATION = "Observation";

    public static final String EN_PHENOMENON = "Phenomenon";

    public static final String EN_RESULT = "result";

    public static final String EN_TIME_PERIOD = "TimePeriod";

    public static final String EN_TIME_INSTANT = "TimeInstant";
    
    public static final String EN_PHENOMENON_TIME = "phenomenonTime";
    
    public static final String EN_RESULT_TIME = "resultTime";
    
    public static final String EN_VALID_TIME = "validTime";
    
}
