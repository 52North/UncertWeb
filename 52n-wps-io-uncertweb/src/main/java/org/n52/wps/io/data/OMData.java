package org.n52.wps.io.data;

import org.apache.log4j.Logger;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.DiscreteNumericObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.ReferenceObservation;
import org.uncertweb.api.om.observation.TextObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.BooleanObservationCollection;
import org.uncertweb.api.om.observation.collections.CategoryObservationCollection;
import org.uncertweb.api.om.observation.collections.DiscreteNumericObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.ReferenceObservationCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;

/**
 * O&M Data, using UncertWeb O&M profile
 * {@link org.uncertweb.api.om.observation.Observation Observations} and
 * {@link org.uncertweb.api.om.observation.ObservationCollection
 * ObservationCollections}
 * 
 * @author Kiesow, staschc
 * 
 */
public class OMData {

	private static Logger log = Logger.getLogger(OMData.class);
	private IObservationCollection obsCol;
	private AbstractObservation obs;
	private String mimeType;

	// private InputStream dataStream;
	// private String mimeType;
	// private String fileExtension;

	public OMData(IObservationCollection obsCol, String mimeType) {
		this.obsCol = obsCol;
		this.mimeType=mimeType;
	}

	public OMData(AbstractObservation obs, String mimeType) {
		this.obs = obs;
		this.mimeType=mimeType;
	}

	// public OMData(InputStream stream, String mimeType) {
	// this.dataStream = stream;
	// this.mimeType = mimeType;
	// this.fileExtension = GenericFileDataConstants.mimeTypeFileTypeLUT()
	// .get(mimeType);
	// }

	/**
	 * gets the observation collection, if available; if there is only one
	 * observation, it is wrapped with an new collection
	 * 
	 * @return an observation collection or null
	 */
	public IObservationCollection getObservationCollection() {

		// wrap a single observation with a new collection
		if (obsCol == null && obs != null) {

			IObservationCollection obsCol = null;

			// build collection depending on the type of observation
			if (obs instanceof BooleanObservation) {
				obsCol = new BooleanObservationCollection();
			} else if (obs instanceof CategoryObservation) {
				obsCol = new CategoryObservationCollection();
			} else if (obs instanceof DiscreteNumericObservation) {
				obsCol = new DiscreteNumericObservationCollection();
			} else if (obs instanceof Measurement) {
				obsCol = new MeasurementCollection();
			} else if (obs instanceof ReferenceObservation) {
				obsCol = new ReferenceObservationCollection();
			} else if (obs instanceof TextObservation) {
				obsCol = new TextObservationCollection();
			} else if (obs instanceof UncertaintyObservation) {
				obsCol = new UncertaintyObservationCollection();
			}

			if (obsCol != null) {
				try {
					obsCol.addObservation((AbstractObservation) obs);
				} catch (Exception e) {
					log.debug("Observation could not be bound to an Collection: "
							+ e.getMessage());
				}
			}
		}
		return obsCol;
	}

	/**
	 * gets the observation, if available
	 * 
	 * @return an observation of null
	 */
	public AbstractObservation getObservation() {
		return obs;
	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}
}

