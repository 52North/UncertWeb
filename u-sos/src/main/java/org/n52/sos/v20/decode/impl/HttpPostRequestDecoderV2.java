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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.opengis.fes.x20.BBOXType;
import net.opengis.fes.x20.BinaryTemporalOpType;
import net.opengis.fes.x20.SpatialOpsType;
import net.opengis.fes.x20.TemporalOpsType;
import net.opengis.fes.x20.ValueReferenceDocument;
import net.opengis.sos.x20.GetCapabilitiesDocument;
import net.opengis.sos.x20.GetCapabilitiesType;
import net.opengis.sos.x20.GetFeatureOfInterestDocument;
import net.opengis.sos.x20.GetFeatureOfInterestType;
import net.opengis.sos.x20.GetObservationDocument;
import net.opengis.sos.x20.GetObservationType;
import net.opengis.swes.x20.DescribeSensorDocument;
import net.opengis.swes.x20.DescribeSensorType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.sos.Sos1Constants.GetObservationParams;
import org.n52.sos.SosConstants;
import org.n52.sos.decode.IHttpPostRequestDecoder;
import org.n52.sos.ogc.filter.FilterConstants;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator2;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.time.ISosTime;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sos.request.AbstractSosRequest;
import org.n52.sos.request.SosDescribeSensorRequest;
import org.n52.sos.request.SosGetCapabilitiesRequest;
import org.n52.sos.request.SosGetFeatureOfInterestRequest;
import org.n52.sos.request.SosGetObservationRequest;

/**
 * class offers parsing method to create a SOSOperationRequest, which
 * encapsulates the request parameters, from a GetOperationDocument (XmlBeans
 * generated Java class representing the request) The different
 * SosOperationRequest classes are useful, because XmlBeans generates no useful
 * documentation and handling of substitution groups is not simple. So it may be
 * easier for foreign developers to implement the DAO implementation classes for
 * other data sources then PGSQL databases.
 * 
 * !! Use for each operation a new parser method. Use 'parse' + operation name +
 * 'Request', e.g. parseGetCapabilitiesRequest. In GetCapabilities the SOS can
 * check for GET and POST implementations. !!!
 * 
 * @author Christoph Stasch
 * @author Carsten Hollmann
 * 
 */
public class HttpPostRequestDecoderV2 implements IHttpPostRequestDecoder {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static Logger LOGGER = Logger.getLogger(HttpPostRequestDecoderV2.class);

    /**
     * method receives request and returns internal SOS representation of
     * request
     * 
     * @param docString
     *            string, which contains the request document
     * @return Returns internal SOS representation of request
     * @throws OwsExceptionReport
     *             if parsing of request fails
     */
    public AbstractSosRequest receiveRequest(XmlObject doc) throws OwsExceptionReport {
        AbstractSosRequest response = null;

        LOGGER.info("REQUESTTYPE:" + doc.getClass());

        // getCapabilities request
        if (doc instanceof GetCapabilitiesDocument) {
            GetCapabilitiesDocument capsDoc = (GetCapabilitiesDocument) doc;
            response = parseGetCapabilitiesRequest(capsDoc);
        }

        // getObservation request
        else if (doc instanceof GetObservationDocument) {
            GetObservationDocument obsDoc = (GetObservationDocument) doc;
            response = parseGetObservationRequest(obsDoc);
        }

        // describeSensor request
        else if (doc instanceof DescribeSensorDocument) {
            DescribeSensorDocument obsDoc = (DescribeSensorDocument) doc;
            response = parseDescribeSensorRequest(obsDoc);
        }

        // getFeatureOfInterest request
        else if (doc instanceof GetFeatureOfInterestDocument) {
            GetFeatureOfInterestDocument obsDoc = (GetFeatureOfInterestDocument) doc;
            response = parseGetFeatureOfInterestRequest(obsDoc);
        }

        return response;
    }

