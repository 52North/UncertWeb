package org.uncertweb.api.gml;

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

	///Constants for Geometry type names
	public final static String POINT_TYPE="Point";
	public final static String LINESTRING_TYPE="LineString";
	public final static String POLYGON_TYPE="Polygon";
	public final static String RECTIFIEDGRID_TYPE="RectifiedGrid";
	public final static String MULTIPOINT_TYPE="MultiPoint";
	public final static String MULTILINESTRING_TYPE="MultiLineString";
	public final static String MULTIPOLYGON_TYPE="MultiPolygon";
	public final static String MULTIRECTIFIEDGRID_TYPE="MultiRectifiedGrid";



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
