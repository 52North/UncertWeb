package org.uncertweb.api.om.io;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import net.opengis.gml.x32.BoundingShapeType;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.MeasureType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.TimeInstantDocument;
import net.opengis.gml.x32.TimeInstantPropertyType;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodDocument;
import net.opengis.gml.x32.TimePeriodPropertyType;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePrimitivePropertyType;
import net.opengis.gml.x32.UncertaintyPropertyType;
import net.opengis.gml.x32.UnitDefinitionType;
import net.opengis.om.x20.FoiPropertyType;
import net.opengis.om.x20.OMAbstractObservationType;
import net.opengis.om.x20.OMBooleanObservationCollectionDocument;
import net.opengis.om.x20.OMBooleanObservationDocument;
import net.opengis.om.x20.OMDiscreteNumericObservationCollectionDocument;
import net.opengis.om.x20.OMDiscreteNumericObservationDocument;
import net.opengis.om.x20.OMMeasurementCollectionDocument;
import net.opengis.om.x20.OMMeasurementDocument;
import net.opengis.om.x20.OMObservationDocument;
import net.opengis.om.x20.OMReferenceObservationCollectionDocument;
import net.opengis.om.x20.OMReferenceObservationDocument;
import net.opengis.om.x20.OMTextObservationDocument;
import net.opengis.om.x20.OMUncertaintyObservationCollectionDocument;
import net.opengis.om.x20.OMUncertaintyObservationDocument;
import net.opengis.om.x20.UWBooleanObservationType;
import net.opengis.om.x20.UWDiscreteNumericObservationType;
import net.opengis.om.x20.UWMeasurementType;
import net.opengis.om.x20.UWReferenceObservationType;
import net.opengis.om.x20.UWTextObservationType;
import net.opengis.om.x20.UWUncertaintyObservationType;
import net.opengis.om.x20.OMBooleanObservationCollectionDocument.OMBooleanObservationCollection;
import net.opengis.om.x20.OMDiscreteNumericObservationCollectionDocument.OMDiscreteNumericObservationCollection;
import net.opengis.om.x20.OMMeasurementCollectionDocument.OMMeasurementCollection;
import net.opengis.om.x20.OMReferenceObservationCollectionDocument.OMReferenceObservationCollection;
import net.opengis.om.x20.OMUncertaintyObservationCollectionDocument.OMUncertaintyObservationCollection;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.isotc211.x2005.gmd.DQElementPropertyType;
import org.isotc211.x2005.gmd.DQQuantitativeAttributeAccuracyDocument;
import org.isotc211.x2005.gmd.DQQuantitativeAttributeAccuracyType;
import org.isotc211.x2005.gmd.DQUncertaintyResultDocument;
import org.isotc211.x2005.gmd.DQUncertaintyResultType;
import org.isotc211.x2005.gmd.DQUncertaintyResultType.Value;
import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyEncoderException;
import org.uncertml.exception.UnsupportedUncertaintyTypeException;
import org.uncertml.io.XMLEncoder;
import org.uncertml.x20.AbstractUncertaintyDocument;
import org.uncertweb.api.gml.geometry.GmlLineString;
import org.uncertweb.api.gml.geometry.GmlMultiGeometry;
import org.uncertweb.api.gml.geometry.GmlPoint;
import org.uncertweb.api.gml.geometry.GmlPolygon;
import org.uncertweb.api.gml.geometry.RectifiedGrid;
import org.uncertweb.api.gml.io.XmlBeansGeometryEncoder;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.OMConstants;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.collections.BooleanObservationCollection;
import org.uncertweb.api.om.observation.collections.DiscreteNumericObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.ReferenceObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.BooleanResult;
import org.uncertweb.api.om.result.IntegerResult;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.ReferenceResult;
import org.uncertweb.api.om.result.TextResult;
import org.uncertweb.api.om.result.UncertaintyResult;

/**
 * encodes Observations by xmlBeans
 * 
 * @author Kiesow, staschc
 * 
 */
public class XBObservationEncoder implements IObservationEncoder {

