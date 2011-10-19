package org.n52.sos.decode.impl.uncertainty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.TimePositionType;
import net.opengis.gml.x32.UncertaintyPropertyType;
import net.opengis.om.x10.CategoryObservationDocument;
import net.opengis.om.x10.CategoryObservationType;
import net.opengis.om.x10.GeometryObservationDocument;
import net.opengis.om.x10.GeometryObservationType;
import net.opengis.om.x10.MeasurementDocument;
import net.opengis.om.x10.MeasurementType;
import net.opengis.om.x10.ObservationDocument;
import net.opengis.om.x10.ObservationType;
import net.opengis.om.x20.OMBooleanObservationDocument;
import net.opengis.om.x20.OMCategoryObservationDocument;
import net.opengis.om.x20.OMDiscreteNumericObservationDocument;
import net.opengis.om.x20.OMMeasurementCollectionDocument;
import net.opengis.om.x20.OMMeasurementDocument;
import net.opengis.om.x20.OMObservationDocument;
import net.opengis.om.x20.OMReferenceObservationDocument;
import net.opengis.om.x20.OMTextObservationDocument;
import net.opengis.om.x20.OMUncertaintyObservationCollectionDocument.OMUncertaintyObservationCollection;
import net.opengis.om.x20.OMUncertaintyObservationDocument;
import net.opengis.om.x20.UWBooleanObservationType;
import net.opengis.om.x20.UWDiscreteNumericObservationType;
import net.opengis.om.x20.UWMeasurementType;
import net.opengis.om.x20.UWReferenceObservationType;
import net.opengis.om.x20.UWTextObservationType;
import net.opengis.om.x20.UWUncertaintyObservationType;
import net.opengis.sensorML.x101.AbstractProcessType;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML.Member;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.sos.x10.InsertObservationDocument;
import net.opengis.sos.x10.InsertObservationDocument.InsertObservation;
import net.opengis.sos.x10.ObservationTemplateDocument.ObservationTemplate;
import net.opengis.sos.x10.RegisterSensorDocument;
import net.opengis.sos.x10.RegisterSensorDocument.RegisterSensor;
import net.opengis.sos.x10.RegisterSensorDocument.RegisterSensor.SensorDescription;
import net.opengis.sos.x10.UpdateSensorDocument;
import net.opengis.sos.x10.UpdateSensorDocument.UpdateSensor;
import net.opengis.swe.x101.PositionType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.decode.IHttpPostRequestDecoder;
import org.n52.sos.decode.impl.OMDecoder;
import org.n52.sos.ogc.om.AbstractSosObservation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sos.ogc.sensorML.SensorSystem;
import org.n52.sos.request.AbstractSosRequest;
import org.n52.sos.request.SosInsertObservationRequest;
import org.n52.sos.request.SosRegisterSensorRequest;
import org.n52.sos.request.SosUpdateSensorRequest;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;

import com.vividsolutions.jts.geom.Point;

/**
 * class offers parsing method to create a SOSOperationRequest, which
 * encapsulates the request parameters, from a GetOperationDocument (XmlBeans
 * generated Java class representing the request) The different
 * SosOperationRequest classes are useful, because XmlBeans generates no useful
 * documentation and handling of substitution groups is not simple. So it may be
 * easier for foreign developers to implement the DAO implementation classes for
 * other data sources then PGSQL databases.
 * 
 * !!! Use for each operation a new parser method. Use 'parse' + operation name
 * + 'Request', e.g. parseGetCapabilitiesRequest. In GetCapabilities the SOS can
 * check for GET and POST implementations. !!!
 * 
 * @author Christoph Stasch, Martin Kiesow
 * 
 */
