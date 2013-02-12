package org.n52.sos.uncertainty.decode.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.n52.sos.SosConfigurator;
import org.n52.sos.SosConstants;
import org.n52.sos.ogc.gml.time.ISosTime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.AbstractSosObservation;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosMeasurement;
import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingPoint;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingSurface;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.uncertainty.ogc.IUncertainObservation;
import org.n52.sos.uncertainty.ogc.om.UNCMeasurementObservation;
import org.n52.sos.uncertainty.ogc.om.UNCUncertaintyObservation;
import org.uncertml.IUncertainty;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.BooleanObservationCollection;
import org.uncertweb.api.om.observation.collections.DiscreteNumericObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.ReferenceObservationCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * Converter class to create O&M 1 Observations out of O&M 2 Observations and
 * vice versa
 * 
 * @see AbstractSosObservation
 * @see AbstractObservation
 * @author Kiesow
 * 
 */
public class ObservationConverter {

	/**
	 * converts single uncertainty enabled O&M 2 observations into O&M 1
	 * observations with uncertainties
	 * 
	 * @param om2Obs
	 *            O&M 2 observation
	 * @return O&M 1 observation
	 * @throws OwsExceptionReport
	 */
	public static AbstractSosObservation getOM1Obs(AbstractObservation om2Obs)
			throws OwsExceptionReport {
		AbstractSosObservation om1Obs = null;

		// convert time
		// resultTime and validTime get lost
		ISosTime samplingTime = null;
		if (om2Obs.getPhenomenonTime().getDateTime() != null) {
			samplingTime = new TimeInstant(om2Obs.getPhenomenonTime()
					.getDateTime(), null);
		} else if (om2Obs.getPhenomenonTime().getInterval() != null) {
			samplingTime = new TimePeriod(om2Obs.getPhenomenonTime()
					.getInterval().getStart(), om2Obs.getPhenomenonTime()
					.getInterval().getEnd());
		} else if (om2Obs.getPhenomenonTime().getHref() != null) {

			OwsExceptionReport se = new OwsExceptionReport();
			se.addCodedException(
					OwsExceptionReport.ExceptionCode.InvalidParameterValue,
					null,
					"Observation could not be converted: PhenomenonTime has to be a time instant or time period.");
			throw se;
		}

		// convert identifier
		String identifier = om2Obs.getIdentifier().toIdentifierString();

		// convert procedure
		String procID = om2Obs.getProcedure().toString();

		// convert phenomenon
		String phenID = om2Obs.getObservedProperty().toString();

		// convert feature of interest
		SosAbstractFeature foi = convertOM2FOI(om2Obs.getFeatureOfInterest());

		// convert result quality
		DQ_UncertaintyResult[] uncQuality = null;
		if (om2Obs.getResultQuality() != null
				&& om2Obs.getResultQuality().length > 0) {
			uncQuality = om2Obs.getResultQuality();
		}

		// find offering and create O&M 1 Observation
		Collection<String> offeringIDs = SosConfigurator.getInstance()
				.getCapsCacheController().getOfferings4Phenomenon(phenID);
		Iterator<String> offIter = offeringIDs.iterator();
		Collection<String> procIDs;

		String notSet = SosConstants.PARAMETER_NOT_SET;

		if (om2Obs.getName() == OM2Constants.OBS_TYPE_UNCERTAINTY) {

			// convert uncertainty result
			UncertaintyResult result = ((UncertaintyObservation) om2Obs)
					.getResult();

			// unit of measurement (optional)
			String uom = notSet;
			if (result.getUnitOfMeasurement() != null) {
				uom = result.getUnitOfMeasurement();
			}
			IUncertainty uncResult = result.getUncertaintyValue();

			// find offering containting given procedure
			if (offeringIDs.size() > 0) {
				while (offIter.hasNext()) {
					String offeringID = offIter.next();

					procIDs = SosConfigurator.getInstance()
							.getCapsCacheController()
							.getProcedures4Offering(offeringID);
					if (procIDs.size() > 0) {
						if (procIDs.contains(procID)) {

							om1Obs = new UNCUncertaintyObservation(identifier,
									samplingTime, null, procID, null, phenID,
									foi, offeringID, notSet, uom, null,
									uncQuality, uncResult);
						}
					}
				}
				if (om1Obs == null) {
					OwsExceptionReport se = new OwsExceptionReport();
					se.addCodedException(
							OwsExceptionReport.ExceptionCode.InvalidParameterValue,
							null,
							"No procedures are contained in db for offerings " + offeringIDs.toString() + ".");
					throw se;
				}
			} else {
				OwsExceptionReport se = new OwsExceptionReport();
				se.addCodedException(
						OwsExceptionReport.ExceptionCode.InvalidParameterValue,
						null, "No offering is contained in db for phenomenon '"
								+ phenID + "'");
				throw se;
			}

		} else if (om2Obs.getName() == OM2Constants.OBS_TYPE_MEASUREMENT) {

			MeasureResult result = ((Measurement) om2Obs).getResult();
			Double value = result.getMeasureValue();
			String uom = result.getUnitOfMeasurement();

			// find offering containting given procedure
			if (offeringIDs.size() > 0) {
				while (offIter.hasNext()) {
					String offeringID = offIter.next();

					procIDs = SosConfigurator.getInstance()
							.getCapsCacheController()
							.getProcedures4Offering(offeringID);
					if (procIDs.size() > 0) {
						if (procIDs.contains(procID)) {

							om1Obs = new UNCMeasurementObservation(identifier,
									samplingTime, null, procID, null, phenID,
									foi, offeringID, notSet, value, uom, null,
									uncQuality);
						}
					}
				}
				if (om1Obs == null) {
					OwsExceptionReport se = new OwsExceptionReport();
					se.addCodedException(
							OwsExceptionReport.ExceptionCode.InvalidParameterValue,
							null,
							"No procedures are contained in db for offerings " + offeringIDs.toString() + ".");
					throw se;
				}
			} else {
				OwsExceptionReport se = new OwsExceptionReport();
				se.addCodedException(
						OwsExceptionReport.ExceptionCode.InvalidParameterValue,
						null, "No offering is contained in db for phenomenon '"
								+ phenID + "'");
				throw se;
			}

			// TODO add further uncertainty types here
			// additional uncertain O&M 2 Observation types

			// } else if (om2Obs.getName() == OM2Constants.OBS_TYPE_BOOLEAN) {
			// om1Obs = new UncertainMeasurementObservation(time, obsID, procID,
			// domainFeatureIDs, phenID, foi, offeringID, mimeType,
			// value, unitsOfMeasurement, quality,
			// OM2Constants.OBS_TYPE_BOOLEAN);
			// } else if (om2Obs.getName() == OM2Constants.OBS_TYPE_DISCNUM) {
			// om1Obs = new UncertainMeasurementObservation(time, obsID, procID,
			// domainFeatureIDs, phenID, foi, offeringID, mimeType,
			// value, unitsOfMeasurement, quality,
			// OM2Constants.OBS_TYPE_DISCNUM);
			// } else if (om2Obs.getName() == OM2Constants.OBS_TYPE_REFERENCE) {
			// om1Obs = new UncertainCategoryObservation(time, obsID, procID,
			// foi, domainFeatureIDs, phenID, offeringID, mimeType,
			// value, unit, quality, OM2Constants.OBS_TYPE_REFERENCE);
			// } else if (om2Obs.getName() == OM2Constants.OBS_TYPE_TEXT) {
			// om1Obs = new UncertainCategoryObservation(time, obsID, procID,
			// foi, domainFeatureIDs, phenID, offeringID, mimeType,
			// value, unit, quality, OM2Constants.OBS_TYPE_TEXT);
		} else {
			throw new IllegalArgumentException(
					"Observation could not be converted: Invalid observation type given.");
		}
		return om1Obs;
	}