	/**
	 * encodes an observation collection
	 * 
	 * @param obsCol
	 *            observation collection
	 * @return observation collections's xml document as formatted String
	 * @throws Exception
	 * 			if encoding fails
	 */
	@Override
	public String encodeObservationCollection(IObservationCollection obsCol)
			throws Exception {
		return encodeObservationCollectionDocument(obsCol).xmlText(
				getOMOptions());
	}

	/**
	 * encodes an observation collection
	 * 
	 * @param obsCol
	 *      observation collection
	 * @return observation collections's xml document
	 * @throws Exception
	 * 		if encoding fails
	 */
	public XmlObject encodeObservationCollectionDocument(
			IObservationCollection obsCol) throws Exception {
		
		//BooleanObservation collection
		if (obsCol instanceof BooleanObservationCollection){
			OMBooleanObservationCollectionDocument result = OMBooleanObservationCollectionDocument.Factory.newInstance();
			OMBooleanObservationCollection xb_boCol = result.addNewOMBooleanObservationCollection();
			if (obsCol.getGmlId()!=null){
				xb_boCol.setId(obsCol.getGmlId());
			}
			Iterator<BooleanObservation> obsIter = ((BooleanObservationCollection) obsCol).getMembers().iterator();
			while (obsIter.hasNext()){
				UWBooleanObservationType xb_obs = xb_boCol.addNewOMBooleanObservation();
				OMObservationDocument xb_boDoc = encodeObservationDocument(obsIter.next());
				xb_obs.set(xb_boDoc.getOMObservation());
			}
			return result;
		}
		//Measurement collection
		else if (obsCol instanceof MeasurementCollection){
			OMMeasurementCollectionDocument result = OMMeasurementCollectionDocument.Factory.newInstance();
			OMMeasurementCollection xb_boCol = result.addNewOMMeasurementCollection();
			if (obsCol.getGmlId()!=null){
				xb_boCol.setId(obsCol.getGmlId());
			}
			Iterator<Measurement> obsIter = ((MeasurementCollection) obsCol).getMembers().iterator();
			while (obsIter.hasNext()){
				UWMeasurementType xb_obs = xb_boCol.addNewOMMeasurement();
				OMObservationDocument xb_boDoc = encodeObservationDocument(obsIter.next());
				xb_obs.set(xb_boDoc.getOMObservation());
			}

			return result;
		}
		//ReferenceObservation collection
		else if (obsCol instanceof ReferenceObservationCollection){
			OMReferenceObservationCollectionDocument result = OMReferenceObservationCollectionDocument.Factory.newInstance();
			OMReferenceObservationCollection xb_boCol = result.addNewOMReferenceObservationCollection();
			if (obsCol.getGmlId()!=null){
				xb_boCol.setId(obsCol.getGmlId());
			}
			Iterator<BooleanObservation> obsIter = ((BooleanObservationCollection) obsCol).getMembers().iterator();
			while (obsIter.hasNext()){
				UWReferenceObservationType xb_obs = xb_boCol.addNewOMReferenceObservation();
				OMObservationDocument xb_boDoc = encodeObservationDocument(obsIter.next());
				xb_obs.set(xb_boDoc.getOMObservation());
			}
			return result;
		}
		//UncertaintyObservation collection
		else if (obsCol instanceof UncertaintyObservationCollection){
			OMUncertaintyObservationCollectionDocument result = OMUncertaintyObservationCollectionDocument.Factory.newInstance();
			OMUncertaintyObservationCollection xb_boCol = result.addNewOMUncertaintyObservationCollection();
			if (obsCol.getGmlId()!=null){
				xb_boCol.setId(obsCol.getGmlId());
			}
			Iterator<BooleanObservation> obsIter = ((BooleanObservationCollection) obsCol).getMembers().iterator();
			while (obsIter.hasNext()){
				UWUncertaintyObservationType xb_obs = xb_boCol.addNewOMUncertaintyObservation();
				OMObservationDocument xb_boDoc = encodeObservationDocument(obsIter.next());
				xb_obs.set(xb_boDoc.getOMObservation());
			}
			return result;
		}
		//DiscreteNumericObservation collection
		else if (obsCol instanceof DiscreteNumericObservationCollection){
			OMDiscreteNumericObservationCollectionDocument result = OMDiscreteNumericObservationCollectionDocument.Factory.newInstance();
			OMDiscreteNumericObservationCollection xb_boCol = result.addNewOMDiscreteNumericObservationCollection();
			if (obsCol.getGmlId()!=null){
				xb_boCol.setId(obsCol.getGmlId());
			}
			Iterator<BooleanObservation> obsIter = ((BooleanObservationCollection) obsCol).getMembers().iterator();
			while (obsIter.hasNext()){
				UWDiscreteNumericObservationType xb_obs = xb_boCol.addNewOMDiscreteNumericObservation();
				OMObservationDocument xb_boDoc = encodeObservationDocument(obsIter.next());
				xb_obs.set(xb_boDoc.getOMObservation());
			}
			return result;
		}
		
		else {
			throw new Exception("Collection type is not supported by encoder!");
		}
		
	}

