package org.n52.sos.uncertainty.decode.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.n52.sos.SosConfigurator;
import org.n52.sos.SosConstants;
import org.n52.sos.ogc.gml.time.ISosTime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.AbstractSosObservation;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingPoint;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingSurface;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.uncertainty.ogc.om.UNCMeasurementObservation;
import org.n52.sos.uncertainty.ogc.om.UNCUncertaintyObservation;
import org.omg.CORBA.NameValuePair;
import org.uncertml.IUncertainty;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

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

	public static AbstractSosObservation getOM1Obs(AbstractObservation om2Obs)
			throws OwsExceptionReport {
		AbstractSosObservation om1Obs = null;

		// convert time
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
		List<String> offeringIDs = SosConfigurator.getInstance()
				.getCapsCacheController().getOfferings4Phenomenon(phenID);
		Iterator<String> offIter = offeringIDs.iterator();
		List<String> procIDs;

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
							"No procedures are contained in db for offerings'");
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
							"No procedures are contained in db for offerings'");
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

			// additional uncertaint O&M 2 Observation types

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

	private static SosAbstractFeature convertOM2FOI(SpatialSamplingFeature foi) {

		String id = foi.getIdentifier().toIdentifierString();
		String featureType = foi.getTypeName();
		Geometry jts_geom = foi.getShape();
		String ns = OMConstants.PARAMETER_NOT_SET;

		if (jts_geom instanceof Point) {
			return new SosSamplingPoint(id, ns, ns, (Point) jts_geom,
					featureType, ns);
		} else {
			return new SosSamplingSurface(id, ns, ns, jts_geom, featureType, ns);
			// TODO test if SOS handles all geometry types correctly
		}
	}

	public static AbstractObservation getOM2Obs(AbstractSosObservation om1Obs) {
		AbstractObservation om2Obs = null;

		return om2Obs;
	}
	
	public static IObservationCollection getOM2ObsCol(SosObservationCollection om1ObsCol, Collection<NameValuePair> uncCol) {
		IObservationCollection om2ObsCol = null;
		
		
		return om2ObsCol;
	}
}
