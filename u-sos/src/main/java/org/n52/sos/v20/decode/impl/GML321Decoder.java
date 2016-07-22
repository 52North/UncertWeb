/***************************************************************
 Copyright (C) 2011
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under
 the terms of the GNU General Public License version 2 as published by the
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.v20.decode.impl;

import javax.xml.namespace.QName;

import net.opengis.fes.x20.SpatialOpsType;
import net.opengis.fes.x20.TemporalOpsType;
import net.opengis.gml.x32.EnvelopeDocument;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.TimeInstantDocument;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodDocument;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePositionType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.n52.sos.Sos1Constants.GetObservationParams;
import org.n52.sos.SosConfigurator;
import org.n52.sos.SosConstants;
import org.n52.sos.SosDateTimeUtilities;
import org.n52.sos.ogc.gml.time.ISosTime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionLevel;
import org.n52.sos.utilities.JTSUtilities;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * class offers methods for parsing GML 3.2.1 elements of requests
 *
 * @author Carsten Hollmann
 *
 */
public class GML321Decoder {

    /** logger */
    private static Logger LOGGER = Logger.getLogger(GML321Decoder.class);

    private static String CS = ",";

    private static String DECIMAL = ".";

    private static String TS = " ";

    /**
     * Parses a XML representation of a spatial filter.
     *
     * @param xbSpatialOpsType
     *            XML spatial filter representation.
     * @return SOS representation of a spatial filter
     * @throws OwsExceptionReport
     */
    public static Geometry parseSpatialFilterOperand(SpatialOpsType xbSpatialOpsType) throws OwsExceptionReport {
        Geometry geometry = null;
        QName envelopeName = new QName(OMConstants.NS_GML_32, OMConstants.EN_ENVELOPE);
        XmlCursor timeCursor = xbSpatialOpsType.newCursor();
        if (timeCursor.toChild(envelopeName)) {
            geometry = getGeometry4BBOX(timeCursor.getDomNode());
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, "Time",
                    "The requested spatial filter operand is not supported by this SOS!");
            LOGGER.error("The requested spatial filter operand is not supported by this SOS!");
            throw se;
        }
        return geometry;
    }

    /**
     * parses the BBOX element of the featureOfInterest element contained in the
     * GetObservation request and returns a String representing the BOX in
     * Well-Known-Text format
     *
     * @param xb_bbox
     *            XmlBean representing the BBOX-element in the request
     * @return Returns WKT-String representing the BBOX as Multipoint with two
     *         elements
     * @throws OwsExceptionReport
     *             if parsing the BBOX element failed
     */
    public static Geometry getGeometry4BBOX(Node node) throws OwsExceptionReport {
        Geometry result = null;
        try {
            EnvelopeDocument xbEnvelopeDocument = EnvelopeDocument.Factory.parse(node);
            EnvelopeType xbEnvelope = xbEnvelopeDocument.getEnvelope();
            int srid = parseSrsName(xbEnvelope.getSrsName());
            String lowerCorner = xbEnvelope.getLowerCorner().getStringValue();
            String upperCorner = xbEnvelope.getUpperCorner().getStringValue();
            if (SosConfigurator.getInstance().switchCoordinatesForEPSG(srid)) {
                lowerCorner = switchCoordinatesInString(lowerCorner);
                upperCorner = switchCoordinatesInString(upperCorner);
            }

            String geomWKT = "MULTIPOINT(" + lowerCorner + ", " + upperCorner + ")";

            result = JTSUtilities.createGeometryFromWKT(geomWKT);
            result.setSRID(srid);
        } catch (XmlException xmle) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, "Time",
                    "Error while parsing envelope: " + xmle.getMessage());
            LOGGER.error(se.getMessage() + xmle.getMessage());
            throw se;
        }

        return result;
    }// end getGeometry4BBOX

    /**
     * switches the order of coordinates contained in a string, e.g. from String
     * '3.5 4.4' to '4.4 3.5'
     *
     * NOTE: ACTUALLY checks, whether dimension is 2D, othewise throws
     * Exception!!
     *
     * @param coordsString
     *            contains coordinates, which should be switched
     * @return Returns String contained coordinates in switched order
     * @throws OwsExceptionReport
     */
    public static String switchCoordinatesInString(String coordsString) throws OwsExceptionReport {
        String switchedCoordString = null;
        String[] coordsArray = coordsString.split(" ");
        if (coordsArray.length != 2) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, null,
                    "An error occurred, while switching coordinates. Only a pair with two coordinates are supported!");
            LOGGER.error("Error while  switching coordinates. Only a pair with two coordinates are supported! "
                    + se.getMessage());
            throw se;
        } else {
            switchedCoordString = coordsArray[1] + " " + coordsArray[0];
        }
        return switchedCoordString;
    }

    /**
     * parses the srsName and returns an integer representing the number of the
     * EPSG-code of the passed srsName
     *
     * @param srsName
     *            name of the spatial reference system in EPSG-format (withn urn
     *            identifier for EPSG)
     * @return Returns an integer representing the number of the EPSG-code of
     *         the passed srsName
     * @throws OwsExceptionReport
     *             if parsing the srsName failed
     */
    public static int parseSrsName(String srsName) throws OwsExceptionReport {
        int srid = Integer.MIN_VALUE;
        String srsNamePrefix = SosConfigurator.getInstance().getSrsNamePrefixSosV2();
        if (!(srsName == null || srsName.equals(""))) {
            try {
                srid = Integer.valueOf(srsName.replace(srsNamePrefix, "")).intValue();
            } catch (Exception e) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(
                        OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                        null,
                        "For geometry of the feature of interest parameter has to have a srsName attribute, which contains the Srs Name as EPSGcode following the following schema:"
                                + srsNamePrefix + "number!");
                LOGGER.error("Error while parsing srsName of featureOfInterest parameter: " + se.getMessage());
                throw se;
            }
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(
                    OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                    null,
                    "For geometry of the feature of interest parameter has to have a srsName attribute, which contains the Srs Name as EPSGcode following the following schema:"
                            + srsNamePrefix + "number!");
            LOGGER.error("Error while parsing srsName of featureOfInterest parameter: " + se.getMessage());
            throw se;
        }
        return srid;
    }// end parseSrsName

    /**
     * Parses a XML time object to a SOS representation.
     *
     * @param xbTemporalOpsType
     *            XML time object
     * @return SOS time object representation.
     * @throws OwsExceptionReport
     */
    public static ISosTime parseTime(TemporalOpsType xbTemporalOpsType) throws OwsExceptionReport {
        ISosTime sosTime = null;
        QName timeInstantName = new QName(OMConstants.NS_GML_32, OMConstants.EN_TIME_INSTANT);
        QName timePeriodName = new QName(OMConstants.NS_GML_32, OMConstants.EN_TIME_PERIOD);
        XmlCursor timeCursor = xbTemporalOpsType.newCursor();
        try {
            if (timeCursor.toChild(timeInstantName)) {
                sosTime = parseTimeInstantNode(timeCursor.getDomNode());
            } else if (timeCursor.toChild(timePeriodName)) {
                sosTime = parseTimePeriodNode(timeCursor.getDomNode());
            } else {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, "Time",
                        "The requested time type is not supported by this SOS!");
                LOGGER.error("The requested time type is not supported by this SOS!");
                throw se;
            }
        } catch (XmlException xmle) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, "Time",
                    "Error while parsing time: " + xmle.getMessage());
            LOGGER.error(se.getMessage() + xmle.getMessage());
            throw se;
        }
        timeCursor.dispose();
        return sosTime;
    }

    /**
     * parses TimeInstant
     *
     * @param tp
     *            XmlBean representation of TimeInstant
     * @return Returns a TimeInstant created from the TimeInstantType
     * @throws java.text.ParseException
     * @throws java.text.ParseException
     *             if parsing the datestring into java.util.Date failed
     * @throws OwsExceptionReport
     */
    public static TimeInstant parseTimeInstant(TimeInstantType xbTimeIntant) throws java.text.ParseException,
            OwsExceptionReport {
        TimeInstant ti = new TimeInstant();
        TimePositionType xbTimePositionType = xbTimeIntant.getTimePosition();
        String timeString = xbTimePositionType.getStringValue();
        if (timeString != null && !timeString.equals("")) {
            ti.setValue(SosDateTimeUtilities.parseIsoString2DateTime(timeString));
            ti.setRequestedTimeLength(timeString.length());
        }
//        if (xbTimePositionType.getIndeterminatePosition() != null) {
//            ti.setIndeterminateValue(xbTimePositionType.getIndeterminatePosition().toString());
//            ti.setRequestedTimeLength(xbTimePositionType.getIndeterminatePosition().toString().length());
//        }
        return ti;
    }

    /**
     * help method, which creates an TimeInstant object from the DOM-node of the
     * TimeInstantType. This constructor is necessary cause XMLBeans does not
     * full support substitution groups. So one has to do a workaround with
     * XmlCursor and the DomNodes of the elements.
     *
     * @param timeInstant
     *            DOM Node of timeInstant element
     * @return Returns a TimeInstant created from the DOM-Node
     * @throws OwsExceptionReport
     *             if no timePosition element is cotained in the timeInstant
     *             element
     * @throws XmlException
     *             if parsing the DomNode to an XMLBeans XmlObject failed
     */
    public static TimeInstant parseTimeInstantNode(Node timeInstant) throws OwsExceptionReport, XmlException {

        TimeInstant ti = new TimeInstant();

        TimeInstantDocument xbTimeInstantDocument = TimeInstantDocument.Factory.parse(timeInstant);
        TimeInstantType xbTimeInstant = xbTimeInstantDocument.getTimeInstant();

        if (xbTimeInstant.getTimePosition() != null) {
            String positionString = xbTimeInstant.getTimePosition().getStringValue();
            if (positionString != null && !positionString.equals("")) {
                if (positionString.equals(SosConstants.LATEST) || positionString.equals(SosConstants.GET_FIRST)) {
                    ti.setIndeterminateValue(positionString);
                    ti.setRequestedTimeLength(positionString.length());
                } else {
                    ti.setValue(SosDateTimeUtilities.parseIsoString2DateTime(positionString));
                    ti.setRequestedTimeLength(positionString.length());
                }
            }

            // if intdeterminateTime attribute is set, set string value
//            if (xbTimeInstant.getTimePosition().getIndeterminatePosition() != null) {
//                ti.setIndeterminateValue(xbTimeInstant.getTimePosition().getIndeterminatePosition().toString());
//            }
//            if (!(ti.getIndeterminateValue() != null && !ti.getIndeterminateValue().isEmpty())
//                    && ti.getValue() == null) {
//                OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
//                se.addCodedException(ExceptionCode.MissingParameterValue, "gml:timePosition",
//                        "No IndeterminateValue attribute and gml:timePosition value ist null or empty!");
//                throw se;
//            }
        } else {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(ExceptionCode.MissingParameterValue, "gml:timePosition",
                    "No timePosition element is contained in the gml:timeInstant element");
            throw se;
        }
        return ti;
    }

    /**
     * parse methode, which creates an TimePeriod object from the DOM-node of
     * the TimePeriodType. This constructor is necessary cause XMLBeans does not
     * fully support substitution groups. So one has to do a workaround with
     * XmlCursor and the DomNodes of the elements.
     *
     *
     * @param timePeriod
     *            the DomNode of the timePeriod element
     * @return Returns a TimePeriod created from the DOM-Node
     * @throws XmlException
     *             if the Node could not be parsed into a XmlBean
     * @throws OwsExceptionReport
     *             if required elements of the timePeriod are missed
     */
    public static TimePeriod parseTimePeriodNode(Node timePeriod) throws XmlException, OwsExceptionReport {

        TimePeriod tp = new TimePeriod();

        TimePeriodDocument xbTimePeriodDocument = TimePeriodDocument.Factory.parse(timePeriod);
        TimePeriodType xbTimePeriod = xbTimePeriodDocument.getTimePeriod();

        if (xbTimePeriod.getBegin() != null) {

        } else if (xbTimePeriod.getBegin() != null) {
            String startString = xbTimePeriod.getBegin().getTimeInstant().getTimePosition().getStringValue();
            if (startString.equals("")) {
                tp.setStart(null);
            } else {
                tp.setStart(SosDateTimeUtilities.parseIsoString2DateTime(startString));
            }
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                    GetObservationParams.eventTime.toString(),
                    "The start time is missed in the timePeriod element of time parameter!");
            throw se;
        }

        if (xbTimePeriod.getEnd() != null) {
            // TODO:
        } else if (xbTimePeriod.getEnd() != null) {
            String endString = xbTimePeriod.getEnd().getTimeInstant().getTimePosition().getStringValue();
            if (endString.equals("")) {
                tp.setEnd(null);
            } else {
                tp.setEnd(SosDateTimeUtilities.setDateTime2EndOfDay4RequestedEndPosition(
                        SosDateTimeUtilities.parseIsoString2DateTime(endString), endString.length()));
            }

//            if (xbTimePeriod.getEndPosition().getIndeterminatePosition() != null) {
//                tp.setEndIndet(xbTimePeriod.getEndPosition().getIndeterminatePosition().toString());
//            }
        }

        // else no endPosition -> throw exception!!
        else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                    GetObservationParams.eventTime.toString(),
                    "The end time is missed in the timePeriod element of time parameter!");
            throw se;
        }

        if (xbTimePeriod.getDuration() != null) {
            // TODO: JODA TIME
            tp.setDuration(SosDateTimeUtilities.parseDuration(xbTimePeriod.getDuration().toString()));
        }

        return tp;
    }

    /**
     * creates SOS representation of time period from XMLBeans representation of
     * time period
     *
     * @param xb_timePeriod
     *            XMLBeans representation of time period
     * @return Returns SOS representation of time period
     * @throws OwsExceptionReport
     */
    public static TimePeriod parseTimePeriod(TimePeriodType xbTimePeriod) throws OwsExceptionReport {

        // begin position
        TimePositionType xbBeginTPT = xbTimePeriod.getBegin().getTimeInstant().getTimePosition();
        DateTime begin = null;
        if (xbBeginTPT != null) {
            String beginString = xbBeginTPT.getStringValue();
            try {
                begin = SosDateTimeUtilities.parseIsoString2DateTime(beginString);
            } catch (Exception e) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(
                        OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                        null,
                        "Error while parsing timestring '" + beginString + "' from TimePeriod parameter:"
                                + e.getMessage());
                LOGGER.error(se.getMessage());
                throw se;
            }
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, null,
                    "gml:TimePeriod! must contain beginPos Element with valid ISO:8601 String!!");
            LOGGER.error(se.getMessage());
            throw se;
        }

        // end position
        DateTime end = null;
        TimePositionType xbEndTPT = xbTimePeriod.getEnd().getTimeInstant().getTimePosition();
        if (xbEndTPT != null) {
            String endString = xbEndTPT.getStringValue();
            try {
                end =
                        SosDateTimeUtilities.setDateTime2EndOfDay4RequestedEndPosition(
                                SosDateTimeUtilities.parseIsoString2DateTime(endString), endString.length());
            } catch (Exception e) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(
                        OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                        null,
                        "Error while parsing timestring '" + endString + "' from TimePeriod parameter:"
                                + e.getMessage());
                LOGGER.error(se.getMessage());
                throw se;
            }
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, null,
                    "gml:TimePeriod! must contain endPos Element with valid ISO:8601 String!!");
            LOGGER.error(se.getMessage());
            throw se;
        }

        return new TimePeriod(begin, end);
    }

}