	/**
	 * converts multiple uncertainty enabled O&M 2 observations into O&M 1
	 * observations with uncertainties
	 * 
	 * @param om2ObsCol
	 *            O&M 2 observation collection
	 * @return O&M 1 observation collection
	 * @throws OwsExceptionReport
	 */
	public static SosObservationCollection getOM1ObsCol(
			IObservationCollection om2ObsCol) throws OwsExceptionReport {

		// TODO derive parameters of OM1 observation collection
		// String id;
		// DateTime expiresDate;
		// TimePeriod time;
		// Envelope boundedBy;

		AbstractSosObservation om1Obs = null;
		Collection<AbstractSosObservation> observationMembers = new ArrayList<AbstractSosObservation>();

		for (AbstractObservation om2Obs : om2ObsCol.getObservations()) {

			om1Obs = getOM1Obs(om2Obs);
			observationMembers.add(om1Obs);
		}

		SosObservationCollection om1ObsCol = new SosObservationCollection();
		om1ObsCol.setObservationMembers(observationMembers);

		return om1ObsCol;
	}

	/**
	 * converts O&M 2 features into SOS features
	 */
	public static SosAbstractFeature convertOM2FOI(SpatialSamplingFeature foi) {

		String notSet = OMConstants.PARAMETER_NOT_SET;
		String id = foi.getIdentifier().toIdentifierString();
		String description = "";
		
		String featureType = foi.getFeatureType();
		String name = foi.getIdentifier().getIdentifier();
		Geometry jts_geom = foi.getShape();
		
		
		

		if (jts_geom instanceof Point) {
			return new SosSamplingPoint(id, name, description, (Point) jts_geom,
					featureType, notSet);
		} else {
			return new SosSamplingSurface(id, name, description, jts_geom,
					featureType, notSet);
			// TODO test if SOS handles all geometry types correctly
		}
	}

