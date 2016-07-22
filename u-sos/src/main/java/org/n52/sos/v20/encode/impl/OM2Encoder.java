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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.AbstractTimeObjectType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.MeasureType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.sos.x20.GetObservationResponseDocument;
import net.opengis.sos.x20.GetObservationResponseType;
import net.opengis.swe.x20.BooleanType;
import net.opengis.swe.x20.CategoryType;
import net.opengis.swe.x20.CountPropertyType;
import net.opengis.swe.x20.CountType;
import net.opengis.swe.x20.DataArrayDocument;
import net.opengis.swe.x20.DataArrayType;
import net.opengis.swe.x20.DataArrayType.ElementType;
import net.opengis.swe.x20.DataArrayType.Encoding;
import net.opengis.swe.x20.DataRecordDocument;
import net.opengis.swe.x20.DataRecordType;
import net.opengis.swe.x20.DataRecordType.Field;
import net.opengis.swe.x20.EncodedValuesPropertyType;
import net.opengis.swe.x20.QualityPropertyType;
import net.opengis.swe.x20.QuantityType;
import net.opengis.swe.x20.Reference;
import net.opengis.swe.x20.TextEncodingType;
import net.opengis.swe.x20.TextType;
import net.opengis.swe.x20.TimeType;
import net.opengis.swe.x20.UnitReference;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.isotc211.x2005.gmd.DQElementPropertyType;
import org.joda.time.DateTime;
import org.n52.sos.Sos2Constants;
import org.n52.sos.SosConfigurator;
import org.n52.sos.SosConstants;
import org.n52.sos.SosConstants.ValueTypes;
import org.n52.sos.SosXmlUtilities;
import org.n52.sos.encode.IOMEncoder;
import org.n52.sos.ogc.om.AbstractSosObservation;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosCategoryObservation;
import org.n52.sos.ogc.om.SosGenericObservation;
import org.n52.sos.ogc.om.SosMeasurement;
import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.om.SosSWEArrayObservation;
import org.n52.sos.ogc.om.SosSpatialObservation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.quality.SosQuality;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.utilities.SosUtilities;

/**
 * Encoder for Observation and Measurements Documents / Elements, using
 * XMLBeans, implemented as singleton
 * 
 * ATTENTION: This class needs the XmlBeans lib and the libs generated from the
 * O&M schema files; the used classes which are generated from XMLBeans are
 * named with prefixed 'xb_'
 * 
 * @author Christoph Stasch
 * 
 */
public class OM2Encoder implements IOMEncoder {

    /** logger */
    private static final Logger LOGGER = Logger.getLogger(OM2Encoder.class);

    private int sfIdCounter = 1;

    private HashMap<String, String> gmlID4sfIdentifier;

    /**
     * constructor
     */
    public OM2Encoder() {
        super();
        this.gmlID4sfIdentifier = new HashMap<String, String>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.encode.IOMEncoder#createObservationCollection(org.n52.sos
     * .ogc.om.SosObservationCollection)
     */
    @Override
    public XmlObject createObservationCollection(SosObservationCollection sosObsCol) throws OwsExceptionReport {
        // reset spatialFeatureID counter and spatialFeatureIdentifier/gmlID map
        gmlID4sfIdentifier.clear();
        sfIdCounter = 1;
        // create XML Document
        GetObservationResponseDocument xbGetObsRespDoc =
                GetObservationResponseDocument.Factory.newInstance(SosXmlUtilities.getInstance().getXmlOptions4Sos2Swe200());
        GetObservationResponseType xbGetObsResp = xbGetObsRespDoc.addNewGetObservationResponse();
        if (sosObsCol.getObservationMembers() != null && !sosObsCol.getObservationMembers().isEmpty()) {
            Iterator<AbstractSosObservation> obsIter = sosObsCol.getObservationMembers().iterator();
            while (obsIter.hasNext()) {
                OMObservationType xbObs = xbGetObsResp.addNewObservationData().addNewOMObservation();
                AbstractSosObservation sosObs = obsIter.next();
                createObservation(xbObs, sosObs);
            }
        }
        return xbGetObsRespDoc;
    }

