package org.uncertweb.sta.wps.sos;

import static org.uncertweb.intamap.utils.Namespace.OM;
import static org.uncertweb.intamap.utils.Namespace.SML;
import static org.uncertweb.intamap.utils.Namespace.SML_VERSION;
import static org.uncertweb.intamap.utils.Namespace.SWE;
import static org.uncertweb.intamap.utils.Namespace.qualify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.opengis.gml.MetaDataPropertyType;
import net.opengis.gml.TimePeriodType;
import net.opengis.om.x10.MeasurementType;
import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.InputsDocument.Inputs.InputList;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.OutputsDocument.Outputs.OutputList;
import net.opengis.sensorML.x101.PositionDocument.Position;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.sensorML.x101.TermDocument.Term;
import net.opengis.sos.x10.ObservationTemplateDocument.ObservationTemplate;
import net.opengis.sos.x10.RegisterSensorDocument;
import net.opengis.sos.x10.RegisterSensorDocument.RegisterSensor;
import net.opengis.sos.x10.RegisterSensorDocument.RegisterSensor.SensorDescription;
import net.opengis.swe.x101.AbstractDataRecordType;
import net.opengis.swe.x101.BooleanDocument.Boolean;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.DataRecordType;
import net.opengis.swe.x101.PositionType;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.TimeDocument.Time;
import net.opengis.swe.x101.VectorType;
import net.opengis.swe.x101.VectorType.Coordinate;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationTimeInstant;
import org.uncertweb.intamap.om.ObservationTimeInterval;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.intamap.utils.TimeUtils;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.om.OriginAwareObservation;

public class RegisterSensorDocumentBuilder {
	protected static final Logger log = LoggerFactory.getLogger(RegisterSensorDocumentBuilder.class);
	
    protected static final String COORD_NAME_LAT = "latitude";
    protected static final String COORD_NAME_LON = "longitude";
    protected static final String QUANTITY_AXIS_ID_LAT = "y";
    protected static final String QUANTITY_AXIS_ID_LON = "x";
    protected static final String QUANTITY_AXIS_ID_ALTITUDE = "z";
    protected static final String COORD_NAME_ALTITUDE = "altitude";
    protected static final String EPSG_4326_REFERENCE_SYSTEM_DEFINITION = "urn:ogc:def:crs:EPSG:4326";
    protected static final String COORDINATE_UOM = "degree";
    protected static final String METER_UOM = "m";	
    protected static final double lat = 0, lon = 0, alt = 0;
    
	private static RegisterSensorDocumentBuilder singleton;

	public static RegisterSensorDocumentBuilder getInstance() {
		if (singleton == null) {
			singleton = new RegisterSensorDocumentBuilder();
		}
		return singleton;
	}
	
	private RegisterSensorDocumentBuilder(){}
	
	public RegisterSensorDocument build(String process, List<Observation> obs, Map<String,String> meta) {
		RegisterSensorDocument regSenDoc = RegisterSensorDocument.Factory.newInstance();
		RegisterSensor regSen = regSenDoc.addNewRegisterSensor();
		regSen.setService(Constants.SOS_SERVICE_NAME);
		regSen.setVersion(Constants.SOS_SERVICE_VERSION);
		buildSensorDescription(regSen, process, obs, meta);
		buildObservationTemplate(regSen);
		log.debug(regSenDoc.xmlText(Namespace.defaultOptions()));
		return regSenDoc;
	}
	
	protected void buildSensorDescription(RegisterSensor regSen,
			String process, List<Observation> obs, Map<String,String> meta) {
		SensorDescription description = regSen.addNewSensorDescription();
		SensorMLDocument smlDocument = SensorMLDocument.Factory.newInstance();
		SensorML sml = smlDocument.addNewSensorML();
		sml.setVersion(SML_VERSION);
		SystemType systemType = (SystemType) sml.addNewMember().addNewProcess()
				.substitute(qualify(SML, "System"), SystemType.type);
//		ProcessModelType processModelType = (ProcessModelType) sml.addNewMember().addNewProcess().substitute(qualify(SML, "ProcessModel"), ProcessModelType.type);
		
		/* unique id */
		IdentifierList idenList = systemType.addNewIdentification().addNewIdentifierList();
		Identifier ident = idenList.addNewIdentifier();
		Term term = ident.addNewTerm();
		term.setDefinition(Constants.URN_UNIQUE_ID_DEFINITION);
		term.setValue(process);

		systemType.addNewDescription().setStringValue(Constants.SENSOR_DESCRIPTION);
		buildValidTime(systemType, obs);
		buildCapabilities(systemType, meta);
		buildPosition(systemType);
		/* TODO additional SensorML information
		 * build keywords 
		 * build contact: no idea... maybe provided as an additional input? 
		 */
		
		buildInputOutputLists(systemType,obs);
		description.set(smlDocument);
	}
    
