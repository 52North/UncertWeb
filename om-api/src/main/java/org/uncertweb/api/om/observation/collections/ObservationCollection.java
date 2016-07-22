package org.uncertweb.api.om.observation.collections;

import java.util.LinkedList;
import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;


/**
 * class represents a soft-typed collection where a mix of different observation types is allowed
 *
 * @author staschc
 *
 */
public class ObservationCollection implements IObservationCollection{

		/**gml Id of collection*/
		private String gmlId;

		/**members of collection*/
		private List<AbstractObservation> members;

		/**type name of observation collection*/
		public static final String NAME = "OM_ObservationCollection";

		/**
		 * constructor creates an empty collection; add method can be used to fill the collection
		 *
		 */
		public ObservationCollection(){
			members = new LinkedList<AbstractObservation>();
		}

		/**
		 * constructor
		 *
		 * @param members
		 * 			members of collection
		 */
		public ObservationCollection(List<AbstractObservation> members){
			this.members = members;
		}

		/**
		 * @return the members
		 */
		public List<AbstractObservation> getMembers() {
			return members;
		}

		@Override
		public String getGmlId() {
			return gmlId;
		}

		@Override
		public void addObservation(AbstractObservation obs) throws IllegalArgumentException {
			this.members.add(obs);
		}

		@Override
		public List<AbstractObservation> getObservations() {
			return members;
		}

		@Override
		public String getTypeName() {
			return NAME;
		}

		@Override
		public void addObservationCollection(IObservationCollection obsCol) {
			if (obsCol.getObservations().get(0) instanceof Measurement){
				for (AbstractObservation ao : obsCol.getObservations()) {
					this.members.add((Measurement) ao);
				}
			}
			else {
				throw new RuntimeException("ObservationCollection with type"+obsCol.getObservations().get(0).getName()+ " cannot be added to MeasurementCollection!!");
			}
		}
	}