	/**
	 * converts single O&M 1 observations into (non-uncertainty-enabled) O&M 2
	 * observations
	 * 
	 * @param om1Obs
	 *            O&M 1 observation
	 * @return O&M 2 observation
	 * @throws OwsExceptionReport
	 * @throws Exception
	 */
	public static AbstractObservation getOM2Obs(AbstractSosObservation om1Obs)
			throws OwsExceptionReport {
		AbstractObservation om2Obs = null;

		// convert phenomenon time
		// phenomenonTime will be use for resultTime as well
		TimeObject phenomenonTime = null;
		if (om1Obs.getSamplingTime() instanceof TimeInstant) {
			phenomenonTime = new TimeObject(
					((TimeInstant) om1Obs.getSamplingTime()).getValue());

		} else if (om1Obs.getSamplingTime() instanceof TimePeriod) {

			TimePeriod tp = (TimePeriod) om1Obs.getSamplingTime();
			phenomenonTime = new TimeObject(tp.getStart(), tp.getEnd());
		}

		// convert procedure
		URI procedure = null;
		try {
			procedure = new URI(om1Obs.getProcedureID());

		} catch (URISyntaxException e) {
			OwsExceptionReport se = new OwsExceptionReport();
			se.addCodedException(
					OwsExceptionReport.ExceptionCode.MissingParameterValue,
					null,
					"Procedure '" + om1Obs.getProcedureID()
							+ "' of observation '" + om1Obs.getObservationID()
							+ "' could not be parsed to an URI: "
							+ e.getMessage());
			throw se;
		}

		// convert observed property
		URI observedProperty = null;
		try {
			observedProperty = new URI(om1Obs.getPhenomenonID());

		} catch (URISyntaxException e) {
			OwsExceptionReport se = new OwsExceptionReport();
			se.addCodedException(
					OwsExceptionReport.ExceptionCode.MissingParameterValue,
					null,
					"Phenomenon '" + om1Obs.getPhenomenonID()
							+ "' of observation '" + om1Obs.getObservationID()
							+ "' could not be parsed to an URI: "
							+ e.getMessage());
			throw se;
		}

		// convert feature of interest
		SpatialSamplingFeature featureOfInterest = convertOM1FOI(om1Obs
				.getFeatureOfInterest());
		Envelope boundedBy = featureOfInterest.getBoundedBy();

		// convert result quality
		DQ_UncertaintyResult[] uncQuality = null;
		Identifier identifier = null;
		if (om1Obs instanceof IUncertainObservation) {

			// convert uncertainty result quality
			uncQuality = ((IUncertainObservation) om1Obs).getUncQuality();

			// convert identifier
			identifier = convertIdentifier(((IUncertainObservation) om1Obs)
					.getIdentifier());
		}

		// convert result and create observations
		// O&M 2 observation types
		if (om1Obs instanceof UNCMeasurementObservation) {

			String uom = ((SosMeasurement) om1Obs).getUnitsOfMeasurement();
			Double value = ((SosMeasurement) om1Obs).getValue();
			MeasureResult result = new MeasureResult(value, uom);

			om2Obs = new Measurement(identifier, boundedBy, phenomenonTime,
					phenomenonTime, null, procedure, observedProperty,
					featureOfInterest, uncQuality, result);

		} else if (om1Obs instanceof UNCUncertaintyObservation) {

			IUncertainty unc = ((UNCUncertaintyObservation) om1Obs)
					.getUncertainty();
			String uom = ((UNCUncertaintyObservation) om1Obs)
					.getUnitsOfMeasurement();
			UncertaintyResult result = new UncertaintyResult(unc, uom);

			om2Obs = new UncertaintyObservation(identifier, boundedBy,
					phenomenonTime, phenomenonTime, null, procedure,
					observedProperty, featureOfInterest, uncQuality, result);

			// TODO add further uncertainty types here
			// add further subtypes of AbstractSosObservation and differ to OM2
			// observation types

			// O&M 1 observation types
		} else if (om1Obs instanceof SosMeasurement) {

			String uom = ((SosMeasurement) om1Obs).getUnitsOfMeasurement();
			Double value = ((SosMeasurement) om1Obs).getValue();
			MeasureResult result = new MeasureResult(value, uom);

			om2Obs = new Measurement(null, boundedBy, phenomenonTime,
					phenomenonTime, null, procedure, observedProperty,
					featureOfInterest, null, result);
		}

		return om2Obs;
	}