    /**
     * parses the XmlBean representing the getCapabilities request and creates a
     * SosGetCapabilities request
     * 
     * @param xbCapsDoc
     *            XmlBean created from the incoming request stream
     * @return Returns SosGetCapabilitiesRequest representing the request
     * @throws OwsExceptionReport
     *             If parsing the XmlBean failed
     */
    private AbstractSosRequest parseGetCapabilitiesRequest(GetCapabilitiesDocument xbCapsDoc)
            throws OwsExceptionReport {
        SosGetCapabilitiesRequest request = new SosGetCapabilitiesRequest();

        // validate document
        validateDocument(xbCapsDoc);

        GetCapabilitiesType xbGetCaps = xbCapsDoc.getGetCapabilities2();

        if (xbGetCaps.getService() != null && !xbGetCaps.getService().isEmpty()) {
            request.setService(xbGetCaps.getService());
        }

        request.setMobileEnabled(false);

        if (xbGetCaps.getAcceptFormats() != null && xbGetCaps.getAcceptFormats().sizeOfOutputFormatArray() != 0) {
            request.setAcceptFormats(xbGetCaps.getAcceptFormats().getOutputFormatArray());
        }

        if (xbGetCaps.getAcceptVersions() != null && xbGetCaps.getAcceptVersions().sizeOfVersionArray() != 0) {
            request.setAcceptVersions(xbGetCaps.getAcceptVersions().getVersionArray());
        }

        if (xbGetCaps.getSections() != null && xbGetCaps.getSections().getSectionArray().length != 0) {
            request.setSections(xbGetCaps.getSections().getSectionArray());
        }

        if (xbGetCaps.getExtensionArray() != null && xbGetCaps.getExtensionArray().length != 0) {
            request.setExtensionArray(xbGetCaps.getExtensionArray());
        }

        return request;
    }

    /**
     * parses the XmlBean representing the getObservation request and creates a
     * SoSGetObservation request
     * 
     * @param xbGetObsDoc
     *            XmlBean created from the incoming request stream
     * @return Returns SosGetObservationRequest representing the request
     * @throws OwsExceptionReport
     *             If parsing the XmlBean failed
     */
    private AbstractSosRequest parseGetObservationRequest(GetObservationDocument xbGetObsDoc)
            throws OwsExceptionReport {
        validateDocument(xbGetObsDoc);

        SosGetObservationRequest getObsRequest = new SosGetObservationRequest();
        GetObservationType xbGetObservation = xbGetObsDoc.getGetObservation();
        getObsRequest.setService(xbGetObservation.getService());
        getObsRequest.setVersion(xbGetObservation.getVersion());
        getObsRequest.setOffering(xbGetObservation.getOfferingArray());
        getObsRequest.setObservedProperty(xbGetObservation.getObservedPropertyArray());
        getObsRequest.setProcedure(xbGetObservation.getProcedureArray());
        getObsRequest.setEventTime(parseTemporalFilters(xbGetObservation.getTemporalFilterArray()));
        getObsRequest.setResultSpatialFilter(parseSpatialFilter4GetObs(xbGetObservation.getSpatialFilter()));
        getObsRequest.setFeatureOfInterest(xbGetObservation.getFeatureOfInterestArray());
        getObsRequest.setResponseFormat(xbGetObservation.getResponseFormat());

        return getObsRequest;
    }

    /**
     * parses the passes XmlBeans document and creates a SOS describeSensor
     * request
     * 
     * @param xbDescSenDoc
     *            XmlBeans document representing the describeSensor request
     * @return Returns SOS describeSensor request
     * @throws OwsExceptionReport
     *             if validation of the request failed
     */
    private AbstractSosRequest parseDescribeSensorRequest(DescribeSensorDocument xbDescSenDoc)
            throws OwsExceptionReport {
        // validate document
        validateDocument(xbDescSenDoc);

        SosDescribeSensorRequest descSensorRequest = new SosDescribeSensorRequest();
        DescribeSensorType xbDescSensor = xbDescSenDoc.getDescribeSensor();
        descSensorRequest.setService(xbDescSensor.getService());
        descSensorRequest.setVersion(xbDescSensor.getVersion());
        descSensorRequest.setProcedures(xbDescSensor.getProcedure());
        if (xbDescSensor.getProcedureDescriptionFormat() != null
                && !xbDescSensor.getProcedureDescriptionFormat().equals("")) {
            descSensorRequest.setOutputFormat(xbDescSensor.getProcedureDescriptionFormat());
        } else {
            descSensorRequest.setOutputFormat(SosConstants.PARAMETER_NOT_SET);
        }
        return descSensorRequest;
    }