    /**
     * Creates XML representation of OM 2.0 observation type from SOS
     * observation.
     * 
     * @param xbObs
     *            OM 2.0 observation
     * @param absObs
     *            SOS observation
     * @throws OwsExceptionReport
     *             if an error occurs during creation.
     */
    private void createObservation(OMObservationType xbObs, AbstractSosObservation absObs) throws OwsExceptionReport {

        if (absObs instanceof SosMeasurement) {
            createMeasurement(xbObs, (SosMeasurement) absObs);
        } else if (absObs instanceof SosCategoryObservation) {
            createCategoryObservation(xbObs, (SosCategoryObservation) absObs);
        } else if (absObs instanceof SosGenericObservation) {
            createGenericObservation(xbObs, (SosGenericObservation) absObs);
        } else if (absObs instanceof SosSpatialObservation) {
            createSpatialObservation(xbObs, (SosSpatialObservation) absObs);
        } else if (absObs instanceof SosSWEArrayObservation) {
            createSWEDataArrayObservation(xbObs, (SosSWEArrayObservation) absObs);
        }

    }

    /**
     * Creates XML representation of OM 2.0 Measurement from SOS observation.
     * 
     * @param xbObs
     *            OM 2.0 Measurement
     * @param meas
     *            SOS Measurement
     * @throws OwsExceptionReport
     *             if an error occurs during creation.
     */
    private void createMeasurement(OMObservationType xbObs, SosMeasurement meas) throws OwsExceptionReport {
        String observationID = SosConstants.OBS_ID_PREFIX + meas.getObservationID();
        xbObs.setId(observationID);
        xbObs.addNewType().setHref(OMConstants.OBS_TYPE_MEASUREMENT);

        // set eventTime
        String phenTimeId = "phenomenonTime_" + observationID;
        // xbObs.addNewPhenomenonTime().set(
        // SosConfigurator.getInstance().getGml321Encoder().createTime(meas.getSamplingTime(),
        // phenTimeId));
        AbstractTimeObjectType xbAbsTimeObject = xbObs.addNewPhenomenonTime().addNewAbstractTimeObject();
        SosConfigurator.getInstance().getGml321Encoder()
                .createTime(meas.getSamplingTime(), phenTimeId, xbAbsTimeObject);
        xbObs.addNewResultTime().setHref("#" + phenTimeId);

        // set procedure
        xbObs.addNewProcedure().setHref(meas.getProcedureID());

        // set observedProperty (phenomenon)
        xbObs.addNewObservedProperty().setHref(meas.getPhenomenonID());

        // set feature
        encodeFeatureOfInterest(xbObs, meas);

        // Currently not used for SOS 2.0 and OM 2.0 encoding.
        // // add quality, if set
        if (meas.getQuality() != null) {
            DQElementPropertyType xbQuality = xbObs.addNewResultQuality();
            xbQuality.set(createQualityProperty(meas.getQuality()));
        }

        // set result
        XmlObject xbMeasResult = xbObs.addNewResult();
        MeasureType xb_result = MeasureType.Factory.newInstance();
        if (meas.getUnitsOfMeasurement() != null) {
            xb_result.setUom(meas.getUnitsOfMeasurement());
        } else {
            xb_result.setUom("");
        }

        if (!Double.valueOf(meas.getValue()).equals(Double.NaN)) {
            xb_result.setStringValue("" + meas.getValue());
        }

        else {
            xb_result.setNil();
        }
        xbMeasResult.set(xb_result);

    }

    /**
     * Creates XML representation of OM 2.0 CategoryObservation from SOS
     * observation.
     * 
     * @param xbObs
     *            OM 2.0 CategoryObservation
     * @param catObs
     *            SOS CategoryObservation
     * @throws OwsExceptionReport
     *             if an error occurs during creation.
     */
    private void createCategoryObservation(OMObservationType xbObs, SosCategoryObservation catObs)
            throws OwsExceptionReport {
        String observationID = SosConstants.OBS_ID_PREFIX + catObs.getObservationID();
        xbObs.setId(observationID);
        xbObs.addNewType().setHref(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION);

        // set eventTime
        String phenTimeId = "phenomenonTime_" + observationID;
        // xbObs.addNewPhenomenonTime().set(
        // SosConfigurator.getInstance().getGml321Encoder().createTime(catObs.getSamplingTime(),
        // phenTimeId));
        AbstractTimeObjectType xbAbsTimeObject = xbObs.addNewPhenomenonTime().addNewAbstractTimeObject();
        SosConfigurator.getInstance().getGml321Encoder()
                .createTime(catObs.getSamplingTime(), phenTimeId, xbAbsTimeObject);
        xbObs.addNewResultTime().setHref("#" + phenTimeId);

        // set procedure
        xbObs.addNewProcedure().setHref(catObs.getProcedureID());

        // set observedProperty (phenomenon)
        xbObs.addNewObservedProperty().setHref(catObs.getPhenomenonID());

        // set feature
        encodeFeatureOfInterest(xbObs, catObs);

        // Currently not used for SOS 2.0 and OM 2.0 encoding.
        // // add quality, if set
        // if (catObs.getQuality() != null) {
        // DQElementPropertyType xbQuality = xbObs.addNewResultQuality();
        // xbQuality.set(createQualityProperty(catObs.getQuality()));
        // }

        // set result
        XmlObject xbRresult = xbObs.addNewResult();
        Reference xbRef = Reference.Factory.newInstance();
        xbRef.setHref(catObs.getTextValue());
        xbRresult.set(xbRef);

    }

