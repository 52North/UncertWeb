package org.uncertweb.api.om.io;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import net.opengis.gml.x32.AbstractTimePrimitiveType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.MeasureType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.TimeInstantPropertyType;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodPropertyType;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePrimitivePropertyType;
import net.opengis.gml.x32.UncertaintyPropertyType;
import net.opengis.om.x20.FoiPropertyType;
import net.opengis.om.x20.OMAbstractObservationType;
import net.opengis.om.x20.OMBooleanObservationDocument;
import net.opengis.om.x20.OMDiscreteNumericObservationDocument;
import net.opengis.om.x20.OMMeasurementDocument;
import net.opengis.om.x20.OMObservationCollectionDocument;
import net.opengis.om.x20.OMObservationDocument;
import net.opengis.om.x20.OMObservationPropertyType;
import net.opengis.om.x20.OMProcessPropertyType;
import net.opengis.om.x20.OMReferenceObservationDocument;
import net.opengis.om.x20.OMTextObservationDocument;
import net.opengis.om.x20.OMUncertaintyObservationDocument;
import net.opengis.om.x20.UWBooleanObservationType;
import net.opengis.om.x20.UWDiscreteNumericObservationType;
import net.opengis.om.x20.UWMeasurementType;
import net.opengis.om.x20.UWReferenceObservationType;
import net.opengis.om.x20.UWTextObservationType;
import net.opengis.om.x20.UWUncertaintyObservationType;
import net.opengis.samplingSpatial.x20.ShapeType;

import org.apache.xmlbeans.XmlObject;
import org.isotc211.x2005.gco.UnitOfMeasurePropertyType;
import org.isotc211.x2005.gmd.DQElementPropertyType;
import org.isotc211.x2005.gmd.DQResultPropertyType;
import org.isotc211.x2005.gmd.DQUncertaintyResultType;
import org.isotc211.x2005.gmd.DQUncertaintyResultType.Value;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.XMLParser;
import org.uncertweb.api.gml.io.XmlBeansGeometryParser;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.UnitDefinition;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.DiscreteNumericObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.ObservationCollection;
import org.uncertweb.api.om.observation.ReferenceObservation;
import org.uncertweb.api.om.observation.TextObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.result.BooleanResult;
import org.uncertweb.api.om.result.IntegerResult;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.ReferenceResult;
import org.uncertweb.api.om.result.TextResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.w3.x1999.xlink.ActuateAttribute.Actuate;
import org.w3.x1999.xlink.ShowAttribute.Show;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * parses an {@link Observation} and it's {@link SpatialSamplingFeature}
 * 
 * @author Kiesow, staschc
 * 
 */
public class XBObservationParser implements IObservationParser {

