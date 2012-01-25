/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software 
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24, 
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.sta.wps.sos;

import static org.uncertweb.utils.UwXmlUtils.Namespace.OM;
import static org.uncertweb.utils.UwXmlUtils.Namespace.SML;
import static org.uncertweb.utils.UwXmlUtils.Namespace.SML_VERSION;
import static org.uncertweb.utils.UwXmlUtils.Namespace.SWE;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.opengis.gml.MetaDataPropertyType;
import net.opengis.gml.TimePeriodType;
import net.opengis.om.x10.MeasurementType;
import net.opengis.sensorML.x101.AbstractComponentType;
import net.opengis.sensorML.x101.AbstractProcessType;
import net.opengis.sensorML.x101.AbstractPureProcessType;
import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.InputsDocument.Inputs.InputList;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.OutputsDocument.Outputs.OutputList;
import net.opengis.sensorML.x101.ParametersDocument.Parameters.ParameterList;
import net.opengis.sensorML.x101.PositionDocument.Position;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.sensorML.x101.TermDocument.Term;
import net.opengis.sos.x10.ObservationTemplateDocument.ObservationTemplate;
import net.opengis.sos.x10.RegisterSensorDocument;
import net.opengis.sos.x10.RegisterSensorDocument.RegisterSensor;
import net.opengis.sos.x10.RegisterSensorDocument.RegisterSensor.SensorDescription;
import net.opengis.swe.x101.AbstractDataComponentType;
import net.opengis.swe.x101.AbstractDataRecordType;
import net.opengis.swe.x101.BooleanDocument.Boolean;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.DataRecordType;
import net.opengis.swe.x101.PositionType;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.TimeDocument.Time;
import net.opengis.swe.x101.TimeRangeDocument.TimeRange;
import net.opengis.swe.x101.VectorType;
import net.opengis.swe.x101.VectorType.Coordinate;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.om.Origin;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.utils.UwConstants;
import org.uncertweb.utils.UwTimeUtils;