public class HttpPostRequestDecoderMobile extends
		org.n52.sos.decode.impl.HttpPostRequestDecoderMobile implements
		IHttpPostRequestDecoder {

	// /**
	// * Feature Decoder
	// */
	// protected FeatureDecoder featureDecoder = new FeatureDecoder();

	/**
	 * internal O&M 1 Decoder
	 */
	protected OMDecoder om1Decoder = new OMDecoder();

	/**
	 * O&M 2 Decoder from UncertWeb O&M 2 API
	 */
	protected XBObservationParser om2Decoder = new XBObservationParser();

	/**
	 * parses the passes XmlBeans document and creates a SOS InsertObservation
	 * request
	 * 
	 * @param xb_insertObsDoc
	 *            XmlBeans document of InsertObservation request
	 * @return Returns SOS representation of InsertObservation request
	 * @throws OwsExceptionReport
	 *             if parsing of request or parameter in Observation, which
	 *             should be inserted, is incorrect
	 */
	public AbstractSosRequest parseInsertObservationRequest(
			InsertObservationDocument xb_insertObsDoc)
			throws OwsExceptionReport {
		InsertObservation xb_insertObs = xb_insertObsDoc.getInsertObservation();
		String assignedSensorID = xb_insertObs.getAssignedSensorId();

		boolean mobileEnabled = xb_insertObs.getMobileEnabled();

		ObservationType xb_obsType = xb_insertObs.getObservation();
		OMObservationDocument om2ObsDoc = null;
		
		// TODO ObservationCollection accepted?
		try {
		if (xb_obsType == null) {
			
			if (xb_insertObs.selectChildren(new QName("http://www.opengis.net/om/2.0",  OM2Constants.OBS_TYPE_UNCERTAINTY)).length > 0) {	
				om2ObsDoc = OMUncertaintyObservationDocument.Factory.newInstance();
				((OMUncertaintyObservationDocument) om2ObsDoc).addNewOMUncertaintyObservation().set(UWUncertaintyObservationType.Factory.parse(xb_insertObs.selectChildren(new QName("http://www.opengis.net/om/2.0", OM2Constants.OBS_TYPE_UNCERTAINTY))[0].xmlText()));
			} else if (xb_insertObs.selectChildren(new QName("http://www.opengis.net/om/2.0", OM2Constants.OBS_TYPE_MEASUREMENT)).length > 0) {
				om2ObsDoc = OMMeasurementDocument.Factory.newInstance();
				((OMMeasurementDocument) om2ObsDoc).addNewOMMeasurement().set(UWMeasurementType.Factory.parse(xb_insertObs.selectChildren(new QName("http://www.opengis.net/om/2.0", OM2Constants.OBS_TYPE_MEASUREMENT))[0].xmlText()));
//			} else if (xb_insertObs.selectChildren(new QName("http://www.opengis.net/om/2.0", OM2Constants.OBS_TYPE_BOOLEAN)).length > 0) {
//				om2ObsDoc = OMBooleanObservationDocument.Factory.newInstance();
//				((OMBooleanObservationDocument) om2ObsDoc).addNewOMBooleanObservation().set(UWBooleanObservationType.Factory.parse(xb_insertObs.selectChildren(new QName("http://www.opengis.net/om/2.0", OM2Constants.OBS_TYPE_BOOLEAN))[0].xmlText()));
//			} else if (xb_insertObs.selectChildren(new QName("http://www.opengis.net/om/2.0", OM2Constants.OBS_TYPE_DISCNUM)).length > 0) {
//				om2ObsDoc = OMDiscreteNumericObservationDocument.Factory.newInstance();
//				((OMDiscreteNumericObservationDocument) om2ObsDoc).addNewOMDiscreteNumericObservation().set(UWDiscreteNumericObservationType.Factory.parse(xb_insertObs.selectChildren(new QName("http://www.opengis.net/om/2.0", OM2Constants.OBS_TYPE_DISCNUM))[0].xmlText()));
//			} else if (xb_insertObs.selectChildren(new QName("http://www.opengis.net/om/2.0", OM2Constants.OBS_TYPE_REFERENCE)).length > 0) {
//				om2ObsDoc = OMReferenceObservationDocument.Factory.newInstance();
//				((OMReferenceObservationDocument) om2ObsDoc).addNewOMReferenceObservation().set(UWReferenceObservationType.Factory.parse(xb_insertObs.selectChildren(new QName("http://www.opengis.net/om/2.0", OM2Constants.OBS_TYPE_REFERENCE))[0].xmlText()));
//			} else if (xb_insertObs.selectChildren(new QName("http://www.opengis.net/om/2.0", OM2Constants.OBS_TYPE_TEXT)).length > 0) {
//				om2ObsDoc = OMTextObservationDocument.Factory.newInstance();
//				((OMTextObservationDocument) om2ObsDoc).addNewOMTextObservation().set(UWTextObservationType.Factory.parse(xb_insertObs.selectChildren(new QName("http://www.opengis.net/om/2.0", OM2Constants.OBS_TYPE_TEXT))[0].xmlText()));
			}			
		}
		} catch (XmlException xmle) {
			OwsExceptionReport se = new OwsExceptionReport();
			se.addCodedException(
					ExceptionCode.InvalidParameterValue,
					null,
					"Error while parsing: " + xmle.getLocalizedMessage());
			throw se;
		}
		
		Collection<AbstractSosObservation> obsCol = null;
		
		if (om2ObsDoc != null) {

			AbstractObservation om2Obs = null;
			try {
				om2Obs = om2Decoder.parseObservation(om2ObsDoc.xmlText());
			} catch (Exception e) {
				OwsExceptionReport se = new OwsExceptionReport();
				se.addCodedException(
						ExceptionCode.InvalidParameterValue,
						null,
						"Error while parsing: " + e.getLocalizedMessage());
				throw se;
			}
			
			// convert om2->om1 Observation
			obsCol = new ArrayList<AbstractSosObservation>();
			obsCol.add(ObservationConverter.getOM1Obs(om2Obs));
			
		} else if (xb_obsType instanceof MeasurementType) {
			obsCol = om1Decoder.parseMeasurement((MeasurementType) xb_obsType,
					mobileEnabled);
		} else if (xb_obsType instanceof CategoryObservationType) {
			obsCol = om1Decoder.parseCategoryObservation(
					(CategoryObservationType) xb_obsType, mobileEnabled);
		} else if (xb_obsType instanceof GeometryObservationType) {
			obsCol = om1Decoder.parseSpatialObservation(
					(GeometryObservationType) xb_obsType, mobileEnabled);
		} else {
			// GenericObservation
			obsCol = om1Decoder.parseGenericObservation(xb_obsType,
					mobileEnabled);
		}
		return new SosInsertObservationRequest(assignedSensorID, obsCol,
				mobileEnabled);
	}

	/**
	 * parses a RegisterSensorDocument and returns a SosRegisterSensorRequest
	 * 
	 * @param xb_regSensDoc
	 *            the XMLBeans document of the RegisterSensor request
	 * @return Returns SosRegisterSensorRequest
	 * @throws OwsExceptionReport
	 *             if request is incorrect or not valid
	 */
	@SuppressWarnings("unchecked")
	public SosRegisterSensorRequest parseRegisterSensorRequest(
			RegisterSensorDocument xb_regSensDoc) throws OwsExceptionReport {

		// validateDocument(xb_regSensDoc);

		SosRegisterSensorRequest request = null;

		RegisterSensor xb_regSens = xb_regSensDoc.getRegisterSensor();
		SensorDescription xb_sensDesc = xb_regSens.getSensorDescription();
		XmlObject xb_obsTemplate = xb_regSens.getObservationTemplate();

		Collection<SosAbstractFeature> foi_col = null;

		// check whether observation template fits the supported types
		try {
			XmlObject xb_object = XmlObject.Factory.parse(xb_obsTemplate
					.toString());
			if (xb_object instanceof MeasurementDocument
					|| xb_object instanceof CategoryObservationDocument
					|| xb_object instanceof GeometryObservationDocument || // O&M
																			// 1
																			// only
					xb_object instanceof ObservationDocument) {

				// parse fois from template
				FeaturePropertyType xb_fpt = ((ObservationTemplate) xb_obsTemplate)
						.getObservation().getFeatureOfInterest();
				Map<String, SosAbstractFeature> foi_map = featureDecoder
						.parseFeatureCollection(xb_fpt);
				foi_col = foi_map.values();

			} else if (xb_object instanceof OMMeasurementDocument) {

				// TODO handle new observation types

				// TODO parse featureOfInterest

			} else if (xb_object instanceof OMCategoryObservationDocument) {

				// TODO parse featureOfInterest

			} else if (xb_object instanceof OMUncertaintyObservationDocument) {

				// TODO parse featureOfInterest

			} else if (xb_object instanceof OMObservationDocument) {

				// TODO parse featureOfInterest

			} else {
				OwsExceptionReport se = new OwsExceptionReport();
				se.addCodedException(
						ExceptionCode.InvalidParameterValue,
						null,
						"52 North SOS currently only allows measurements, category observations, geometry observations, uncertainty observations and observation (for generic Observation) to be inserted!!");
				throw se;
			}
		} catch (XmlException xmle) {
			OwsExceptionReport se = new OwsExceptionReport();
			se.addCodedException(
					ExceptionCode.InvalidParameterValue,
					null,
					"Parsing of observation template failed because: "
							+ xmle.getLocalizedMessage());
			throw se;
		}

		// parse sensorML
		SensorMLDocument xb_sensorML = null;
		SystemType xb_system = null;
		String smlFile = "";
		try {
			xb_sensorML = SensorMLDocument.Factory
					.parse(xb_sensDesc.toString());
			smlFile = xb_sensorML.xmlText();
			Member[] xb_memberArray = xb_sensorML.getSensorML()
					.getMemberArray();
			if (xb_memberArray.length == 1) {
				AbstractProcessType xb_proc = xb_memberArray[0].getProcess();
				if (xb_proc instanceof SystemType) {
					xb_system = (SystemType) xb_proc;
				}
			}
		} catch (XmlException xmle) {
			OwsExceptionReport se = new OwsExceptionReport();
			se.addCodedException(
					ExceptionCode.InvalidParameterValue,
					null,
					"52 North SOS currently only allows sml:Systems to be registered!! Parsing of sml:System failed because: "
							+ xmle.getLocalizedMessage());
			throw se;
		}
		SensorSystem sensorSystem;
		if (xb_system != null) {
			sensorSystem = SensorMLDecoder.parseSystem(xb_system, smlFile);
		} else {
			sensorSystem = SensorMLDecoder.parseSystem(
					SystemType.Factory.newInstance(), smlFile);
		}
		String systemDescription = xb_sensDesc.toString();

		SosAbstractFeature domFeat = null;
		Collection<SosAbstractFeature> df_col = null;

		// parse domain features if mobile request
		if (xb_regSens.getMobileEnabled()) {
			FeaturePropertyType[] xb_domainFeatures = xb_regSens
					.getDomainFeatureArray();
			if (xb_domainFeatures.length > 0) {
				df_col = new ArrayList<SosAbstractFeature>();
				for (FeaturePropertyType xb_df : xb_domainFeatures) {
					domFeat = featureDecoder.parseGenericDomainFeature(xb_df);
					df_col.add(domFeat);
				}
			}
		}

		request = new SosRegisterSensorRequest(sensorSystem,
				sensorSystem.getOutputs(), systemDescription, foi_col, df_col,
				xb_regSens.getMobileEnabled());
		return request;
	}

	/**
	 * parses the XMLBeans representation of the UpdateSensorDocument and
	 * creates an SosUpdateSensorRequest
	 * 
	 * @param xb_usDoc
	 *            XMLBeans representation of the UpdateSensor request
	 * @return Returns SOSmobile representation of the UpdateSensor request
	 * @throws OwsExceptionReport
	 *             if validation of request failed
	 */
	public AbstractSosRequest parseUpdateSensorRequest(
			UpdateSensorDocument xb_usDoc) throws OwsExceptionReport {

		// validateDocument(xb_usDoc);

		SosUpdateSensorRequest updateSensorRequest = null;
		UpdateSensor xb_us = xb_usDoc.getUpdateSensor();

		Point point = null;
		SosAbstractFeature domFeat = null;

		String procID = xb_us.getSensorID();
		TimePositionType time = xb_us.getTimeStamp().getTimePosition();
		boolean mobile = xb_us.getIsMobile();
		boolean active = xb_us.getIsActive();

		PositionType xb_pos = xb_us.getPosition();
		if (xb_pos != null) {
			point = SensorMLDecoder.parsePointPosition(xb_pos);
		}

		FeaturePropertyType xb_fpt = xb_us.getDomainFeature();
		if (xb_fpt != null) {
			domFeat = featureDecoder.parseGenericDomainFeature(xb_fpt);
		}

		updateSensorRequest = new SosUpdateSensorRequest(procID, time, point,
				mobile, active, domFeat);

		return updateSensorRequest;
	}
}