	/**
	 * parses an {@link OMObservationCollectionDocument}
	 * 
	 * @param xmlObsCol
	 *            String containing an XML encoded observation collection
	 * @return POJO observation collection
	 * @throws Exception
	 *             if parsing of observations fails
	 */
	@Override
	public ObservationCollection parseObservationCollection(String xmlObsCol)
			throws Exception {

		ObservationCollection obsCol = new ObservationCollection();
		XmlObject xb_obsColDoc = XmlObject.Factory.parse(xmlObsCol);

		if (xb_obsColDoc instanceof OMObservationCollectionDocument) {

			OMObservationPropertyType[] obsMemberArray = ((OMObservationCollectionDocument) xb_obsColDoc)
					.getOMObservationCollection().getMemberArray();

			for (int i = 0; i < obsMemberArray.length; i++) {

				OMObservationDocument xb_obsDoc = null;
				OMAbstractObservationType xb_obs = null;

				if (obsMemberArray[i].getOMObservation() instanceof UWBooleanObservationType) {
					xb_obsDoc = OMBooleanObservationDocument.Factory
							.newInstance();
					xb_obs = ((OMBooleanObservationDocument) xb_obsDoc)
							.addNewOMBooleanObservation();
					xb_obs.set(obsMemberArray[i].getOMObservation());

				} else if (obsMemberArray[i].getOMObservation() instanceof UWDiscreteNumericObservationType) {
					xb_obsDoc = OMDiscreteNumericObservationDocument.Factory
							.newInstance();
					xb_obs = ((OMDiscreteNumericObservationDocument) xb_obsDoc)
							.addNewOMDiscreteNumericObservation();
					xb_obs.set(obsMemberArray[i].getOMObservation());

				} else if (obsMemberArray[i].getOMObservation() instanceof UWMeasurementType) {
					xb_obsDoc = OMMeasurementDocument.Factory.newInstance();
					xb_obs = ((OMMeasurementDocument) xb_obsDoc)
							.addNewOMMeasurement();
					xb_obs.set(obsMemberArray[i].getOMObservation());

				} else if (obsMemberArray[i].getOMObservation() instanceof UWReferenceObservationType) {
					xb_obsDoc = OMReferenceObservationDocument.Factory
							.newInstance();
					xb_obs = ((OMReferenceObservationDocument) xb_obsDoc)
							.addNewOMReferenceObservation();
					xb_obs.set(obsMemberArray[i].getOMObservation());

				} else if (obsMemberArray[i].getOMObservation() instanceof UWTextObservationType) {
					xb_obsDoc = OMTextObservationDocument.Factory.newInstance();
					xb_obs = ((OMTextObservationDocument) xb_obsDoc)
							.addNewOMTextObservation();
					xb_obs.set(obsMemberArray[i].getOMObservation());

				} else if (obsMemberArray[i].getOMObservation() instanceof UWUncertaintyObservationType) {
					xb_obsDoc = OMUncertaintyObservationDocument.Factory
							.newInstance();
					xb_obs = ((OMUncertaintyObservationDocument) xb_obsDoc)
							.addNewOMUncertaintyObservation();
					xb_obs.set(obsMemberArray[i].getOMObservation());
				}

				obsCol.getMembers().add(parseObservationDocument(xb_obsDoc));
			}
		}

		return obsCol;
	}

	/**
	 * parses an Observation and it's SpatialSamplingFeature
	 * 
	 * @param xmlObs
	 *            xml observation document's string
	 * @return POJO observation
	 * @throws Exception
	 *             If parsing of observation fails.
	 */
	@Override
	public AbstractObservation parseObservation(String xmlObs) throws Exception {

		XmlObject xb_obsDoc = XmlObject.Factory.parse(xmlObs);

		return parseObservationDocument((OMObservationDocument) xb_obsDoc);
	}

