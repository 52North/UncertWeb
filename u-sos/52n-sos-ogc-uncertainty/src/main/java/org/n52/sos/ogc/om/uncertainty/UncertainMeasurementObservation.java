package org.n52.sos.ogc.om.uncertainty;

import java.util.Collection;

import org.n52.sos.ogc.gml.time.ISosTime;
import org.n52.sos.ogc.om.SosMeasurement;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.quality.SosQuality;
import org.n52.sos.ogc.uncertainty.IUncertainObject;

public class UncertainMeasurementObservation extends SosMeasurement implements
		IUncertainObject {
	
	/** type name of this observation, e.g. OM_Measurement or OM_UncertaintyObservation */
	public final String NAME;

    /**
     * constructor
     * 
     * @param time
     *            time at which the observation event took place
     * @param obsID
     *            id of the observation
     * @param procID
     *            id of the procedure, by which the value was produced
     * @param foiID
     *            id of the feature of interest, to which the observation
     *            belongs
     * @param phenID
     *            id of the phenomenon, of which the value is
     * @param offeringID
     *            id of the offering to which this observation belongs
     * @param mimeType
     *            mimeType of the observation result
     * @param value
     *            result value
     */
	public UncertainMeasurementObservation(ISosTime time, String obsID,
			String procID, Collection<SosAbstractFeature> domainFeatureIDs,
			String phenID, SosAbstractFeature foi, String offeringID,
			String mimeType, double value, String unitsOfMeasurement,
			Collection<SosQuality> quality, String name) {
		super(time, obsID, procID, domainFeatureIDs, phenID, foi, offeringID, mimeType,
				value, unitsOfMeasurement, quality);
		this.NAME = name;
	}

	@Override
	public String getUncertainty() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * returns the name of observation type
	 * 
	 * @return name
	 */
	public String getName() {
		return NAME;
	}

}
