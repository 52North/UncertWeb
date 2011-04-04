package de.ifgi.airbase.feeder.util;

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
	public static final String OM_NAMESPACE_URI = "http://www.opengis.net/om/1.0";
	public static final String SOS_NAMESPACE_URI = "http://www.opengis.net/sos/1.0";
	public static final String SWE_NAMESPACE_URI = "http://www.opengis.net/swe/1.0.1";
	public static final String SA_NAMESPACE_URI = "http://www.opengis.net/sampling/1.0";
	public static final String OWS_NAMESPACE_URI = "http://www.opengis.net/ows/1.1";
	public static final String SML_NAMESPACE_URI = "http://www.opengis.net/sensorML/1.0.1";
	public static final String GML_NAMESPACE_URI = "http://www.opengis.net/gml"; // FIXME should end in /3.2
	public static final String XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String XLINK_NAMESPACE_URI = "http://www.w3.org/1999/xlink";
	public static final String OM_NAMESPACE_PREFIX = "om";
	public static final String SOS_NAMESPACE_PREFIX = "sos";
	public static final String SWE_NAMESPACE_PREFIX = "swe";
	public static final String SA_NAMESPACE_PREFIX = "sa";
	public static final String OWS_NAMESPACE_PREFIX = "ows";
	public static final String GML_NAMESPACE_PREFIX = "gml";
	public static final String SML_NAMESPACE_PREFIX = "sml";
	public static final String XSI_NAMESPACE_PREFIX = "xsi";
	public static final String XLINK_NAMESPACE_PREFIX = "xlink";
	public static final String SOS_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/sos/1.0.0/sosAll.xsd";
	public static final String OM_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/om/1.0.0/extensions/observationSpecialization_constraint.xsd";
	public static final String SA_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/sampling/1.0.0/sampling.xsd";
	public static final String SWE_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/sweCommon/1.0.1/swe.xsd";
	public static final String GML_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";
	public static final String OWS_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/ows/1.1.0/owsAll.xsd";
	public static final String SML_NAMESPACE_SCHEMA_LOCATION = "http://schemas.opengis.net/sensorML/1.0.1/sensorML.xsd";
	public static final String SML_NAMESPACE_SCHEMA_VERSION = "1.0.1";
	public static final String SCHEMA_LOCATIONS = new StringBuilder()
			.append(SOS_NAMESPACE_URI).append(" ")
			.append(SOS_NAMESPACE_SCHEMA_LOCATION).append(" ")
			.append(OM_NAMESPACE_URI).append(" ")
			.append(OM_NAMESPACE_SCHEMA_LOCATION).append(" ")
			.append(SA_NAMESPACE_URI).append(" ")
			.append(SA_NAMESPACE_SCHEMA_LOCATION).append(" ")
			.append(SWE_NAMESPACE_URI).append(" ")
			.append(SWE_NAMESPACE_SCHEMA_LOCATION).append(" ")
			.append(GML_NAMESPACE_URI).append(" ")
			.append(GML_NAMESPACE_SCHEMA_LOCATION).append(" ")
			.append(OWS_NAMESPACE_URI).append(" ")
			.append(OWS_NAMESPACE_SCHEMA_LOCATION).append(" ")
			.append(SML_NAMESPACE_URI).append(" ")
			.append(SML_NAMESPACE_SCHEMA_LOCATION).toString();

	private static final String XML_OPTIONS_CHARACTER_ENCODING = "UTF-8";

	/**
	 * Qualifies the entity.
	 * 
	 * @param tag
	 *            the entity
	 * @return the qualified name
	 */
	public static QName swe(String tag) {
		return qualify(SWE_NAMESPACE_URI, tag);
	}

	/**
	 * Qualifies the entity.
	 * 
	 * @param tag
	 *            the entity
	 * @return the qualified name
	 */
	public static QName om(String tag) {
		return qualify(OM_NAMESPACE_URI, tag);
	}

	/**
	 * Qualifies the entity.
	 * 
	 * @param tag
	 *            the entity
	 * @return the qualified name
	 */
	public static QName gml(String tag) {
		return qualify(GML_NAMESPACE_URI, tag);
	}

	/**
	 * Qualifies the entity.
	 * 
	 * @param tag
	 *            the entity
	 * @return the qualified name
	 */
	public static QName sa(String tag) {
		return qualify(SA_NAMESPACE_URI, tag);
	}

	/**
	 * Qualifies the entity.
	 * 
	 * @param tag
	 *            the entity
	 * @return the qualified name
	 */
	public static QName ows(String tag) {
		return qualify(OWS_NAMESPACE_URI, tag);
	}

	/**
	 * Qualifies the entity.
	 * 
	 * @param tag
	 *            the entity
	 * @return the qualified name
	 */
	public static QName sml(String tag) {
		return qualify(SML_NAMESPACE_URI, tag);
	}

	/**
	 * Qualifies the entity.
	 * 
	 * @param tag
	 *            the entity
	 * @return the qualified name
	 */
	public static QName sos(String tag) {
		return qualify(SOS_NAMESPACE_URI, tag);
	}

	private static QName qualify(String uri, String tag) {
		return new QName(uri, tag);
	}

	/**
	 * Inserts the schema locations for the needed namespaces into the
	 * {@code XmlTokenSource}.
	 * 
	 * @param source
	 *            the XML document
	 */
	public static void schemaLocations(XmlTokenSource source) {
		XmlCursor cursor = source.newCursor();
		cursor.toFirstContentToken();
		cursor.toLastAttribute();
		cursor.insertAttributeWithValue(new QName(XSI_NAMESPACE_URI,
				"schemaLocation"), SCHEMA_LOCATIONS);
		cursor.dispose();
	}

	/**
	 * Creates the needed namespaces.
	 * 
	 * @param source
	 *            the XML document.
	 */
	public static void nameSpaceOptions(XmlTokenSource source) {
		XmlCursor cursor = source.newCursor();
		cursor.toFirstContentToken();
		cursor.toLastAttribute();
		cursor.insertNamespace(OM_NAMESPACE_PREFIX, OM_NAMESPACE_URI);
		cursor.insertNamespace(SA_NAMESPACE_PREFIX, SA_NAMESPACE_URI);
		cursor.insertNamespace(SWE_NAMESPACE_PREFIX, SWE_NAMESPACE_URI);
		cursor.insertNamespace(SOS_NAMESPACE_PREFIX, SOS_NAMESPACE_URI);
		cursor.insertNamespace(OWS_NAMESPACE_PREFIX, OWS_NAMESPACE_URI);
		cursor.insertNamespace(GML_NAMESPACE_PREFIX, GML_NAMESPACE_URI);
		cursor.insertNamespace(SML_NAMESPACE_PREFIX, SML_NAMESPACE_URI);
		cursor.insertNamespace(XSI_NAMESPACE_PREFIX, XSI_NAMESPACE_URI);
		cursor.insertNamespace(XLINK_NAMESPACE_PREFIX, XLINK_NAMESPACE_URI);
		cursor.dispose();
		XmlOptions options = new XmlOptions();
		options.setCharacterEncoding(XML_OPTIONS_CHARACTER_ENCODING);
		options.setUseDefaultNamespace();
		options.setSaveNamespacesFirst();
		options.setSavePrettyPrint();
		options.setSaveAggressiveNamespaces();
		source.xmlText(options);
	}

}
