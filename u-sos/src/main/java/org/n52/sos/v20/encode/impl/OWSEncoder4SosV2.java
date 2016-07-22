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

package org.n52.sos.v20.encode.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.fes.x20.ComparisonOperatorsType;
import net.opengis.fes.x20.ConformanceType;
import net.opengis.fes.x20.GeometryOperandsType;
import net.opengis.fes.x20.IdCapabilitiesType;
import net.opengis.fes.x20.ScalarCapabilitiesType;
import net.opengis.fes.x20.SpatialCapabilitiesType;
import net.opengis.fes.x20.SpatialOperatorType;
import net.opengis.fes.x20.SpatialOperatorsType;
import net.opengis.fes.x20.TemporalCapabilitiesType;
import net.opengis.fes.x20.TemporalOperandsType;
import net.opengis.fes.x20.TemporalOperatorType;
import net.opengis.fes.x20.TemporalOperatorsType;
import net.opengis.fes.x20.impl.ComparisonOperatorNameTypeImpl;
import net.opengis.fes.x20.impl.SpatialOperatorNameTypeImpl;
import net.opengis.fes.x20.impl.TemporalOperatorNameTypeImpl;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePositionType;
import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.ows.x11.DCPDocument.DCP;
import net.opengis.ows.x11.DomainType;
import net.opengis.ows.x11.HTTPDocument.HTTP;
import net.opengis.ows.x11.OperationDocument.Operation;
import net.opengis.ows.x11.OperationsMetadataDocument.OperationsMetadata;
import net.opengis.ows.x11.RangeType;
import net.opengis.ows.x11.ServiceIdentificationDocument;
import net.opengis.ows.x11.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.x11.ServiceProviderDocument;
import net.opengis.ows.x11.ValueType;
import net.opengis.sos.x20.CapabilitiesDocument;
import net.opengis.sos.x20.CapabilitiesType;
import net.opengis.sos.x20.CapabilitiesType.Contents;
import net.opengis.sos.x20.ContentsType;
import net.opengis.sos.x20.ObservationOfferingType;
import net.opengis.sos.x20.ObservationOfferingType.PhenomenonTime;
import net.opengis.swes.x20.AbstractContentsType.Offering;
import net.opengis.swes.x20.AbstractOfferingType.RelatedFeature;
import net.opengis.swes.x20.FeatureRelationshipType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.ISosRequestListener;
import org.n52.sos.Sos1Constants;
import org.n52.sos.Sos2Constants;
import org.n52.sos.SosConfigurator;
import org.n52.sos.SosConstants;
import org.n52.sos.SosDateTimeUtilities;
import org.n52.sos.SosOfferingsForContents;
import org.n52.sos.SosXmlUtilities;
import org.n52.sos.encode.IOWSEncoder;
import org.n52.sos.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sos.ogc.filter.FilterConstants.ConformanceClassConstraintNames;
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSOperationsMetadata;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosCapabilities;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Encoder for OWS reponses (capabilities docuement).
 * 
 * @author Carsten Hollmann
 * 
 */
public class OWSEncoder4SosV2 implements IOWSEncoder {

    /** the logger, used to log exceptions and additonaly information */
    private static Logger LOGGER = Logger.getLogger(OWSEncoder4SosV2.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.encode.IOWSEncoder#createCapabilitiesDocument(org.n52.sos
     * .ogc.ows.SosCapabilities)
     */
    @Override
    public XmlObject createCapabilitiesDocument(SosCapabilities sosCapabilities) throws OwsExceptionReport {
        CapabilitiesDocument xbCapsDoc =
                CapabilitiesDocument.Factory.newInstance(SosXmlUtilities.getInstance().getXmlOptions4Sos2Swe200());
        CapabilitiesType xbCaps = xbCapsDoc.addNewCapabilities();

        // set version.
        xbCaps.setVersion(sosCapabilities.getVersion());

        if (sosCapabilities.getServiceIdentification() != null) {
            setServiceIdentification(sosCapabilities.getServiceIdentification(), xbCaps);
        }
        if (sosCapabilities.getServiceProvider() != null) {
            setServiceProvider(sosCapabilities.getServiceProvider(), xbCaps);
        }
        if (sosCapabilities.getOperationsMetadata() != null) {
            setOperationsMetadata(xbCaps.addNewOperationsMetadata(), sosCapabilities.getOperationsMetadata());
        }
        if (sosCapabilities.getFilterCapabilities() != null) {
            setFilterCapabilities(xbCaps.addNewFilterCapabilities().addNewFilterCapabilities(),
                    sosCapabilities.getFilterCapabilities());
        }
        if (sosCapabilities.getContents() != null) {
            setContents(xbCaps.addNewContents(), sosCapabilities.getContents(), sosCapabilities.getVersion());
        }
        // TODO extensions section
        // if (EXTENSIONS != null) {
        // setExensions(xbCaps.addNewExtension());
        // }

        return xbCapsDoc;
    }

    /**
     * Set the service identification information
     * 
     * @param serviceIdentification
     *            XML object loaded from file.
     * @param xbCaps
     *            XML capabilities document.
     * @throws OwsExceptionReport
     *             if the file is invalid.
     */
    private void setServiceIdentification(XmlObject serviceIdentification, CapabilitiesType xbCaps)
            throws OwsExceptionReport {
        if (serviceIdentification instanceof ServiceIdentificationDocument) {
            ServiceIdentification serviceIdent =
                    ((ServiceIdentificationDocument) serviceIdentification).getServiceIdentification();
            // set service type versions
            List<String> serviceVersions = new ArrayList<String>();
            if (SosConfigurator.getInstance().getSupportedVersions().equals(SosConstants.Versions.BOTH.name())
                    || SosConfigurator.getInstance().getSupportedVersions().equals(SosConstants.Versions.SOS_1.name())) {
                serviceVersions.add(Sos1Constants.SERVICEVERSION);
            }
            if (SosConfigurator.getInstance().getSupportedVersions().equals(SosConstants.Versions.BOTH.name())
                    || SosConfigurator.getInstance().getSupportedVersions().equals(SosConstants.Versions.SOS_2.name())) {
                serviceVersions.add(Sos2Constants.SERVICEVERSION);
            }
            serviceIdent.setServiceTypeVersionArray(serviceVersions.toArray(new String[0]));
            // set Profiles
            serviceIdent.setProfileArray(getProfileArray());
            xbCaps.setServiceIdentification(serviceIdent);
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(
                    OwsExceptionReport.ExceptionCode.NoApplicableCode,
                    SosConstants.CapabilitiesSections.ServiceIdentification.name(),
                    "The service identification file is not a ServiceIdentificationDocument or invalid! Check the file in the Tomcat webapps: /SOS_webapp/WEB-INF/conf/capabilities/.");
            LOGGER.error("The service identification file is not a ServiceIdentificationDocument or invalid!");
            throw se;
        }
    }