	/**
	 * converts multiple O&M1 observations with uncertainties into uncertainty
	 * enabled O&M 2 observations
	 * 
	 * @param om1ObsCol
	 *            O&M 1 observation collection
	 * @return O&M 2 observation collection; null if input is null or empty
	 * @throws OwsExceptionReport
	 */
	public static IObservationCollection getOM2ObsCol(
			SosObservationCollection om1ObsCol) throws OwsExceptionReport {

		IObservationCollection om2ObsCol = null;

		if (om1ObsCol != null && om1ObsCol.getObservationMembers() != null
				&& !om1ObsCol.getObservationMembers().isEmpty()) {

			String typeName = null;

			for (AbstractSosObservation om1Obs : om1ObsCol
					.getObservationMembers()) {

				AbstractObservation om2Obs = getOM2Obs(om1Obs);

				if (typeName == null) {
					typeName = om2Obs.getName();

					// create fitting observation collection
					if (om2Obs.getName().equals(OM2Constants.OBS_TYPE_BOOLEAN)) {
						om2ObsCol = new BooleanObservationCollection();
					} else if (om2Obs.getName().equals(
							OM2Constants.OBS_TYPE_DISCNUM)) {
						om2ObsCol = new DiscreteNumericObservationCollection();
					} else if (om2Obs.getName().equals(
							OM2Constants.OBS_TYPE_MEASUREMENT)) {
						om2ObsCol = new MeasurementCollection();
					} else if (om2Obs.getName().equals(
							OM2Constants.OBS_TYPE_REFERENCE)) {
						om2ObsCol = new ReferenceObservationCollection();
					} else if (om2Obs.getName().equals(
							OM2Constants.OBS_TYPE_TEXT)) {
						om2ObsCol = new TextObservationCollection();
					} else if (om2Obs.getName().equals(
							OM2Constants.OBS_TYPE_UNCERTAINTY)) {
						om2ObsCol = new UncertaintyObservationCollection();
					}
				}

				// add observation
				if (om2Obs.getName().equals(typeName)) {
					om2ObsCol.addObservation(om2Obs);
				} else {
					OwsExceptionReport se = new OwsExceptionReport();
					se.addCodedException(
							OwsExceptionReport.ExceptionCode.InvalidParameterValue,
							null,
							"All observations have to be of the same type (e.g. Measurement).");
					throw se;
				}
			}
		}
		return om2ObsCol;
	}

	/**
	 * converts SOS features into O&M 2 features
	 * 
	 * @throws OwsExceptionReport
	 */
	private static SpatialSamplingFeature convertOM1FOI(SosAbstractFeature foi)
			throws OwsExceptionReport {

		
		String sampledFeature = OMConstants.PARAMETER_NOT_SET;
		Geometry shape = foi.getGeom();
		Envelope boundedBy = shape.getEnvelopeInternal();

		// create identifier from URI string
		Identifier identifier = convertIdentifier(foi.getId());

		try {
			// create feature
			if (SosConfigurator.getInstance().isFoiEncodedInObservation() == false) {

				return new SpatialSamplingFeature(new URI(identifier.toIdentifierString()));
			} else {
				return new SpatialSamplingFeature(identifier, boundedBy,
					sampledFeature, shape);
			}
		} catch (Exception e) {
			OwsExceptionReport se = new OwsExceptionReport();
			se.addCodedException(
					OwsExceptionReport.ExceptionCode.InvalidParameterValue,
					null, "Spatial sampling feature could not be created.");
			throw se;
		}
	}

	/**
	 * converts a String identifier into an Identifier class object
	 * @throws OwsExceptionReport 
	 */
	private static Identifier convertIdentifier(String id) throws OwsExceptionReport {

		if (id == null) {
			return null;
		}

		Identifier identifier = null;

		String[] splitID = id.split("/");
		String idName = splitID[splitID.length - 1];

		try {
			
			if (id.length() == idName.length()) {
				// identifier without codeSpace
				identifier = new Identifier(new URI(""), idName);
				
			} else {
				// codeSpace without appended slash
				identifier = new Identifier(new URI(id.substring(0, id.length()
					- idName.length() - 1)), idName);
			}
			
		} catch (URISyntaxException e) {
			OwsExceptionReport se = new OwsExceptionReport();
			se.addCodedException(
					OwsExceptionReport.ExceptionCode.InvalidParameterValue,
					null, "Identifier '" + id + "' could not be converted: " + e.getLocalizedMessage());
			throw se;
		}

		return identifier;
	}
}