	/**
	 * encodes an {@link OMObservationDocument}
	 * 
	 * @param obs
	 *            observation
	 * @return observation's xml document as formatted String
	 * @throws Exception
	 */
	@Override
	public String encodeObservation(AbstractObservation obs) throws Exception {
		return encodeObservationDocument(obs).xmlText(getOMOptions());
	}

	/**
	 * encodes an {@link OMObservationDocument}
	 * 
	 * @param obs
	 *            observation
	 * @return observation's xml document
	 * @throws Exception
	 */
	public OMObservationDocument encodeObservationDocument(
			AbstractObservation obs) throws Exception {

		// define observation type
		OMObservationDocument xb_obsDoc = null;
		OMAbstractObservationType xb_obs = null;

		if (obs.getResult() instanceof BooleanResult) {

			xb_obsDoc = OMBooleanObservationDocument.Factory.newInstance();
			xb_obs = ((OMBooleanObservationDocument) xb_obsDoc)
					.addNewOMBooleanObservation();

		} else if (obs.getResult() instanceof MeasureResult) {

			xb_obsDoc = OMMeasurementDocument.Factory.newInstance();
			xb_obs = ((OMMeasurementDocument) xb_obsDoc).addNewOMMeasurement();

		} else if (obs.getResult() instanceof IntegerResult) {

			xb_obsDoc = OMDiscreteNumericObservationDocument.Factory
					.newInstance();
			xb_obs = ((OMDiscreteNumericObservationDocument) xb_obsDoc)
					.addNewOMDiscreteNumericObservation();

		} else if (obs.getResult() instanceof TextResult) {

			xb_obsDoc = OMTextObservationDocument.Factory.newInstance();
			xb_obs = ((OMTextObservationDocument) xb_obsDoc)
					.addNewOMTextObservation();

		} else if (obs.getResult() instanceof UncertaintyResult) {

			xb_obsDoc = OMUncertaintyObservationDocument.Factory.newInstance();
			xb_obs = ((OMUncertaintyObservationDocument) xb_obsDoc)
					.addNewOMUncertaintyObservation();

		} else if (obs.getResult() instanceof ReferenceResult) {

			xb_obsDoc = OMReferenceObservationDocument.Factory.newInstance();
			xb_obs = ((OMReferenceObservationDocument) xb_obsDoc)
					.addNewOMReferenceObservation();
		}

		// encode id
		xb_obs.setId(obs.getGmlId());

		// encode boundedBy (optional parameter)
		if (obs.getBoundedBy() != null) {
			encodeBoundedBy(xb_obs, obs);
		}

		// encode phenomenonTime
		encodePhenomenonTime(xb_obs, obs);

		// encode resultTime
		encodeResultTime(xb_obs, obs);

		// encode validTime (optional parameter)
		if (obs.getValidTime() != null) {
			encodeValidTime(xb_obs, obs);
		}

		// encode procedure
		xb_obs.addNewProcedure().setHref(obs.getProcedure().toString());

		// encode observedProperty
		xb_obs.addNewObservedProperty().setHref(
				obs.getObservedProperty().toString());

		// encode featureOfInterest
		encodeFeatureOfInterest(xb_obs, obs);

		// encode resultQuality (optional parameter)
		if (obs.getResultQuality() != null && obs.getResultQuality().length > 0) {
			encodeResultQuality(xb_obs, obs);
		}

		// encode result
		encodeResult(xb_obs, obs);

		return xb_obsDoc;
	}