    /**
     * Get the SOS 2.0 supported profiles for the service identification
     * section.
     * 
     * @return Array with supported profiles.
     */
    private String[] getProfileArray() {
        List<String> profiles = new ArrayList<String>();
        profiles.add("http://www.opengis.net/spec/OMXML/2.0/conf/samplingPoint");
        profiles.add("http://www.opengis.net/spec/SOS/2.0/conf/soap");
        profiles.add("http://www.opengis.net/spec/OMXML/2.0/req/SWEArrayObservation");
        return profiles.toArray(new String[0]);
    }

    /**
     * Set the service provider information
     * 
     * @param serviceProvider
     *            XML object loaded from file.
     * @param xbCaps
     *            XML capabilities document.
     * @throws OwsExceptionReport
     *             if the file is invalid.
     */
    private void setServiceProvider(XmlObject serviceProvider, CapabilitiesType xbCaps) throws OwsExceptionReport {
        if (serviceProvider instanceof ServiceProviderDocument) {
            xbCaps.setServiceProvider(((ServiceProviderDocument) serviceProvider).getServiceProvider());
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(
                    OwsExceptionReport.ExceptionCode.NoApplicableCode,
                    SosConstants.CapabilitiesSections.ServiceProvider.name(),
                    "The service provider file is not a ServiceProviderDocument or invalid! Check the file in the Tomcat webapps: /SOS_webapp/WEB-INF/conf/capabilities/.");
            LOGGER.error("The service provider file is not a ServiceProviderDocument or invalid!");
            throw se;
        }
    }

    /**
     * Sets the OperationsMetadata section to the capabilities document.
     * 
     * @param xbMeta
     *            OWS operations metadata
     * @param operationsMetadata
     *            SOS metadatas for the operations
     * @throws OwsExceptionReport
     *             if an error occurs
     */
    private void setOperationsMetadata(OperationsMetadata xbMeta, Collection<OWSOperationsMetadata> operationsMetadata)
            throws OwsExceptionReport {

        Map<String, ISosRequestListener> listener =
                SosConfigurator.getInstance().getRequestOperator().getReqListener();
        for (OWSOperationsMetadata owsOperationsMetadata : operationsMetadata) {
            if (listener.containsKey(owsOperationsMetadata.getOperationName())
                    && owsOperationsMetadata.getOperationName().equalsIgnoreCase(
                            Sos2Constants.Operations.GetCapabilities.name())) {
                setOpsGetCapabilities(xbMeta.addNewOperation(), owsOperationsMetadata);
            }
            if (listener.containsKey(owsOperationsMetadata.getOperationName())
                    && owsOperationsMetadata.getOperationName().equalsIgnoreCase(
                            Sos2Constants.Operations.DescribeSensor.name())) {
                setOpsDescribeSensor(xbMeta.addNewOperation(), owsOperationsMetadata);
            }
            if (listener.containsKey(owsOperationsMetadata.getOperationName())
                    && owsOperationsMetadata.getOperationName().equalsIgnoreCase(
                            Sos2Constants.Operations.GetObservation.name())) {
                setOpsGetObservation(xbMeta.addNewOperation(), owsOperationsMetadata);
            }
            if (listener.containsKey(owsOperationsMetadata.getOperationName())
                    && owsOperationsMetadata.getOperationName().equalsIgnoreCase(
                            Sos2Constants.Operations.GetObservationById.name())) {
                setOpsGetObservationById(xbMeta.addNewOperation(), owsOperationsMetadata);
            }
            if (listener.containsKey(owsOperationsMetadata.getOperationName())
                    && owsOperationsMetadata.getOperationName().equalsIgnoreCase(
                            Sos2Constants.Operations.GetFeatureOfInterest.name())) {
                setOpsGetFeatureOfInterest(xbMeta.addNewOperation(), owsOperationsMetadata);
            }
            if (listener.containsKey(owsOperationsMetadata.getOperationName())
                    && owsOperationsMetadata.getOperationName().equalsIgnoreCase(
                            Sos2Constants.Operations.InsertSensor.name())) {
                setOpsInsertSensor(xbMeta.addNewOperation(), owsOperationsMetadata);
            }
            if (listener.containsKey(owsOperationsMetadata.getOperationName())
                    && owsOperationsMetadata.getOperationName().equalsIgnoreCase(
                            Sos2Constants.Operations.DeleteSensor.name())) {
                setOpsDeleteSensor(xbMeta.addNewOperation(), owsOperationsMetadata);
            }
            if (listener.containsKey(owsOperationsMetadata.getOperationName())
                    && owsOperationsMetadata.getOperationName().equalsIgnoreCase(
                            Sos2Constants.Operations.InsertObservation.name())) {
                setOpsInsertObservation(xbMeta.addNewOperation(), owsOperationsMetadata);
            }
            if (listener.containsKey(owsOperationsMetadata.getOperationName())
                    && owsOperationsMetadata.getOperationName().equalsIgnoreCase(
                            Sos2Constants.Operations.InsertResult.name())) {
                setOpsInsertResult(xbMeta.addNewOperation(), owsOperationsMetadata);
            }
            if (listener.containsKey(owsOperationsMetadata.getOperationName())
                    && owsOperationsMetadata.getOperationName().equalsIgnoreCase(
                            Sos2Constants.Operations.InsertResultTemplate.name())) {
                setOpsInsertResultTemplate(xbMeta.addNewOperation(), owsOperationsMetadata);
            }
            if (listener.containsKey(owsOperationsMetadata.getOperationName())
                    && owsOperationsMetadata.getOperationName().equalsIgnoreCase(
                            Sos2Constants.Operations.GetResultTemplate.name())) {
                setOpsGetResultTemplate(xbMeta.addNewOperation(), owsOperationsMetadata);
            }
            if (listener.containsKey(owsOperationsMetadata.getOperationName())
                    && owsOperationsMetadata.getOperationName().equalsIgnoreCase(
                            Sos2Constants.Operations.GetResult.name())) {
                setOpsGetResult(xbMeta.addNewOperation(), owsOperationsMetadata);
            }
        }
        // set SERVICE and VERSION for all operations.
        setParamValue(xbMeta.addNewParameter(), Sos2Constants.GetObservationParams.service.name(), Sos2Constants.SOS);
        setParamValue(xbMeta.addNewParameter(), Sos2Constants.GetObservationParams.version.name(),
                Sos2Constants.SERVICEVERSION);
    }

