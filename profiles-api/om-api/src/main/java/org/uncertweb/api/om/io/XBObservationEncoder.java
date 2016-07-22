package org.uncertweb.api.om.io;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import net.opengis.gml.x32.AbstractGeometryDocument;
import net.opengis.gml.x32.AbstractGeometryType;
import net.opengis.gml.x32.BoundingShapeType;
import net.opengis.gml.x32.CodeWithAuthorityType;
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
import net.opengis.om.x20.OMBooleanObservationCollectionDocument.OMBooleanObservationCollection;
import net.opengis.om.x20.OMBooleanObservationDocument;
import net.opengis.om.x20.OMDiscreteNumericObservationCollectionDocument;
import net.opengis.om.x20.OMDiscreteNumericObservationCollectionDocument.OMDiscreteNumericObservationCollection;
import net.opengis.om.x20.OMDiscreteNumericObservationDocument;
import net.opengis.om.x20.OMMeasurementCollectionDocument;
import net.opengis.om.x20.OMMeasurementCollectionDocument.OMMeasurementCollection;
import net.opengis.om.x20.OMMeasurementDocument;
import net.opengis.om.x20.OMObservationDocument;
import net.opengis.om.x20.OMReferenceObservationCollectionDocument;
import net.opengis.om.x20.OMReferenceObservationCollectionDocument.OMReferenceObservationCollection;
import net.opengis.om.x20.OMReferenceObservationDocument;
import net.opengis.om.x20.OMTextObservationCollectionDocument;
import net.opengis.om.x20.OMTextObservationCollectionDocument.OMTextObservationCollection;
import net.opengis.om.x20.OMTextObservationDocument;
import net.opengis.om.x20.OMUncertaintyObservationCollectionDocument;
import net.opengis.om.x20.OMUncertaintyObservationCollectionDocument.OMUncertaintyObservationCollection;
import net.opengis.om.x20.OMUncertaintyObservationDocument;
import net.opengis.om.x20.UWBooleanObservationType;
import net.opengis.om.x20.UWDiscreteNumericObservationType;
import net.opengis.om.x20.UWMeasurementType;
import net.opengis.om.x20.UWReferenceObservationType;
import net.opengis.om.x20.UWTextObservationType;
import net.opengis.om.x20.UWUncertaintyObservationType;
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
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.gml.geometry.RectifiedGrid;
import org.uncertweb.api.gml.io.XmlBeansGeometryEncoder;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.OMConstants;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.DiscreteNumericObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.TextObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.BooleanObservationCollection;
import org.uncertweb.api.om.observation.collections.DiscreteNumericObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.ReferenceObservationCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.BooleanResult;
import org.uncertweb.api.om.result.IntegerResult;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.ReferenceResult;
import org.uncertweb.api.om.result.TextResult;
import org.uncertweb.api.om.result.UncertaintyResult;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * encodes Observations by xmlBeans
 * 
 * @author Kiesow, staschc
 * 
 */
public class XBObservationEncoder implements IObservationEncoder {

	// ////////////////////////////////////////////
	// counters used for generating gml IDs
	/**id counter for temporal elements; is resetted to 0, if encoding of a new observation collection starts.*/
	private int timeIdCounter = 0;
	
	/**id counter for sampling features; only if the gml:identifier subelement of a sampling feature is equal, the gml:id is used for referencing the same feature*/
	private int sfIdCounter = 0;
	
	/**id counter for observations; used to generate observation IDs*/
	private int obsIdCounter = 0;
	
	private int unitIdCounter = 0;
	
	private String idPrefix = null;
	
	/**encoder for geometries*/
	private XmlBeansGeometryEncoder encoder;


	// maps used for caching information about already encoded geometries
	
	/**
	 * map which stores TimeStrings as keys and gml IDs used for encoding these strings as values;
	 * the timestrings are created in the following manner: if it is a single timestamp, this is used as string;
	 * if the time is a time period, the string of the start date and the string of the end date are merged to one string
	 * 
	 * This map is used to check, whether a timeinstant or timeperiod with the same values has already been encoded in an observation or 
	 * observation collection.
	 */
	private HashMap<String, String> gmlID4TimeStrings;
	
	/**
	 * map which contains the identifiers of sampling features as keys and the gmlId as values
	 * 
	 * This map is used to check whether a feature has already been encoded in an observation or
	 * observation collection.
	 */
	private HashMap<String, String> gmlID4sfIdentifier;