	/**
	 * parses an Observation and it's SpatialSamplingFeature
	 * 
	 * @param xb_obsDoc
	 *            XMLBeans observation document
	 * @return POJO observation
	 * @throws Exception
	 *             If parsing of observation fails
	 */
	@Override
	public AbstractObservation parseObservationDocument(
			OMObservationDocument xb_obsDoc) throws Exception {

		AbstractObservation obs = null;

		if (xb_obsDoc instanceof OMObservationDocument) {
			OMAbstractObservationType xb_obsType = ((OMObservationDocument) xb_obsDoc)
					.getOMObservation();

			// parse id
			String gmlId = xb_obsType.getId();

			// parse boundedBy (optional parameter)
			Envelope boundedBy = null;

			if (!(xb_obsType.getBoundedBy() == null)) {
				boundedBy = parseEnvelope(xb_obsType.getBoundedBy()
						.getEnvelope());
			}

			// parse phenomenonTime
			TimeObject phenomenonTime = parseTimeProperty(xb_obsType
					.getPhenomenonTime());

			// parse resultTime
			TimeObject resultTime = parseTimeProperty(xb_obsType
					.getResultTime());

			// parse validTime (optional parameter)
			TimeObject validTime = null;

			if (!(xb_obsType.getValidTime() == null)) {
				validTime = parseTimeProperty(xb_obsType.getValidTime());
			}

			// parse procedure
			URI procedure = parseProcedure(xb_obsType.getProcedure());

			// parse observedProperty
			URI observedProperty = parseObservedProperty(xb_obsType
					.getObservedProperty());

			// parse featureOfInterest
			SpatialSamplingFeature featureOfInterest = parseSamplingFeature(xb_obsType
					.getFeatureOfInterest());

			// parse resultQuality (optional parameter)
			DQ_UncertaintyResult[] resultQuality = null;
			if (xb_obsType.getResultQualityArray() != null
					&& xb_obsType.getResultQualityArray().length > 0) {

				resultQuality = parseResultQuality(xb_obsType
						.getResultQualityArray());

			}

			// parse result and build observation
			if (xb_obsDoc instanceof OMMeasurementDocument) {
				MeasureType xb_measureType = ((OMMeasurementDocument) xb_obsDoc)
						.getOMMeasurement().getResult();
				double value = xb_measureType.getDoubleValue();
				String uom = xb_measureType.getUom();

				MeasureResult result = new MeasureResult(value, uom);
				obs = new Measurement(gmlId, boundedBy, phenomenonTime,
						resultTime, validTime, procedure, observedProperty,
						featureOfInterest, resultQuality, result);

			} else if (xb_obsDoc instanceof OMTextObservationDocument) {

				String value = ((OMTextObservationDocument) xb_obsDoc)
						.getOMTextObservation().getResult();

				TextResult result = new TextResult(value);
				obs = new TextObservation(gmlId, boundedBy, phenomenonTime,
						resultTime, validTime, procedure, observedProperty,
						featureOfInterest, resultQuality, result);

			} else if (xb_obsDoc instanceof OMUncertaintyObservationDocument) {

				UncertaintyPropertyType xb_uncPropType = ((OMUncertaintyObservationDocument) xb_obsDoc)
						.getOMUncertaintyObservation().getResult();

				System.out.println(xb_uncPropType.toString());
				XMLParser uncertaintyParser = new XMLParser();
				IUncertainty uncertainty = uncertaintyParser
						.parse(xb_uncPropType.toString());
				UncertaintyResult result = new UncertaintyResult(uncertainty);
				obs = new UncertaintyObservation(gmlId, boundedBy,
						phenomenonTime, resultTime, validTime, procedure,
						observedProperty, featureOfInterest, resultQuality,
						result);

			} else if (xb_obsDoc instanceof OMDiscreteNumericObservationDocument) {

				BigInteger value = ((OMDiscreteNumericObservationDocument) xb_obsDoc)
						.getOMDiscreteNumericObservation().getResult();

				IntegerResult result = new IntegerResult(value);
				obs = new DiscreteNumericObservation(gmlId, boundedBy,
						phenomenonTime, resultTime, validTime, procedure,
						observedProperty, featureOfInterest, resultQuality,
						result);

			} else if (xb_obsDoc instanceof OMBooleanObservationDocument) {

				boolean value = ((OMBooleanObservationDocument) xb_obsDoc)
						.getOMBooleanObservation().getResult();

				BooleanResult result = new BooleanResult(value);
				obs = new BooleanObservation(gmlId, boundedBy, phenomenonTime,
						resultTime, validTime, procedure, observedProperty,
						featureOfInterest, resultQuality, result);

			} else if (xb_obsDoc instanceof OMReferenceObservationDocument) {

				ReferenceType xb_referenceType = ((OMReferenceObservationDocument) xb_obsDoc)
						.getOMReferenceObservation().getResult();
				String type = xb_referenceType.getType();
				String href = xb_referenceType.getHref();
				String role = xb_referenceType.getRole();
				String arcrole = xb_referenceType.getArcrole();
				String title = xb_referenceType.getTitle();
				Show.Enum show = xb_referenceType.getShow();
				Actuate.Enum actuate = xb_referenceType.getActuate();

				ReferenceResult result = new ReferenceResult(type, href, role,
						arcrole, title, show, actuate);
				obs = new ReferenceObservation(gmlId, boundedBy,
						phenomenonTime, resultTime, validTime, procedure,
						observedProperty, featureOfInterest, resultQuality,
						result);
			}
		}
		return obs;
	}