    /**
     * Sets the filter capabilities section to capabilities
     * 
     * @param xbFilterCapabilities
     *            FES filter capabilities
     * @param sosFilterCaps
     *            SOS filter capabilities
     */
    private void setFilterCapabilities(
            net.opengis.fes.x20.FilterCapabilitiesDocument.FilterCapabilities xbFilterCapabilities,
            org.n52.sos.ogc.filter.FilterCapabilities sosFilterCaps) {
        setConformance(xbFilterCapabilities.addNewConformance());
        if (sosFilterCaps.getComparisonOperators() != null && !sosFilterCaps.getComparisonOperators().isEmpty()) {
            setScalarFilterCapabilities(xbFilterCapabilities.addNewScalarCapabilities(), sosFilterCaps);
        }
        if (sosFilterCaps.getSpatialOperands() != null && !sosFilterCaps.getSpatialOperands().isEmpty()) {
            setSpatialFilterCapabilities(xbFilterCapabilities.addNewSpatialCapabilities(), sosFilterCaps);
        }
        if (sosFilterCaps.getTemporalOperands() != null && !sosFilterCaps.getTemporalOperands().isEmpty()) {
            setTemporalFilterCapabilities(xbFilterCapabilities.addNewTemporalCapabilities(), sosFilterCaps);
        }
        // setIdFilterCapabilities(filterCapabilities.addNewIdCapabilities());

    }

    /**
     * Sets the content section to the Capabilities document.
     * 
     * @param xbContents
     *            SOS 2.0 contents section
     * @param offerings
     *            SOS offerings for contents
     * @param version
     *            SOS response version
     * @throws OwsExceptionReport
     *             if an error occurs.
     */
    private void setContents(Contents xbContents, Collection<SosOfferingsForContents> offerings, String version)
            throws OwsExceptionReport {
        ContentsType xbContType = xbContents.addNewContents();
        for (SosOfferingsForContents offering : offerings) {
            ObservationOfferingType xbObsOff =
                    ObservationOfferingType.Factory.newInstance(SosXmlUtilities.getInstance()
                            .getXmlOptions4Sos2Swe200());
            xbObsOff.setIdentifier(offering.getOfferingId());
            for (String procedure : offering.getProcedures()) {
                xbObsOff.setProcedure(procedure);
            }
            // TODO: procedureDescriptionFormat [0..*]
            // set phenomenons [0..*]
            for (String phenomenon : offering.getPhenomenons()) {
                xbObsOff.addNewObservableProperty().setStringValue(phenomenon);
            }

            // set featureOfInterestType [0..1]

            // set relatedFeatures [0..*]
            if (offering.getRelatedFeatures() != null) {
                    createRelatedFeature(xbObsOff.addNewRelatedFeature(), offering.getRelatedFeatures());
            }

            // set observed area [0..1]
            if (offering.getBoundeBy() != null && offering.getSrid() != -1) {
                xbObsOff.addNewObservedArea()
                        .setEnvelope(getBBOX4Offering(offering.getBoundeBy(), offering.getSrid()));
            }
            // set up phenomenon time [0..1]
            if (offering.getTime() instanceof TimePeriod) {
                TimePeriod tp = (TimePeriod) offering.getTime();
                if (tp.getStart() != null && tp.getEnd() != null) {
                    PhenomenonTime xbPhenTime = xbObsOff.addNewPhenomenonTime();
                    TimePeriodType xbTime = xbPhenTime.addNewTimePeriod();
                    xbTime.setId("pt_" + offering.getOfferingId());
                    
                    TimeInstantType xbTimeInstantBegin = TimeInstantType.Factory.newInstance();
                    TimePositionType xbTimePositionBegin = TimePositionType.Factory.newInstance();
                    xbTimePositionBegin.setStringValue(tp.getStart().toString());
                    xbTimeInstantBegin.setTimePosition(xbTimePositionBegin);
                    xbTime.addNewBegin().setTimeInstant(xbTimeInstantBegin);
                    
                    TimeInstantType xbTimeInstantEnd = TimeInstantType.Factory.newInstance();
                    TimePositionType xbTimePositionEnd = TimePositionType.Factory.newInstance();
                    xbTimePositionEnd.setStringValue(tp.getEnd().toString());
                    xbTimeInstantEnd.setTimePosition(xbTimePositionEnd);
                    xbTime.addNewEnd().setTimeInstant(xbTimeInstantEnd);
                }
            }

            // set responseFormat [0..*]
            for (String responseFormat : offering.getResponseFormats()) {
                xbObsOff.addNewResponseFormat().setStringValue(responseFormat);
            }
            // set observationType [0..*]
            for (String obsType : offering.getObservationTypes()) {
                xbObsOff.addNewObservationType().setStringValue(obsType);
            }

            // set resultTime [0..1]

            xbContType.addNewOffering().setAbstractOffering(xbObsOff);
        }

        // TODO: change swes:AbstractOffering to sos:ObservationOffering due to
        // XMLBeans problems with substitution
        for (Offering offering : xbContents.getContents().getOfferingArray()) {
            XmlCursor cursor = offering.getAbstractOffering().newCursor();
            cursor.setName(new QName(OMConstants.NS_SOS_V2, OWSConstants.EN_OBSERVATION_OFFERING));
            cursor.dispose();
        }
    }

    /**
     * Creates a XML FeatureRelationship for the relatedFeature
     * @param relatedFeature XML feature relationship
     * @param map Feature id
     * @param role Features role
     */
    private void createRelatedFeature(RelatedFeature relatedFeature, Map<String, List<String>> map) {
        for (String relatedFeatureID : map.keySet()) {
            FeatureRelationshipType featureRelationchip = relatedFeature.addNewFeatureRelationship();
            featureRelationchip.addNewTarget().setHref(relatedFeatureID);
            for (String role : map.get(relatedFeatureID)) {
                featureRelationchip.setRole(role);
            }
        }
    }

    /**
     * Set the SOS 2.0 extensions section.
     * 
     * @param xbExtensions
     *            Extension
     */
    private void setExensions(XmlObject xbExtensions) {
        // currently not supported by this SOS
    }