	/**
	 * boolean that indicates whether a collection is currrently encoded or not;
	 * this flag is used in the encodeObservation operation. This can either be invoked externally for
	 * encoding just one observation or internally for encoding members of an observation collection.
	 */
	private boolean isCol;
	
	/**
	 * constructor initializes geometry encoded
	 * 
	 */
	public XBObservationEncoder(){
		encoder = new XmlBeansGeometryEncoder();
	}

	/**
	 * encodes an observation collection
	 * 
	 * @param obsCol
	 *            observation collection
	 * @return observation collections's xml document as formatted String
	 * @throws UncertaintyEncoderException 
	 * 			if encoding of uncertainty fails
	 * @throws UnsupportedUncertaintyTypeException
	 * 			if type of uncertainty is not supported 
	 * @throws XmlException 
	 * 			if encoding fails
	 * @throws IllegalArgumentException
	 *          if encoding fails
	 */
	@Override
	public synchronized String encodeObservationCollection(
			IObservationCollection obsCol) throws IllegalArgumentException, XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException {
		return encodeObservationCollection(obsCol, null);
	}
	
	public synchronized String encodeObservationCollection(
		IObservationCollection obsCol, String idPrefix) throws IllegalArgumentException, XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException {
		return encodeObservationCollectionDocument(obsCol, idPrefix).xmlText(
				getOMOptions());
	}
	
	/**
	 * encodes an {@link OMObservationDocument}
	 * 
	 * @param obs
	 *            observation
	 * @return observation's xml document as formatted String
	 * @throws UncertaintyEncoderException 
	 * 			if encoding of uncertainty fails
	 * @throws UnsupportedUncertaintyTypeException
	 * 			if type of uncertainty is not supported 
	 * @throws XmlException 
	 * 			if encoding fails
	 * @throws IllegalArgumentException
	 *          if encoding fails
	 */
	@Override
	public synchronized String encodeObservation(AbstractObservation obs) throws IllegalArgumentException, XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException
	{
		return encodeObservation(obs, null);
	}
	
	public synchronized String encodeObservation(AbstractObservation obs, String idPrefix) throws IllegalArgumentException, XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException
	{
		return encodeObservationDocument(obs, idPrefix).xmlText(getOMOptions());
	}

	/**
	 * encodes an observation collection
	 * 
	 * @param obsCol
	 *            observation collection
	 * @return observation collections's xml document
	 * @throws UncertaintyEncoderException 
	 * 			if encoding of uncertainty fails
	 * @throws UnsupportedUncertaintyTypeException
	 * 			if type of uncertainty is not supported 
	 * @throws XmlException 
	 * 			if encoding fails
	 * @throws IllegalArgumentException
	 *          if encoding fails
	 */
	public synchronized XmlObject encodeObservationCollectionDocument(
		IObservationCollection obsCol) throws IllegalArgumentException, XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException
	{
		return encodeObservationCollectionDocument(obsCol, null);
	}
	
