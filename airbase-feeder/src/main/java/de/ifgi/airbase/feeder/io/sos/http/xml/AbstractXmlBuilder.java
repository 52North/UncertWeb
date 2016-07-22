package de.ifgi.airbase.feeder.io.sos.http.xml;

import de.ifgi.airbase.feeder.data.EEAConfiguration;
import de.ifgi.airbase.feeder.data.EEAMeasurement;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.util.SOSNamespaceUtils;
import de.ifgi.airbase.feeder.util.Utils;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.xmlbeans.XmlObject;

/**
 * @param <T>
 * @author Christian Autermann
 */
public abstract class AbstractXmlBuilder<T extends XmlObject> {
    protected static final String OM_2_MEASUREMENT_OBSERVATION = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement";
    protected static final String SF_SAMPLING_POINT = "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint";
    protected static final String OGC_UNKNOWN = "http://www.opengis.net/def/nil/OGC/0/unknown";

    protected static final String SOS_SERVICE_NAME = "SOS";
	protected static final String SOS_V1_SERVICE_VERSION = "1.0.0";
    protected static final String SOS_V2_SERVICE_VERSION = "2.0.0";

	protected static final String UNIQUE_ID_DEFINITION = Utils.get("eea.urn.definition.uniqueId");
	protected static final String LONG_NAME_DEFINITION = Utils.get("eea.urn.definition.longName");
    protected static final String SHORT_NAME_DEFINITION = Utils.get("eea.urn.definition.shortName");
    protected static final String OFFERING_DEFINITION = Utils.get("eea.urn.definition.offering");
	protected static final String FOI_DEFINITION = Utils.get("eea.urn.definition.foi");
	protected static final String EPSG_4326_REFERENCE_SYSTEM_DEFINITION = Utils.get("eea.urn.definition.epsg.4326.urn");
    protected static final String EPSG_4326_REFERENCE_SYSTEM_DEFINITION_URL = Utils.get("eea.urn.definition.epsg.4326.url");
	protected static final String ISO8601_TIME_FORMAT_DEFINITION = Utils.get("eea.urn.definition.iso.8601");
	protected static final String STATION_URN_PREFIX = Utils.get("eea.urn.id.stationPrefix");
	protected static final String FOI_URN_PREFIX = Utils.get("eea.urn.id.foiPrefix");
	protected static final String DF_URN_PREFIX = Utils.get("eea.urn.id.dfPrefix");
	protected static final String COMPONENT_URN_PREFIX = Utils.get("eea.urn.id.propertyPrefix.component");

	protected static final String COORDINATE_UOM = "degree";
	protected static final String METER_UOM = "m";
	protected static final String SWE_DATA_ARRAY_BLOCK_SEPERATOR = ";";
	protected static final String SWE_DATA_ARRAY_TOKEN_SEPERATOR = ",";
	protected static final String SWE_DATA_ARRAY_DECIMAL_SEPERATOR = ".";

    protected static final NumberFormat MEASUREMENT_VALUE_FORMAT = new DecimalFormat(
            "0.########",  new DecimalFormatSymbols(Locale.US));

	protected static final Logger log = LoggerFactory.getLogger(AbstractXmlBuilder.class);

	private static HashMap<Integer,String> offeringsCache = new HashMap<Integer, String>();
	private static HashMap<Integer,String> phenomenonsCache = new HashMap<Integer, String>();
	private static HashMap<Integer,String> componentsCache = new HashMap<Integer, String>();
    protected static final String ISO8601_GREGORIAN_UOM = "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian";
    protected static final String PHENOMENON_TIME_DEFINITION = "http://www.opengis.net/def/property/OGC/0/PhenomenonTime";
    protected static final String PHENOMENON_TIME_FIELD = "phenomenonTime";
    protected static final String TEMPLATE_NIL_REASON = "template";

    public static final String FEATURE_CODE_SPACE = Utils.get("eea.urn.featureCodeSpace");
    public static final String URN_PREFIX = "urn:";