    /**
     * Sets the GetObservation operation and parameters to OperationsMetadata.
     * 
     * @param operation
     *            GetObservation operation.
     * @param owsOperationsMetadata
     *            SOS GetCapabilities metadata
     * @throws OwsExceptionReport
     */
    private void setOpsGetCapabilities(Operation operation, OWSOperationsMetadata owsOperationsMetadata) {
        // set operation name
        operation.setName(refactorOpsName(Sos2Constants.Operations.GetCapabilities.name()));
        // set DCP
        setDCP(operation.addNewDCP(), owsOperationsMetadata.getDcp());
        // set param updateSequence
        DomainType updateSequence = operation.addNewParameter();
        updateSequence.setName(SosConstants.GetCapabilitiesParams.updateSequence.name());
        updateSequence.addNewAnyValue();
        // set param AcceptVersions
        // DomainType acceptVersions = operation.addNewParameter();
        // acceptVersions.setName(SosConstants.GetCapabilitiesParams.AcceptVersions.name());
        setParamList(
                operation.addNewParameter(),
                SosConstants.GetCapabilitiesParams.AcceptVersions.name(),
                owsOperationsMetadata.getParameterValues().get(
                        SosConstants.GetCapabilitiesParams.AcceptVersions.name()));
        setParamList(operation.addNewParameter(), SosConstants.GetCapabilitiesParams.Sections.name(),
                owsOperationsMetadata.getParameterValues().get(SosConstants.GetCapabilitiesParams.Sections.name()));
        // set param AcceptFormats
        setParamList(operation.addNewParameter(), SosConstants.GetCapabilitiesParams.AcceptFormats.name(),
                Arrays.asList(SosConstants.getAcceptFormats()));

    }

    /**
     * Sets the DescribeSensor operation and parameters to OperationsMetadata.
     * 
     * @param operation
     *            DescribeSensor operation.
     * @param owsOperationsMetadata
     *            SOS DescribeSensor metadata
     * @throws OwsExceptionReport
     */
    private void setOpsDescribeSensor(Operation operation, OWSOperationsMetadata owsOperationsMetadata)
            throws OwsExceptionReport {
        // set operation name
        operation.setName(refactorOpsName(Sos2Constants.Operations.DescribeSensor.name()));
        // set param DCP
        setDCP(operation.addNewDCP(), owsOperationsMetadata.getDcp());
        // set param procedure
        setParamList(operation.addNewParameter(), Sos2Constants.DescribeSensorParams.procedure.name(),
                owsOperationsMetadata.getParameterValues().get(Sos2Constants.DescribeSensorParams.procedure.name()));
        // set param output format
        setParamValue(operation.addNewParameter(),
                Sos2Constants.DescribeSensorParams.procedureDescriptionFormat.name(),
                Sos2Constants.SENSORML_OUTPUT_FORMAT);
        // set valid time
        // TODO: implement set valid time values
    }

    /**
     * Sets the GetObservation operation and parameters to OperationsMetadata.
     * 
     * @param operation
     *            GetObservation operation.
     * @param owsOperationsMetadata
     *            SOS GetObservation metadata
     * @throws OwsExceptionReport
     */
    private void setOpsGetObservation(Operation operation, OWSOperationsMetadata owsOperationsMetadata)
            throws OwsExceptionReport {
        // set operation name
        operation.setName(refactorOpsName(Sos2Constants.Operations.GetObservation.name()));
        // set param DCP
        setDCP(operation.addNewDCP(), owsOperationsMetadata.getDcp());
        // set param srsName
        setParamList(operation.addNewParameter(), Sos2Constants.GetObservationParams.srsName.name(), null);
        // set param offering
        setParamList(operation.addNewParameter(), Sos2Constants.GetObservationParams.offering.name(),
                owsOperationsMetadata.getParameterValues().get(Sos2Constants.GetObservationParams.offering.name()));
        // set param eventTime
        setEventTime(operation.addNewParameter());
        // set param procedure
        setParamList(operation.addNewParameter(), Sos2Constants.GetObservationParams.procedure.name(),
                owsOperationsMetadata.getParameterValues().get(Sos2Constants.GetObservationParams.procedure.name()));
        // set param observedProperty
        setParamList4Obs(
                operation.addNewParameter(),
                Sos2Constants.GetObservationParams.observedProperty.name(),
                owsOperationsMetadata.getParameterValues().get(
                        Sos2Constants.GetObservationParams.observedProperty.name()));
        // set param foi
        setParamList4Obs(
                operation.addNewParameter(),
                Sos2Constants.GetObservationParams.featureOfInterest.name(),
                owsOperationsMetadata.getParameterValues().get(
                        Sos2Constants.GetObservationParams.featureOfInterest.name()));
        // set param result
        setParamResult(operation.addNewParameter(), Sos2Constants.GetObservationParams.result.name());
        // set param responseFormat
        setParamList(
                operation.addNewParameter(),
                Sos2Constants.GetObservationParams.responseFormat.name(),
                owsOperationsMetadata.getParameterValues().get(
                        Sos2Constants.GetObservationParams.responseFormat.name()));
    }

    /**
     * Sets the GetFeatureOfInterest operation and parameters to
     * OperationsMetadata.
     * 
     * @param operation
     *            GetFeatureOfInterest operation.
     * @param owsOperationsMetadata
     *            SOS GetFeatureOfInterest metadata
     */
    private void setOpsGetFeatureOfInterest(Operation operation, OWSOperationsMetadata owsOperationsMetadata) {
        // set operation name
        operation.setName(refactorOpsName(Sos2Constants.Operations.GetFeatureOfInterest.name()));
        // set param DCP
        setDCP(operation.addNewDCP(), owsOperationsMetadata.getDcp());
        // set param feature of interest id
        setParamList(
                operation.addNewParameter(),
                Sos2Constants.GetFeatureOfInterestParams.featureOfInterest.name(),
                owsOperationsMetadata.getParameterValues().get(
                        Sos2Constants.GetFeatureOfInterestParams.featureOfInterest.name()));
        // set param observable properties
        setParamList(
                operation.addNewParameter(),
                Sos2Constants.GetFeatureOfInterestParams.observableProperty.name(),
                owsOperationsMetadata.getParameterValues().get(
                        Sos2Constants.GetFeatureOfInterestParams.observableProperty.name()));
        // set param procedures
        setParamList(
                operation.addNewParameter(),
                Sos2Constants.GetFeatureOfInterestParams.procedure.name(),
                owsOperationsMetadata.getParameterValues().get(
                        Sos2Constants.GetFeatureOfInterestParams.procedure.name()));
        // set param location.
        setParamSpatialFilter(operation.addNewParameter(),
                Sos2Constants.GetFeatureOfInterestParams.spatialFilter.name());
    }

    /**
     * Sets the metadata for GetObservationById operation
     * 
     * @param operation
     *            GetObservationById operation
     * @param owsOperationsMetadata
     *            SOS GetObservationById metadata
     */
    private void setOpsGetObservationById(Operation operation, OWSOperationsMetadata owsOperationsMetadata) {
        // set operation name
        operation.setName(refactorOpsName(Sos2Constants.Operations.GetObservationById.name()));
        // set param DCP
        setDCP(operation.addNewDCP(), owsOperationsMetadata.getDcp());
        // Currently not supported by this SOS

    }

