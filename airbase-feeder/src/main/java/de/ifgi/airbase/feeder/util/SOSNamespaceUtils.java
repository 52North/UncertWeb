package de.ifgi.airbase.feeder.util;

import de.ifgi.airbase.feeder.io.sos.http.xml.AbstractXmlBuilder;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlTokenSource;

/**
 * Utility class to handle XML namespaces needed to communicate with a SOS.
 * 
 * @author Christian Autermann
 */
public class SOSNamespaceUtils {
    public static final String GML_NAMESPACE_URI = "http://www.opengis.net/gml"; // FIXME should end in /3.2
    public static final String GML_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";
    public static final String GML_3_2_NAMESPACE_URI = "http://www.opengis.net/gml/3.2";
    public static final String GML_3_2_SCHEMA_LOCATION = GML_NAMESPACE_SCHEMA_LOCATION;
    public static final String GML_NAMESPACE_PREFIX = "gml";
    public static final String GML_3_2_NAMESPACE_PREFIX = "gml";
    
    public static final String OM_1_0_NAMESPACE_URI = "http://www.opengis.net/om/1.0";
    public static final String OM_2_0_NAMESPACE_URI = "http://www.opengis.net/om/2.0";
    public static final String OM_1_0_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/om/1.0.0/extensions/observationSpecialization_constraint.xsd";
    public static final String OM_2_0_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/om/2.0/observation.xsd";
    public static final String OM_1_0_NAMESPACE_PREFIX = "om1";
    public static final String OM_2_0_NAMESPACE_PREFIX = "om2";
    
    public static final String OWS_1_1_0_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/ows/1.1.0/owsAll.xsd";
    public static final String OWS_1_1_NAMESPACE_URI = "http://www.opengis.net/ows/1.1";
    public static final String OWS_1_1_NAMESPACE_PREFIX = "ows";
    
    public static final String SA_1_0_0_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/sampling/1.0.0/sampling.xsd";
    public static final String SA_1_0_NAMESPACE_URI = "http://www.opengis.net/sampling/1.0";
    public static final String SA_1_0_NAMESPACE_PREFIX = "sa";
    
    public static final String SAMS_2_0_NAMESPACE_URI = "http://www.opengis.net/samplingSpatial/2.0";
    public static final String SAMS_2_0_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/samplingSpatial/2.0/spatialSamplingFeature.xsd";
    public static final String SAMS_2_0_NAMESPACE_PREFIX = "sams";
    
    public static final String SF_2_0_NAMESPACE_URI = "http://www.opengis.net/sampling/2.0";
    public static final String SF_2_0_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/sampling/2.0/samplingFeature.xsd";
    public static final String SF_2_0_NAMESPACE_PREFIX = "sf";
    
    public static final String SML_1_0_1_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/sensorML/1.0.1/sensorML.xsd";
    public static final String SML_1_0_1_NAMESPACE_SCHEMA_VERSION = "1.0.1";
    public static final String SML_1_0_1_NAMESPACE_URI = "http://www.opengis.net/sensorML/1.0.1";
    public static final String SML_1_0_1_NAMESPACE_PREFIX = "sml";
    public static final String SML_1_0_1_PROCEDURE_DESCRIPTION_FORMAT = SML_1_0_1_NAMESPACE_URI;

    public static final String SOS_1_0_NAMESPACE_URI = "http://www.opengis.net/sos/1.0";
    public static final String SOS_1_0_0_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/sos/1.0.0/sosAll.xsd";
    public static final String SOS_1_0_0_SERVICE_VERSION = "1.0.0";
    public static final String SOS_2_0_NAMESPACE_URI = "http://www.opengis.net/sos/2.0";
    public static final String SOS_2_0_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/sos/2.0/sos.xsd";
    public static final String SOS_2_0_0_SERVICE_VERSION = "2.0.0";
    public static final String SOS_1_0_NAMESPACE_PREFIX = "sos";
    public static final String SOS_2_0_NAMESPACE_PREFIX = "sos";
    