	public synchronized XmlObject encodeObservationCollectionDocument(
			IObservationCollection obsCol, String idPrefix) throws IllegalArgumentException, XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException {
		this.isCol = true;
		this.timeIdCounter = 0;
		this.sfIdCounter = 0;
		this.obsIdCounter = 0;
		this.gmlID4TimeStrings = new HashMap<String, String>();
		this.gmlID4sfIdentifier = new HashMap<String, String>();

		// BooleanObservation collection
		if (obsCol instanceof BooleanObservationCollection) {
			OMBooleanObservationCollectionDocument result = OMBooleanObservationCollectionDocument.Factory
					.newInstance();
			OMBooleanObservationCollection xb_boCol = result
					.addNewOMBooleanObservationCollection();
			if (obsCol.getGmlId() != null) {
				xb_boCol.setId(obsCol.getGmlId());
			}
			Iterator<BooleanObservation> obsIter = ((BooleanObservationCollection) obsCol)
					.getMembers().iterator();
			while (obsIter.hasNext()) {
				UWBooleanObservationType xb_obs = xb_boCol
						.addNewOMBooleanObservation();
				OMObservationDocument xb_boDoc = encodeObservationDocument(obsIter
						.next(), idPrefix);
				xb_obs.set(xb_boDoc.getOMObservation());
			}
			reset();
			return result;
		}
		// Measurement collection
		else if (obsCol instanceof MeasurementCollection) {
			OMMeasurementCollectionDocument result = OMMeasurementCollectionDocument.Factory
					.newInstance();
			OMMeasurementCollection xb_boCol = result
					.addNewOMMeasurementCollection();
			if (obsCol.getGmlId() != null) {
				xb_boCol.setId(obsCol.getGmlId());
			}
			Iterator<Measurement> obsIter = ((MeasurementCollection) obsCol)
					.getMembers().iterator();
			while (obsIter.hasNext()) {
				UWMeasurementType xb_obs = xb_boCol.addNewOMMeasurement();
				OMObservationDocument xb_boDoc = encodeObservationDocument(obsIter
						.next(), idPrefix);
				xb_obs.set(xb_boDoc.getOMObservation());
			}
			reset();
			return result;
		}
		// ReferenceObservation collection
		else if (obsCol instanceof ReferenceObservationCollection) {
			OMReferenceObservationCollectionDocument result = OMReferenceObservationCollectionDocument.Factory
					.newInstance();
			OMReferenceObservationCollection xb_boCol = result
					.addNewOMReferenceObservationCollection();
			if (obsCol.getGmlId() != null) {
				xb_boCol.setId(obsCol.getGmlId());
			}
			Iterator<BooleanObservation> obsIter = ((BooleanObservationCollection) obsCol)
					.getMembers().iterator();
			while (obsIter.hasNext()) {
				UWReferenceObservationType xb_obs = xb_boCol
						.addNewOMReferenceObservation();
				OMObservationDocument xb_boDoc = encodeObservationDocument(obsIter
						.next(), idPrefix);
				xb_obs.set(xb_boDoc.getOMObservation());
			}
			reset();
			return result;
		}
		// UncertaintyObservation collection
		else if (obsCol instanceof UncertaintyObservationCollection) {
			OMUncertaintyObservationCollectionDocument result = OMUncertaintyObservationCollectionDocument.Factory
					.newInstance();
			OMUncertaintyObservationCollection xb_boCol = result
					.addNewOMUncertaintyObservationCollection();
			if (obsCol.getGmlId() != null) {
				xb_boCol.setId(obsCol.getGmlId());
			}
			Iterator<UncertaintyObservation> obsIter = ((UncertaintyObservationCollection) obsCol)
					.getMembers().iterator();
			while (obsIter.hasNext()) {
				UWUncertaintyObservationType xb_obs = xb_boCol
						.addNewOMUncertaintyObservation();
				OMObservationDocument xb_boDoc = encodeObservationDocument(obsIter
						.next(), idPrefix);
				xb_obs.set(xb_boDoc.getOMObservation());
			}
			reset();
			return result;
		}
		// DiscreteNumericObservation collection
		else if (obsCol instanceof DiscreteNumericObservationCollection) {
			OMDiscreteNumericObservationCollectionDocument result = OMDiscreteNumericObservationCollectionDocument.Factory
					.newInstance();
			OMDiscreteNumericObservationCollection xb_boCol = result
					.addNewOMDiscreteNumericObservationCollection();
			if (obsCol.getGmlId() != null) {
				xb_boCol.setId(obsCol.getGmlId());
			}
			Iterator<DiscreteNumericObservation> obsIter = ((DiscreteNumericObservationCollection) obsCol)
					.getMembers().iterator();
			while (obsIter.hasNext()) {
				UWDiscreteNumericObservationType xb_obs = xb_boCol
						.addNewOMDiscreteNumericObservation();
				OMObservationDocument xb_boDoc = encodeObservationDocument(obsIter
						.next(), idPrefix);
				xb_obs.set(xb_boDoc.getOMObservation());
			}
			reset();
			return result;
		}
		// TextObservationCollection
		else if (obsCol instanceof TextObservationCollection) {
			OMTextObservationCollectionDocument result = OMTextObservationCollectionDocument.Factory
					.newInstance();
			OMTextObservationCollection xb_boCol = result
					.addNewOMTextObservationCollection();
			if (obsCol.getGmlId() != null) {
				xb_boCol.setId(obsCol.getGmlId());
			}
			Iterator<TextObservation> obsIter = ((TextObservationCollection) obsCol)
					.getMembers().iterator();
			while (obsIter.hasNext()) {
				UWTextObservationType xb_obs = xb_boCol
						.addNewOMTextObservation();
				OMObservationDocument xb_boDoc = encodeObservationDocument(obsIter
						.next(), idPrefix);
				xb_obs.set(xb_boDoc.getOMObservation());
			}
			reset();
			return result;
		}

		// TODO add CategoryObservationCollection

		else {
			throw new IllegalArgumentException("Collection type is not supported by encoder!");
		}

	}

	