    /**
     * Sets the metadata for InsertSensor operation
     * 
     * @param operation
     *            InsertSensor operation
     * @param owsOperationsMetadata
     *            SOS InsertSensor metadata
     */
    private void setOpsInsertSensor(Operation operation, OWSOperationsMetadata owsOperationsMetadata) {
        // set operation name
        operation.setName(refactorOpsName(Sos2Constants.Operations.InsertSensor.name()));
        // set param DCP
        setDCP(operation.addNewDCP(), owsOperationsMetadata.getDcp());
        // Currently not supported by this SOS

    }

    /**
     * Sets the metadata for DeleteSensor operation
     * 
     * @param operation
     *            DeleteSensor operation
     * @param owsOperationsMetadata
     *            SOS DeleteSensor metadata
     */
    private void setOpsDeleteSensor(Operation operation, OWSOperationsMetadata owsOperationsMetadata) {
        // set operation name
        operation.setName(refactorOpsName(Sos2Constants.Operations.DescribeSensor.name()));
        // set param DCP
        setDCP(operation.addNewDCP(), owsOperationsMetadata.getDcp());
        // Currently not supported by this SOS
    }

    /**
     * Sets the metadata for InsertObservation operation
     * 
     * @param operation
     *            InsertObservation operation
     * @param owsOperationsMetadata
     *            SOS InsertObservation metadata
     */
    private void setOpsInsertObservation(Operation operation, OWSOperationsMetadata owsOperationsMetadata) {
        // set operation name
        operation.setName(refactorOpsName(Sos2Constants.Operations.InsertObservation.name()));
        // set param DCP
        setDCP(operation.addNewDCP(), owsOperationsMetadata.getDcp());
        // Currently not supported by this SOS
    }

    /**
     * Sets the metadata for InsertResult operation
     * 
     * @param operation
     *            InsertResult operation
     * @param owsOperationsMetadata
     *            SOS InsertResult metadata
     */
    private void setOpsInsertResult(Operation operation, OWSOperationsMetadata owsOperationsMetadata) {
        // set operation name
        operation.setName(refactorOpsName(Sos2Constants.Operations.InsertResult.name()));
        // set param DCP
        setDCP(operation.addNewDCP(), owsOperationsMetadata.getDcp());
        // Currently not supported by this SOS
    }

    /**
     * Sets the metadata for InsertResultTemplate operation
     * 
     * @param operation
     *            InsertResultTemplate operation
     * @param owsOperationsMetadata
     *            SOS InsertResultTemplate metadata
     */
    private void setOpsInsertResultTemplate(Operation operation, OWSOperationsMetadata owsOperationsMetadata) {
        // set operation name
        operation.setName(refactorOpsName(Sos2Constants.Operations.InsertResultTemplate.name()));
        // set param DCP
        setDCP(operation.addNewDCP(), owsOperationsMetadata.getDcp());
        // Currently not supported by this SOS
    }

    /**
     * Sets the metadata for GetResultTemplate operation
     * 
     * @param operation
     *            GetResultTemplate operation
     * @param owsOperationsMetadata
     *            SOS GetResultTemplate metadata
     */
    private void setOpsGetResultTemplate(Operation operation, OWSOperationsMetadata owsOperationsMetadata) {
        // set operation name
        operation.setName(refactorOpsName(Sos2Constants.Operations.GetResultTemplate.name()));
        // set param DCP
        setDCP(operation.addNewDCP(), owsOperationsMetadata.getDcp());
        // Currently not supported by this SOS
    }

    /**
     * Sets the metadata for GetResult operation
     * 
     * @param operation
     *            GetResult operation
     * @param owsOperationsMetadata
     *            SOS GetResult metadata
     * @throws OwsExceptionReport
     *             if an error occurs,
     */
    private void setOpsGetResult(Operation operation, OWSOperationsMetadata owsOperationsMetadata)
            throws OwsExceptionReport {
        // set operation name
        operation.setName(refactorOpsName(Sos2Constants.Operations.GetResult.name()));
        // set param DCP
        setDCP(operation.addNewDCP(), owsOperationsMetadata.getDcp());
        // Currently not supported by this SOS
    }

    /**
     * Sets an operation parameter to AnyValue, NoValues or AllowedValues.
     * 
     * @param domainType
     *            Parameter.
     * @param name
     *            Parameter name.
     * @param value
     *            The value.
     */
    private void setParamValue(DomainType domainType, String name, String value) {
        domainType.setName(name);
        if (value != null && value.equalsIgnoreCase(Sos2Constants.PARAMETER_ANY)) {
            domainType.addNewAnyValue();
        } else if (value != null && !value.equals("")) {
            domainType.addNewAllowedValues().addNewValue().setStringValue(value);
        } else {
            domainType.addNewNoValues();
        }
    }

    /**
     * Sets operation parameters to AnyValue, NoValues or AllowedValues.
     * 
     * @param domainType
     *            Paramter.
     * @param name
     *            Parameter name.
     * @param values
     *            List of values.
     */
    private void setParamList(DomainType domainType, String name, Collection<String> values) {
        domainType.setName(name);
        if (SosConfigurator.getInstance().isShowFullOperationsMetadata()) {
            if (values != null) {
                if (!values.isEmpty()) {
                    AllowedValues allowedValues = domainType.addNewAllowedValues();
                    for (String value : values) {
                        allowedValues.addNewValue().setStringValue(value);
                    }
                } else {
                    domainType.addNewAnyValue();
                }
            } else {
                domainType.addNewNoValues();
            }
        } else {
            domainType.addNewAnyValue();
        }
    }

    /**
     * Sets operation parameters to AnyValue, NoValues or AllowedValues for
     * GetObservation operation. If not all parameters are to be shown.
     * 
     * @param domainType
     *            Paramter.
     * @param name
     *            Parameter name.
     * @param values
     *            List of values.
     */
    private void setParamList4Obs(DomainType domainType, String name, Collection<String> values) {
        if (SosConfigurator.getInstance().isShowFullOperationsMetadata4Observations()) {
            setParamList(domainType, name, values);
        } else {
            domainType.setName(name);
            domainType.addNewAnyValue();
        }
    }

    /**
     * Sets the parameter for result.
     * 
     * @param domainType
     *            Paramter.
     * @param name
     *            Parameter name.
     */
    private void setParamResult(DomainType domainType, String name) {
        domainType.setName(name);
        domainType.addNewAnyValue();
    }

    /**
     * Sets the ResultModel parameters.
     * 
     * @param domainType
     *            Paramter.
     * @param name
     *            Parameter name.
     * @param values
     *            QName array of resultModels.
     */
    private void setParamResultModels(DomainType domainType, String name, QName[] resultModels) {
        List<String> resultModelsList = new ArrayList<String>();
        for (QName qname : Arrays.asList(Sos2Constants.getResultModels())) {
            resultModelsList.add(qname.getPrefix() + ":" + qname.getLocalPart());
        }
        setParamList(domainType, name, resultModelsList);
    }