	/**
	 * helper method for encoding the boundedBy property from an envelope
	 * 
	 * @param xb_observation
	 * @param obs
	 */
	private void encodeBoundedBy(OMAbstractObservationType xb_observation,
			AbstractObservation obs) {
		BoundingShapeType xb_boundedBy = xb_observation.addNewBoundedBy();

		EnvelopeType xb_envelope = xb_boundedBy.addNewEnvelope();
		xb_envelope.addNewUpperCorner().setStringValue(
				encodePositionString(obs.getBoundedBy().getMaxX(), obs
						.getBoundedBy().getMaxY()));
		xb_envelope.addNewLowerCorner().setStringValue(
				encodePositionString(obs.getBoundedBy().getMinX(), obs
						.getBoundedBy().getMinY()));
	}

	/**
	 * helper method for encoding the phenomenonTime property
	 * 
	 * @param xb_observation
	 * @param obs
	 * @throws Exception
	 */
	private void encodePhenomenonTime(OMAbstractObservationType xb_observation,
			AbstractObservation obs) throws Exception {

		TimePrimitivePropertyType xb_phenTime = xb_observation
				.addNewPhenomenonTime();
		if (obs.getPhenomenonTime().getDateTime() != null) {

			TimeInstantDocument xb_tiDoc = TimeInstantDocument.Factory
					.newInstance();
			TimeInstantType xb_timeInstant = xb_tiDoc.addNewTimeInstant();

			xb_timeInstant.addNewTimePosition().setStringValue(
					obs.getPhenomenonTime().getDateTime().toString());
			// TODO use DateTimeFormatter? .toString(String pattern)

			xb_timeInstant.setId(obs.getPhenomenonTime().getId());

			xb_phenTime.set(xb_tiDoc);

		} else if (obs.getPhenomenonTime().getInterval() != null) {
			TimePeriodDocument xb_tpDoc = TimePeriodDocument.Factory.newInstance();

			TimePeriodType xb_timePeriod = xb_tpDoc.addNewTimePeriod();
			xb_timePeriod
					.addNewBegin()
					.addNewTimeInstant()
					.addNewTimePosition()
					.setStringValue(
							obs.getPhenomenonTime().getInterval().getStart()
									.toString());
			// TODO use DateTimeFormatter? .toString(String pattern)
			xb_timePeriod
					.addNewEnd()
					.addNewTimeInstant()
					.addNewTimePosition()
					.setStringValue(
							obs.getPhenomenonTime().getInterval().getEnd()
									.toString());
			// TODO use DateTimeFormatter? .toString(String pattern)

			xb_timePeriod.setId(obs.getPhenomenonTime().getId());
			xb_phenTime.set(xb_tpDoc);

		} else if (obs.getPhenomenonTime().getHref() != null) {

			xb_phenTime.setHref(obs.getPhenomenonTime().getHref());

		} else {
			throw new Exception(
					"PhenomenonTime has to be an instant, an interval, or a reference to another time property.");
		}
	}

	/**
	 * helper method for encoding the resultTime property
	 * 
	 * @param xb_observation
	 * @param obs
	 * @throws Exception
	 */
	private void encodeResultTime(OMAbstractObservationType xb_observation,
			AbstractObservation obs) throws Exception {

		TimeInstantPropertyType xb_resultTime = xb_observation
				.addNewResultTime();

		if (obs.getResultTime().getDateTime() != null) {
			TimeInstantType xb_timeInstant = xb_resultTime.addNewTimeInstant();

			xb_timeInstant.addNewTimePosition().setStringValue(
					obs.getResultTime().getDateTime().toString());
			// TODO use DateTimeFormatter? .toString(String pattern)

			xb_timeInstant.setId(obs.getResultTime().getId());

		} else if (obs.getResultTime().getHref() != null) {

			xb_resultTime.setHref(obs.getResultTime().getHref());

		} else {
			throw new Exception(
					"ResultTime has to be an instant, or a reference to another time property.");
		}
	}

