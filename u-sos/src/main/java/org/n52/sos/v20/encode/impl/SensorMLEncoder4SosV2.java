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

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.CharacteristicsDocument.Characteristics;
import net.opengis.sensorML.x101.ClassificationDocument.Classification;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList.Classifier;
import net.opengis.sensorML.x101.ComponentsDocument.Components;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList;
import net.opengis.sensorML.x101.EventDocument.Event;
import net.opengis.sensorML.x101.EventListDocument.EventList;
import net.opengis.sensorML.x101.HistoryDocument.History;
import net.opengis.sensorML.x101.IdentificationDocument.Identification;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.InputsDocument.Inputs;
import net.opengis.sensorML.x101.InputsDocument.Inputs.InputList;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.OutputsDocument.Outputs;
import net.opengis.sensorML.x101.OutputsDocument.Outputs.OutputList;
import net.opengis.sensorML.x101.PositionDocument.Position;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML;
import net.opengis.sensorML.x101.SystemDocument;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.sensorML.x101.TermDocument.Term;
import net.opengis.swe.x101.AnyScalarPropertyType;
import net.opengis.swe.x101.BooleanDocument.Boolean;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.PositionType;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.SimpleDataRecordType;
import net.opengis.swe.x101.VectorPropertyType;
import net.opengis.swe.x101.VectorType;
import net.opengis.swe.x101.VectorType.Coordinate;
import net.opengis.swes.x20.DescribeSensorResponseDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.n52.sos.Sos1Constants;
import org.n52.sos.Sos2Constants;
import org.n52.sos.SosConfigurator;
import org.n52.sos.SosDateTimeUtilities;
import org.n52.sos.SosXmlUtilities;
import org.n52.sos.encode.ISensorMLEncoder;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionLevel;
import org.n52.sos.ogc.sensorML.ProcedureHistory;
import org.n52.sos.ogc.sensorML.ProcedureHistoryEvent;
import org.n52.sos.ogc.sensorML.SWEConstants;
import org.n52.sos.ogc.sensorML.SWEConstants.SensorMLType;
import org.n52.sos.ogc.sensorML.SWEConstants.SweAggregateType;
import org.n52.sos.ogc.sensorML.SWEConstants.SweSimpleType;
import org.n52.sos.ogc.sensorML.SosSensorML;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.simpleType.ISosSweSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.resp.SensorDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Point;

/**
 * class offers static operations for encoding of SensorML documents/elements
 * 
 * @author Carsten Hollmann
 * 
 */
public class SensorMLEncoder4SosV2 implements ISensorMLEncoder {

    /** the logger, used to log exceptions and additonaly information */
    private static Logger LOGGER = Logger.getLogger(SensorMLEncoder4SosV2.class);