    /**
     * Acts the parameter for observation ids as range
     * 
     * @param domainType
     *            Parameter.
     * @param name
     *            Parameter name.
     * @param minObservationId
     *            Min range value.
     * @param maxObservationId
     *            Max range value.
     */
    private void setParamRangeOId(DomainType domainType, String name, Map<String, String> minMaxOId) {
        domainType.setName(name);
        String minObservationId = minMaxOId.get(SosConstants.MIN_VALUE);
        String maxObservationId = minMaxOId.get(SosConstants.MAX_VALUE);
        if (minObservationId == null || maxObservationId == null || minObservationId.equals("")
                || maxObservationId.equals("")) {
            domainType.addNewNoValues();
        } else {
            RangeType range = domainType.addNewAllowedValues().addNewRange();
            range.addNewMinimumValue().setStringValue(minObservationId);
            range.addNewMaximumValue().setStringValue(maxObservationId);
        }
    }

    /**
     * Sets the parameter for SpatialFilter.
     * 
     * @param domainType
     *            Paramter.
     * @param name
     *            Parameter name.
     */
    private void setParamSpatialFilter(DomainType domainType, String name) {
        domainType.setName(name);
        domainType.addNewAnyValue();
    }

    /**
     * Sets the DCP operation.
     * 
     * @param dcp
     *            The operation.
     * @param get
     *            Add GET.
     */
    private void setDCP(DCP dcp, Map<String, String> supportedDcp) {
        HTTP http = dcp.addNewHTTP();
        String dcpGet = supportedDcp.get(SosConstants.HTTP_GET);
        String dcpPost = supportedDcp.get(SosConstants.HTTP_POST);
        if (dcpGet != null && !dcpGet.isEmpty()) {
            http.addNewGet().setHref(dcpGet);
        }
        if (dcpPost != null && !dcpPost.isEmpty()) {
            http.addNewPost().setHref(dcpPost);
        }
    }

    /**
     * Sets the EventTime parameter.
     * 
     * @param domainType
     *            Parameter.
     * @throws OwsExceptionReport
     */
    private void setEventTime(DomainType domainType) throws OwsExceptionReport {
        domainType.setName(Sos2Constants.GetObservationParams.temporalFilter.name());
        // set the min value of the eventTime
        RangeType range = RangeType.Factory.newInstance();
        ValueType minValue = ValueType.Factory.newInstance();
        DateTime minDate = SosConfigurator.getInstance().getFactory().getConfigDAO().getMinDate4Observations();
        DateTime maxDate = SosConfigurator.getInstance().getFactory().getConfigDAO().getMaxDate4Observations();
        if (minDate != null && maxDate != null) {
            String minDateString = SosDateTimeUtilities.formatDateTime2ResponseString(minDate);
            minValue.setStringValue(minDateString);
            range.addNewMinimumValue();
            range.setMinimumValue(minValue);

            // set the max value of the eventTime
            ValueType maxValue = ValueType.Factory.newInstance();
            String maxDateString = SosDateTimeUtilities.formatDateTime2ResponseString(maxDate);
            maxValue.setStringValue(maxDateString);
            range.addNewMaximumValue();
            range.setMaximumValue(maxValue);

            // set the range for eventTime element
            RangeType[] ranges = { range };
            domainType.addNewAllowedValues().setRangeArray(ranges);
        } else {
            domainType.addNewNoValues();
        }
    }

    /**
     * Sets the FES conformance classes in the filter capabilities section.
     * 
     * @param conformance
     *            FES conformence
     */
    private void setConformance(ConformanceType conformance) {
        // set Query conformance class
        DomainType implQuery = conformance.addNewConstraint();
        implQuery.setName(ConformanceClassConstraintNames.ImplementsQuery.name());
        implQuery.addNewNoValues();
        implQuery.addNewDefaultValue().setStringValue("false");
        // set Ad hoc query conformance class
        DomainType implAdHocQuery = conformance.addNewConstraint();
        implAdHocQuery.setName(ConformanceClassConstraintNames.ImplementsAdHocQuery.name());
        implAdHocQuery.addNewNoValues();
        implAdHocQuery.addNewDefaultValue().setStringValue("false");
        // set Functions conformance class
        DomainType implFunctions = conformance.addNewConstraint();
        implFunctions.setName(ConformanceClassConstraintNames.ImplementsFunctions.name());
        implFunctions.addNewNoValues();
        implFunctions.addNewDefaultValue().setStringValue("false");
        // set Resource Identification conformance class
        DomainType implResourceId = conformance.addNewConstraint();
        implResourceId.setName(ConformanceClassConstraintNames.ImplementsResourceld.name());
        implResourceId.addNewNoValues();
        implResourceId.addNewDefaultValue().setStringValue("false");
        // set Minimum Standard Filter conformance class
        DomainType implMinStandardFilter = conformance.addNewConstraint();
        implMinStandardFilter.setName(ConformanceClassConstraintNames.ImplementsMinStandardFilter.name());
        implMinStandardFilter.addNewNoValues();
        implMinStandardFilter.addNewDefaultValue().setStringValue("false");
        // set Standard Filter conformance class
        DomainType implStandardFilter = conformance.addNewConstraint();
        implStandardFilter.setName(ConformanceClassConstraintNames.ImplementsStandardFilter.name());
        implStandardFilter.addNewNoValues();
        implStandardFilter.addNewDefaultValue().setStringValue("false");
        // set Minimum Spatial Filter conformance class
        DomainType implMinSpatialFilter = conformance.addNewConstraint();
        implMinSpatialFilter.setName(ConformanceClassConstraintNames.ImplementsMinSpatialFilter.name());
        implMinSpatialFilter.addNewNoValues();
        implMinSpatialFilter.addNewDefaultValue().setStringValue("true");
        // set Spatial Filter conformance class
        DomainType implSpatialFilter = conformance.addNewConstraint();
        implSpatialFilter.setName(ConformanceClassConstraintNames.ImplementsSpatialFilter.name());
        implSpatialFilter.addNewNoValues();
        implSpatialFilter.addNewDefaultValue().setStringValue("true");
        // set Minimum Temporal Filter conformance class
        DomainType implMinTemporalFilter = conformance.addNewConstraint();
        implMinTemporalFilter.setName(ConformanceClassConstraintNames.ImplementsMinTemporalFilter.name());
        implMinTemporalFilter.addNewNoValues();
        implMinTemporalFilter.addNewDefaultValue().setStringValue("true");
        // set Temporal Filter conformance class
        DomainType implTemporalFilter = conformance.addNewConstraint();
        implTemporalFilter.setName(ConformanceClassConstraintNames.ImplementsTemporalFilter.name());
        implTemporalFilter.addNewNoValues();
        implTemporalFilter.addNewDefaultValue().setStringValue("true");
        // set Version navigation conformance class
        DomainType implVersionNav = conformance.addNewConstraint();
        implVersionNav.setName(ConformanceClassConstraintNames.ImplementsVersionNav.name());
        implVersionNav.addNewNoValues();
        implVersionNav.addNewDefaultValue().setStringValue("false");
        // set Sorting conformance class
        DomainType implSorting = conformance.addNewConstraint();
        implSorting.setName(ConformanceClassConstraintNames.ImplementsSorting.name());
        implSorting.addNewNoValues();
        implSorting.addNewDefaultValue().setStringValue("false");
        // set Extended Operators conformance class
        DomainType implExtendedOperators = conformance.addNewConstraint();
        implExtendedOperators.setName(ConformanceClassConstraintNames.ImplementsExtendedOperators.name());
        implExtendedOperators.addNewNoValues();
        implExtendedOperators.addNewDefaultValue().setStringValue("false");
        // set Minimum XPath conformance class
        DomainType implMinimumXPath = conformance.addNewConstraint();
        implMinimumXPath.setName(ConformanceClassConstraintNames.ImplementsMinimumXPath.name());
        implMinimumXPath.addNewNoValues();
        implMinimumXPath.addNewDefaultValue().setStringValue("false");
        // set Schema Element Function conformance class
        DomainType implSchemaElementFunc = conformance.addNewConstraint();
        implSchemaElementFunc.setName(ConformanceClassConstraintNames.ImplementsSchemaElementFunc.name());
        implSchemaElementFunc.addNewNoValues();
        implSchemaElementFunc.addNewDefaultValue().setStringValue("false");

    }