	/**
	 * encodes an {@link OMObservationDocument}
	 * 
	 * @param obs
	 *            observation
	 * @return observation's xml document
	 * @throws UncertaintyEncoderException 
	 * 			if encoding of uncertainty fails
	 * @throws UnsupportedUncertaintyTypeException
	 * 			if type of uncertainty is not supported 
	 * @throws XmlException 
	 * 			if encoding fails
	 * @throws IllegalArgumentException
	 *          if encoding fails
	 */
	public synchronized OMObservationDocument encodeObservationDocument(
		AbstractObservation obs) throws IllegalArgumentException, XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException {
		return encodeObservationDocument(obs, null);
	}
	
	public synchronized OMObservationDocument encodeObservationDocument(
			AbstractObservation obs, String idPrefix) throws IllegalArgumentException, XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException {

		// initialize maps for IDs and timestrings
		if (!this.isCol) {
			this.gmlID4TimeStrings = new HashMap<String, String>();
			this.gmlID4sfIdentifier = new HashMap<String, String>();
		}
		
		this.idPrefix = idPrefix;

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
		// xb_obs.setId(obs.getGmlId());
		xb_obs.setId((idPrefix != null ? idPrefix + "_" : "") + "o_" + obsIdCounter);
		obsIdCounter++;

		// add identifier
		if (obs.getIdentifier() != null) {
			CodeWithAuthorityType xb_id = xb_obs.addNewIdentifier();
			xb_id.setCodeSpace(obs.getIdentifier().getCodeSpace().toString());
			xb_id.setStringValue(obs.getIdentifier().getIdentifier());
		}

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
		xb_obs.addNewProcedure().setHref(obs.getProcedure().toASCIIString());

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

		if (!this.isCol) {
			reset();
		}
		return xb_obsDoc;
	}

	/**
	 * helper method for encoding the boundedBy property from an envelope
	 * 
	 * @param xb_observation
	 * 			XMLBeans representation to which the boundedBy property should be encoded
	 * @param obs
	 * 			Observation carrying the boundedBy property which should be encoded
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
	 * 			XMLBeans representation of observation to whcih the phenomenon time should be encoded
	 * @param obs
	 * 			observation which carries the phenomenonTime that should be encoded
	 * @throws IllegalArgumentException
	 * 			if type of phenomenonTime is not supported
	 */
	private void encodePhenomenonTime(OMAbstractObservationType xb_observation,
			AbstractObservation obs) throws IllegalArgumentException {

		TimePrimitivePropertyType xb_phenTime = xb_observation
				.addNewPhenomenonTime();

		// phenomenon time is time instant
		if (obs.getPhenomenonTime().getDateTime() != null) {
			String timeString = obs.getPhenomenonTime().getDateTime()
					.toString();
			if (this.gmlID4TimeStrings.containsKey(timeString)) {
				xb_phenTime.setHref("#"
						+ this.gmlID4TimeStrings.get(timeString));
			} else {
				TimeInstantDocument xb_tiDoc = TimeInstantDocument.Factory
						.newInstance();
				TimeInstantType xb_timeInstant = xb_tiDoc.addNewTimeInstant();

				xb_timeInstant.addNewTimePosition().setStringValue(
						obs.getPhenomenonTime().getDateTime().toString());
				// TODO use DateTimeFormatter? .toString(String pattern)
				String gmlId = (idPrefix != null ? idPrefix + "_" : "")  + "t" + timeIdCounter;
				xb_timeInstant.setId(gmlId);
					this.gmlID4TimeStrings.put(obs.getPhenomenonTime()
							.getDateTime().toString(), gmlId);
					timeIdCounter++;
				xb_phenTime.set(xb_tiDoc);
			}
		}

		// phenomenon time is time period
		else if (obs.getPhenomenonTime().getInterval() != null) {

			String startString = obs.getPhenomenonTime().getInterval()
					.getStart().toString();
			String endString = obs.getPhenomenonTime().getInterval().getEnd()
					.toString();
			String key = startString + endString;

			// check whether time period has been already encoded
			if (this.gmlID4TimeStrings.containsKey(key)) {
				String ref = "#" + this.gmlID4TimeStrings.get(key);
				xb_phenTime.setHref(ref);
				return;
			}

			// time period has to be encoded as new element
			else {
					String gmlID = (idPrefix != null ? idPrefix + "_" : "") + "t" + this.timeIdCounter;
					timeIdCounter++;

				TimePeriodDocument xb_tpDoc = TimePeriodDocument.Factory
						.newInstance();

				TimePeriodType xb_timePeriod = xb_tpDoc.addNewTimePeriod();

				// encode start
				TimeInstantPropertyType xb_tiStart = xb_timePeriod
						.addNewBegin();

				if (this.gmlID4TimeStrings.containsKey(startString)) {
					xb_tiStart.setHref("#"
							+ this.gmlID4TimeStrings.get(startString));
				} else {
					String gmlId = (idPrefix != null ? idPrefix + "_" : "") + "t" + this.timeIdCounter;
					this.timeIdCounter++;
					TimeInstantType xb_ti = xb_tiStart.addNewTimeInstant();
					xb_ti.addNewTimePosition().setStringValue(startString);
					xb_ti.setId(gmlId);
					this.gmlID4TimeStrings.put(startString, gmlId);
				}

				// encode end
				TimeInstantPropertyType xb_tiEnd = xb_timePeriod.addNewEnd();
				if (this.gmlID4TimeStrings.containsKey(endString)) {
					xb_tiEnd.setHref("#"
							+ this.gmlID4TimeStrings.get(endString));
				} else {
					String gmlId = (idPrefix != null ? idPrefix + "_" : "")  + "t" + this.timeIdCounter;
					this.timeIdCounter++;
					TimeInstantType xb_ti = xb_tiEnd.addNewTimeInstant();
					xb_ti.addNewTimePosition().setStringValue(endString);
					xb_ti.setId(gmlId);
					this.gmlID4TimeStrings.put(startString, gmlId);
				}

				xb_timePeriod.setId(gmlID);
				this.gmlID4TimeStrings.put(key, gmlID);
				xb_phenTime.set(xb_tpDoc);
				return;
			}

		} else if (obs.getPhenomenonTime().getHref() != null) {

			xb_phenTime.setHref(obs.getPhenomenonTime().getHref().toASCIIString());

		} else {
			throw new IllegalArgumentException(
					"PhenomenonTime has to be an instant, an interval, or a reference to another time property.");
		}
	}

