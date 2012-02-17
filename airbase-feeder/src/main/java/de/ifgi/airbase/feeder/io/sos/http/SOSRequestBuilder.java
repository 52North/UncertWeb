package de.ifgi.airbase.feeder.io.sos.http;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.util.Utils;

/**
 * @author Christian Autermann
 */
public abstract class SOSRequestBuilder {
	protected static final String SOS_SERVICE_NAME = "SOS";
	protected static final String SOS_SERVICE_VERSION = "1.0.0";
	protected static final String UNIQUE_ID_DEFINITION = Utils.get("eea.urn.definition.uniqueId");
	protected static final String LONG_NAME_DEFINITION = Utils.get("eea.urn.definition.longName");
    protected static final String SHORT_NAME_DEFINITION = Utils.get("eea.urn.definition.shortName");
	protected static final String FOI_DEFINITION = Utils.get("eea.urn.definition.foi");
	protected static final String EPSG_4326_REFERENCE_SYSTEM_DEFINITION = Utils.get("eea.urn.definition.epsg.4326");
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
	protected static final NumberFormat MEASUREMENT_VALUE_FORMAT = 
		new DecimalFormat("0.########", 
		new DecimalFormatSymbols(Locale.US));
	protected static final Logger log = LoggerFactory.getLogger(SOSRequestBuilder.class);
	
	private static HashMap<Integer,String> offeringsCache = new HashMap<Integer, String>();
	private static HashMap<Integer,String> phenomenonsCache = new HashMap<Integer, String>();
	private static HashMap<Integer,String> componentsCache = new HashMap<Integer, String>();

	protected static String getPhenomenonId(int componentCode) {
		Integer code = new Integer(componentCode);
		String id = phenomenonsCache.get(code);
		if (id == null) {
			phenomenonsCache.put(code, id = Utils.get("eea.phenomenon.mapping." + componentCode));
		}
		return id;
	}

	protected static String getComponentId(int componentCode) {
		Integer code = new Integer(componentCode);
		String id = componentsCache.get(code);
		if (id == null) {
			componentsCache.put(code, id = COMPONENT_URN_PREFIX + getOfferingName(componentCode));
		}
		return id;
	}
	
	protected static String getOfferingName(int componentCode) {
		Integer code = new Integer(componentCode);
		String offering = offeringsCache.get(code);
		if (offering == null) {

			String phen = getPhenomenonId(componentCode);
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

	protected static String getStationId(EEAStation station) {
		String code = station.getEuropeanCode();
		
		return STATION_URN_PREFIX + code;//.substring(0, 2) + ":" + code.substring(2);
	}

	protected static String getFeatureOfInterestId(EEAStation station) {
		return FOI_URN_PREFIX + station.getEuropeanCode();
	}

	protected static String getDomainFeatureId(EEAStation station) {
		return DF_URN_PREFIX + station.getEuropeanCode();
	}

	protected static String buildPosString(EEAStation station) {
		return new StringBuilder()
				.append(new BigDecimal(station.getLatitude())).append(" ")
				.append(new BigDecimal(station.getLongitude())).append(" ")
				.append(new BigDecimal(station.getAltitude())).toString();
	}

}