    /**
     * Sets the SpatialFilterCapabilities.
     * 
     * !!! Modify method addicted to your implementation !!!
     * 
     * @param spatialCapabilitiesType
     *            FES SpatialCapabilities.
     * @param sosFilterCaps
     *            SOS spatial filter information
     */
    private void setSpatialFilterCapabilities(SpatialCapabilitiesType spatialCapabilitiesType,
            org.n52.sos.ogc.filter.FilterCapabilities sosFilterCaps) {

        // set GeometryOperands
        if (sosFilterCaps.getSpatialOperands() != null && !sosFilterCaps.getSpatialOperands().isEmpty()) {
            GeometryOperandsType spatialOperands = spatialCapabilitiesType.addNewGeometryOperands();
            for (QName operand : sosFilterCaps.getSpatialOperands()) {
                spatialOperands.addNewGeometryOperand().setName(operand);
            }
        }

        // set SpatialOperators
        if (sosFilterCaps.getSpatialOperators() != null && !sosFilterCaps.getSpatialOperators().isEmpty()) {
            SpatialOperatorsType spatialOps = spatialCapabilitiesType.addNewSpatialOperators();
            Set<SpatialOperator> keys = sosFilterCaps.getSpatialOperators().keySet();
            for (SpatialOperator spatialOperator : keys) {
                SpatialOperatorType operator = spatialOps.addNewSpatialOperator();
                operator.setName(getEnum4SpatialOperator(spatialOperator));
                GeometryOperandsType geomOps = operator.addNewGeometryOperands();
                for (QName operand : sosFilterCaps.getSpatialOperators().get(spatialOperator)) {
                    geomOps.addNewGeometryOperand().setName(operand);
                }
            }
        }
    }

    /**
     * Sets the TemporalFilterCapabilities.
     * 
     * !!! Modify method addicted to your implementation !!!
     * 
     * @param temporalCapabilitiesType
     *            FES TemporalCapabilities.
     * @param sosFilterCaps
     *            SOS temporal filter information
     */
    private void setTemporalFilterCapabilities(TemporalCapabilitiesType temporalCapabilitiesType,
            org.n52.sos.ogc.filter.FilterCapabilities sosFilterCaps) {

        // set TemporalOperands
        if (sosFilterCaps.getTemporalOperands() != null && !sosFilterCaps.getTemporalOperands().isEmpty()) {
            TemporalOperandsType tempOperands = temporalCapabilitiesType.addNewTemporalOperands();
            for (QName operand : sosFilterCaps.getTemporalOperands()) {
                tempOperands.addNewTemporalOperand().setName(operand);
            }
        }

        // set TemporalOperators
        if (sosFilterCaps.getTempporalOperators() != null && !sosFilterCaps.getTempporalOperators().isEmpty()) {
            TemporalOperatorsType temporalOps = temporalCapabilitiesType.addNewTemporalOperators();
            Set<TimeOperator> keys = sosFilterCaps.getTempporalOperators().keySet();
            for (TimeOperator temporalOperator : keys) {
                TemporalOperatorType operator = temporalOps.addNewTemporalOperator();
                operator.setName(getEnum4TemporalOperator(temporalOperator));
                TemporalOperandsType bboxGeomOps = operator.addNewTemporalOperands();
                for (QName operand : sosFilterCaps.getTempporalOperators().get(temporalOperator)) {
                    bboxGeomOps.addNewTemporalOperand().setName(operand);
                }
            }
        }
    }

    /**
     * Sets the ScalarFilterCapabilities.
     * 
     * !!! Modify method addicted to your implementation !!!
     * 
     * @param scalarCapabilitiesType
     *            FES ScalarCapabilities.
     * @param sosFilterCaps
     *            SOS scalar filter information
     */
    private void setScalarFilterCapabilities(ScalarCapabilitiesType scalarCapabilitiesType,
            org.n52.sos.ogc.filter.FilterCapabilities sosFilterCaps) {

        if (sosFilterCaps.getComparisonOperators() != null && !sosFilterCaps.getComparisonOperators().isEmpty()) {
            ComparisonOperatorsType scalarOps = scalarCapabilitiesType.addNewComparisonOperators();
            for (ComparisonOperator operator : sosFilterCaps.getComparisonOperators()) {
                scalarOps.addNewComparisonOperator().setName(getEnum4ComparisonOperator(operator));
            }
        }
    }

    /**
     * Set the IdFilterCapabilities.
     * 
     * !!! Modify method addicted to your implementation !!!
     * 
     * @param idCapabilitiesType
     *            FES IdCapabilities.
     */
    private void setIdFilterCapabilities(IdCapabilitiesType idCapabilitiesType) {
        idCapabilitiesType.addNewResourceIdentifier();
    }