	/**
	 * helper method for encoding the validTime property
	 * 
	 * @param xb_observation
	 * @param obs
	 * @throws Exception
	 */
	private void encodeValidTime(OMAbstractObservationType xb_observation,
			AbstractObservation obs) throws Exception {
		TimePeriodPropertyType xb_validTime = xb_observation.addNewValidTime();
		if (obs.getValidTime().getInterval() != null) {

			TimePeriodType xb_timePeriod = xb_validTime.addNewTimePeriod();

			xb_timePeriod
					.addNewBegin()
					.addNewTimeInstant()
					.addNewTimePosition()
					.setStringValue(
							obs.getValidTime().getInterval().getStart()
									.toString());
			// TODO use DateTimeFormatter? .toString(String pattern)
			xb_timePeriod
					.addNewEnd()
					.addNewTimeInstant()
					.addNewTimePosition()
					.setStringValue(
							obs.getValidTime().getInterval().getEnd()
									.toString());
			// TODO use DateTimeFormatter? .toString(String pattern)

			xb_timePeriod.setId(obs.getValidTime().getId());

		} else if (obs.getValidTime().getHref() != null) {

			xb_validTime.setHref(obs.getValidTime().getHref());

		} else {
			throw new Exception(
					"ValidTime has to be an interval, or a reference to another time property.");
		}
	}

	/**
	 * helper method for encoding featureOfInterest property
	 * 
	 * @param xb_observation
	 * @param obs
	 * @throws Exception
	 */
	private void encodeFeatureOfInterest(
			OMAbstractObservationType xb_observation, AbstractObservation obs)
			throws Exception {

		FoiPropertyType xb_foi = xb_observation
				.addNewFeatureOfInterest();
		
		if (obs.getFeatureOfInterest().getHref()!=null&&!obs.getFeatureOfInterest().equals("")){
			xb_foi.setHref(obs.getFeatureOfInterest().getHref());
		}
		
		else {
			SFSpatialSamplingFeatureType xb_sfType = xb_foi.addNewSFSpatialSamplingFeature();
		xb_sfType.setId(obs.getFeatureOfInterest().getGmlId());

		if (obs.getFeatureOfInterest().getBoundedBy() != null) {
			// TODO add boundedBy
		}

		if (obs.getFeatureOfInterest().getSampledFeature() != null
				&& obs.getFeatureOfInterest().getSampledFeature() != "") {
			xb_sfType.addNewSampledFeature().setHref(
					obs.getFeatureOfInterest().getSampledFeature());
		} else {
			xb_sfType.setNilSampledFeature();
		}
		ReferenceType xb_rt = xb_sfType.addNewType();
		xb_rt.setHref(obs.getFeatureOfInterest().getFeatureType());
		ShapeType xb_shape = xb_sfType.addNewShape();
		XmlBeansGeometryEncoder encoder = new XmlBeansGeometryEncoder();

		XmlObject xb_geometry=null;
		if (obs.getFeatureOfInterest().getShape() instanceof GmlPoint) {
			xb_geometry = encoder.encodePoint2Doc((GmlPoint) obs
					.getFeatureOfInterest().getShape());

		} else if (obs.getFeatureOfInterest().getShape() instanceof GmlPolygon) {
			xb_geometry = encoder
					.encodePolygon2Doc((GmlPolygon) obs.getFeatureOfInterest()
							.getShape());
		} else if (obs.getFeatureOfInterest().getShape() instanceof GmlLineString) {
			xb_geometry = encoder
					.encodeLineString2Doc((GmlLineString) obs
							.getFeatureOfInterest().getShape());
		} else if (obs.getFeatureOfInterest().getShape() instanceof RectifiedGrid) {
			xb_geometry = encoder
					.encodeRectifiedGrid2Doc((RectifiedGrid) obs
							.getFeatureOfInterest().getShape());

		} else if (obs.getFeatureOfInterest().getShape() instanceof GmlMultiGeometry) {
			xb_geometry = encoder
					.encodeMultiGeometry2Doc((GmlMultiGeometry) obs
							.getFeatureOfInterest().getShape());

		} else {
			throw new Exception(
					"Geometry type is not supported by UncertWeb GML profile!");
		}
		xb_shape.set(xb_geometry);
		
		}
	}