	/**
	 * helper method for encoding the resultTime property
	 * 
	 * @param xb_observation
	 * 			XMLBeans representation of observation to which the result time should be encoded
	 * @param obs
	 * 			observation which carries the resultTime that should be encoded
	 * @throws IllegalArgumentException
	 * 			if type of resultTime is not supported
	 */
	private void encodeResultTime(OMAbstractObservationType xb_observation,
			AbstractObservation obs) throws IllegalArgumentException {

		TimeInstantPropertyType xb_resultTime = xb_observation
				.addNewResultTime();

		// result time is time instant
		if (obs.getResultTime().getDateTime() != null) {
			String timeString = obs.getResultTime().getDateTime().toString();
			if (this.gmlID4TimeStrings.containsKey(timeString)) {
				xb_resultTime.setHref("#"
						+ this.gmlID4TimeStrings.get(timeString));
			} else {
				TimeInstantDocument xb_tiDoc = TimeInstantDocument.Factory
						.newInstance();
				TimeInstantType xb_timeInstant = xb_tiDoc.addNewTimeInstant();

				xb_timeInstant.addNewTimePosition().setStringValue(
						obs.getResultTime().getDateTime().toString());
				// TODO use DateTimeFormatter? .toString(String pattern)
				String gmlId = (idPrefix != null ? idPrefix + "_" : "")  + "t" + timeIdCounter;
				xb_timeInstant.setId(gmlId);
					this.gmlID4TimeStrings.put(obs.getResultTime()
							.getDateTime().toString(), gmlId);
					timeIdCounter++;
				xb_resultTime.set(xb_tiDoc);
			}
		}

		// phenomenon time is time period
		else if (obs.getResultTime().getInterval() != null) {

			String startString = obs.getResultTime().getInterval().getStart()
					.toString();
			String endString = obs.getResultTime().getInterval().getEnd()
					.toString();
			String key = startString + endString;

			// check whether time period has been already encoded
			if (this.gmlID4TimeStrings.containsKey(key)) {
				String ref = "#" + this.gmlID4TimeStrings.get(key);
				xb_resultTime.setHref(ref);
				return;
			}

			// time period has to be encoded as new element
			else {
				String gmlID = (idPrefix != null ? idPrefix + "_" : "") + "t" + this.timeIdCounter;
				timeIdCounter++;

				TimePeriodDocument xb_tpDoc = TimePeriodDocument.Factory
						.newInstance();

				TimePeriodType xb_timePeriod = xb_tpDoc.addNewTimePeriod();

				// encode start
				TimeInstantPropertyType xb_tiStart = xb_timePeriod
						.addNewBegin();

				if (this.gmlID4TimeStrings.containsKey(startString)) {
					xb_tiStart.setHref("#"
							+ this.gmlID4TimeStrings.get(startString));
				} else {
					String gmlId = (idPrefix != null ? idPrefix + "_" : "") + "t" + this.timeIdCounter;
					this.timeIdCounter++;
					TimeInstantType xb_ti = xb_tiStart.addNewTimeInstant();
					xb_ti.addNewTimePosition().setStringValue(startString);
					xb_ti.setId(gmlId);
					this.gmlID4TimeStrings.put(startString, gmlId);
				}

				// encode end
				TimeInstantPropertyType xb_tiEnd = xb_timePeriod.addNewEnd();
				if (this.gmlID4TimeStrings.containsKey(endString)) {
					xb_tiEnd.setHref("#"
							+ this.gmlID4TimeStrings.get(endString));
				} else {
					String gmlId = (idPrefix != null ? idPrefix + "_" : "") + "pt" + this.timeIdCounter;
					this.timeIdCounter++;
					TimeInstantType xb_ti = xb_tiEnd.addNewTimeInstant();
					xb_ti.addNewTimePosition().setStringValue(startString);
					xb_ti.setId(gmlId);
					this.gmlID4TimeStrings.put(startString, gmlId);
				}

				xb_timePeriod.setId(gmlID);
				this.gmlID4TimeStrings.put(key, gmlID);
				xb_resultTime.set(xb_tpDoc);
				return;
			}

		} else if (obs.getResultTime().getHref() != null) {

			xb_resultTime.setHref(obs.getResultTime().getHref().toASCIIString());

		} else {
			throw new IllegalArgumentException(
					"PhenomenonTime has to be an instant, an interval, or a reference to another time property.");
		}
	}