    /**
     * Get the FES spatial operator name for SOS spatial operator
     * 
     * @param spatialOperator
     *            SOS spatial operator
     * @return FES spatial operator name
     */
    private String getEnum4SpatialOperator(SpatialOperator spatialOperator) {
        switch (spatialOperator) {
        case BBOX:
            return SpatialOperatorNameTypeImpl.BBOX.toString();
        case Beyond:
            return SpatialOperatorNameTypeImpl.BEYOND.toString();
        case Contains:
            return SpatialOperatorNameTypeImpl.CONTAINS.toString();
        case Crosses:
            return SpatialOperatorNameTypeImpl.CROSSES.toString();
        case Disjoint:
            return SpatialOperatorNameTypeImpl.DISJOINT.toString();
        case DWithin:
            return SpatialOperatorNameTypeImpl.D_WITHIN.toString();
        case Equals:
            return SpatialOperatorNameTypeImpl.EQUALS.toString();
        case Intersects:
            return SpatialOperatorNameTypeImpl.INTERSECTS.toString();
        case Overlaps:
            return SpatialOperatorNameTypeImpl.OVERLAPS.toString();
        case Touches:
            return SpatialOperatorNameTypeImpl.TOUCHES.toString();
        case Within:
            return SpatialOperatorNameTypeImpl.WITHIN.toString();
        default:
            break;
        }
        return null;
    }

    /**
     * Get the FES temporal operator name for SOS temporal operator
     * 
     * @param temporalOperator
     *            SOS temporal operator
     * @return FES temporal operator name
     */
    private String getEnum4TemporalOperator(TimeOperator temporalOperator) {
        switch (temporalOperator) {
        case TM_After:
            return TemporalOperatorNameTypeImpl.AFTER.toString();
        case TM_Before:
            return TemporalOperatorNameTypeImpl.BEFORE.toString();
        case TM_Begins:
            return TemporalOperatorNameTypeImpl.BEGINS.toString();
        case TM_BegunBy:
            return TemporalOperatorNameTypeImpl.BEGUN_BY.toString();
        case TM_Contains:
            return TemporalOperatorNameTypeImpl.T_CONTAINS.toString();
        case TM_During:
            return TemporalOperatorNameTypeImpl.DURING.toString();
        case TM_EndedBy:
            return TemporalOperatorNameTypeImpl.ENDED_BY.toString();
        case TM_Ends:
            return TemporalOperatorNameTypeImpl.ENDS.toString();
        case TM_Equals:
            return TemporalOperatorNameTypeImpl.T_EQUALS.toString();
        case TM_Meets:
            return TemporalOperatorNameTypeImpl.MEETS.toString();
        case TM_MetBy:
            return TemporalOperatorNameTypeImpl.MET_BY.toString();
        case TM_OverlappedBy:
            return TemporalOperatorNameTypeImpl.OVERLAPPED_BY.toString();
        case TM_Overlaps:
            return TemporalOperatorNameTypeImpl.T_OVERLAPS.toString();
        default:
            break;
        }
        return null;
    }

    /**
     * Get the FES comparison operator name for SOS comparison operator
     * 
     * @param comparisonOperator
     *            SOS comparison operator
     * @return FES comparison operator name
     */
    private String getEnum4ComparisonOperator(ComparisonOperator comparisonOperator) {
        switch (comparisonOperator) {
        case PropertyIsBetween:
            return ComparisonOperatorNameTypeImpl.PROPERTY_IS_BETWEEN.toString();
        case PropertyIsEqualTo:
            return ComparisonOperatorNameTypeImpl.PROPERTY_IS_EQUAL_TO.toString();
        case PropertyIsGreaterThan:
            return ComparisonOperatorNameTypeImpl.PROPERTY_IS_GREATER_THAN.toString();
        case PropertyIsGreaterThanOrEqualTo:
            return ComparisonOperatorNameTypeImpl.PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO.toString();
        case PropertyIsLessThan:
            return ComparisonOperatorNameTypeImpl.PROPERTY_IS_LESS_THAN.toString();
        case PropertyIsLessThanOrEqualTo:
            return ComparisonOperatorNameTypeImpl.PROPERTY_IS_LESS_THAN_OR_EQUAL_TO.toString();
        case PropertyIsLike:
            return ComparisonOperatorNameTypeImpl.PROPERTY_IS_LIKE.toString();
        case PropertyIsNil:
            return ComparisonOperatorNameTypeImpl.PROPERTY_IS_NIL.toString();
        case PropertyIsNotEqualTo:
            return ComparisonOperatorNameTypeImpl.PROPERTY_IS_NOT_EQUAL_TO.toString();
        case PropertyIsNull:
            return ComparisonOperatorNameTypeImpl.PROPERTY_IS_NULL.toString();
        default:
            break;
        }
        return null;
    }

    /**
     * Sets the first character to UpperCase.
     * 
     * @param name
     *            String to be modified.
     * @return Modified string.
     */
    private String refactorOpsName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);

    }

    /**
     * queries the bounding box of all requested feature of interest IDs
     * 
     * @param envelope
     * 
     * @param foiIDs
     *            ArrayList with String[]s containing the ids of the feature of
     *            interests for which the BBOX should be returned
     * @return Returns EnvelopeType XmlBean which represents the BBOX of the
     *         requested feature of interests
     * @throws OwsExceptionReport
     *             if query of the BBOX failed
     */
    private EnvelopeType getBBOX4Offering(Envelope envelope, int srsID) throws OwsExceptionReport {

        double minx = envelope.getMinX();
        double maxx = envelope.getMaxX();
        double miny = envelope.getMinY();
        double maxy = envelope.getMaxY();
        @SuppressWarnings("unused")
        String minz = null;
        @SuppressWarnings("unused")
        String maxz = null;

        EnvelopeType envelopeType = EnvelopeType.Factory.newInstance();

        // set lower corner
        // TODO for full 3D support add minz to parameter in setStringValue
        DirectPositionType lowerCorner = envelopeType.addNewLowerCorner();
        DirectPositionType upperCorner = envelopeType.addNewUpperCorner();

        if (!SosConfigurator.getInstance().switchCoordinatesForEPSG(srsID)) {
            lowerCorner.setStringValue(minx + " " + miny);
        } else {
            lowerCorner.setStringValue(miny + " " + minx);
        }

        // set upper corner
        // TODO for full 3D support add maxz to parameter in setStringValue
        if (!SosConfigurator.getInstance().switchCoordinatesForEPSG(srsID)) {
            upperCorner.setStringValue(maxx + " " + maxy);
        } else {
            upperCorner.setStringValue(maxy + " " + maxx);
        }
        // set SRS
        envelopeType.setSrsName(SosConfigurator.getInstance().getSrsNamePrefixSosV2() + srsID);

        return envelopeType;
    }

}