	protected void buildPosition(SystemType systemType) {
		//FIXME real position 
		Position position = systemType.addNewPosition();
		position.setName("sensorPosition");
		PositionType positionType = (PositionType) position.addNewProcess()
				.substitute(qualify(SWE, "Position"), PositionType.type);
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


	protected void buildInputOutputLists(SystemType systemType, List<Observation> obs) {
		InputList inputList = systemType.addNewInputs().addNewInputList();
		OutputList outputList = systemType.addNewOutputs().addNewOutputList();
		
		HashSet<String> processes = new HashSet<String>();
		
		String url = null;		
		for (Observation o : obs) {
			if (o instanceof OriginAwareObservation) {
				OriginAwareObservation oao = (OriginAwareObservation) o;
				if (url == null) {
					url = oao.getSourceUrl();
					int i = url.lastIndexOf('?');
					if (i < url.length() && i > 0) 
						url = url.substring(0, i); /* cut off old parameters */
				}
				processes.addAll(oao.getSourceSensors());
			}
		}
		
        /* inputs */
		int j = 0;
		for (String s : processes) {
			IoComponentPropertyType ioComp = inputList.addNewInput();
			ioComp.setName("inputSensor" + String.valueOf(j++));
			ioComp.setHref(Utils.getDescribeSensorUrl(url, s));
		}

		HashMap<String, String> obsProps = new HashMap<String, String>();
		for (Observation o : obs) {
			obsProps.put(o.getObservedProperty(), o.getUom());
		}
		int i = 0;
        /* outputs */
		for (String obsProp : obsProps.keySet()) {
			IoComponentPropertyType output = outputList.addNewOutput();
			output.setName("output" + String.valueOf(i++));
			Quantity quantity = output.addNewQuantity();
			quantity.setDefinition(obsProp);
			quantity.addNewUom().setCode(obsProps.get(obsProp));
			MetaDataPropertyType metaDataProperty = quantity
					.addNewMetaDataProperty();

			/* offering. there will be only one offering for all observations */
			XmlCursor cursor = metaDataProperty.newCursor();
			cursor.toNextToken();
			cursor.beginElement(Constants.ELEMENT_NAME_OFFERING);
			cursor.insertElementWithText(Constants.ELEMENT_NAME_ID, Constants.AGGREGATION_OFFERING_ID);
			cursor.insertElementWithText(Constants.ELEMENT_NAME_NAME, Constants.AGGREGATION_OFFERING_NAME);
		}
		
		
	}

	protected void buildValidTime(SystemType systemType, List<Observation> obs) {
		DateTime start = null, end = null;
		for (Observation o : obs) {
			if (o.getObservationTime() instanceof ObservationTimeInstant) {
				DateTime t = ((ObservationTimeInstant) o.getObservationTime()).getDateTime();
				if (start == null || t.isBefore(start))
					start = t;
				if (end == null || t.isAfter(end))
					end = t;
			} else if (o.getObservationTime() instanceof ObservationTimeInterval) {
				DateTime s = ((ObservationTimeInterval) o.getObservationTime()).getStart();
				if (start == null || s.isBefore(start))
					start = s;
				DateTime e = ((ObservationTimeInterval) o.getObservationTime()).getEnd();
				if (end == null || e.isAfter(end)) 
					end = e;
			}
		}
		TimePeriodType validTime = systemType.addNewValidTime().addNewTimePeriod();
		validTime.addNewBeginPosition().setStringValue(TimeUtils.format(start));
		validTime.addNewEndPosition().setStringValue(TimeUtils.format(end));
	}

	protected void buildObservationTemplate(RegisterSensor regSen) {
		ObservationTemplate template = regSen.addNewObservationTemplate();
		MeasurementType measurementType = (MeasurementType) template
				.addNewObservation().substitute(qualify(OM, "Measurement"),
						MeasurementType.type);
		measurementType.addNewSamplingTime();
		measurementType.addNewProcedure();
		measurementType.addNewObservedProperty();
		measurementType.addNewFeatureOfInterest();
		XmlObject result = measurementType.addNewResult();
		XmlCursor resultCursor = result.newCursor();
		resultCursor.toNextToken();
		resultCursor.insertAttributeWithValue(Constants.ATTRIBUTE_NAME_UOM, "");
		resultCursor.insertChars("0.0");
		resultCursor.dispose();
	}

	protected void buildCapabilities(SystemType systemType, Map<String,String> meta) {
		Capabilities capabilities = systemType.addNewCapabilities();
		AbstractDataRecordType abstractDataRecord = capabilities.addNewAbstractDataRecord();
		DataRecordType dataRecord = (DataRecordType) abstractDataRecord
				.substitute(qualify(SWE, "DataRecord"), DataRecordType.type);
		dataRecord.setDefinition(Constants.URN_CAPABILITIES_DEFINITION);
		

		/* TODO generate BBOX 
		DataComponentPropertyType field_bbox = dataRecord.addNewField();
		field_bbox.setName(Constants.FIELD_NAME_BBOX);
		*/
		
		DataComponentPropertyType statusField = dataRecord.addNewField();
        statusField.setName(Constants.FIELD_NAME_STATUS);
        Boolean statusBoolean = statusField.addNewBoolean();
        statusBoolean.setDefinition(Constants.URN_IS_ACTIVE);
        statusBoolean.setValue(false);
        
        // spatial grouping method
        DataComponentPropertyType sgmField = dataRecord.addNewField();
        sgmField.setName(Constants.PROPERTY_NAME_SPATIAL_AGGREGATION_METHOD);
        Text sgmText = sgmField.addNewText();
        sgmText.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_SPATIAL_GROUPING_METHOD);
        sgmText.setValue(meta.get(Constants.PROPERTY_NAME_SPATIAL_GROUPING_METHOD));
    
        // temporal grouping method
        DataComponentPropertyType tgmField = dataRecord.addNewField();
        tgmField.setName(Constants.PROPERTY_NAME_TEMPORAL_GROUPING_METHOD);
        Text tgmText = tgmField.addNewText();
        tgmText.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_TEMPORAL_GROUPING_METHOD);
        tgmText.setValue(meta.get(Constants.PROPERTY_NAME_TEMPORAL_GROUPING_METHOD));
        