    public static final String SWE_1_0_1_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/sweCommon/1.0.1/swe.xsd";
    public static final String SWE_1_0_1_NAMESPACE_URI = "http://www.opengis.net/swe/1.0.1";
    public static final String SWE_2_0_NAMESPACE_URI = "http://www.opengis.net/swe/2.0";
    public static final String SWE_2_0_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/sweCommon/2.0/swe.xsd";
    public static final String SWE_1_0_1_NAMESPACE_PREFIX = "swe";
    public static final String SWE_2_0_NAMESPACE_PREFIX = "swe";

    public static final String SWES_2_0_NAMESPACE_URI = "http://www.opengis.net/swes/2.0";
    public static final String SWES_2_0_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/swes/2.0/swes.xsd";
    public static final String SWES_2_0_NAMEPSACE_PREFIX = "swes";

    public static final String XLINK_1999_NAMESPACE_URI = "http://www.w3.org/1999/xlink";
    public static final String XLINK_1999_NAMESPACE_SCHEMA_LOCATION = "http://www.w3.org/1999/xlink.xsd";
    public static final String XLINK_1999_NAMESPACE_PREFIX = "xlink";
    
    public static final String XSI_2001_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String XSI_2001_NAMESPACE_PREFIX = "xsi";
    
	public static final String SCHEMA_LOCATIONS = new StringBuilder()
			.append(SOS_1_0_NAMESPACE_URI).append(" ").append(SOS_1_0_0_NAMESPACE_SCHEMA_LOCATION).append(" ")
            .append(SOS_2_0_NAMESPACE_URI).append(" ").append(SOS_2_0_NAMESPACE_SCHEMA_LOCATION).append(" ")
			.append(OM_1_0_NAMESPACE_URI).append(" ").append(OM_1_0_NAMESPACE_SCHEMA_LOCATION).append(" ")
			.append(SA_1_0_NAMESPACE_URI).append(" ").append(SA_1_0_0_NAMESPACE_SCHEMA_LOCATION).append(" ")
			.append(SWE_1_0_1_NAMESPACE_URI).append(" ").append(SWE_1_0_1_NAMESPACE_SCHEMA_LOCATION).append(" ")
			.append(GML_NAMESPACE_URI).append(" ").append(GML_NAMESPACE_SCHEMA_LOCATION).append(" ")
			.append(OWS_1_1_NAMESPACE_URI).append(" ").append(OWS_1_1_0_NAMESPACE_SCHEMA_LOCATION).append(" ")
			.append(SML_1_0_1_NAMESPACE_URI).append(" ").append(SML_1_0_1_NAMESPACE_SCHEMA_LOCATION)
            .append(OM_2_0_NAMESPACE_URI).append(" ").append(OM_2_0_NAMESPACE_SCHEMA_LOCATION).append(" ")
            .append(SWE_2_0_NAMESPACE_URI).append(" ").append(SWE_2_0_NAMESPACE_SCHEMA_LOCATION).append(" ")
            .append(SWES_2_0_NAMESPACE_URI).append(" ").append(SWES_2_0_NAMESPACE_SCHEMA_LOCATION).append(" ")
            .append(GML_3_2_NAMESPACE_URI).append(" ").append(GML_3_2_SCHEMA_LOCATION).append(" ")
            .append(XLINK_1999_NAMESPACE_URI).append(" ").append(XLINK_1999_NAMESPACE_SCHEMA_LOCATION).append(" ")
            .append(SAMS_2_0_NAMESPACE_URI).append(" ").append(SAMS_2_0_NAMESPACE_SCHEMA_LOCATION).append(" ")
            .append(SF_2_0_NAMESPACE_URI).append(" ").append(SF_2_0_NAMESPACE_SCHEMA_LOCATION).append(" ")
            .toString();

