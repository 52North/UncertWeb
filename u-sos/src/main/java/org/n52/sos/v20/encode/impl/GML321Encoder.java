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

 Author: Christoph Stasch, Stephan Kuenster
 Created: <CREATION DATE>
 Modified: 08/11/2008
 ***************************************************************/

package org.n52.sos.v20.encode.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.AbstractRingPropertyType;
import net.opengis.gml.x32.AbstractRingType;
import net.opengis.gml.x32.DirectPositionListType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.LineStringType;
import net.opengis.gml.x32.LinearRingType;
import net.opengis.gml.x32.PointType;
import net.opengis.gml.x32.PolygonType;
import net.opengis.gml.x32.TimeInstantPropertyType;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePositionType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.SosConfigurator;
import org.n52.sos.SosDateTimeUtilities;
import org.n52.sos.encode.IGMLEncoder;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.ISosTime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.PolygonExtracter;

/**
 * class encapsulates encoding methods for GML elements as features or
 * geometries
 *
 * @author Christoph Stasch
 * @author Carsten Hollmanns
 *
 */
public class GML321Encoder implements IGMLEncoder {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.n52.sos.encode.IGMLEncoder#createFeature(org.n52.sos.ogc.om.features
     * .SosAbstractFeature)
     */
    @Override
    public XmlObject createFeature(SosAbstractFeature absFeature) throws OwsExceptionReport {
        // This method is not used for SOS 2.0. Features are created in the
        // FeatureEncoderV2.
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.n52.sos.encode.IGMLEncoder#createTime(org.n52.sos.ogc.gml.time.ISosTime
     * )
     */
    @Override
    public XmlObject createTime(ISosTime time, String id, XmlObject xbAbsTimeObject) throws OwsExceptionReport {
        // AbstractTimeGeometricPrimitiveType xbTime = null;
        // AbstractTimeObjectType xbTime =
        // (AbstractTimeObjectType)xbAbsTimeObject;

        if (time == null) {
            return xbAbsTimeObject;
        }

        else if (time instanceof TimeInstant) {
            TimeInstantType xbTime =
                    (TimeInstantType) xbAbsTimeObject.substitute(new QName(GMLConstants.NS_GML_32,
                            GMLConstants.EN_TIME_INSTANT, GMLConstants.NS_GML_PREFIX), TimeInstantType.type);
            xbTime.set(createTimeInstant((TimeInstant) time));
            // TimeInstantType xbTime = createTimeInstant((TimeInstant) time);
            // xbTime = xbTimeInstant;
            if (id != null && !id.isEmpty()) {
                xbTime.setId(id);
            }
            return xbTime;
        }

        else if (time instanceof TimePeriod) {
            TimePeriodType xbTime =
                    (TimePeriodType) xbAbsTimeObject.substitute(new QName(GMLConstants.NS_GML_32,
                            GMLConstants.EN_TIME_PERIOD, GMLConstants.NS_GML_PREFIX), TimePeriodType.type);
            xbTime.set(createTimePeriod((TimePeriod) time));
            if (id != null && !id.isEmpty()) {
                xbTime.setId(id);
            }
            return xbTime;
            // TimePeriodType xbTimePeriod = createTimePeriod((TimePeriod)
            // time);
            // xbTime = xbTimePeriod;
            // change name of _TimeObject to TimeInstant

        }
        // if(id != null && !id.isEmpty()) {
        // xbTime.setId(id);
        // }

        return xbAbsTimeObject;
    }

    /**
     * Creates a XML TimePeriod from the SOS time object.
     *
     * @param timePeriod
     *            SOS time object
     * @return XML TimePeriod
     * @throws OwsExceptionReport
     *             if an error occurs.
     */
    private TimePeriodType createTimePeriod(TimePeriod timePeriod) throws OwsExceptionReport {
        TimePeriodType xbTimePeriod = TimePeriodType.Factory.newInstance();

        if (timePeriod != null) {
            // beginPosition
        	TimeInstantPropertyType xbTimeInstantPropertyBegin = TimeInstantPropertyType.Factory.newInstance();
            TimeInstantType xbTimeInstantBegin = TimeInstantType.Factory.newInstance();
            TimePositionType xbTimePositionBegin = TimePositionType.Factory.newInstance();
            String beginString = SosDateTimeUtilities.formatDateTime2ResponseString(timePeriod.getStart());

            // concat minutes for timeZone offset, because gml requires
            // xs:dateTime, which needs minutes in
            // timezone offset
            // TODO enable really
            xbTimePositionBegin.setStringValue(beginString);

            xbTimeInstantBegin.setTimePosition(xbTimePositionBegin);
            xbTimeInstantPropertyBegin.setTimeInstant(xbTimeInstantBegin);

            // endPosition
            TimeInstantPropertyType xbTimeInstantPropertyEnd = TimeInstantPropertyType.Factory.newInstance();
            TimeInstantType xbTimeInstantEnd = TimeInstantType.Factory.newInstance();
            TimePositionType xbTimePositionEnd = TimePositionType.Factory.newInstance();
            String endString = SosDateTimeUtilities.formatDateTime2ResponseString(timePeriod.getEnd());

            // concat minutes for timeZone offset, because gml requires
            // xs:dateTime, which needs minutes in
            // timezone offset
            // TODO enable really
            xbTimePositionEnd.setStringValue(endString);

            xbTimeInstantEnd.setTimePosition(xbTimePositionEnd);
            xbTimeInstantPropertyEnd.setTimeInstant(xbTimeInstantEnd);


            xbTimePeriod.setBegin(xbTimeInstantPropertyBegin);
            xbTimePeriod.setEnd(xbTimeInstantPropertyEnd);
        }

        return xbTimePeriod;
    }

    /**
     * Creates a XML TimeInstant from the SOS time object.
     *
     * @param timeInstant
     *            SOS time object
     * @return XML TimeInstant
     * @throws OwsExceptionReport
     *             if an error occurs.
     */
    private TimeInstantType createTimeInstant(TimeInstant timeInstant) throws OwsExceptionReport {
        // create time instant
        TimeInstantType xbTimeInstant = TimeInstantType.Factory.newInstance();
        TimePositionType xb_posType = xbTimeInstant.addNewTimePosition();

        // parse db date string and format into GML format
        DateTime date = timeInstant.getValue();
        String timeString = SosDateTimeUtilities.formatDateTime2ResponseString(date);

        // concat minutes for timeZone offset, because gml requires xs:dateTime,
        // which needs minutes in
        // timezone offset
        // TODO enable really
        xb_posType.setStringValue(timeString);

        return xbTimeInstant;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.n52.sos.encode.IGMLEncoder#createPosition(com.vividsolutions.jts.
     * geom.Geometry, org.apache.xmlbeans.XmlObject)
     */
    @Override
    public void createPosition(Geometry geom, XmlObject xbAbsGeomType, String foiId) {

        if (geom instanceof Point) {
            PointType xbPoint =
                    (PointType) xbAbsGeomType.substitute(new QName(OMConstants.NS_GML_32, "Point",
                            OMConstants.NS_GML_PREFIX), PointType.type);
            xbPoint.setId("point_" + foiId);
            createPointFromJtsGeometry((Point) geom, xbPoint);
        }

        else if (geom instanceof LineString) {
            LineStringType xbLineString =
                    (LineStringType) xbAbsGeomType.substitute(new QName(OMConstants.NS_GML_32, "LineString",
                            OMConstants.NS_GML_PREFIX), LineStringType.type);
            xbLineString.setId("lineString_" + foiId);
            createLineStringFromJtsGeometry((LineString) geom, xbLineString);
        }

        else if (geom instanceof Polygon) {
            PolygonType xbPolygon =
                    (PolygonType) xbAbsGeomType.substitute(new QName(OMConstants.NS_GML_32, "Polygon",
                            OMConstants.NS_GML_PREFIX), PolygonType.type);
            xbPolygon.setId("polygon_" + foiId);
            createPolygonFromJtsGeometry((Polygon) geom, xbPolygon);
        }
    }

    /**
     * Creates a XML Point from a SOS Point.
     *
     * @param jtsPoint
     *            SOS Point
     * @param xbPoint
     *            XML Point
     */
    private void createPointFromJtsGeometry(Point jtsPoint, PointType xbPoint) {
        DirectPositionType xbPos = xbPoint.addNewPos();
        xbPos.setSrsName(SosConfigurator.getInstance().getSrsNamePrefixSosV2() + jtsPoint.getSRID());
        String coords;
        if (SosConfigurator.getInstance().switchCoordinatesForEPSG(jtsPoint.getSRID())) {
            coords = switchCoordinates4String(jtsPoint);
        } else {
            coords = getCoordinates4String(jtsPoint);
        }
        xbPos.setStringValue(coords);
    }

    /**
     * Creates a XML LineString from a SOS LineString.
     *
     * @param jtsLineString
     *            SOS LineString
     * @param xbLst
     *            XML LinetSring
     */
    private void createLineStringFromJtsGeometry(LineString jtsLineString, LineStringType xbLst) {
        xbLst.setSrsName(SosConfigurator.getInstance().getSrsNamePrefixSosV2()
                + Integer.toString(jtsLineString.getSRID()));


        if (SosConfigurator.getInstance().switchCoordinatesForEPSG(jtsLineString.getSRID())) {
        	xbLst.setPosArray(switchCoordinates4Array(jtsLineString));
        } else {
        	xbLst.setPosArray(getCoordinates4Array(jtsLineString));
        }

//        Coordinate[] crds = jtsLineString.getCoordinates();
//
//        for (Coordinate crd : crds) {
//        	DirectPositionType xbDirPos = xbLst.addNewPos();
//
//        	if (SosConfigurator.getInstance().switchCoordinatesForEPSG(jtsLineString.getSRID())) {
//
//        		if (Double.isNaN(crd.z)) {
//        			xbDirPos.setStringValue(crd.y + " " + crd.x);
//                } else {
//                	xbDirPos.setStringValue(crd.y + " " + crd.x + " " + crd.z);
//                }
//        	} else {
//        		if (Double.isNaN(crd.z)) {
//        			xbDirPos.setStringValue(crd.x + " " + crd.y);
//                } else {
//                	xbDirPos.setStringValue(crd.x + " " + crd.y + " " + crd.z);
//                }
//        	}
//        }
    }

    /**
     * Creates a XML Polygon from a SOS Polygon.
     *
     * @param jtsPolygon
     *            SOS Polygon
     * @param xbPolType
     *            XML Polygon
     */
    private void createPolygonFromJtsGeometry(Polygon jtsPolygon, PolygonType xbPolType) {
        List<?> jtsPolygons = PolygonExtracter.getPolygons(jtsPolygon);
        for (int i = 0; i < jtsPolygons.size(); i++) {

            Polygon pol = (Polygon) jtsPolygons.get(i);

            AbstractRingPropertyType xbArpt = xbPolType.addNewExterior();
            AbstractRingType xbArt = xbArpt.addNewAbstractRing();

            // SrsName is no longer set for single xbLrt, but for the polygon
            xbPolType.setSrsName(SosConfigurator.getInstance().getSrsNamePrefixSosV2() + jtsPolygon.getSRID());

            LinearRingType xbLrt = LinearRingType.Factory.newInstance();

            // Exterior ring
            LineString ring = pol.getExteriorRing();

            // switch coordinates
            if (SosConfigurator.getInstance().switchCoordinatesForEPSG(jtsPolygon.getSRID())) {
            	xbLrt.setPosArray(switchCoordinates4Array(ring));
            } else {
            	xbLrt.setPosArray(getCoordinates4Array(ring));
            }
            xbArt.set(xbLrt);

            // Rename element name for output
            XmlCursor cursor2 = xbArpt.newCursor();
            boolean hasChild2 = cursor2.toChild(new QName(OMConstants.NS_GML_32, OMConstants.EN_ABSTRACT_RING));
            if (hasChild2) {
                cursor2.setName(new QName(OMConstants.NS_GML_32, OMConstants.EN_LINEAR_RING));
            }

            // Interior ring
            int numberOfInteriorRings = pol.getNumInteriorRing();
            for (int ringNumber = 0; ringNumber < numberOfInteriorRings; ringNumber++) {
                xbArpt = xbPolType.addNewInterior();
                xbArt = xbArpt.addNewAbstractRing();

                xbLrt = LinearRingType.Factory.newInstance();

                ring = pol.getInteriorRingN(ringNumber);

                // SrsName is no longer set for single xbLrt

                // switch coordinates
                if (SosConfigurator.getInstance().switchCoordinatesForEPSG(jtsPolygon.getSRID())) {
                	xbLrt.setPosArray(switchCoordinates4Array(ring));
                } else {
                	xbLrt.setPosArray(getCoordinates4Array(ring));
                }
                xbArt.set(xbLrt);

                // Rename element name for output
                cursor2 = xbArpt.newCursor();
                hasChild2 = cursor2.toChild(new QName(OMConstants.NS_GML_32, OMConstants.EN_ABSTRACT_RING));
                if (hasChild2) {
                    cursor2.setName(new QName(OMConstants.NS_GML_32, OMConstants.EN_LINEAR_RING));
                }
            }
        }
    }

    /**
     * Builds a String from jts_Geometry coordinates.
     *
     * @param sourceGeom
     *            jts_Geometry to get the coordinates.
     * @return String with coordinates.
     */
    protected String getCoordinates4String(Geometry sourceGeom) {
        StringBuffer stringCoords = new StringBuffer();
        Coordinate[] sourceCoords = sourceGeom.getCoordinates();

        for (Coordinate coordinate : sourceCoords) {
            stringCoords.append(coordinate.x);
            stringCoords.append(" ");
            stringCoords.append(coordinate.y);
            if (Double.isNaN(coordinate.z)) {
                stringCoords.append(" ");
            } else {
                stringCoords.append(" ");
                stringCoords.append(coordinate.z + " ");
            }
        }

        if (stringCoords.toString().endsWith(" ")) {
            stringCoords.delete(stringCoords.toString().length() - 1, stringCoords.toString().length());
        }
        return stringCoords.toString();
    }

    /**
     * Builds a String from jts_Geometry coordinates and switches the xy.
     *
     * @param sourceGeom
     *            jts_Geometry to get the coordinates.
     * @return String with coordinates.
     */
    protected String switchCoordinates4String(Geometry sourceGeom) {
        StringBuffer switchedCoords = new StringBuffer();
        Coordinate[] sourceCoords = sourceGeom.getCoordinates();

        for (Coordinate coordinate : sourceCoords) {
            switchedCoords.append(coordinate.y);
            switchedCoords.append(" ");
            switchedCoords.append(coordinate.x);
            if (Double.isNaN(coordinate.z)) {
                switchedCoords.append(" ");
            } else {
                switchedCoords.append(" ");
                switchedCoords.append(coordinate.z + " ");
            }
        }

        if (switchedCoords.toString().endsWith(" ")) {
            switchedCoords.delete(switchedCoords.toString().length() - 1, switchedCoords.toString().length());
        }
        return switchedCoords.toString();
    }

    /**
     * Builds an array of position coordinates.
     *
     * @param sourceGeom
     *            jts_Geometry to get the coordinates.
     * @return positions with coordinates.
     */
    protected DirectPositionType[] getCoordinates4Array(Geometry sourceGeom) {

    	Coordinate[] crds = sourceGeom.getCoordinates();
        ArrayList<DirectPositionType> posList = new ArrayList<DirectPositionType>(sourceGeom.getCoordinates().length);

        for (Coordinate crd : crds) {

        	DirectPositionType xbDirPos = DirectPositionType.Factory.newInstance();

    		if (Double.isNaN(crd.z)) {
    			xbDirPos.setStringValue(crd.x + " " + crd.y);
            } else {
            	xbDirPos.setStringValue(crd.x + " " + crd.y + " " + crd.z);
            }
        }

    	return posList.toArray(new DirectPositionType[0]);
    }

    /**
     * Builds an array of positions and switches the xy.
     *
     * @param sourceGeom
     *            jts_Geometry to get the coordinates.
     * @return positions with coordinates.
     */
    protected DirectPositionType[] switchCoordinates4Array(Geometry sourceGeom) {

    	Coordinate[] crds = sourceGeom.getCoordinates();
    	ArrayList<DirectPositionType> posList = new ArrayList<DirectPositionType>(sourceGeom.getCoordinates().length);

        for (Coordinate crd : crds) {

        	DirectPositionType xbDirPos = DirectPositionType.Factory.newInstance();

    		if (Double.isNaN(crd.z)) {
    			xbDirPos.setStringValue(crd.y + " " + crd.x);
            } else {
            	xbDirPos.setStringValue(crd.y + " " + crd.x + " " + crd.z);
            }
        }

    	return posList.toArray(new DirectPositionType[0]);
    }

    @Override
    public XmlObject createTimePeriodDocument(TimePeriod timePeriod) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }
}