    /**
     * Creates XML representation of OM 2.0 GeometryObservation from SOS
     * observation.
     * 
     * @param xbObs
     *            OM 2.0 SosSpatialObservation
     * @param spatialObs
     *            SOS SosSpatialObservation
     * @throws OwsExceptionReport
     *             if an error occurs during creation.
     */
    private void createSpatialObservation(OMObservationType xbObs, SosSpatialObservation spatialObs)
            throws OwsExceptionReport {
        String observationID = SosConstants.OBS_ID_PREFIX + spatialObs.getObservationID();
        xbObs.setId(observationID);
        xbObs.addNewType().setHref(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION);

        // set eventTime
        String phenTimeId = "phenomenonTime_" + observationID;
        // xbObs.addNewPhenomenonTime().set(
        // SosConfigurator.getInstance().getGml321Encoder().createTime(spatialObs.getSamplingTime(),
        // phenTimeId));
        AbstractTimeObjectType xbAbsTimeObject = xbObs.addNewPhenomenonTime().addNewAbstractTimeObject();
        SosConfigurator.getInstance().getGml321Encoder()
                .createTime(spatialObs.getSamplingTime(), phenTimeId, xbAbsTimeObject);
        xbObs.addNewResultTime().setHref("#" + phenTimeId);

        // set procedure
        xbObs.addNewProcedure().setHref(spatialObs.getProcedureID());

        // set observedProperty (phenomenon)
        xbObs.addNewObservedProperty().setHref(spatialObs.getPhenomenonID());

        // set feature
        encodeFeatureOfInterest(xbObs, spatialObs);

        // Currently not used for SOS 2.0 and OM 2.0 encoding.
        // // add quality, if set
        // if (spatialObs.getQuality() != null) {
        // DQElementPropertyType xbQuality = xbObs.addNewResultQuality();
        // xbQuality.set(createQualityProperty(spatialObs.getQuality()));
        // }

        // set result
        // xbObs.addNewResult().set(
        SosConfigurator
                .getInstance()
                .getGml321Encoder()
                .createPosition(spatialObs.getResult(), xbObs.addNewResult(),
                        SosConstants.OBS_ID_PREFIX + spatialObs.getObservationID());

    }

    /**
     * Creates XML representation of OM 2.0 GenericObservation from SOS
     * observation.
     * 
     * @param xbObs
     *            OM 2.0 GenericObservation
     * @param genObs
     *            SOS GenericObservation
     * @throws OwsExceptionReport
     *             if an error occurs during creation.
     */
    private void createGenericObservation(OMObservationType xbObs, SosGenericObservation genObs)
            throws OwsExceptionReport {
        String observationID = SosConstants.OBS_ID_PREFIX + genObs.getObservationID();
        xbObs.setId(observationID);
        xbObs.addNewType().setHref(OMConstants.OBS_TYPE_SWE_OBSERVATION);

        // set eventTime
        String phenTimeId = "phenomenonTime_" + observationID;
        AbstractTimeObjectType xbAbsTimeObject = xbObs.addNewPhenomenonTime().addNewAbstractTimeObject();
        SosConfigurator.getInstance().getGml321Encoder()
                .createTime(genObs.getSamplingTime(), phenTimeId, xbAbsTimeObject);
        xbObs.addNewResultTime().setHref("#" + phenTimeId);

        // set procedure
        xbObs.addNewProcedure().setHref(genObs.getProcedureID());

        // set observedProperty (phenomenon)
        if (genObs.getPhenomenonID() != null) {
            xbObs.addNewObservedProperty().setHref(genObs.getPhenomenonID());
        } else if (genObs.getPhenComponents() != null) {
            for (String phenomenon : genObs.getPhenComponents()) {
                xbObs.addNewObservedProperty().setHref(phenomenon);
            }
        }

        // set feature
        encodeFeatureOfInterest(xbObs, genObs);

        // set result
        XmlObject xbRresult = xbObs.addNewResult();
        xbRresult.set(createDataArrayResult(genObs.getPhenComponents(), genObs.createResultString(),
                genObs.getTupleCount()));
        XmlCursor cursor = xbRresult.newCursor();
        cursor.setAttributeText(new QName(OMConstants.NS_XSI, "type"), "swe:DataArrayPropertyType");

    }