	private static final String XML_OPTIONS_CHARACTER_ENCODING = "UTF-8";
    
    public static final String EN_GML_3_2_ABSTRACT_FEATURE = "AbstractFeature";
    public static final String EN_SWE_2_ABSTRACT_DATA_COMPONENT = "AbstractDataComponent";
    public static final String EN_SAMS_SF_SPATIAL_SAMPLING_FEATURE = "SF_SpatialSamplingFeature";
    public static final String EN_SWE_2_TEXT_ENCODING = "TextEncoding";
    public static final String EN_GML_3_2_POINT = "Point";
    public static final String EN_QUANTITY = "Quantity";
    public static final String EN_SWE_2_DATA_RECORD = "DataRecord";
    public static final String EN_SWE_2_ABSTRACT_ENCODING = "AbstractEncoding";
    public static final String EN_GML_ABSTRACT_FEATURE = "_Feature";
    public static final String EN_GML_FEATURE_COLLECTION = "FeatureCollection";
    public static final String EN_SA_SAMPLING_POINT = "SamplingPoint";
    public static final String EN_GML_TIME_PERIOD = "TimePeriod";
    public static final String EN_SWE_ENVELOPE = "Envelope";
    public static final String EN_SWE_POSITION = "Position";
    public static final String EN_SML_SYSTEM = "System";
    public static final String EN_SWE_DATA_RECORD = "DataRecord";
    public static final String EN_OM_MEASUREMENT = "Measurement";
    public static final String EN_XSI_SCHEMA_LOCATION = "schemaLocation";
    
    public static final QName QN_XSI_2001_SCHEMA_LOCATION = new QName(XSI_2001_NAMESPACE_URI, EN_XSI_SCHEMA_LOCATION);
    public static final QName QN_OM_1_0_MEASUREMENT = new QName(OM_1_0_NAMESPACE_URI, EN_OM_MEASUREMENT);
    public static final QName QN_SWE_1_0_1_DATA_RECORD = new QName(SWE_1_0_1_NAMESPACE_URI, EN_SWE_DATA_RECORD);
    public static final QName QN_SML_1_0_1_SYSTEM = new QName(SML_1_0_1_NAMESPACE_URI, EN_SML_SYSTEM);
    public static final QName QN_SWE_1_0_1_POSITION = new QName(SWE_1_0_1_NAMESPACE_URI, EN_SWE_POSITION);
    public static final QName QN_SWE_1_0_1_ENVELOPE = new QName(SWE_1_0_1_NAMESPACE_URI, EN_SWE_ENVELOPE);
    public static final QName QN_GML_TIME_PERIOD = new QName(GML_NAMESPACE_URI, EN_GML_TIME_PERIOD);
    public static final QName QN_SA_SAMPLING_POINT = new QName(SA_1_0_NAMESPACE_URI, EN_SA_SAMPLING_POINT);
    public static final QName QN_GML_FEATURE_COLLECTION = new QName(GML_NAMESPACE_URI, EN_GML_FEATURE_COLLECTION);
    public static final QName QN_GML_ABSTRACT_FEATURE = new QName(GML_NAMESPACE_URI, EN_GML_ABSTRACT_FEATURE);
    public static final QName QN_SAMS_2_0_SF_SPATIAL_SAMPLING_FEATURE = new QName(SAMS_2_0_NAMESPACE_URI, EN_SAMS_SF_SPATIAL_SAMPLING_FEATURE);
    public static final QName QN_SWE_2_DATA_RECORD = new QName(SWES_2_0_NAMESPACE_URI, EN_SWE_2_DATA_RECORD);
    public static final QName QN_SWE_2_ABSTRACT_DATA_COMPONENT = new QName(SWES_2_0_NAMESPACE_URI, EN_SWE_2_ABSTRACT_DATA_COMPONENT);
    public static final QName QN_SWE_2_TEXT_ENCODING = new QName(SWES_2_0_NAMESPACE_URI, EN_SWE_2_TEXT_ENCODING);
    public static final QName QN_SWE_2_ABSTRACT_ENCODING = new QName(SWES_2_0_NAMESPACE_URI, EN_SWE_2_ABSTRACT_ENCODING);
    public static final QName QN_GML_3_2_ABSTRACT_FEATURE = new QName(GML_3_2_NAMESPACE_URI, EN_GML_3_2_ABSTRACT_FEATURE);
    public static final QName QN_GML_3_2_POINT = new QName(GML_3_2_NAMESPACE_URI, EN_GML_3_2_POINT);
    