    /**
     * parses the passes XmlBeans document and creates a SOS
     * getFeatureOfInterest request
     * 
     * @param xbGetFoiDoc
     *            XmlBeans document representing the getFeatureOfInterest
     *            request
     * @return Returns SOS getFeatureOfInterest request
     * @throws OwsExceptionReport
     *             if validation of the request failed
     */
    private AbstractSosRequest parseGetFeatureOfInterestRequest(GetFeatureOfInterestDocument xbGetFoiDoc)
            throws OwsExceptionReport {
        // validate document
        validateDocument(xbGetFoiDoc);

        SosGetFeatureOfInterestRequest getFoiRequest = new SosGetFeatureOfInterestRequest();
        GetFeatureOfInterestType xbGetFoi = xbGetFoiDoc.getGetFeatureOfInterest();
        getFoiRequest.setService(xbGetFoi.getService());
        getFoiRequest.setVersion(xbGetFoi.getVersion());
        getFoiRequest.setFeatureIDs(xbGetFoi.getFeatureOfInterestArray());
        getFoiRequest.setObservableProperties(xbGetFoi.getObservablePropertyArray());
        getFoiRequest.setProcedures(xbGetFoi.getProcedureArray());
        getFoiRequest.setSpatialFilters(parseSpatialFilters4GetFoi(xbGetFoi.getSpatialFilterArray()));

        return getFoiRequest;
    }