	/**
	 * helper method for encoding resultQuality property
	 * 
	 * @param xb_observation
	 * @param obs
	 * @throws UncertaintyEncoderException 
	 * @throws UnsupportedUncertaintyTypeException 
	 * @throws XmlException 
	 */
	private void encodeResultQuality(OMAbstractObservationType xb_observation,
			AbstractObservation obs) throws XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException {

		for (DQ_UncertaintyResult resultObject : obs.getResultQuality()) {

			
			DQUncertaintyResultDocument xb_dqURDoc = DQUncertaintyResultDocument.Factory
					.newInstance();
			DQUncertaintyResultType xb_dqUncRes = xb_dqURDoc
					.addNewDQUncertaintyResult();

			if (resultObject.getId() != null) {
				xb_dqUncRes.setId(resultObject.getId());
			}
			if (resultObject.getUuid() != null) {
				xb_dqUncRes.setUuid(resultObject.getUuid());
			}
			//add value unit
			if (resultObject.getValueUnit() != null) {
				UnitDefinitionType xb_vu = xb_dqUncRes.addNewValueUnit().addNewUnitDefinition();
				xb_vu.setId(resultObject.getValueUnit().getGmlId());
				xb_vu.addNewIdentifier().setStringValue(resultObject.getValueUnit().getIdentifier());
			}
			//encode uncertainty value
			IUncertainty[] valueArray = resultObject.getValues();
			for (int i=0;i<valueArray.length;i++) {
				IUncertainty value =valueArray[i];
				Value xb_value = xb_dqUncRes.addNewValue();//Value.Factory.newInstance();
				xb_value.set(encodeUncertainty(value));
			}
			

			DQQuantitativeAttributeAccuracyDocument xb_dqQAADoc = DQQuantitativeAttributeAccuracyDocument.Factory
					.newInstance();
			DQQuantitativeAttributeAccuracyType xb_dqQAA = xb_dqQAADoc
					.addNewDQQuantitativeAttributeAccuracy();

			xb_dqQAA.addNewResult().set(xb_dqURDoc);

			DQElementPropertyType xb_dqElemProp = xb_observation
					.addNewResultQuality();
			xb_dqElemProp.set(xb_dqQAADoc);
		}
	}