    /**
     * Creates a SWE 1.0.1 DataArray for the result element in
     * GenericObservation
     * 
     * @param phenComponents
     *            List of all phenomenon of this observation
     * @param resultString
     *            Value string
     * @param count
     *            Count of values
     * @return SWE DataArray
     */
    private XmlObject createDataArrayResult(List<String> phenComponents, String resultString, int count) {

        // create DataArray
        DataArrayDocument xbDataArrayDoc = DataArrayDocument.Factory.newInstance();
        DataArrayType xbDataArray = xbDataArrayDoc.addNewDataArray1();

        // set element count
        CountPropertyType xbElementCount = xbDataArray.addNewElementCount();
        xbElementCount.addNewCount().setValue(BigInteger.valueOf(count));

        // create data definition
        ElementType xb_elementType = xbDataArray.addNewElementType();

        DataRecordDocument xb_dataRecordDoc = DataRecordDocument.Factory.newInstance();
        DataRecordType xb_dataRecord = xb_dataRecordDoc.addNewDataRecord();

        // add time component
        Field xb_field = xb_dataRecord.addNewField();
        xb_field.setName("SamplingTime");

        TimeType xbTimeComponent = TimeType.Factory.newInstance();
        xbTimeComponent.setDefinition(OMConstants.PHEN_SAMPLING_TIME);
        xbTimeComponent.addNewUom().setHref(OMConstants.PHEN_UOM_ISO8601);
        xb_field.setAbstractDataComponent(xbTimeComponent);

        // add foi
        xb_field = xb_dataRecord.addNewField();
        xb_field.setName("FeatureOfInterest");
        TextType xbFoiText = TextType.Factory.newInstance();
        xbFoiText.setDefinition(OMConstants.PHEN_FEATURE_OF_INTEREST);
        xb_field.setAbstractDataComponent(xbFoiText);

        // add phenomenon components
        Map<String, ValueTypes> valueTypes4phens =
                SosConfigurator.getInstance().getCapsCacheController().getValueTypes4ObsProps();
        for (String phenComponent : phenComponents) {

            ValueTypes valueType = valueTypes4phens.get(phenComponent);
            if (valueType != null) {
                switch (valueType) {
                case booleanType: {
                    xb_field = xb_dataRecord.addNewField();
                    BooleanType xbBool = BooleanType.Factory.newInstance();
                    xbBool.setDefinition(phenComponent);
                    xb_field.setAbstractDataComponent(xbBool);
                    break;
                }
                case countType: {
                    xb_field = xb_dataRecord.addNewField();
                    CountType xbCount = CountType.Factory.newInstance();
                    xbCount.setDefinition(phenComponent);
                    xb_field.setAbstractDataComponent(xbCount);
                    break;
                }
                case numericType: {
                    xb_field = xb_dataRecord.addNewField();
                    QuantityType xbQuantity = QuantityType.Factory.newInstance();
                    xbQuantity.setDefinition(phenComponent);
                    UnitReference xb_uom = xbQuantity.addNewUom();
                    xb_uom.setCode(SosConfigurator.getInstance().getCapsCacheController()
                            .getUnit4ObsProp(phenComponent));
                    xb_field.setAbstractDataComponent(xbQuantity);
                    break;
                }
                case isoTimeType: {
                    xb_field = xb_dataRecord.addNewField();
                    TimeType xbTime = TimeType.Factory.newInstance();
                    xbTime.setDefinition(phenComponent);
                    xbTime.addNewUom().setHref(OMConstants.PHEN_UOM_ISO8601);
                    xb_field.setAbstractDataComponent(xbTime);
                    break;
                }
                case textType: {
                    xb_field = xb_dataRecord.addNewField();
                    TextType xbText = TextType.Factory.newInstance();
                    xbText.setDefinition(phenComponent);
                    xb_field.setAbstractDataComponent(xbText);
                    break;
                }
                case categoryType: {
                    xb_field = xb_dataRecord.addNewField();
                    CategoryType xbCategory = CategoryType.Factory.newInstance();
                    xbCategory.setDefinition(phenComponent);
                    xb_field.setAbstractDataComponent(xbCategory);
                    break;
                }
                default:
                    xb_field = xb_dataRecord.addNewField();
                    TextType xbText = TextType.Factory.newInstance();
                    xbText.setDefinition(phenComponent);
                    xb_field.setAbstractDataComponent(xbText);
                    break;
                }
                String[] uriParts = phenComponent.split("/|:");
                xb_field.setName(uriParts[uriParts.length - 1]);
            } else {
                xb_field = xb_dataRecord.addNewField();
                xb_field.setName(phenComponent.replace(SosConstants.PHENOMENON_PREFIX, ""));
                TextType xbText = TextType.Factory.newInstance();
                xbText.setDefinition(phenComponent);
                xb_field.setAbstractDataComponent(xbText);
            }

        }

        // set components to SimpleDataRecord
        xb_elementType.set(xb_dataRecordDoc);
        xb_elementType.setName("Components");

        // add encoding element
        Encoding xb_encoding = xbDataArray.addNewEncoding();
        TextEncodingType xb_textBlock = TextEncodingType.Factory.newInstance();

        xb_textBlock.setDecimalSeparator(SosConfigurator.getInstance().getDecimalSeparator());
        xb_textBlock.setTokenSeparator(SosConfigurator.getInstance().getTokenSeperator());
        xb_textBlock.setBlockSeparator(SosConfigurator.getInstance().getTupleSeperator());
        xb_encoding.setAbstractEncoding(xb_textBlock);

        EncodedValuesPropertyType xb_values = xbDataArray.addNewValues();
        xb_values.newCursor().setTextValue(resultString);

        return xbDataArrayDoc;
    }

