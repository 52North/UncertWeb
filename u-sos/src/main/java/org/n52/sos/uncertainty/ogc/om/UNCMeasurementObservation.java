package org.n52.sos.uncertainty.ogc.om;

import java.util.Collection;

import org.n52.sos.ogc.gml.time.ISosTime;
import org.n52.sos.ogc.om.SosMeasurement;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.quality.SosQuality;
import org.n52.sos.uncertainty.ogc.IUncertainObservation;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.observation.Measurement;

/**
 * Adapter class to handle O&M 2 measurement observations in O&M 1 observation
 * classes
 * 
 * @see Measurement
 * @see SosMeasurement
 * @author Kiesow
 * 
 */
public class UNCMeasurementObservation extends SosMeasurement implements
		IUncertainObservation {

	/**
	 * gml identifier (consisting of code space and
	 * identifier)
	 */
	private String identifier;
	
	/**
	 * data quality as uncertainties, replacing om1 observations quality
	 */
	private DQ_UncertaintyResult[] uncQuality;
	
	/**
	 * xml type name of this observation
	 */
	public static final String NAME = "OM_Measurement";

	/**
	 * constructor
	 * 
	 * @param identifier
	 * 			  gml identifier
	 * @param time
	 *            time at which the observation event took place
	 * @param obsID
	 *            id of the observation
	 * @param procID
	 *            id of the procedure, by which the value was produced
	 * @param domainFeatureIDs
	 *            domain features, which this observation is produced for
	 * @param phenID
	 *            id of the phenomenon, of which the value is
	 * @param foi
	 *            feature of interest, to which the observation belongs
	 * @param offeringID
	 *            id of the offering to which this observation belongs
	 * @param mimeType
	 *            mimeType of the observation result
	 * @param value
	 *            result value
	 * @param unitsOfMeasurement
	 *            units of the measurement value
	 * @param quality
	 *            simple quantitative data quality
	 * @param uncQuality
	 * 			  data quality as uncertainties, replacing om1 observations quality
	 */
	public UNCMeasurementObservation(String identifier, ISosTime time, String obsID,
			String procID, Collection<SosAbstractFeature> domainFeatureIDs,
			String phenID, SosAbstractFeature foi, String offeringID,
			String mimeType, double value, String unitsOfMeasurement,
			Collection<SosQuality> quality, DQ_UncertaintyResult[] uncQuality) {
		super(time, obsID, procID, domainFeatureIDs, phenID, foi, offeringID,
				mimeType, value, unitsOfMeasurement, quality);
		
		this.identifier = identifier;
		this.uncQuality = uncQuality;
	}
	
	/**
	 * constructor 
	 * 
	 * @param time
	 *            time at which the observation event took place
	 * @param obsID
	 *            id of the observation
	 * @param procID
	 *            id of the procedure, by which the value was produced
	 * @param domainFeatureIDs
	 *            domain features, which this observation is produced for
	 * @param phenID
	 *            id of the phenomenon, of which the value is
	 * @param foi
	 *            feature of interest, to which the observation belongs
	 * @param offeringID
	 *            id of the offering to which this observation belongs
	 * @param mimeType
	 *            mimeType of the observation result
	 * @param value
	 *            result value
	 * @param unitsOfMeasurement
	 *            units of the measurement value
	 * @param quality
	 *            simple quantitative data quality
	 */
	public UNCMeasurementObservation(ISosTime time, String obsID,
			String procID, Collection<SosAbstractFeature> domainFeatureIDs,
			String phenID, SosAbstractFeature foi, String offeringID,
			String mimeType, double value, String unitsOfMeasurement,
			Collection<SosQuality> quality) {
		super(time, obsID, procID, domainFeatureIDs, phenID, foi, offeringID,
				mimeType, value, unitsOfMeasurement, quality);
		
	}

	/**
	 * returns gml identifier (not to be confused with observation id/obsID)
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * sets gml identifier (not to be confused with observation id/obsID)
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * returns data quality as uncertainties, replacing om1 observations quality
	 */
	public DQ_UncertaintyResult[] getUncQuality() {
		return uncQuality;
	}
	
	/**
	 * sets data quality as uncertainties, replacing om1 observations quality
	 */
	public void setUncQuality(DQ_UncertaintyResult[] resultQuality) {
		this.uncQuality = resultQuality;
	}	
	
	@Override
	public String getName() {
		return NAME;
	}
}