	/**
	 * helper method for encoding result property
	 * 
	 * @param xb_observation
	 * 			XmlBeans object to which the result should be written
	 * @param obs
	 * 			observation whose result should be encoded
	 * @throws UncertaintyEncoderException 
	 * 			if uncertainty cannot be encoded
	 * @throws UnsupportedUncertaintyTypeException
	 * 			if type of uncertainty is not supported by UncertML API 
	 * @throws XmlException 
	 * 			if encoding fails
	 */
	private void encodeResult(OMAbstractObservationType xb_obs,
			AbstractObservation obs) throws XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException {

		if (obs.getResult() instanceof BooleanResult
				&& xb_obs instanceof UWBooleanObservationType) {

			((UWBooleanObservationType) xb_obs).setResult(((BooleanResult) obs
					.getResult()).getBooleanValue());

		} else if (obs.getResult() instanceof MeasureResult
				&& xb_obs instanceof UWMeasurementType) {

			MeasureType xb_mResult = ((UWMeasurementType) xb_obs)
					.addNewResult();

			MeasureResult resultObject = (MeasureResult) obs.getResult();

			xb_mResult.setUom(resultObject.getUnitOfMeasurement());
			xb_mResult.setDoubleValue(resultObject.getMeasureValue());

		} else if (obs.getResult() instanceof IntegerResult
				&& xb_obs instanceof UWDiscreteNumericObservationType) {

			((UWDiscreteNumericObservationType) xb_obs)
					.setResult(((IntegerResult) obs.getResult())
							.getIntegerValue());

		} else if (obs.getResult() instanceof TextResult
				&& xb_obs instanceof UWTextObservationType) {

			((UWTextObservationType) xb_obs).setResult(((TextResult) obs
					.getResult()).getTextValue());

		} else if (obs.getResult() instanceof UncertaintyResult
				&& xb_obs instanceof UWUncertaintyObservationType) {

			UncertaintyPropertyType xb_uncResult = ((UWUncertaintyObservationType) xb_obs)
					.addNewResult();

			UncertaintyResult resultObject = (UncertaintyResult) obs
					.getResult();

			//encode UOM attribute, if UOM is set
			if (resultObject.getUnitOfMeasurement()!=null&&!resultObject.getUnitOfMeasurement().equals("")){
				xb_uncResult.setUom(resultObject.getUnitOfMeasurement());
			}
			xb_uncResult.addNewAbstractUncertainty().set(encodeUncertainty(
					(IUncertainty)resultObject.getUncertaintyValue()));

		} else if (obs.getResult() instanceof ReferenceResult
				&& xb_obs instanceof UWReferenceObservationType) {

			ReferenceType xb_refResult = ((UWReferenceObservationType) xb_obs)
					.addNewResult();

			ReferenceResult refObject = (ReferenceResult) obs.getResult();

			xb_refResult.setType(refObject.getType());
			xb_refResult.setHref(refObject.getHref());
			xb_refResult.setRole(refObject.getRole());
			xb_refResult.setArcrole(refObject.getArcrole());
			xb_refResult.setShow(refObject.getShow());
			xb_refResult.setActuate(refObject.getActuate());
		}
	}

	/**
	 * helper method for encoding 2D coordinates not mentioning their order
	 * 
	 * @param x
	 *            Coordinate
	 * @param y
	 *            Coordinate
	 * @return coordinates separated by a space character
	 */
	private String encodePositionString(double x, double y) {

		String posString = x + " " + y;
		return posString;
	}
	
	/**
	 * helper method used to encode uncertainty in UncertML
	 * 
	 * @param uncertainty
	 * 			uncertainty which should be encoded in UncertML XML
	 * @return String representing the uncertainty encoded as UncertML XML
	 * @throws XmlException
	 * 			if encoding fails
	 * @throws UnsupportedUncertaintyTypeException
	 * 			if uncertainty 
	 * @throws UncertaintyEncoderException
	 */
	private AbstractUncertaintyDocument encodeUncertainty(IUncertainty uncertainty) throws XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException{
		XMLEncoder unEncoder = new XMLEncoder();
		AbstractUncertaintyDocument object = AbstractUncertaintyDocument.Factory.parse(unEncoder.encode(uncertainty));
		return object;
	}

	/**
	 * method returns XmlOptions which are used by XmlBeans for a proper
	 * encoding
	 * 
	 * @return
	 * 
	 */
	private XmlOptions getOMOptions() {
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSaveAggressiveNamespaces();
		Map<String, String> lPrefixMap = new Hashtable<String, String>();
		lPrefixMap.put(OMConstants.NS_GML, OMConstants.NS_GML_PREFIX);
		lPrefixMap.put(OMConstants.NS_OM, OMConstants.NS_OM_PREFIX);
		lPrefixMap.put(OMConstants.NS_SAMS, OMConstants.NS_SAMS_PREFIX);
		lPrefixMap.put(OMConstants.NS_SA, OMConstants.NS_SA_PREFIX);
		lPrefixMap.put(OMConstants.NS_XLINK, OMConstants.NS_XLINK_PREFIX);
		xmlOptions.setSaveSuggestedPrefixes(lPrefixMap);
		xmlOptions.setSavePrettyPrint();
		return xmlOptions;
	}

}
