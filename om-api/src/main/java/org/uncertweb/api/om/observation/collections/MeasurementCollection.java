package org.uncertweb.api.om.observation.collections;

import java.util.ArrayList;
import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;

/**
 * collection of O&M measurements
 * 
 * @author staschc
 *
 */
public class MeasurementCollection implements IObservationCollection{
	
	/**gml Id of collection*/
	private String gmlId;
	
	/**members of collection*/
	private List<Measurement> members;
	
	/**type name of observation collection*/
	public static final String NAME = "OM_MeasurementCollection";
	
	/**
	 * constructor creates an empty collection; add method can be used to fill the collection
	 * 
	 */
	public MeasurementCollection(){
		members = new ArrayList<Measurement>();
	}
	
	/**
	 * constructor
	 * 
	 * @param members
	 * 			members of collection
	 */
	public MeasurementCollection(List<Measurement> members){
		this.members = members;
	}
	
	/**
	 * @return the members
	 */
	public List<Measurement> getMembers() {
		return members;
	}
	
	@Override
	public String getGmlId() {
		return gmlId;
	}

	@Override
	public void addObservation(AbstractObservation obs) throws IllegalArgumentException {
		if (!(obs instanceof Measurement)){
			throw new IllegalArgumentException("Only Measurement could be added to MeasurementCollection!");
		}
		this.members.add((Measurement)obs);
	}

	@Override
	public List<Measurement> getObservations() {
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