	/**
	 * helper method for encoding the validTime property
	 * 
	 * @param xb_observation
	 * 			XMLBeans representation of observation to which the valid time should be encoded
	 * @param obs
	 * 			observation which carries the validTime that should be encoded
	 * @throws IllegalArgumentException
	 * 			if type of resultTime is not supported
	 */
	private void encodeValidTime(OMAbstractObservationType xb_observation,
			AbstractObservation obs) throws IllegalArgumentException {
		TimePeriodPropertyType xb_validTime = xb_observation.addNewValidTime();
		if (obs.getValidTime().getInterval() != null) {

			String startString = obs.getValidTime().getInterval().getStart()
					.toString();
			String endString = obs.getValidTime().getInterval().getEnd()
					.toString();
			String key = startString + endString;

			// check whether time period has been already encoded
			if (this.gmlID4TimeStrings.containsKey(key)) {
				String ref = "#" + this.gmlID4TimeStrings.get(key);
				xb_validTime.setHref(ref);
				return;
			}

			// time period has to be encoded as new element
			else {
				String gmlID = (idPrefix != null ? idPrefix + "_" : "") + "t" + this.timeIdCounter;
				timeIdCounter++;

				TimePeriodDocument xb_tpDoc = TimePeriodDocument.Factory
						.newInstance();

				TimePeriodType xb_timePeriod = xb_tpDoc.addNewTimePeriod();

				// encode start
				TimeInstantPropertyType xb_tiStart = xb_timePeriod
						.addNewBegin();

				if (this.gmlID4TimeStrings.containsKey(startString)) {
					xb_tiStart.setHref("#"
							+ this.gmlID4TimeStrings.get(startString));
				} else {
					String gmlId = (idPrefix != null ? idPrefix + "_" : "") + "t" + this.timeIdCounter;
					this.timeIdCounter++;
					TimeInstantType xb_ti = xb_tiStart.addNewTimeInstant();
					xb_ti.addNewTimePosition().setStringValue(startString);
					xb_ti.setId(gmlId);
					this.gmlID4TimeStrings.put(startString, gmlId);
				}

				// encode end
				TimeInstantPropertyType xb_tiEnd = xb_timePeriod.addNewEnd();
				if (this.gmlID4TimeStrings.containsKey(endString)) {
					xb_tiEnd.setHref("#"
							+ this.gmlID4TimeStrings.get(endString));
				} else {
					String gmlId = (idPrefix != null ? idPrefix + "_" : "") + "pt" + this.timeIdCounter;
					this.timeIdCounter++;
					TimeInstantType xb_ti = xb_tiEnd.addNewTimeInstant();
					xb_ti.addNewTimePosition().setStringValue(endString);
					xb_ti.setId(gmlId);
					this.gmlID4TimeStrings.put(endString, gmlId);
				}

				xb_timePeriod.setId(gmlID);
				this.gmlID4TimeStrings.put(key, gmlID);
				xb_validTime.set(xb_tpDoc);
				return;
			}

		} else if (obs.getValidTime().getHref() != null) {

			xb_validTime.setHref(obs.getValidTime().getHref().toASCIIString());

		} else {
			throw new IllegalArgumentException(
					"PhenomenonTime has to be an instant, an interval, or a reference to another time property.");
		}
	}