	/**
	 * Inserts the schema locations for the needed namespaces into the
	 * {@code XmlTokenSource}.
	 * 
	 * @param source
	 *            the XML document
	 */
	public static void insertSchemaLocations(XmlTokenSource source) {
		XmlCursor cursor = source.newCursor();
		cursor.toFirstContentToken();
		cursor.toLastAttribute();
		cursor.insertAttributeWithValue(QN_XSI_2001_SCHEMA_LOCATION, SCHEMA_LOCATIONS);
		cursor.dispose();
	}

	/**
	 * Creates the needed namespaces.
	 * 
	 * @param source
	 *            the XML document.
	 */
	public static Map<String,String> getNamesSpaceprefixes() {
        HashMap<String,String> prefixes = new HashMap<String, String>(16);
        prefixes.put(GML_NAMESPACE_URI, GML_NAMESPACE_PREFIX);
        prefixes.put(GML_3_2_NAMESPACE_URI, GML_3_2_NAMESPACE_PREFIX);
        prefixes.put(OM_1_0_NAMESPACE_URI, OM_1_0_NAMESPACE_PREFIX);
        prefixes.put(OM_2_0_NAMESPACE_URI, OM_2_0_NAMESPACE_PREFIX);
        prefixes.put(OWS_1_1_NAMESPACE_URI, OWS_1_1_NAMESPACE_PREFIX);
        prefixes.put(SA_1_0_NAMESPACE_URI, SA_1_0_NAMESPACE_PREFIX);
        prefixes.put(SAMS_2_0_NAMESPACE_URI, SAMS_2_0_NAMESPACE_PREFIX);
        prefixes.put(SF_2_0_NAMESPACE_URI, SF_2_0_NAMESPACE_PREFIX);
        prefixes.put(SML_1_0_1_NAMESPACE_URI, SML_1_0_1_NAMESPACE_PREFIX);
        prefixes.put(SOS_1_0_NAMESPACE_URI, SOS_1_0_NAMESPACE_PREFIX);
        prefixes.put(SOS_2_0_NAMESPACE_URI, SOS_2_0_NAMESPACE_PREFIX);
        prefixes.put(SWE_1_0_1_NAMESPACE_URI, SWE_1_0_1_NAMESPACE_PREFIX);
        prefixes.put(SWE_2_0_NAMESPACE_URI, SWE_2_0_NAMESPACE_PREFIX);
        prefixes.put(SWES_2_0_NAMESPACE_URI, SWES_2_0_NAMEPSACE_PREFIX);
        prefixes.put(XLINK_1999_NAMESPACE_URI, XLINK_1999_NAMESPACE_PREFIX);
        prefixes.put(XSI_2001_NAMESPACE_URI, XSI_2001_NAMESPACE_PREFIX);
        return prefixes;
	}
    
    public static String toString(XmlTokenSource source) {
        XmlOptions options = new XmlOptions();
		options.setCharacterEncoding(XML_OPTIONS_CHARACTER_ENCODING);
        options.setSaveSuggestedPrefixes(getNamesSpaceprefixes());
		options.setUseDefaultNamespace();
		options.setSaveNamespacesFirst();
		options.setSavePrettyPrint();
		options.setSaveAggressiveNamespaces();
        return source.xmlText(options);
    } 

    private SOSNamespaceUtils() {
    }

}
