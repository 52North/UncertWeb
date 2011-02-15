package org.uncertweb.sta.wps;

import static org.uncertweb.intamap.utils.Namespace.OM;
import static org.uncertweb.intamap.utils.Namespace.SML;
import static org.uncertweb.intamap.utils.Namespace.SML_VERSION;
import static org.uncertweb.intamap.utils.Namespace.SWE;
import static org.uncertweb.intamap.utils.Namespace.qualify;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.gml.MetaDataPropertyType;
import net.opengis.gml.TimePeriodType;
import net.opengis.om.x10.MeasurementType;
import net.opengis.ows.x11.ExceptionReportDocument;
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
import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.sos.x10.GetObservationDocument.GetObservation;
import net.opengis.sos.x10.InsertObservationDocument;
import net.opengis.sos.x10.InsertObservationResponseDocument;
import net.opengis.sos.x10.ObservationTemplateDocument.ObservationTemplate;
import net.opengis.sos.x10.RegisterSensorDocument;
import net.opengis.sos.x10.RegisterSensorDocument.RegisterSensor;
import net.opengis.sos.x10.RegisterSensorDocument.RegisterSensor.SensorDescription;
import net.opengis.sos.x10.RegisterSensorResponseDocument;
import net.opengis.sos.x10.ResponseModeType;
import net.opengis.swe.x101.AbstractDataRecordType;
import net.opengis.swe.x101.BooleanDocument.Boolean;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.DataRecordType;
import net.opengis.swe.x101.PositionType;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.VectorType;
import net.opengis.swe.x101.VectorType.Coordinate;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.wps.server.ExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationTimeInstant;
import org.uncertweb.intamap.om.ObservationTimeInterval;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.intamap.utils.TimeUtils;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.xml.binding.GetObservationRequestBinding;
import org.uncertweb.sta.wps.xml.io.enc.ObservationGenerator;

public class SOSRequestBuilder {
	private static final Logger log = LoggerFactory.getLogger(SOSRequestBuilder.class);
	
    private static final String COORD_NAME_LAT = "latitude";
    private static final String COORD_NAME_LON = "longitude";
    private static final String QUANTITY_AXIS_ID_LAT = "y";
    private static final String QUANTITY_AXIS_ID_LON = "x";
    private static final String QUANTITY_AXIS_ID_ALTITUDE = "z";
    private static final String COORD_NAME_ALTITUDE = "altitude";
    private static final String EPSG_4326_REFERENCE_SYSTEM_DEFINITION = "urn:ogc:def:crs:EPSG:4326";
    private static final String COORDINATE_UOM = "degree";
    private static final String METER_UOM = "m";	
    private static final double lat = 0, lon = 0, alt = 0;

	
	public GetObservationRequestBinding registerAggregatedObservations(
			List<Observation> obs, String url, String process)
			throws IOException {

		// register sensor
		RegisterSensorDocument regSenDoc = RegisterSensorDocument.Factory.newInstance();
		RegisterSensor regSen = regSenDoc.addNewRegisterSensor();
		regSen.setService(Constants.SOS_SERVICE_NAME);
		regSen.setVersion(Constants.SOS_SERVICE_VERSION);
		Set<String> obsProps = buildSensorDescription(regSen, process, obs);
		buildObservationTemplate(regSen);

//		if (!regSenDoc.validate())
//			throw new RuntimeException("Invalid RegisterSensor Request generated:\n"
//							+ regSenDoc.xmlText(defaultOptions()));
		log.debug("RegisterSensor Request:\n{}", 
				regSenDoc.xmlText(Namespace.defaultOptions()));
		sendPostRequests(url, regSenDoc);

		// insert observations
		ObservationGenerator generator = new ObservationGenerator();
		boolean printed =false;
		for (Observation o : obs) {
			InsertObservationDocument insObsDoc = InsertObservationDocument.Factory.newInstance();
			insObsDoc.addNewInsertObservation().set(generator.generateXML(o));
			insObsDoc.getInsertObservation().setAssignedSensorId(process);

//			if (!insObsDoc.validate())
//				throw new RuntimeException("Invalid InsertObservation Request generated:\n"
//					+ insObsDoc.xmlText(defaultOptions()));
			if (!printed) {
				printed = true;
				log.debug("InstertObservation:\n{}", insObsDoc);
			}
			sendPostRequests(url, insObsDoc);
		}
		GetObservationDocument getObsDoc = buildGetObsDoc(process, obsProps);
		log.debug("GetObservation Request:\n{}", getObsDoc.xmlText(Namespace.defaultOptions()));
		return new GetObservationRequestBinding(getObsDoc);
	}

	
	private GetObservationDocument buildGetObsDoc(String process, Set<String> obsProps) {
		GetObservationDocument getObsDoc = GetObservationDocument.Factory.newInstance();
		GetObservation getObs = getObsDoc.addNewGetObservation();
		getObs.setService(Constants.SOS_SERVICE_NAME);
		getObs.setVersion(Constants.SOS_SERVICE_VERSION);
		getObs.setResultModel(Constants.MEASUREMENT_RESULT_MODEL);
		getObs.setOffering(Constants.AGGREGATION_OFFERING_ID);
		getObs.setResponseFormat(Constants.SOS_OBSERVATION_OUTPUT_FORMAT);
		getObs.setResponseMode(ResponseModeType.INLINE);
		getObs.addNewProcedure().setStringValue(process);
		for (String s : obsProps) {
			getObs.addNewObservedProperty().setStringValue(s);
		}
		return getObsDoc;
	}
	