	/**
	 * helper method for parsing a resultQuality array; ATTENTION: currently,
	 * only uncertainty results as defined in the UncertWeb profile are parsed;
	 * has to be extended, if further data quality types as defined in ISO 19139
	 * have to be supported.
	 * 
	 * @param xb_resultQualityArray
	 *            XmlBeans representation of resultQualities
	 * @return Array with POJO objects representing uncertainty results
	 * @throws UncertaintyParserException
	 */
	private DQ_UncertaintyResult[] parseResultQuality(
			DQElementPropertyType[] xb_resultQualityArray)
			throws UncertaintyParserException {

		DQ_UncertaintyResult[] resultQuality = new DQ_UncertaintyResult[0];

		ArrayList<DQ_UncertaintyResult> rqList = new ArrayList<DQ_UncertaintyResult>();

		XMLParser uncertaintyParser = new XMLParser();
		for (DQElementPropertyType xb_rq : xb_resultQualityArray) {

			DQResultPropertyType[] xb_resultArray = xb_rq
					.getAbstractDQElement().getResultArray();

			for (DQResultPropertyType xb_r : xb_resultArray) {

				if (!xb_r.isNil()
						&& !xb_r.getAbstractDQResult().isNil()
						&& xb_r.getAbstractDQResult() instanceof DQUncertaintyResultType) {

					DQUncertaintyResultType uncResult = (DQUncertaintyResultType) xb_r
							.getAbstractDQResult().changeType(
									DQUncertaintyResultType.type);

					String id = uncResult.getId();
					String uuid = uncResult.getUuid();

					UnitOfMeasurePropertyType valueUnit = uncResult
							.getValueUnit();
					String unitGmlId = valueUnit.getUnitDefinition().getId();
					String identifier = valueUnit.getUnitDefinition()
							.getIdentifier().getStringValue();
					UnitDefinition unitDef = new UnitDefinition(identifier,
							unitGmlId);
					Value[] value = uncResult.getValueArray();
					IUncertainty[] values = new IUncertainty[value.length];
					for (int i = 0; i < value.length; ++i) {
						IUncertainty uncertainty = uncertaintyParser
								.parse(value[i].toString());
						values[i] = uncertainty;
					}

					rqList.add(new DQ_UncertaintyResult(id, uuid, values,
							unitDef));
				}
			}
		}
		return rqList.toArray(resultQuality);
	}

	/**
	 * helper method for parsing observedProperty
	 * 
	 * @param xb_observedProperty
	 *            XmlBeans representation of observed property
	 * @return the property's URI
	 * @throws URISyntaxException
	 *             If creation of URI from observed property fails
	 */
	private URI parseObservedProperty(ReferenceType xb_observedProperty)
			throws URISyntaxException {

		return new URI(xb_observedProperty.getHref());
	}

	/**
	 * helper method for parsing procedure
	 * 
	 * @param xb_procedure
	 *            XmlBeans representation of procedure
	 * @return the procedure's URI
	 * @throws URISyntaxException
	 *             if creation of the URI out of the procedure property fails
	 */
	private URI parseProcedure(OMProcessPropertyType xb_procedure)
			throws URISyntaxException {

		return new URI(xb_procedure.getHref());
	}

	/**
	 * helper method for parsing time properteis
	 * 
	 * @param xb_absTimeObj
	 *            XmlBeans XmlObject representing the time property
	 * @return Returns Java representation of temporal property
	 */
	private TimeObject parseTimeProperty(XmlObject xb_absTimeObj) {

		TimeObject timeObject = null;
		DateTime dateTime = null;
		Interval interval = null;
		String href = null;

		if (xb_absTimeObj instanceof TimePrimitivePropertyType) {
			TimePrimitivePropertyType xb_tppt = (TimePrimitivePropertyType) xb_absTimeObj;
			href = xb_tppt.getHref();
			if (href == null || href.equals("")) {
				AbstractTimePrimitiveType xb_absTp = xb_tppt
						.getAbstractTimePrimitive();
				if (xb_absTp instanceof TimeInstantType) {

					dateTime = parseTimePosition(((TimeInstantType) xb_absTp)
							.getTimePosition().getStringValue());
					timeObject = new TimeObject(dateTime);

				} else if (xb_absTp instanceof TimePeriodType) {

					DateTime beginTime = parseTimeInstant(((TimePeriodType) xb_absTp)
							.getBegin().getTimeInstant());
					DateTime endTime = parseTimeInstant(((TimePeriodType) xb_absTp)
							.getEnd().getTimeInstant());

					interval = new Interval(beginTime.getMillis(), endTime
							.getMillis());
					timeObject = new TimeObject(interval);

				}
				timeObject.setId(xb_absTp.getId());

			} else {
				timeObject = new TimeObject(href);
			}

		} else if (xb_absTimeObj instanceof TimeInstantPropertyType) {
			TimeInstantPropertyType xb_tipt = (TimeInstantPropertyType) xb_absTimeObj;
			href = xb_tipt.getHref();
			if (href == null || href.equals("")) {

				dateTime = parseTimePosition(xb_tipt.getTimeInstant()
						.getTimePosition().getStringValue());

				timeObject = new TimeObject(dateTime);
				timeObject.setId(xb_tipt.getTimeInstant().getId());

			} else {
				timeObject = new TimeObject(href);
			}
		} else if (xb_absTimeObj instanceof TimePeriodPropertyType) {

			TimePeriodPropertyType xb_tppt = (TimePeriodPropertyType) xb_absTimeObj;
			href = xb_tppt.getHref();
			if (href == null || href.equals("")) {

				DateTime beginTime = parseTimeInstant(xb_tppt.getTimePeriod()
						.getBegin().getTimeInstant());
				DateTime endTime = parseTimeInstant(xb_tppt.getTimePeriod()
						.getEnd().getTimeInstant());

				interval = new Interval(beginTime.getMillis(), endTime
						.getMillis());
				timeObject = new TimeObject(interval);
				timeObject.setId(xb_tppt.getTimePeriod().getId());

			} else {
				timeObject = new TimeObject(href);
			}
		}

		return timeObject;
	}