        // spatial aggregation method
        DataComponentPropertyType samField = dataRecord.addNewField();
        samField.setName(Constants.PROPERTY_NAME_SPATIAL_AGGREGATION_METHOD);
        Text samText = samField.addNewText();
        samText.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_SPATIAL_AGGREGATION_METHOD);
        samText.setValue(meta.get(Constants.PROPERTY_NAME_SPATIAL_AGGREGATION_METHOD));
        
        // temporal aggregation method
        DataComponentPropertyType tamField = dataRecord.addNewField();
        tamField.setName(Constants.PROPERTY_NAME_TEMPORAL_AGGREGATION_METHOD);
        Text tamText = tamField.addNewText();
        tamText.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_TEMPORAL_AGGREGATION_METHOD);
        tamText.setValue(meta.get(Constants.PROPERTY_NAME_TEMPORAL_AGGREGATION_METHOD));
        
        // temporal before spatial
        DataComponentPropertyType tbsField = dataRecord.addNewField();
        tbsField.setName(Constants.PROPERTY_NAME_TEMPORAL_BEFORE_SPATIAL_AGGREGATION);
        Boolean tbsBoolean = tbsField.addNewBoolean();
        tbsBoolean.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_TEMPORAL_BEFORE_SPATIAL_AGGREGATION);
        tbsBoolean.setValue(java.lang.Boolean.parseBoolean(meta.get(Constants.PROPERTY_NAME_TEMPORAL_BEFORE_SPATIAL_AGGREGATION)));
     
        // grouped by observed property
        DataComponentPropertyType gbopField = dataRecord.addNewField();
        gbopField.setName(Constants.PROPERTY_NAME_GROUPED_BY_OBSERVED_PROPERTY);
        Boolean gbopBoolean = gbopField.addNewBoolean();
        gbopBoolean.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_GROUPED_BY_OBSERVED_PROPERTY);
        gbopBoolean.setValue(java.lang.Boolean.parseBoolean(meta.get(Constants.PROPERTY_NAME_GROUPED_BY_OBSERVED_PROPERTY)));
        
        // time of aggregation
        DataComponentPropertyType toaField = dataRecord.addNewField();
        toaField.setName(Constants.PROPERTY_NAME_TIME_OF_AGGREGATION);
        Time toaTime = toaField.addNewTime();
        toaTime.setDefinition(Constants.CAPS_PROPERTY_PREFIX + Constants.PROPERTY_NAME_TIME_OF_AGGREGATION);
        toaTime.setValue(TimeUtils.format(new DateTime()));
	}
}