    /**
     * Creates XML representation of OM 2.0 SWEDataArrayObservation from SOS
     * observation.
     * 
     * @param xbObs
     *            OM 2.0 SWEDataArrayObservation
     * @param meas
     *            SOS SWEDataArrayObservation
     * @throws OwsExceptionReport
     *             if an error occurs during creation.
     */
    private void createSWEDataArrayObservation(OMObservationType xbObs, SosSWEArrayObservation sweDataArrayObs)
            throws OwsExceptionReport {
        String obsID = null;
        if (sweDataArrayObs.getObservationID() != null) {
            obsID = sweDataArrayObs.getObservationID();
        } else {
            obsID = Long.toString(new DateTime().getMillis());
        }
        xbObs.setId(sweDataArrayObs.getOfferingID() + "_" + obsID);
        xbObs.addNewType().setHref(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);

        // set eventTime
        String phenTimeId = "phenomenonTime_" + obsID;
        AbstractTimeObjectType xbAbsTimeObject = xbObs.addNewPhenomenonTime().addNewAbstractTimeObject();
        SosConfigurator.getInstance().getGml321Encoder()
                .createTime(sweDataArrayObs.getSamplingTime(), phenTimeId, xbAbsTimeObject);
        xbObs.addNewResultTime().setHref("#" + phenTimeId);

        // set procedure
        xbObs.addNewProcedure().setHref(sweDataArrayObs.getProcedureID());

        // set observedProperty (phenomenon)
        if (sweDataArrayObs.getPhenomenonID() != null) {
            xbObs.addNewObservedProperty().setHref(sweDataArrayObs.getPhenomenonID());
        }

        // set feature
        encodeFeatureOfInterest(xbObs, sweDataArrayObs);

        // set result
        XmlObject xbRresult = xbObs.addNewResult();
        xbRresult.set(createDataArrayForSWEDataArrayObservation(sweDataArrayObs.getValue()));
        XmlCursor cursor = xbRresult.newCursor();
        cursor.setAttributeText(new QName(OMConstants.NS_XSI, "type"), "swe:DataArrayPropertyType");

    }