    /**
     * Parses the spatial filter of a request.
     * 
     * @param xbSpatialOpsType
     *            XmlBean representing the feature of interest parameter of the
     *            request
     * @return Returns SpatialFilter created from the passed foi request
     *         parameter
     * @throws OwsExceptionReport
     *             if creation of the SpatialFilter failed
     */
    private SpatialFilter parseSpatialFilterType(SpatialOpsType xbSpatialOpsType) throws OwsExceptionReport {
        SpatialFilter spatialFilter = new SpatialFilter();
        try {
            if (xbSpatialOpsType instanceof BBOXType) {
                spatialFilter.setOperator(FilterConstants.SpatialOperator.BBOX);
                BBOXType xbBBOX = (BBOXType) xbSpatialOpsType;
                if (xbBBOX.getExpression().getDomNode().getLocalName().equals(OMConstants.EN_VALUE_REFERENCE)) {
                    ValueReferenceDocument valueRefernece =
                            ValueReferenceDocument.Factory.parse(((BBOXType) xbSpatialOpsType).getExpression()
                                    .getDomNode());
                    spatialFilter.setValueReference(valueRefernece.getValueReference().trim());
                }
                spatialFilter.setGeometry(GML321Decoder.parseSpatialFilterOperand(xbSpatialOpsType));
            } else {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                        GetObservationParams.eventTime.toString(),
                        "The requested spatial filter is not supported by this SOS!");
                LOGGER.error("The requested spatial filter is not supported by this SOS!", se);
                throw se;
            }
        } catch (XmlException xmle) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, "Time",
                    "Error while parsing referenceValue: " + xmle.getMessage());
            LOGGER.error(se.getMessage() + xmle.getMessage());
            throw se;
        }
        return spatialFilter;
    }

    /**
     * Parses the spatial filter of a GetObservation request.
     * 
     * @param xbFilter
     *            XmlBean representing the spatial filter parameter of the
     *            request
     * @return Returns SpatialFilter created from the passed foi request
     *         parameter
     * @throws OwsExceptionReport
     *             if creation of the SpatialFilter failed
     */
    private SpatialFilter parseSpatialFilter4GetObs(net.opengis.sos.x20.GetObservationType.SpatialFilter xbFilter)
            throws OwsExceptionReport {
        if (xbFilter != null && xbFilter.getSpatialOps() != null) {
            return parseSpatialFilterType(xbFilter.getSpatialOps());
        }
        return null;
    }

    /**
     * Parses the spatial filters of a GetFeatureOfInterest request.
     * 
     * @param xbFilters
     *            XmlBean representing the spatial filter parameter of the
     *            request
     * @return Returns SpatialFilter created from the passed foi request
     *         parameter
     * @throws OwsExceptionReport
     *             if creation of the SpatialFilter failed
     */
    private SpatialFilter[] parseSpatialFilters4GetFoi(
            net.opengis.sos.x20.GetFeatureOfInterestType.SpatialFilter[] xbFilters) throws OwsExceptionReport {
        List<SpatialFilter> spatialFilters = new ArrayList<SpatialFilter>(xbFilters.length);
        for (net.opengis.sos.x20.GetFeatureOfInterestType.SpatialFilter filter : xbFilters) {
            spatialFilters.add(parseSpatialFilterType(filter.getSpatialOps()));
        }
        return spatialFilters.toArray(new SpatialFilter[0]);
    }

    /**
     * parses the Time of the requests and returns an array representing the
     * temporal filters
     * 
     * @param xbTemporalFilters
     *            array of XmlObjects representing the Time element in the
     *            request
     * @return Returns array representing the temporal filters
     * @throws OwsExceptionReport
     *             if parsing of the element failed
     */
    private TemporalFilter[] parseTemporalFilters(
            net.opengis.sos.x20.GetObservationType.TemporalFilter[] xbTemporalFilters) throws OwsExceptionReport {
        List<TemporalFilter> temporalFilters = new ArrayList<TemporalFilter>();
        for (net.opengis.sos.x20.GetObservationType.TemporalFilter xbTemporalFilter : xbTemporalFilters) {
            temporalFilters.add(parseTemporalFilterType(xbTemporalFilter.getTemporalOps()));
        }
        return temporalFilters.toArray(new TemporalFilter[0]);
    }

    /**
     * parses a single temporal filter of the requests and returns SOS temporal
     * filter
     * 
     * @param xbTemporalOpsType
     *            XmlObject representing the temporal filter
     * @return Returns SOS representation of temporal filter
     * @throws OwsExceptionReport
     *             if parsing of the element failed
     */
    private TemporalFilter parseTemporalFilterType(TemporalOpsType xbTemporalOpsType) throws OwsExceptionReport {
        TemporalFilter temporalFilter = null;

        if (xbTemporalOpsType instanceof BinaryTemporalOpType) {
            String valueReference = null;
            valueReference = ((BinaryTemporalOpType) xbTemporalOpsType).getValueReference();
            ISosTime time = GML321Decoder.parseTime(xbTemporalOpsType);
            String localName = xbTemporalOpsType.getDomNode().getLocalName();
            if (localName.equals(TimeOperator2.During.name())) {
                temporalFilter = new TemporalFilter(TimeOperator.TM_During, time, valueReference);
            } else if (localName.equals(TimeOperator2.TEquals.name())) {
                temporalFilter = new TemporalFilter(TimeOperator.TM_Equals, time, valueReference);
            } else {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                        GetObservationParams.eventTime.toString(),
                        "The requested temporal filter is not supported by this SOS!");
                LOGGER.error("The requested temporal filter is not supported by this SOS!", se);
                throw se;
            }
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                    GetObservationParams.eventTime.toString(), "The requested filter is not a temporal filter!");
            LOGGER.error("The requested filter is not a temporal filter!", se);
            throw se;
        }
        return temporalFilter;
    }

    /**
     * checks whether the getObservationRequest XMLDocument is valid
     * 
     * @param getObsDoc
     *            the document which should be checked
     * 
     * @throws OwsExceptionReport
     *             if the Document is not valid
     */
    private void validateDocument(XmlObject xb_doc) throws OwsExceptionReport {

        // Create an XmlOptions instance and set the error listener.
        ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
        XmlOptions validationOptions = new XmlOptions();
        validationOptions.setErrorListener(validationErrors);

        // Validate the GetCapabilitiesRequest XML document
        boolean isValid = xb_doc.validate(validationOptions);

        // Create Exception with error message if the xml document is invalid
        if (!isValid) {

            String message = null;
            String parameterName = null;

            // getValidation error and throw service exception for the first
            // error
            Iterator<XmlError> iter = validationErrors.iterator();
            while (iter.hasNext()) {

                // ExceptionCode for Exception
                ExceptionCode exCode = null;

                // get name of the missing or invalid parameter
                XmlError error = iter.next();
                message = error.getMessage();
                if (message != null) {

                    // check, if parameter is missing or value of parameter is
                    // invalid to ensure, that correct
                    // exceptioncode in exception response is used

                    // invalid parameter value
                    if (message.startsWith("The value")) {
                        exCode = ExceptionCode.InvalidParameterValue;

                        // split message string to get attribute name
                        String[] messAndAttribute = message.split("attribute '");
                        if (messAndAttribute.length == 2) {
                            parameterName = messAndAttribute[1].replace("'", "");
                        }
                    }

                    // invalid enumeration value --> InvalidParameterValue
                    else if (message.contains("not a valid enumeration value")) {
                        exCode = ExceptionCode.InvalidParameterValue;

                        // get attribute name
                        String[] messAndAttribute = message.split(" ");
                        parameterName = messAndAttribute[10];
                    }

                    // mandatory attribute is missing --> missingParameterValue
                    else if (message.startsWith("Expected attribute")) {
                        exCode = ExceptionCode.MissingParameterValue;

                        // get attribute name
                        String[] messAndAttribute = message.split("attribute: ");
                        if (messAndAttribute.length == 2) {
                            String[] attrAndRest = messAndAttribute[1].split(" in");
                            if (attrAndRest.length == 2) {
                                parameterName = attrAndRest[0];
                            }
                        }
                    }

                    // mandatory element is missing --> missingParameterValue
                    else if (message.startsWith("Expected element")) {
                        exCode = ExceptionCode.MissingParameterValue;

                        // get element name
                        String[] messAndElements = message.split(" '");
                        if (messAndElements.length >= 2) {
                            String elements = messAndElements[1];
                            if (elements.contains("offering")) {
                                parameterName = "offering";
                            } else if (elements.contains("observedProperty")) {
                                parameterName = "observedProperty";
                            } else if (elements.contains("responseFormat")) {
                                parameterName = "responseFormat";
                            } else if (elements.contains("procedure")) {
                                parameterName = "procedure";
                            } else if (elements.contains("featureOfInterest")) {
                                parameterName = "featureOfInterest";
                            } else {
                                // TODO check if other elements are invalid
                            }
                        }
                    }
                    // invalidParameterValue
                    else if (message.startsWith("Element")) {
                        exCode = ExceptionCode.InvalidParameterValue;

                        // get element name
                        String[] messAndElements = message.split(" '");
                        if (messAndElements.length >= 2) {
                            String elements = messAndElements[1];
                            if (elements.contains("offering")) {
                                parameterName = "offering";
                            } else if (elements.contains("observedProperty")) {
                                parameterName = "observedProperty";
                            } else if (elements.contains("responseFormat")) {
                                parameterName = "responseFormat";
                            } else if (elements.contains("procedure")) {
                                parameterName = "procedure";
                            } else if (elements.contains("featureOfInterest")) {
                                parameterName = "featureOfInterest";
                            } else {
                                // TODO check if other elements are invalid
                            }
                        }
                    } else {
                        // create service exception
                        OwsExceptionReport se = new OwsExceptionReport();
                        se.addCodedException(ExceptionCode.InvalidRequest, null, "[XmlBeans validation error:] "
                                + message);
                        LOGGER.error("The request is invalid!", se);
                        throw se;
                    }

                    // create service exception
                    OwsExceptionReport se = new OwsExceptionReport();
                    se.addCodedException(exCode, parameterName, "[XmlBeans validation error:] " + message);
                    LOGGER.error("The request is invalid!", se);
                    throw se;
                }

            }

        }
    }

}