	/**
	 * helper method for encoding featureOfInterest property
	 * 
	 * @param xb_observation
	 * 			XMLBeans representation whose feature should be encoded
	 * @param obs
	 * 			observation which should be encoded
	 * @throws IllegalArgumentException
	 * 			if enconding fails
	 */
	private void encodeFeatureOfInterest(
			OMAbstractObservationType xb_observation, AbstractObservation obs)
			throws IllegalArgumentException {

		FoiPropertyType xb_foi = xb_observation.addNewFeatureOfInterest();

		//feature has is externally referenced
		if (obs.getFeatureOfInterest().getHref() != null
				&& !obs.getFeatureOfInterest().equals("")) {
			xb_foi.setHref(obs.getFeatureOfInterest().getHref().toASCIIString());
		}

		else {

			// if identifier of feature is set, check whether foi has been
			// already encoded
			Identifier identifier = obs.getFeatureOfInterest().getIdentifier();
			if (identifier != null && !identifier.equals("")) {
				if (this.gmlID4sfIdentifier.containsKey(identifier.getIdentifier())) {
					xb_foi.setHref("#" + this.gmlID4sfIdentifier.get(identifier.getIdentifier()));
					return;
				}
			}

			//if feature has not been implemented, encode the feature.
			SFSpatialSamplingFeatureType xb_sfType = xb_foi
					.addNewSFSpatialSamplingFeature();
			String gmlId = (idPrefix != null ? idPrefix + "_" : "") + "sf" + this.sfIdCounter;
			this.sfIdCounter++;
			xb_sfType.setId(gmlId);
			

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
			
			XmlObject xb_geometry = null;
			if (obs.getFeatureOfInterest().getShape() instanceof Point) {
				xb_geometry = encoder.encodePoint2Doc((Point) obs
						.getFeatureOfInterest().getShape());

			} else if (obs.getFeatureOfInterest().getShape() instanceof Polygon) {
				xb_geometry = encoder.encodePolygon2Doc((Polygon) obs
						.getFeatureOfInterest().getShape());
			} else if (obs.getFeatureOfInterest().getShape() instanceof LineString) {
				xb_geometry = encoder.encodeLineString2Doc((LineString) obs
						.getFeatureOfInterest().getShape());
			} else if (obs.getFeatureOfInterest().getShape() instanceof RectifiedGrid) {
				xb_geometry = encoder
						.encodeRectifiedGrid2Doc((RectifiedGrid) obs
								.getFeatureOfInterest().getShape());

			} else if (obs.getFeatureOfInterest().getShape() instanceof MultiPoint) {
				xb_geometry = encoder.encodeMultiPoint2Doc((MultiPoint) obs
						.getFeatureOfInterest().getShape());

			} else if (obs.getFeatureOfInterest().getShape() instanceof MultiLineString) {
				xb_geometry = encoder
						.encodeMultiLineString2Doc((MultiLineString) obs
								.getFeatureOfInterest().getShape());

			} else if (obs.getFeatureOfInterest().getShape() instanceof MultiPolygon) {
				xb_geometry = encoder
						.encodeMultiPolygon2Doc((MultiPolygon) obs
								.getFeatureOfInterest().getShape());

			} else {
				throw new IllegalArgumentException(
						"Geometry type is not supported by UncertWeb GML profile!");
			}
			
			// set id prefix
			if (idPrefix != null) {
				AbstractGeometryType xb_geometry_type = ((AbstractGeometryDocument) xb_geometry).getAbstractGeometry();
				xb_geometry_type.setId(idPrefix + "_" + xb_geometry_type.getId());
			}
			
			xb_shape.set(xb_geometry);
			if (identifier != null) {
				CodeWithAuthorityType xb_identifier = xb_sfType.addNewIdentifier();
				xb_identifier.setStringValue(identifier.getIdentifier());
				xb_identifier.setCodeSpace(identifier.getCodeSpace().toString());
				this.gmlID4sfIdentifier.put(identifier.getIdentifier(), gmlId);
			}
		}
	}