	private Set<String> buildSensorDescription(RegisterSensor regSen,
			String process, List<Observation> obs) {
		SensorDescription description = regSen.addNewSensorDescription();
		SensorMLDocument smlDocument = SensorMLDocument.Factory.newInstance();
		SensorML sml = smlDocument.addNewSensorML();
		sml.setVersion(SML_VERSION);
		SystemType systemType = (SystemType) sml.addNewMember().addNewProcess()
				.substitute(qualify(SML, "System"), SystemType.type);
		
		/* unique id */
		IdentifierList idenList = systemType.addNewIdentification().addNewIdentifierList();
		Identifier ident = idenList.addNewIdentifier();
		Term term = ident.addNewTerm();
		term.setDefinition(Constants.URN_UNIQUE_ID_DEFINITION);
		term.setValue(process);

		
		buildValidTime(systemType, obs);

		buildCapabilities(systemType);
		buildPosition(systemType);
		/* TODO additional SensorML information
		 * build keywords 
		 * build description: It would be nice to know the used methods... 
		 * build contact: no idea... maybe provided as an additional input? 
		 * FIXME position: no idea... best to omit it... 
		 * build components: ??? 
		 */
		
		Set<String> obsProps = buildInputOutputLists(systemType,obs);
		description.set(smlDocument);
		return obsProps;
	}
    
	private void buildPosition(SystemType systemType) {
		Position position = systemType.addNewPosition();
		PositionType positionType = (PositionType) position.addNewProcess()
				.substitute(new QName(Namespace.SWE.URI, "Position"),
						PositionType.type);
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


	private Set<String> buildInputOutputLists(SystemType systemType, List<Observation> obs) {
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
		for (String s : processes) {
			IoComponentPropertyType ioComp = inputList.addNewInput();
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
			/* 
			TODO we don't have any description... omit it? empty? some standard text?
			quantity.addNewDescription().setStringValue("description");
			*/
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
		return obsProps.keySet();
	}

	private void buildCapabilities(SystemType systemType) {
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
	}

	private void buildValidTime(SystemType systemType, List<Observation> obs) {
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

	private void buildObservationTemplate(RegisterSensor regSen) {
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
	
	private void sendPostRequests(String url, XmlObject doc) throws IOException {
		try {
			XmlObject xml = XmlObject.Factory.parse(Utils.sendPostRequest(url, doc.xmlText()));

			if (xml instanceof RegisterSensorResponseDocument) {
				log.info("RegisterSensor successfull: {}",
						((RegisterSensorResponseDocument) xml)
								.getRegisterSensorResponse()
								.getAssignedSensorId());
			} else if (xml instanceof InsertObservationResponseDocument) {
				log.info("InsertObservation successfull: {}",
						((InsertObservationResponseDocument) xml)
								.getInsertObservationResponse()
								.getAssignedObservationId());
			} else if (xml instanceof ExceptionReportDocument) {
				ExceptionReportDocument ex = (ExceptionReportDocument) xml;
				String errorKey = ex.getExceptionReport().getExceptionArray(0)
						.getExceptionCode();
				String message = ex.getExceptionReport().getExceptionArray(0)
						.getExceptionTextArray(0);
				throw new RuntimeException(new ExceptionReport(message,
						errorKey));
			}
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
	}
	
}
