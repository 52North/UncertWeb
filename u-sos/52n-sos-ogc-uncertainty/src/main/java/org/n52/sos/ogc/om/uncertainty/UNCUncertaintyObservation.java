package org.n52.sos.ogc.om.uncertainty;

import java.util.Collection;

import org.n52.sos.ogc.gml.time.ISosTime;
import org.n52.sos.ogc.om.SosMeasurement;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.quality.SosQuality;
import org.n52.sos.ogc.uncertainty.IUncertainObservation;
import org.uncertml.IUncertainty;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.observation.UncertaintyObservation;

/**
 * Adapter class to handle O&M 2 uncertainty observations in O&M 1 observation
 * classes
 * 
 * @see UncertaintyObservation
 * @see SosMeasurement
 * @author Kiesow
 * 
 */
public class UNCUncertaintyObservation extends SosMeasurement implements
		IUncertainObservation {

	/**
	 * gml identifier
	 */
	private String identifier;
	
	/**
	 * data quality as uncertainties, replacing om1 observations quality
	 */
	private DQ_UncertaintyResult[] uncQuality;
	/**
	 * uncertainty result, replacing om1 observations result value
	 */
	private IUncertainty uncertainty;

	/**
	 * xml type name of this observation
	 */
	public static final String NAME = "OM_UncertaintyObservation";
	
	
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
	 * @param unitsOfMeasurement
	 *            units of the measurement value
	 * @param quality
	 *            simple quantitative data quality
	 * @param uncQuality
	 *            result quality as array of uncertainties
	 * @param uncertainty
	 * 			  result as single uncertainty
	 */
	public UNCUncertaintyObservation(String identifier, ISosTime time, String obsID,
			String procID, Collection<SosAbstractFeature> domainFeatureIDs,
			String phenID, SosAbstractFeature foi, String offeringID,
			String mimeType, String unitsOfMeasurement,
			Collection<SosQuality> quality, DQ_UncertaintyResult[] uncQuality,
			IUncertainty uncertainty) {
		super(time, obsID, procID, domainFeatureIDs, phenID, foi, offeringID,
				mimeType, Double.NaN, unitsOfMeasurement, quality);

		this.identifier = identifier;
		this.uncQuality = uncQuality;
		this.uncertainty = uncertainty;
	}

	/**
	 * returns gml identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * returns the uncertainty result
	 */
	public IUncertainty getUncertainty() {
		return uncertainty;
	}

	/**
	 * returns data quality as uncertainties, replacing om1 observations quality
	 */
	public DQ_UncertaintyResult[] getUncQuality() {
		return uncQuality;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