	/**
	 * helper method for encoding resultQuality property; ATTENTION: currently only DQ_UncertaintyResult (resultQuality carrying Uncertainty
	 * encoded as UncertML is supported!!
	 * 
	 * @param xb_observation
	 * 			XMLBeans representation to which the resultQuality should be encoded
	 * @param obs
	 * 			Observation carrying the resultQuality which should be encoded
	 * @throws XmlException
	 * 			if encoding fails
	 * @throws UnsupportedUncertaintyTypeException
	 * 			if type of uncertainty in resultQuality is not supported
	 * @throws UncertaintyEncoderException
	 * 			if encoding of uncertainty fails
	 */
	private void encodeResultQuality(OMAbstractObservationType xb_observation,
			AbstractObservation obs) throws XmlException,
			UnsupportedUncertaintyTypeException, UncertaintyEncoderException {

		for (DQ_UncertaintyResult resultObject : obs.getResultQuality()) {

			DQUncertaintyResultDocument xb_dqURDoc = DQUncertaintyResultDocument.Factory
					.newInstance();
			DQUncertaintyResultType xb_dqUncRes = xb_dqURDoc
					.addNewDQUncertaintyResult();

			
			// add value unit
			UnitDefinitionType xb_vu = xb_dqUncRes.addNewValueUnit()
						.addNewUnitDefinition();
				xb_vu.setId((idPrefix != null ? idPrefix + "_" : "") + "u"+unitIdCounter);
				unitIdCounter++;
				xb_vu.addNewIdentifier().setStringValue(
						resultObject.getUom());
			// encode uncertainty value
			IUncertainty[] valueArray = resultObject.getValues();
			for (int i = 0; i < valueArray.length; i++) {
				IUncertainty value = valueArray[i];
				Value xb_value = xb_dqUncRes.addNewValue();// Value.Factory.newInstance();
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
	 *            XmlBeans object to which the result should be written
	 * @param obs
	 *            observation whose result should be encoded
	 * @throws UncertaintyEncoderException
	 *             if uncertainty cannot be encoded
	 * @throws UnsupportedUncertaintyTypeException
	 *             if type of uncertainty is not supported by UncertML API
	 * @throws XmlException
	 *             if encoding fails
	 */
	private void encodeResult(OMAbstractObservationType xb_obs,
			AbstractObservation obs) throws XmlException,
			UnsupportedUncertaintyTypeException, UncertaintyEncoderException {

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

			// encode UOM attribute, if UOM is set
			if (resultObject.getUnitOfMeasurement() != null
					&& !resultObject.getUnitOfMeasurement().equals("")) {
				xb_uncResult.setUom(resultObject.getUnitOfMeasurement());
			}
			xb_uncResult.addNewAbstractUncertainty();
			xb_uncResult.set(encodeUncertainty((IUncertainty) resultObject
					.getUncertaintyValue()));

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

		// TODO add CategoryObservation
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
	 *            uncertainty which should be encoded in UncertML XML
	 * @return String representing the uncertainty encoded as UncertML XML
	 * @throws XmlException
	 *             if encoding fails
	 * @throws UnsupportedUncertaintyTypeException
	 *             if uncertainty
	 * @throws UncertaintyEncoderException
	 */
	private AbstractUncertaintyDocument encodeUncertainty(
			IUncertainty uncertainty) throws XmlException,
			UnsupportedUncertaintyTypeException, UncertaintyEncoderException {
		XMLEncoder unEncoder = new XMLEncoder();
		AbstractUncertaintyDocument object = AbstractUncertaintyDocument.Factory
				.parse(unEncoder.encode(uncertainty));
		return object;
	}

	/**
	 * helper methods for resetting member variables used for gml ID encoding
	 * 
	 */
	private void reset() {
		this.gmlID4TimeStrings = null;
		this.gmlID4sfIdentifier = null;
		this.isCol = false;
		this.obsIdCounter = 0;
		this.timeIdCounter = 0;
		this.encoder.resetCounter();
		this.unitIdCounter = 0;
	}

	/**
	 * method returns XmlOptions which are used by XmlBeans for a proper
	 * encoding
	 * 
	 * @return XmlOptions used to encode the XML document with XMLBeans
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
