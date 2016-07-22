package org.n52.wps.io.data.binding.complex;

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
 * Binding for {@link OMData}
 * @author Kiesow
 *
 */
public class OMBinding extends UncertWebIODataBinding {
	private static final long serialVersionUID = -3033594002991918048L;

	private static final Logger log = Logger.getLogger(OMBinding.class);
	private IObservationCollection obsCol;
	private AbstractObservation obs;

	public OMBinding(IObservationCollection obsCol) {
		this.obsCol = obsCol;
	}

	public OMBinding(AbstractObservation obs) {
		this.obs = obs;
	}

	@Override
	public IObservationCollection getPayload() {
		return getObservationCollection();
	}

	@Override
	public Class<?> getSupportedClass() {
		return IObservationCollection.class;
	}

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

}