    /**
     * Creates a dataArray for the SWEArrayObservation.
     * 
     * @param sosSweDataArray
     *            SOS internal representation
     * @return DataArray XmlObject representation
     * @throws OwsExceptionReport 
     */
    private DataArrayDocument createDataArrayForSWEDataArrayObservation(SosSweDataArray sosSweDataArray) throws OwsExceptionReport {

        // create DataArray
        DataArrayDocument xbDataArrayDoc = DataArrayDocument.Factory.newInstance();
        DataArrayType xbDataArray = xbDataArrayDoc.addNewDataArray1();

        // set element count
        CountPropertyType xbElementCount = xbDataArray.addNewElementCount();
        xbElementCount.addNewCount().setValue(BigInteger.valueOf(sosSweDataArray.getElementCount()));

        // create data definition
        ElementType xbElementType = xbDataArray.addNewElementType();

        // add phenomenon components
        List<String> fieldElementDefinition = new ArrayList<String>();
        if (sosSweDataArray.getElementType() instanceof SosSweDataRecord) {
            DataRecordDocument xbDataRecordDoc = DataRecordDocument.Factory.newInstance();
            DataRecordType xbDataRecord = xbDataRecordDoc.addNewDataRecord();

            for (SosSweField field : sosSweDataArray.getElementType().getFields()) {
                Field xbField = xbDataRecord.addNewField();
                if (field.getName() != null && !field.getName().isEmpty()) {
                    xbField.setName(field.getName());
                } else {
                    if (field.getElement().getDefinition() != null && !field.getElement().getDefinition().isEmpty()) {
                        String[] uriParts = field.getElement().getDefinition().split("/|:");
                        xbField.setName(uriParts[uriParts.length - 1]);
                    }
                }
                fieldElementDefinition.add(field.getElement().getDefinition());
                xbField.setAbstractDataComponent(Swe200Encoder.addSweSimpleTypToField(field.getElement()));
            }
            xbElementType.set(xbDataRecordDoc);
        }

        // set components to SimpleDataRecord
        xbElementType.setName("Components");

        // add encoding element
        Encoding xbEncoding = xbDataArray.addNewEncoding();
        TextEncodingType xbTextBlock = TextEncodingType.Factory.newInstance();
        xbTextBlock.setDecimalSeparator(SosConfigurator.getInstance().getDecimalSeparator());
        xbTextBlock.setTokenSeparator(SosConfigurator.getInstance().getTokenSeperator());
        xbTextBlock.setBlockSeparator(SosConfigurator.getInstance().getTupleSeperator());
        xbEncoding.setAbstractEncoding(xbTextBlock);

        EncodedValuesPropertyType xbValues = xbDataArray.addNewValues();
        StringBuffer resultString = new StringBuffer();
        for (Map<String, String> blockValues : sosSweDataArray.getValues()) {
            for (String definition : fieldElementDefinition) {
                resultString.append(blockValues.get(definition));
                resultString.append(SosConfigurator.getInstance().getTokenSeperator());
            }
            resultString.deleteCharAt(resultString.lastIndexOf(SosConfigurator.getInstance().getTokenSeperator()));
            resultString.append(SosConfigurator.getInstance().getTupleSeperator());
        }
        resultString.deleteCharAt(resultString.lastIndexOf(SosConfigurator.getInstance().getTupleSeperator()));
        xbValues.newCursor().setTextValue(resultString.toString());
        return xbDataArrayDoc;
    }

    /**
     * creates a QuantityProperty representing the quality element for
     * observations
     * 
     * @param qualityColl
     *            The Collection of qualities for the Observation.
     * @return Returns XMLBean representing the quality element for observations
     */
    public static QualityPropertyType createQualityProperty(Collection<SosQuality> qualityColl) {
        Iterator<SosQuality> qualityIter = qualityColl.iterator();
        QualityPropertyType xbQpt = QualityPropertyType.Factory.newInstance();
        while (qualityIter.hasNext()) {
            SosQuality sosQuality = qualityIter.next();
            switch (sosQuality.getQualityType()) {
            case text:
                TextType xbText = xbQpt.addNewText();
                xbText.set(createQualityText(sosQuality));
                break;

            default:
                TextType xbText2 = xbQpt.addNewText();
                xbText2.set(createQualityText(sosQuality));
                break;
            }
        }
        return xbQpt;
    }

