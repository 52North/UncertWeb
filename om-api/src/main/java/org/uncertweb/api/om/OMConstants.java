package org.uncertweb.api.om;

import java.util.ArrayList;
import java.util.List;

import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.DiscreteNumericObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.ReferenceObservation;
import org.uncertweb.api.om.observation.TextObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;


/**
 * contains constants like namespace urls
 * 
 * @author Kiesow, staschc
 *
 */
public class OMConstants {

	// namespaces and prefixes
	/**namespace URL for Observations and Measurements*/
	public static final String NS_OM = "http://www.opengis.net/om/2.0";
	/**namespace prefix for Observations and Measurements*/
	public static final String NS_OM_PREFIX = "om";
	
	/**namespace URL for spatial sampling features*/
	public static final String NS_SAMS = "http://www.opengis.net/samplingSpatial/2.0";
	/**namespace prefix for spatial sampling features*/
	public static final String NS_SAMS_PREFIX = "sams";
	
	/**namespace URL for sampling features*/
	public static final String NS_SA = "http://www.opengis.net/sampling/2.0";

	/**namespace prefix for sampling features*/
	public static final String NS_SA_PREFIX = "sam";
	
	/**namespace prefix for sampling features*/
	public static final String NS_SF_PREFIX = "sf";
	
	/**namespace URL for xlinks*/
	public static final String NS_XLINK = "http://www.w3.org/1999/xlink";
	/**namespace prefix for xlinks*/
	public static final String NS_XLINK_PREFIX = "xlink";
	
	/**namespace URL for GML 3.2*/
	public static final String NS_GML = "http://www.opengis.net/gml/3.2";
	/**namespace prefix for GML 3.2*/
	public static final String NS_GML_PREFIX = "gml";
	
	/**namespace URL for XML Schema Instance*/
	public static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	/**namespace prefix for XML Schema Instance*/
	public static final String NS_XSI_PREFIX = "xsi";
	
	/**namespace URL for Geographic Information Metadata*/
	public static final String NS_GMD = "http://www.isotc211.org/2005/gmd";
	/**namespace prefix for Geographic Information Metadata */
	public static final String NS_GMD_PREFIX = "gmd";
	
	
	public static final String OM_SCHEMA_LOCATION = "http://52north.org/schema/geostatistics/uncertweb/Profiles/OM/UncertWeb_OM.xsd";
	
	/**definition URL for sampling feature types*/ 
	public static final String NS_SFT = "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/";
	/**definition prefix for sampling feature types*/
    public static final String NS_SFT_PREFIX = "sft";
    
    /**sampling feature type sampling point*/
    public static final String EN_SAMPLINGPOINT = "SF_SamplingPoint";
    /**sampling feature type sampling curve*/
    public static final String EN_SAMPLINGCURVE = "SF_SamplingCurve";
    /**sampling feature type sampling surface*/
    public static final String EN_SAMPLINGSURFACE = "SF_SamplingSurface";
    /**sampling feature type sampling grid*/
    public static final String EN_SAMPLINGGRID = "SF_SamplingGrid";
	public static final String NS_UNCERTML = "http://www.uncertml.org/2.0";
	public static final String NS_UNCERTML_PREFIX = "un";
    
    
	/**
	 * inner class holding the column names for CSV encoding
	 * 
	 * @author staschc
	 *
	 */
	public class Columns{
		
		//number of fixed columns (obs properties); number of uncertainty columns are depending on uncertainty type
		public static final int NUMBER_OF_COLUMNS = 7;
		
		//column names for observation properties
		public static final String PHEN_TIME="PhenomenonTime";
		public static final String FOI_IDENTIFIER = "Feature_ID";
		public static final String WKT_GEOM="WKTGeometry";
		public static final String SRID="EPSG";
		public static final String OBS_PROP="ObservedProperty";
		public static final String PROCEDURE="Procedure";
		public static final String RESULT="Result";
		
		//column names for uncertainty values
		public static final String CN_ND_MEAN="NormalDistribution.Mean";
		public static final String CN_ND_VAR="NormalDistribution.Variance";
		public static final String CN_REALISATION = "Realisation.";
		public static final String CN_PROBABILITY = "Probability";
		//TODO add more columnnames!
	}
	
	
	public static List<String> getObservationNames(){
		List<String> names = new ArrayList<String>(7);
		names.add(BooleanObservation.NAME);
		names.add(CategoryObservation.NAME);
		names.add(DiscreteNumericObservation.NAME);
		names.add(Measurement.NAME);
		names.add(ReferenceObservation.NAME);
		names.add(TextObservation.NAME);
		names.add(UncertaintyObservation.NAME);
		return names;
		
	}
}