	/**
	 * helper method for parsing time instant
	 * 
	 * @param timeInstant
	 * 
	 * @return
	 */
	private DateTime parseTimeInstant(TimeInstantType timeInstant) {
		DateTime dateTime = null;

		dateTime = parseTimePosition(timeInstant.getTimePosition()
				.getStringValue());

		return dateTime;
	}

	/**
	 * helper method for parsing timePosition to DateTime
	 * 
	 * @param timePosition
	 *            time as a string e.g. 1970-01-01T00:00:00Z
	 * @return time as an Object
	 */
	private DateTime parseTimePosition(String timePosition) {
		DateTime dateTime = null;

		DateTimeFormatter dtf = ISODateTimeFormat.dateTimeParser();
		dateTime = dtf.withOffsetParsed().parseDateTime(timePosition);

		return dateTime;
	}

	/**
	 * helper method for parsing a gml Envelope XmlBeans object to a jts
	 * envelope
	 * 
	 * @param env
	 *            XmlBeans representation of Envelope
	 * @return Returns JTS envelope
	 * @throws Exception
	 *             if parsing of envelope fails
	 */
	private Envelope parseEnvelope(EnvelopeType env) throws Exception {

		// get shape geometry
		GeometryFactory geomFac = new GeometryFactory();
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser(geomFac);

		DirectPositionType xb_upperCorner = env.getUpperCorner();
		Coordinate upperCoord = parser.parsePositionString(xb_upperCorner
				.getStringValue());

		DirectPositionType xb_lowerCorner = env.getLowerCorner();
		Coordinate lowerCoord = parser.parsePositionString(xb_lowerCorner
				.getStringValue());

		return new Envelope(upperCoord, lowerCoord);
	}

	/**
	 * parses a SpatialSamlingFeature from a feature of interest
	 * 
	 * @param xb_featureOfInterest
	 *            XmlBeans representation of SamplingFeature
	 * @return Returns POJO representation of sampling feature
	 * @throws Exception
	 *             if parsing of the feature fails
	 */
	@Override
	public SpatialSamplingFeature parseSamplingFeature(
			FoiPropertyType xb_featureOfInterest) throws Exception {

		SpatialSamplingFeature ssf = null;

		// if reference is set, create SamplingFeature and set reference
		if (xb_featureOfInterest.isSetHref()) {
			return new SpatialSamplingFeature(xb_featureOfInterest.getHref());
		}

		// FOI is encoded inline, so parse the feature
		else {
			// get id
			String id = xb_featureOfInterest.getSFSpatialSamplingFeature()
					.getId();

			// TODO add boundedBy, location

			// get id of the sampled feature
			String sampledFeature = null;

			if (!(xb_featureOfInterest.getSFSpatialSamplingFeature()
					.getSampledFeature().getAbstractFeature() == null)) {

				sampledFeature = xb_featureOfInterest
						.getSFSpatialSamplingFeature().getSampledFeature()
						.getAbstractFeature().getId();
			}

			// get shape geometry
			GeometryFactory geomFac = new GeometryFactory();
			XmlBeansGeometryParser parser = new XmlBeansGeometryParser(geomFac);

			ShapeType geomString = xb_featureOfInterest
					.getSFSpatialSamplingFeature().getShape();
			Geometry shape = parser.parseUwGeometry(geomString.toString());

			ssf = new SpatialSamplingFeature(id, sampledFeature, shape);

			return ssf;
		}
	}

}