    /**
     * creates a Text representing the quality element for observations in case
     * of quality type equals 'text'
     * 
     * @param qualityType
     *            the quality, for which the quality element should be created
     * @return Returns XMLBean representing the quality element for observations
     */
    public static TextType createQualityText(SosQuality sosQuality) {
        // new Text
        TextType xbText = TextType.Factory.newInstance();
        // quality name
        xbText.setDefinition(sosQuality.getResultName());
        // quality value
        xbText.setValue(sosQuality.getResultValue());
        return xbText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.encode.IOMEncoder#createObservationCollectionMobile(org.n52
     * .sos.ogc.om.SosObservationCollection)
     */
    @Override
    public XmlObject createObservationCollectionMobile(SosObservationCollection sosObsCol) throws OwsExceptionReport {
        GetObservationResponseDocument xbGetObsRespDoc =
                GetObservationResponseDocument.Factory.newInstance(SosXmlUtilities.getInstance().getXmlOptions4Sos2Swe200());
        return xbGetObsRespDoc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.encode.IOMEncoder#createCompositePhenomenon(java.lang.String,
     * java.util.List)
     */
    @Override
    public XmlObject createCompositePhenomenon(String compPhenId, Collection<String> phenComponents) {
        // Currently not used for SOS 2.0 and OM 2.0 encoding.
        return null;
    }

    /**
     * Encodes a SosAbstractFeature to an SpatialSamplingFeature under
     * consideration of duplicated SpatialSamplingFeature in the XML document.
     * 
     * @param xbObs
     *            XmlObject O&M observation
     * @param absObs
     *            SOS observation
     */
    private void encodeFeatureOfInterest(OMObservationType xbObs, AbstractSosObservation absObs) {

        FeaturePropertyType xbFoiType = xbObs.addNewFeatureOfInterest();
        if (!SosConfigurator.getInstance().isFoiEncodedInObservation()) {
            xbFoiType.setHref(SosUtilities.createFoiGetUrl(absObs.getFeatureOfInterestID(),
                    Sos2Constants.SERVICEVERSION));
        } else {
            if (absObs.getFeatureOfInterest() != null) {
                // if identifier of feature is set, check whether foi has been
                // already encoded
                String identifier = null;
                if (absObs.getFeatureOfInterest().getId() != null && !absObs.getFeatureOfInterest().getId().isEmpty()) {
                    identifier = absObs.getFeatureOfInterest().getId();
                } else if (absObs.getFeatureOfInterest().getName() != null && !absObs.getFeatureOfInterest().getId().isEmpty()) {
                    identifier = absObs.getFeatureOfInterest().getName();
                } else {
                    identifier = Long.toString(new DateTime().getMillis());
                }
                if (gmlID4sfIdentifier.containsKey(identifier)) {
                    xbFoiType.setHref("#" + gmlID4sfIdentifier.get(identifier));
                } else {
                    String gmlId = "sf_" + sfIdCounter;
                    sfIdCounter++;
                    xbFoiType.set(SosConfigurator.getInstance().getFeatureEncoderV2()
                            .createSpatialSamplingFeature(absObs.getFeatureOfInterest(), gmlId));
                    gmlID4sfIdentifier.put(identifier, gmlId);
                }
            } else if (absObs instanceof SosGenericObservation) {
                SosGenericObservation genObs = (SosGenericObservation) absObs;
                if (genObs.getFois() != null) {
                    Map<SosAbstractFeature, String> foiGmlIds = new HashMap<SosAbstractFeature, String>();
                    for (SosAbstractFeature feature : genObs.getFois()) {
                        String identifier = null;
                        if (feature.getId() != null && !feature.getId().isEmpty()) {
                            identifier = feature.getId();
                        } else if (feature.getName() != null && !feature.getId().isEmpty()) {
                            identifier = feature.getName();
                        } else {
                            identifier = Long.toString(new DateTime().getMillis());
                        }
                        if (gmlID4sfIdentifier.containsKey(identifier)) {
                            foiGmlIds.put(feature, "#" + gmlID4sfIdentifier.get(identifier));
                        } else {
                            String gmlId = "sf_" + sfIdCounter;
                            sfIdCounter++;
                            gmlID4sfIdentifier.put(identifier, gmlId);
                            foiGmlIds.put(feature, gmlId);
                        }
                    }
                    xbFoiType.set(SosConfigurator.getInstance().getFeatureEncoderV2()
                            .createSamplingFeatureCollection(foiGmlIds));
                }
            }
        }
    }

}