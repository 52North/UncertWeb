package org.uncertweb.api.gml;

import java.util.Hashtable;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Container for common constants and helper methods used in the API
 * 
 * @author staschc
 *
 */
public class UwGMLUtil {

	//////////////////////////////////////////////////////////////////
	//CONSTANTS
	/**constant for EPSG URL used in srsName*/
	public final static String EPSG_URL = "http://www.opengis.net/def/crs/EPSG/0/";
	
	///////////////////////////////////////////////////////////////////
	// HELPER METHODS
	/**
     * parses an iso8601 time String to a DateTime Object.
     * 
     * @param timeString
     *            the time String
     * @return Returns a DateTime Object.
     * @throws OwsExceptionReport
     */
    public static DateTime parseIsoString2DateTime(String timeString) throws Exception {
        if (timeString == null || timeString.equals("")) {
            return null;
        }
        return ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(timeString);
        
    }
    
    
}