/**
 * TODO JavaDoc
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class RegisterSensorBuilder {

	public static final String SWE_DATA_ARRAY_BLOCK_SEPERATOR = ";";
	public static final String SWE_DATA_ARRAY_TOKEN_SEPERATOR = ",";
	public static final String SWE_DATA_ARRAY_DECIMAL_SEPERATOR = ".";
	public static final String FIELD_NAME_BBOX = "observedBBOX";
    public static final String ELEMENT_NAME_OFFERING = "offering";
    public static final String ELEMENT_NAME_ID = "id";
    public static final String ELEMENT_NAME_NAME = "name";
    public static final String FIELD_NAME_STATUS = "status";
    public static final String ATTRIBUTE_NAME_UOM = "uom";
	
    public static final String COORD_NAME_LAT = "latitude";
    public static final String COORD_NAME_LON = "longitude";
    public static final String QUANTITY_AXIS_ID_LAT = "y";
    public static final String QUANTITY_AXIS_ID_LON = "x";
    public static final String QUANTITY_AXIS_ID_ALTITUDE = "z";
    public static final String COORD_NAME_ALTITUDE = "altitude";
    public static final String EPSG_4326_REFERENCE_SYSTEM_DEFINITION = UwConstants.URN.EPSG_SRS_PREFIX.value + "4326";
    public static final String COORDINATE_UOM = "degree";
    public static final String METER_UOM = "m";	
    public static final double lat = 0, lon = 0, alt = 0;
    
    protected static final Logger log = LoggerFactory.getLogger(RegisterSensorBuilder.class);
	private static RegisterSensorBuilder singleton;

	public static RegisterSensorBuilder getInstance() {
		if (singleton == null) {
			singleton = new RegisterSensorBuilder();
		}
		return singleton;
	}
	
	private RegisterSensorBuilder(){}
	
	public RegisterSensorDocument build(URI process, List<AbstractObservation> obs, Map<String,Object> meta) {
		RegisterSensorDocument regSenDoc = RegisterSensorDocument.Factory.newInstance();
		RegisterSensor regSen = regSenDoc.addNewRegisterSensor();
		regSen.setService(Constants.Sos.SERVICE_NAME);
		regSen.setVersion(Constants.Sos.SERVICE_VERSION);
		buildSensorDescription(regSen, process, obs, meta);
		buildObservationTemplate(regSen);
		return regSenDoc;
	}
	
	protected void buildParameters(AbstractPureProcessType pmt, Map<String,Object> meta) {
		if (meta.isEmpty()) return;
		ParameterList pl = pmt.addNewParameters().addNewParameterList();
		for (Entry<String,Object> e : meta.entrySet()) {
			DataComponentPropertyType field = pl.addNewParameter();
			Object o = e.getValue();
			AbstractDataComponentType adrt = null;
			if (o instanceof java.lang.Boolean) {
				adrt = field.addNewBoolean();
		        ((Boolean) adrt).setValue((java.lang.Boolean) o);
			} else if (o instanceof DateTime) {
				adrt = field.addNewTime();
				((Time) adrt).setValue(UwTimeUtils.format((DateTime) o));
			} else if (o instanceof Interval) {
				adrt = field.addNewTimeRange();
				Interval i = (Interval) o;
				((TimeRange) adrt).setValue(UwCollectionUtils.list(
						UwTimeUtils.format(i.getStart()),
						UwTimeUtils.format(i.getEnd())));
			} else if (o instanceof Class) {
				adrt = field.addNewText();
				((Text) adrt).setValue(((Class<?>) o).getName());
			} else {
				if (!(o instanceof String)) {
					log.warn("Can not handle {}. Defaulting to String.", o.getClass());
				}
				adrt = field.addNewText();
				((Text) adrt).setValue(o.toString());
			}
			adrt.setDefinition(Constants.Sos.ProcessDescription.Parameter.URN_PREFIX + e.getKey());
		}
	}
	
	protected void buildSensorDescription(RegisterSensor regSen,
			URI process, List<AbstractObservation> obs, Map<String,Object> meta) {
		SensorDescription description = regSen.addNewSensorDescription();
		SensorMLDocument smlDocument = SensorMLDocument.Factory.newInstance();
		SensorML sml = smlDocument.addNewSensorML();
		sml.setVersion(SML_VERSION);
		
		SystemType processType = (SystemType) sml.addNewMember().addNewProcess().substitute(SML.q("System"), SystemType.type);
		buildPosition(processType);

//		ProcessModelType processType = (ProcessModelType) sml.addNewMember().addNewProcess().substitute(qualify(SML, "ProcessModel"), ProcessModelType.type);
//		buildParameters(processType, meta);
		
		/* unique id */
		IdentifierList idenList = processType.addNewIdentification().addNewIdentifierList();
		Identifier ident = idenList.addNewIdentifier();
		Term term = ident.addNewTerm();
		term.setDefinition(UwConstants.URN.OGC_UNIQUE_ID_DEFINITION.value);
		term.setValue(process.toString());

		processType.addNewDescription().setStringValue(Constants.Sos.ProcessDescription.SENSOR_DESCRIPTION);
		buildValidTime(processType, obs);
		buildCapabilities(processType);
		/* TODO additional SensorML information
		 * build keywords 
		 * build contact: no idea... maybe provided as an additional input? 
		 */
		
		buildInputOutputLists(processType, obs);
		description.set(smlDocument);
	}
    
	protected void buildPosition(SystemType systemType) {
		Position position = systemType.addNewPosition();
		position.setName("sensorPosition");
		PositionType positionType = (PositionType) position.addNewProcess()
				.substitute(SWE.q("Position"), PositionType.type);
		positionType.setReferenceFrame(EPSG_4326_REFERENCE_SYSTEM_DEFINITION);
		positionType.setFixed(true);
		VectorType vector = positionType.addNewLocation().addNewVector();

		/* Latitude */
		Coordinate coordLatitude = vector.addNewCoordinate();
		coordLatitude.setName(COORD_NAME_LAT);
		Quantity quantityLatitude = coordLatitude.addNewQuantity();
		quantityLatitude.setAxisID(QUANTITY_AXIS_ID_LAT);
		quantityLatitude.addNewUom().setCode(COORDINATE_UOM);
		quantityLatitude.setValue(lat);

		/* Longitude */
		Coordinate coordLongitude = vector.addNewCoordinate();
		coordLongitude.setName(COORD_NAME_LON);
		Quantity quantityLongitude = coordLongitude.addNewQuantity();
		quantityLongitude.setAxisID(QUANTITY_AXIS_ID_LON);
		quantityLongitude.addNewUom().setCode(COORDINATE_UOM);
		quantityLongitude.setValue(lon);

		/* Altitude */
		Coordinate coordAltitude = vector.addNewCoordinate();
		coordAltitude.setName(COORD_NAME_ALTITUDE);
		Quantity quantityAltitude = coordAltitude.addNewQuantity();
		quantityAltitude.setAxisID(QUANTITY_AXIS_ID_ALTITUDE);
		quantityAltitude.addNewUom().setCode(METER_UOM);
		quantityAltitude.setValue(alt);
	}


	protected void buildInputOutputLists(XmlObject systemType, List<AbstractObservation> obs) {
		InputList inputList = null;
		OutputList outputList = null;
		if (systemType instanceof AbstractPureProcessType) {
			 inputList = ((AbstractPureProcessType) systemType).addNewInputs().addNewInputList();
			 outputList = ((AbstractPureProcessType) systemType).addNewOutputs().addNewOutputList();
		} else if (systemType instanceof AbstractComponentType){
			 inputList = ((AbstractComponentType) systemType).addNewInputs().addNewInputList();
			 outputList = ((AbstractComponentType) systemType).addNewOutputs().addNewOutputList();
		} else {
			throw new RuntimeException("Can only handle AbstractComponentType and AbstractPureProcessType");
		}
		
		Set<URI> processes = UwCollectionUtils.set();
		
		String url = null;		
		for (AbstractObservation o : obs) {
			Origin origin = (Origin) o.getParameter(Constants.OBSERVATION_PARAMETER_AGGREGATED_OF);
			if (origin != null) {
				if (url == null) {
					url = origin.getSourceUrl();
					int i = url.lastIndexOf('?');
					if (i < url.length() && i > 0) 
						url = url.substring(0, i); /* cut off old parameters */
				}
				processes.addAll(origin.getSourceSensors());
			}
		}
		
        /* inputs */
		int j = 0;
		for (URI s : processes) {
			IoComponentPropertyType ioComp = inputList.addNewInput();
			ioComp.setName("inputSensor" + String.valueOf(j++));
			ioComp.setHref(getDescribeSensorUrl(url, s.toString()));
		}

		Map<URI, String> obsProps = UwCollectionUtils.map();
		for (AbstractObservation o : obs) {
			String uom = ((MeasureResult)o.getResult()).getUnitOfMeasurement();
			obsProps.put(o.getObservedProperty(), uom);
		}
		int i = 0;
        /* outputs */
		for (URI obsProp : obsProps.keySet()) {
			IoComponentPropertyType output = outputList.addNewOutput();
			output.setName("output" + String.valueOf(i++));
			Quantity quantity = output.addNewQuantity();
			quantity.setDefinition(obsProp.toString());
			quantity.addNewUom().setCode(obsProps.get(obsProp));
			MetaDataPropertyType metaDataProperty = quantity
					.addNewMetaDataProperty();

			/* offering. there will be only one offering for all observations */
			XmlCursor cursor = metaDataProperty.newCursor();
			cursor.toNextToken();
			cursor.beginElement(ELEMENT_NAME_OFFERING);
			cursor.insertElementWithText(ELEMENT_NAME_ID, Constants.Sos.AGGREGATION_OFFERING_ID);
			cursor.insertElementWithText(ELEMENT_NAME_NAME, Constants.Sos.AGGREGATION_OFFERING_NAME);
		}
	}
	
	public static String getDescribeSensorUrl(String url, String sensorId) {
		HashMap<Constants.Sos.Parameter, Object> props = new HashMap<Constants.Sos.Parameter, Object>();
		props.put(Constants.Sos.Parameter.REQUEST, Constants.Sos.Operation.DESCRIBE_SENSOR);
		props.put(Constants.Sos.Parameter.SERVICE, Constants.Sos.SERVICE_NAME);
		props.put(Constants.Sos.Parameter.VERSION, Constants.Sos.SERVICE_VERSION);
		props.put(Constants.Sos.Parameter.OUTPUT_FORMAT, Constants.Sos.SENSOR_OUTPUT_FORMAT);
		props.put(Constants.Sos.Parameter.PROCEDURE, sensorId);
		return Utils.buildGetRequest(url, props);
	}
	
	
	protected void buildValidTime(AbstractProcessType systemType, List<? extends AbstractObservation> obs) {
		DateTime start = null, end = null;
		for (AbstractObservation o : obs) {
			TimeObject to = o.getPhenomenonTime();
			
			if (to.isInstant()) {
				DateTime t = to.getDateTime();
				if (start == null || t.isBefore(start))
					start = t;
				if (end == null || t.isAfter(end))
					end = t;
			} else if (to.isInterval()) {
				DateTime s = to.getInterval().getStart();
				if (start == null || s.isBefore(start))
					start = s;
				DateTime e = to.getInterval().getEnd();
				if (end == null || e.isAfter(end)) 
					end = e;
			}
		}
		TimePeriodType validTime = systemType.addNewValidTime().addNewTimePeriod();
		validTime.addNewBeginPosition().setStringValue(UwTimeUtils.format(start));
		validTime.addNewEndPosition().setStringValue(UwTimeUtils.format(end));
	}

	protected void buildObservationTemplate(RegisterSensor regSen) {
		ObservationTemplate template = regSen.addNewObservationTemplate();
		MeasurementType measurementType = (MeasurementType) template
				.addNewObservation().substitute(OM.q("Measurement"),
						MeasurementType.type);
		measurementType.addNewSamplingTime();
		measurementType.addNewProcedure();
		measurementType.addNewObservedProperty();
		measurementType.addNewFeatureOfInterest();
		XmlObject result = measurementType.addNewResult();
		XmlCursor resultCursor = result.newCursor();
		resultCursor.toNextToken();
		resultCursor.insertAttributeWithValue(ATTRIBUTE_NAME_UOM, "");
		resultCursor.insertChars("0.0");
		resultCursor.dispose();
	}

	protected void buildCapabilities(AbstractProcessType systemType) {
		Capabilities capabilities = systemType.addNewCapabilities();
		AbstractDataRecordType abstractDataRecord = capabilities.addNewAbstractDataRecord();
		DataRecordType dataRecord = (DataRecordType) abstractDataRecord
				.substitute(SWE.q("DataRecord"), DataRecordType.type);
		dataRecord.setDefinition(UwConstants.URN.CAPABILITIES_DEFINITION.value);
		

		/* TODO generate BBOX 
		DataComponentPropertyType field_bbox = dataRecord.addNewField();
		field_bbox.setName(Constants.FIELD_NAME_BBOX);
		*/
		
		DataComponentPropertyType statusField = dataRecord.addNewField();
        statusField.setName(FIELD_NAME_STATUS);
        Boolean statusBoolean = statusField.addNewBoolean();
        statusBoolean.setDefinition(UwConstants.URN.IS_ACTIVE.value);
        statusBoolean.setValue(false);
        
//        // spatial grouping method
//        DataComponentPropertyType sgmField = dataRecord.addNewField();
//        sgmField.setName(Constants.PROPERTY_NAME_SPATIAL_AGGREGATION_METHOD);
//        Text sgmText = sgmField.addNewText();
//        sgmText.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_SPATIAL_GROUPING_METHOD);
//        sgmText.setValue(meta.get(Constants.PROPERTY_NAME_SPATIAL_GROUPING_METHOD));
//    
//        // temporal grouping method
//        DataComponentPropertyType tgmField = dataRecord.addNewField();
//        tgmField.setName(Constants.PROPERTY_NAME_TEMPORAL_GROUPING_METHOD);
//        Text tgmText = tgmField.addNewText();
//        tgmText.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_TEMPORAL_GROUPING_METHOD);
//        tgmText.setValue(meta.get(Constants.PROPERTY_NAME_TEMPORAL_GROUPING_METHOD));
//        
//        // spatial aggregation method
//        DataComponentPropertyType samField = dataRecord.addNewField();
//        samField.setName(Constants.PROPERTY_NAME_SPATIAL_AGGREGATION_METHOD);
//        Text samText = samField.addNewText();
//        samText.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_SPATIAL_AGGREGATION_METHOD);
//        samText.setValue(meta.get(Constants.PROPERTY_NAME_SPATIAL_AGGREGATION_METHOD));
//        
//        // temporal aggregation method
//        DataComponentPropertyType tamField = dataRecord.addNewField();
//        tamField.setName(Constants.PROPERTY_NAME_TEMPORAL_AGGREGATION_METHOD);
//        Text tamText = tamField.addNewText();
//        tamText.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_TEMPORAL_AGGREGATION_METHOD);
//        tamText.setValue(meta.get(Constants.PROPERTY_NAME_TEMPORAL_AGGREGATION_METHOD));
//        
//        // temporal before spatial
//        DataComponentPropertyType tbsField = dataRecord.addNewField();
//        tbsField.setName(Constants.PROPERTY_NAME_TEMPORAL_BEFORE_SPATIAL_AGGREGATION);
//        Boolean tbsBoolean = tbsField.addNewBoolean();
//        tbsBoolean.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_TEMPORAL_BEFORE_SPATIAL_AGGREGATION);
//        tbsBoolean.setValue(java.lang.Boolean.parseBoolean(meta.get(Constants.PROPERTY_NAME_TEMPORAL_BEFORE_SPATIAL_AGGREGATION)));
//     
//        // grouped by observed property
//        DataComponentPropertyType gbopField = dataRecord.addNewField();
//        gbopField.setName(Constants.PROPERTY_NAME_GROUPED_BY_OBSERVED_PROPERTY);
//        Boolean gbopBoolean = gbopField.addNewBoolean();
//        gbopBoolean.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_GROUPED_BY_OBSERVED_PROPERTY);
//        gbopBoolean.setValue(java.lang.Boolean.parseBoolean(meta.get(Constants.PROPERTY_NAME_GROUPED_BY_OBSERVED_PROPERTY)));
//        
        // time of aggregation
        DataComponentPropertyType toaField = dataRecord.addNewField();
        toaField.setName(Constants.Sos.ProcessDescription.Capabilities.TIME_OF_AGGREGATION);
        Time toaTime = toaField.addNewTime();
        toaTime.setDefinition(Constants.Sos.ProcessDescription.Capabilities.URN_PREFIX + Constants.Sos.ProcessDescription.Capabilities.TIME_OF_AGGREGATION);
        toaTime.setValue(UwTimeUtils.format(new DateTime()));
	}
}