    /**
     * creates sml:System
     * 
     * @param smlDescription
     *            SensorML encoded system description
     * @param history
     *            history of sensor parameters
     * @return Returns XMLBeans representation of sml:System
     * @throws OwsExceptionReport
     */
    public SensorDocument createSensor(SosSensorML sensorDesc, ProcedureHistory history, Point actualPosition)
            throws OwsExceptionReport {
        SensorDocument xbSensorDoc = null;

        DescribeSensorResponseDocument xbDescSensorRespDoc =
                DescribeSensorResponseDocument.Factory.newInstance(SosXmlUtilities.getInstance().getXmlOptions4Sos2Swe101());
        xbDescSensorRespDoc.addNewDescribeSensorResponse().setProcedureDescriptionFormat(
                Sos2Constants.SENSORML_OUTPUT_FORMAT);

        SensorMLDocument xbSmlDoc = null;
        SystemDocument xbSystemDoc = null;
        // sensor description as a string
        if (sensorDesc != null) {
            if (sensorDesc.getSosSensorDescriptionType()
                    .equals(SWEConstants.SosSensorDescription.XmlStringDescription)) {
                // get SystemDocument
                try {
                    xbSmlDoc = SensorMLDocument.Factory.parse(sensorDesc.getSensorDescriptionString());
                    SensorML sml = xbSmlDoc.getSensorML();
                    SensorMLDocument.SensorML.Member[] memb = sml.getMemberArray();

                    xbSystemDoc = SystemDocument.Factory.parse(memb[0].toString());

                } catch (XmlException xmle) {
                    OwsExceptionReport se = new OwsExceptionReport();
                    se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, null,
                            "Error while encoding SensorML description from stored SensorML encoded sensor description with XMLBeans: "
                                    + xmle.getMessage());
                    throw se;
                }

                SystemType xb_system = xbSystemDoc.getSystem();

                // if history is not null, set sensor history
                // if (history != null) {
                if (history != null && !history.getHistory().isEmpty()) {
                    History xb_history = xb_system.addNewHistory();
                    EventList xb_eventList = xb_history.addNewEventList();

                    Collection<ProcedureHistoryEvent> events = history.getHistory();

                    Iterator<ProcedureHistoryEvent> iter = events.iterator();
                    ProcedureHistoryEvent lastEvent = null;
                    int i = 1;
                    while (iter.hasNext()) {
                        // check memory
                        checkFreeMemory();

                        lastEvent = iter.next();
                        net.opengis.sensorML.x101.EventListDocument.EventList.Member xb_member =
                                xb_eventList.addNewMember();
                        xb_member.setName("Position" + i);
                        i++;
                        Event xb_event = xb_member.addNewEvent();
                        String timeString =
                                SosDateTimeUtilities.formatDateTime2ResponseString(lastEvent.getTimeStamp());
                        xb_event.setDate(timeString);
                        DataComponentPropertyType xb_prop = xb_event.addNewProperty();
                        xb_prop.set(createPosition(lastEvent.getPosition()));
                        xb_prop.setName("position");

                        DataComponentPropertyType xb_prop1 = xb_event.addNewProperty();
                        Boolean bool = xb_prop1.addNewBoolean();
                        bool.setValue(lastEvent.isActive());
                        xb_prop1.setName("active");

                        DataComponentPropertyType xb_prop2 = xb_event.addNewProperty();
                        bool = xb_prop2.addNewBoolean();
                        bool.setValue(lastEvent.isMobile());
                        xb_prop2.setName("mobile");
                    }

                    if (lastEvent == null) {
                        OwsExceptionReport se = new OwsExceptionReport();
                        se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                                Sos1Constants.DescribeSensorParams.time.name(),
                                "No positions are contained for the sensor for the requested time period!!");
                        throw se;
                    }
                }

                // set actual position
                if (actualPosition != null) {
                    Position xb_pos = xb_system.getPosition();
                    xb_pos.set(createPosition(actualPosition));
                    xb_pos.setName("actualPosition");
                }

            }
            // sensor description as SOS internal representation.
            else if (sensorDesc.getSosSensorDescriptionType().equals(SWEConstants.SosSensorDescription.SosDescription)) {
                switch (sensorDesc.getSensorMLType()) {
                case System:
                    xbSystemDoc =
                            SystemDocument.Factory.newInstance(SosXmlUtilities.getInstance().getXmlOptions4Sos2Swe101());
                    SystemType xbSystem = xbSystemDoc.addNewSystem();
                    // set identification
                    if (sensorDesc.getIdentifications() != null && !sensorDesc.getIdentifications().isEmpty()) {
                        createIdentification(xbSystem.addNewIdentification(), sensorDesc.getIdentifications());
                    }
                    // set classification
                    if (sensorDesc.getClassifications() != null && !sensorDesc.getClassifications().isEmpty()) {
                        createClassification(xbSystem.addNewClassification(), sensorDesc.getClassifications());
                    }
                    // set characteristics
                    if (sensorDesc.getCharacteristics() != null) {
                        createCharacteristics(xbSystem.addNewCharacteristics(), sensorDesc.getCharacteristics());
                    }
                    // set capabilities
                    if (sensorDesc.getCapabilities() != null) {
                        createCapabilities(xbSystem.addNewCapabilities(), sensorDesc.getCapabilities());
                    }
                    // set position
                    if (sensorDesc.getPosition() != null) {
                        createPosition(xbSystem.addNewPosition(), sensorDesc.getPosition());
                    }
                    // set inputs
                    if (sensorDesc.getInputs() != null && !sensorDesc.getInputs().isEmpty()) {
                        createInputs(xbSystem.addNewInputs(), sensorDesc.getInputs());
                    }
                    // set outputs
                    if (sensorDesc.getOutputs() != null && !sensorDesc.getOutputs().isEmpty()) {
                        createOutputs(xbSystem.addNewOutputs(), sensorDesc.getOutputs());
                    }
                    // set components
                    if (sensorDesc.getComponents() != null && !sensorDesc.getComponents().isEmpty()) {
                        createComponents(xbSystem.addNewComponents(), sensorDesc.getComponents());
                    }
                    break;
                case Component:
                    OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                    LOGGER.error("The SensorML member '" + SensorMLType.Component.name() + "' is not supported by this SOS!");
                    se.addCodedException(ExceptionCode.NoApplicableCode, null, "The SensorML member '" + SensorMLType.Component.name() + "' is not supported by this SOS!");
                    throw se;
                case ProcessModel:
                    OwsExceptionReport se1 = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                    LOGGER.error("The SensorML member '" + SensorMLType.ProcessModel.name() + "' is not supported by this SOS!");
                    se1.addCodedException(ExceptionCode.NoApplicableCode, null, "The SensorML member '" + SensorMLType.ProcessModel.name() + "' is not supported by this SOS!");
                    throw se1;
                case ProcessChain:
                    OwsExceptionReport se2 = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                    LOGGER.error("The SensorML member '" + SensorMLType.ProcessChain.name() + "' is not supported by this SOS!");
                    se2.addCodedException(ExceptionCode.NoApplicableCode, null, "The SensorML member '" + SensorMLType.ProcessChain.name() + "' is not supported by this SOS!");
                    throw se2;
                default:
                    break;
                }
            } else {
                OwsExceptionReport se2 = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                LOGGER.error("The SOS internal sensor description type '" + sensorDesc.getSosSensorDescriptionType() + "' is not supported by this SOS!");
                se2.addCodedException(ExceptionCode.NoApplicableCode, null, "The SOS internal sensor description type '" + sensorDesc.getSosSensorDescriptionType() + "' is not supported by this SOS!");
                throw se2;
            }
        }

        // xb_smlDoc.getSensorML().getMemberArray(0).set(xb_systemDoc);
        xbDescSensorRespDoc.getDescribeSensorResponse().addNewDescription().addNewSensorDescription().addNewData()
                .set(xbSystemDoc);
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc =
                    builder.parse(new InputSource(new StringReader(xbDescSensorRespDoc.xmlText(SosXmlUtilities.getInstance().getXmlOptions4Sos2Swe101()))));

            xbSensorDoc = new SensorDocument(doc);

        } catch (SAXException saxe) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("An error occured while parsing the sensor description document!", saxe);
            se.addCodedException(ExceptionCode.NoApplicableCode, null, saxe);
            throw se;
        } catch (IOException ioe) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("An error occured while parsing the sensor description document!", ioe);
            se.addCodedException(ExceptionCode.NoApplicableCode, null, ioe);
            throw se;
        } catch (ParserConfigurationException pce) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("An error occured while parsing the sensor description document!", pce);
            se.addCodedException(ExceptionCode.NoApplicableCode, null, pce);
            throw se;
        }

        return xbSensorDoc;
    }

    /**
     * Creates the identification section of the SensorML description.
     * 
     * @param xbIdentification
     *            Xml identification object
     * @param identifications
     *            SOS SWE representation.
     */
    private void createIdentification(Identification xbIdentification, List<SosSMLIdentifier> identifications) {
        IdentifierList xbIdentifierList = xbIdentification.addNewIdentifierList();
        for (SosSMLIdentifier sosSMLIdentifier : identifications) {
            Identifier xbIdentifier = xbIdentifierList.addNewIdentifier();
            xbIdentifier.setName(sosSMLIdentifier.getName());
            Term xbTerm = xbIdentifier.addNewTerm();
            xbTerm.setDefinition(sosSMLIdentifier.getDefinition());
            xbTerm.setValue(sosSMLIdentifier.getValue());
        }
    }

    /**
     * Creates the classification section of the SensorML description.
     * 
     * @param xbClassification
     *            Xml classifications object
     * @param classifications
     *            SOS SWE representation.
     */
    private void createClassification(Classification xbClassification, List<SosSMLClassifier> classifications) {
        ClassifierList xbClassifierList = xbClassification.addNewClassifierList();
        for (SosSMLClassifier sosSMLClassifier : classifications) {
            Classifier xbClassifier = xbClassifierList.addNewClassifier();
            xbClassifier.setName(sosSMLClassifier.getName());
            Term xbTerm = xbClassifier.addNewTerm();
            xbTerm.setValue(sosSMLClassifier.getValue());
        }
    }

    /**
     * Creates the characteristics section of the SensorML description.
     * 
     * @param xbCharacteristics
     *            Xml characteristics object
     * @param characteristics
     *            SOS SWE representation.
     * @throws OwsExceptionReport 
     */
    private void createCharacteristics(Characteristics xbCharacteristics, SosSMLCharacteristics characteristics) throws OwsExceptionReport {
        if (characteristics.getCharacteristicsType().equals(SweAggregateType.SimpleDataRecord)) {
            SimpleDataRecordType xbSimpleDataRecord =
                    (SimpleDataRecordType) xbCharacteristics.addNewAbstractDataRecord().substitute(
                            SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
            if (characteristics.getTypeDefinition() != null && !characteristics.getTypeDefinition().isEmpty()) {
                xbSimpleDataRecord.setDefinition(characteristics.getTypeDefinition());
            }
            for (SosSweField field : characteristics.getFields()) {
                AnyScalarPropertyType xbField = xbSimpleDataRecord.addNewField();
                xbField.setName(field.getName());
                addSweSimpleTypeToField(xbField, field.getElement());
            }
        } else if (characteristics.getCharacteristicsType().equals(SweAggregateType.DataRecord)) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE characteristics type '" + SweAggregateType.DataRecord.name() + "' is not supported by this SOS for SensorML!");
            se.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE characteristics type '" + SweAggregateType.DataRecord.name() + "' is not supported by this SOS for SensorML characteristics!");
            throw se;
        } else {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE characteristics type '" + characteristics.getCharacteristicsType().name() + "' is not supported by this SOS for SensorML characteristics!");
            se.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE characteristics type '" + characteristics.getCharacteristicsType().name() + "' is not supported by this SOS for SensorML characteristics!");
            throw se;
        }
    }

    /**
     * Creates the capabilities section of the SensorML description.
     * 
     * @param xbCapabilities
     *            Xml capabilities object
     * @param capabilities
     *            SOS SWE representation.
     * @throws OwsExceptionReport 
     */
    private void createCapabilities(Capabilities xbCapabilities, SosSMLCapabilities capabilities) throws OwsExceptionReport {
        if (capabilities.getCapabilitiesType().equals(SweAggregateType.SimpleDataRecord)) {
            SimpleDataRecordType xbSimpleDataRecord =
                    (SimpleDataRecordType) xbCapabilities.addNewAbstractDataRecord().substitute(
                            SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
            for (SosSweField field : capabilities.getFields()) {
                AnyScalarPropertyType xbField = xbSimpleDataRecord.addNewField();
                xbField.setName(field.getName());
                addSweSimpleTypeToField(xbField, field.getElement());
            }
        } else if (capabilities.getCapabilitiesType().equals(SweAggregateType.DataRecord)) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE capabilities type '" + SweAggregateType.DataRecord.name() + "' is not supported by this SOS for SensorML!");
            se.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE capabilities type '" + SweAggregateType.DataRecord.name() + "' is not supported by this SOS for SensorML characteristics!");
            throw se;
        } else {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE capabilities type '" + capabilities.getCapabilitiesType().name() + "' is not supported by this SOS for SensorML!");
            se.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE capabilities type '" + capabilities.getCapabilitiesType().name() + "' is not supported by this SOS for SensorML characteristics!");
            throw se;
        }

    }

    /**
     * Creates the position section of the SensorML description.
     * 
     * @param xbPosition
     *            Xml position object
     * @param position
     *            SOS SWE representation.
     */
    private void createPosition(Position xbPosition, SosSMLPosition position) {
        if (position.getName() != null && !position.getName().isEmpty()) {
            xbPosition.setName(position.getName());
        }
        PositionType xbSwePosition = xbPosition.addNewPosition();
        xbSwePosition.setFixed(position.isFixed());
        xbSwePosition.setReferenceFrame(position.getReferenceFrame());
        VectorType xbVector = xbSwePosition.addNewLocation().addNewVector();
        for (SosSweCoordinate coordinate : position.getPosition()) {
            if (coordinate.getValue().getValue() != null
                    && (!coordinate.getValue().getValue().isEmpty() || !coordinate.getValue().getValue()
                            .equals(Double.NaN))) {
                Swe101Encoder.addValuesToCoordinate(xbVector.addNewCoordinate(), coordinate);
            }
        }
    }

    /**
     * Creates the inputs section of the SensorML description.
     * 
     * @param xbInputs
     *            Xml inputs object
     * @param inputs
     *            SOS SWE representation.
     * @throws OwsExceptionReport 
     */
    private void createInputs(Inputs xbInputs, List<SosSMLIo> inputs) throws OwsExceptionReport {
        InputList xbInputList = xbInputs.addNewInputList();
        for (SosSMLIo sosSMLIo : inputs) {
            addIoComponentPropertyType(xbInputList.addNewInput(), sosSMLIo);
        }

    }

    /**
     * Creates the outputs section of the SensorML description.
     * 
     * @param xbOutputs
     *            Xml outputs object
     * @param outputs
     *            SOS SWE representation.
     * @throws OwsExceptionReport 
     */
    private void createOutputs(Outputs xbOutputs, List<SosSMLIo> outputs) throws OwsExceptionReport {
        OutputList xbOutputList = xbOutputs.addNewOutputList();
        for (SosSMLIo sosSMLIo : outputs) {
            addIoComponentPropertyType(xbOutputList.addNewOutput(), sosSMLIo);
        }

    }

    /**
     * Creates the components section of the SensorML description.
     * 
     * @param xbComponents
     *            Xml components object
     * @param sosComponents
     *            SOS SWE representation.
     */
    private void createComponents(Components xbComponents, List<SosSMLComponent> sosComponents) {
        ComponentList xbComList = xbComponents.addNewComponentList();
        for (SosSMLComponent sosSMLComponent : sosComponents) {
            xbComList.addNewComponent().setName(sosSMLComponent.getIdentifier());
        }
    }

    /**
     * Adds a SOS SWE simple type to a XML SWE field.
     * 
     * @param xbField
     *            XML SWE field
     * @param sosSweSimpleType
     *            SOS SWE simple type.
     * @throws OwsExceptionReport 
     */
    private void addSweSimpleTypeToField(AnyScalarPropertyType xbField, ISosSweSimpleType sosSweSimpleType) throws OwsExceptionReport {
        switch (sosSweSimpleType.getSimpleType()) {
        case Boolean:
            OwsExceptionReport seBool = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.Boolean.name() + "' is not supported by this SOS for SWE fields!");
            seBool.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.Boolean.name() + "' is not supported by this SOS for SWE fields!");
            throw seBool;
        case Category:
            OwsExceptionReport seCategory = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.Category.name() + "' is not supported by this SOS for SWE fields!");
            seCategory.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.Category.name() + "' is not supported by this SOS for SWE fields!");
            throw seCategory;
        case Count:
            OwsExceptionReport seCount = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.Count.name() + "' is not supported by this SOS for SWE fields!");
            seCount.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.Count.name() + "' is not supported by this SOS for SWE fields!");
            throw seCount;
        case CountRange:
            OwsExceptionReport seCountRange = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.CountRange.name() + "' is not supported by this SOS for SWE fields!");
            seCountRange.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.CountRange.name() + "' is not supported by this SOS for SWE fields!");
            throw seCountRange;
        case Quantity:
            OwsExceptionReport seQuantity = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.Quantity.name() + "' is not supported by this SOS for SWE fields!");
            seQuantity.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.Quantity.name() + "' is not supported by this SOS for SWE fields!");
            throw seQuantity;
        case QuantityRange:
            OwsExceptionReport seQuantityRange = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.QuantityRange.name() + "' is not supported by this SOS for SWE fields!");
            seQuantityRange.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.QuantityRange.name() + "' is not supported by this SOS for SWE fields!");
            throw seQuantityRange;
        case Text:
            Swe101Encoder.addValuesToSimpleTypeText(xbField.addNewText(), (SosSweText) sosSweSimpleType);
            break;
        case Time:
            OwsExceptionReport seTime = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.Time.name() + "' is not supported by this SOS for SWE fields!");
            seTime.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.Time.name() + "' is not supported by this SOS for SWE fields!");
            throw seTime;
        case TimeRange:
            OwsExceptionReport seTimeRange = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.TimeRange.name() + "' is not supported by this SOS for SWE fields!");
            seTimeRange.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.TimeRange.name() + "' is not supported by this SOS for SWE fields!");
            throw seTimeRange;
        default:
            OwsExceptionReport seDefault = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + sosSweSimpleType.getSimpleType().name() + "' is not supported by this SOS for SWE fields!");
            seDefault.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + sosSweSimpleType.getSimpleType().name() + "' is not supported by this SOS for SWE fields!");
            throw seDefault;
        }
    }

    /**
     * Adds a SOS SWE simple type to a XML SML IO component.
     * 
     * @param xbIoComponentPopertyType
     *            SML IO component
     * @param sosSMLInput
     *            SOS SWE simple type.
     * @throws OwsExceptionReport 
     */
    private void addIoComponentPropertyType(IoComponentPropertyType xbIoComponentPopertyType, SosSMLIo sosSMLIO) throws OwsExceptionReport {
        xbIoComponentPopertyType.setName(sosSMLIO.getIoName());
        switch (sosSMLIO.getIoValue().getSimpleType()) {
        case Boolean:
            OwsExceptionReport seBool = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.Boolean.name() + "' is not supported by this SOS for SensorML input/output!");
            seBool.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.Boolean.name() + "' is not supported by this SOS for SensorML input/output!");
            throw seBool;
        case Category:
            OwsExceptionReport seCategory = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.Category.name() + "' is not supported by this SOS for SensorML input/output!");
            seCategory.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.Category.name() + "' is not supported by this SOS for SensorML input/output!");
            throw seCategory;
        case Count:
            OwsExceptionReport seCount = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.Count.name() + "' is not supported by this SOS for SensorML input/output!");
            seCount.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.Count.name() + "' is not supported by this SOS for SensorML input/output!");
            throw seCount;
        case CountRange:
            OwsExceptionReport seCountRange = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.CountRange.name() + "' is not supported by this SOS for SensorML input/output!");
            seCountRange.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.CountRange.name() + "' is not supported by this SOS for SensorML input/output!");
            throw seCountRange;
        case ObservableProperty:
            xbIoComponentPopertyType.addNewObservableProperty().setDefinition(
                    ((SosSweObservableProperty) sosSMLIO.getIoValue()).getDefinition());
            break;
        case Quantity:
            Swe101Encoder.addValuesToSimpleTypeQuantity(xbIoComponentPopertyType.addNewQuantity(),
                    (SosSweQuantity) sosSMLIO.getIoValue());
            break;
        case QuantityRange:
            OwsExceptionReport seQuantityRange = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.QuantityRange.name() + "' is not supported by this SOS for SensorML input/output!");
            seQuantityRange.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.QuantityRange.name() + "' is not supported by this SOS for SensorML input/output!");
            throw seQuantityRange;
        case Text:
            OwsExceptionReport seText = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.Text.name() + "' is not supported by this SOS for SensorML input/output!");
            seText.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.Text.name() + "' is not supported by this SOS for SensorML input/output!");
            throw seText;
        case Time:
            OwsExceptionReport seTime = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.Time.name() + "' is not supported by this SOS for SensorML input/output!");
            seTime.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.Time.name() + "' is not supported by this SOS for SensorML input/output!");
            throw seTime;
        case TimeRange:
            OwsExceptionReport seTimeRange = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + SweSimpleType.TimeRange.name() + "' is not supported by this SOS for SensorML input/output!");
            seTimeRange.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + SweSimpleType.TimeRange.name() + "' is not supported by this SOS for SensorML input/output!");
            throw seTimeRange;
        default:
            OwsExceptionReport seDefault = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("The SWE simpleType '" + sosSMLIO.getIoValue().getSimpleType().name() + "' is not supported by this SOS for SensorML input/output!");
            seDefault.addCodedException(ExceptionCode.NoApplicableCode, null, "The SWE simpleType '" + sosSMLIO.getIoValue().getSimpleType().name() + "' is not supported by this SOS for SensorML input/output!");
            throw seDefault;
        }
    }

    /**
     * creates swe:Position element from passed JTS Point
     * 
     * @param point
     *            JTS point containing the coords for swe:POsition
     * @return Returns XMLBeans representation of swe:Position
     */
    // public PositionDocument createPosition(Point point) {
    public Position createPosition(Point point) {

        Position xb_pos = Position.Factory.newInstance();
        PositionType xb_posType = xb_pos.addNewPosition();

        xb_posType.setReferenceFrame(SosConfigurator.getInstance().getSrsNamePrefixSosV2() + point.getSRID());

        VectorPropertyType xb_location = xb_posType.addNewLocation();
        VectorType xb_vector = xb_location.addNewVector();

        Coordinate xb_coord = xb_vector.addNewCoordinate();
        xb_coord.setName("xcoord");
        Quantity xb_quantity = xb_coord.addNewQuantity();
        xb_quantity.setValue(point.getX());

        xb_coord = xb_vector.addNewCoordinate();
        xb_coord.setName("ycoord");
        xb_quantity = xb_coord.addNewQuantity();
        xb_quantity.setValue(point.getY());

        // return xb_posDoc;
        return xb_pos;
    }

    /**
     * checks the remaining heapsize and throws exception, if it is smaller than
     * 8 kB
     * 
     * @throws OwsExceptionReport
     *             if remaining heapsize is smaller than 8kb
     */
    private void checkFreeMemory() throws OwsExceptionReport {
        long freeMem;
        // check remaining free memory on heap
        // if too small, throw exception to avoid an OutOfMemoryError
        freeMem = Runtime.getRuntime().freeMemory();
        LOGGER.debug("Remaining Heap Size: " + freeMem);
        if (Runtime.getRuntime().totalMemory() == Runtime.getRuntime().maxMemory() && freeMem < 256000) { // 256000
            // accords
            // to
            // 256
            // kB
            // create service exception
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(
                    ExceptionCode.ResponseExceedsSizeLimit,
                    null,
                    "The describeSensor response is to big for the maximal heap size = "
                            + Runtime.getRuntime().maxMemory()
                            + " Byte of the virtual machine! "
                            + "Please either refine your describeSensor request to reduce the number of history sensor positions in the response or ask the administrator of this SOS to increase the maximum heap size of the virtual machine!");
            throw se;
        }
    }

}