	public static String getPhenomenonId(int componentCode) {
		Integer code = new Integer(componentCode);
		String id = phenomenonsCache.get(code);
		if (id == null) {
			phenomenonsCache.put(code, id = Utils.get("eea.phenomenon.mapping." + componentCode));
		}
		return id;
	}

	public static String getComponentId(int componentCode) {
		Integer code = new Integer(componentCode);
		String id = componentsCache.get(code);
		if (id == null) {
			componentsCache.put(code, id = COMPONENT_URN_PREFIX + getOfferingName(componentCode));
		}
		return id;
	}

	public static String getOfferingName(int componentCode) {
		Integer code = new Integer(componentCode);
		String offering = offeringsCache.get(code);
		if (offering == null) {

			String phen = getPhenomenonId(componentCode);
            if (phen == null) {
                return null;
            }
			char[] phenChars = phen.toCharArray();
			StringBuffer buf = new StringBuffer();
			if (phenChars[phenChars.length - 1] != ')') {
				throw new RuntimeException(
						"Expected Phenomenon Id that ends with '(...)'; was '" + phen + "'.");
			}
			for (int i = phenChars.length - 2; i >= 0; i--) {
				if (phenChars[i] != '(') {
					buf.append(phenChars[i]);
				} else {
					break;
				}
			}
			offeringsCache.put(code, offering = buf.reverse().toString());
		}
		return offering;
	}

    public static String getOfferingName(EEAStation station) {
        String procedure = getStationId(station);
        if (procedure.startsWith(URN_PREFIX)) {
            return station.getEuropeanCode();
        } else {
            return getStationId(station) + "/observations";
        }
	}

	public static String getStationId(EEAStation station) {
		String code = station.getEuropeanCode();
		return STATION_URN_PREFIX + code;//.substring(0, 2) + ":" + code.substring(2);
	}

	public static String getFeatureOfInterestId(EEAStation station) {
		return FOI_URN_PREFIX + station.getEuropeanCode();
	}

	public static String getDomainFeatureId(EEAStation station) {
		return DF_URN_PREFIX + station.getEuropeanCode();
	}

	protected String buildPosString(EEAStation station) {
		return new StringBuilder()
				.append(new BigDecimal(station.getLatitude())).append(" ")
				.append(new BigDecimal(station.getLongitude())).append(" ")
				.append(new BigDecimal(station.getAltitude())).toString();
	}

    protected List<EEAMeasurement> sortMeasurements(Collection<EEAMeasurement> eeams) {
        List<EEAMeasurement> sortedMeasurements = new LinkedList<EEAMeasurement>(
				eeams);
		Collections.sort(sortedMeasurements, new Comparator<EEAMeasurement>() {
			@Override public int compare(EEAMeasurement o1, EEAMeasurement o2) {
				return o1.getTime().compareTo(o2.getTime());
			}
		});
        return sortedMeasurements;
    }

    protected String getResultTemplateIdentifier(EEAStation station, EEAConfiguration configuration) {
        String procedure = getStationId(station);
        boolean urn = procedure.startsWith(InsertResultTemplateRequestBuilder.URN_PREFIX);
        return new StringBuilder(procedure)
                .append(urn ? ":" : "/")
                .append("resultTemplate")
                .append(urn ? ":" : "/")
                .append(getOfferingName(configuration.getComponentCode())).toString();
    }


    protected Collection<EEAConfiguration> getUniqueConfigurations(EEAStation station) throws NoValidInputsOrOutputsException {
        HashMap<Integer, EEAConfiguration> uniqueConfigs = new HashMap<Integer, EEAConfiguration>();
        for (EEAConfiguration config : station.getConfigurations()) {
            if (!Utils.shouldBeIgnored(config.getComponentCode())) {
                uniqueConfigs.put(Integer.valueOf(config.getComponentCode()), config);
            }
        }
        if (uniqueConfigs.isEmpty()) {
            throw new NoValidInputsOrOutputsException();
        }
        return uniqueConfigs.values();
    }

    public String asString() throws EncodingException {
        return SOSNamespaceUtils.toString(build());
    }

    protected abstract T build() throws EncodingException;

}
