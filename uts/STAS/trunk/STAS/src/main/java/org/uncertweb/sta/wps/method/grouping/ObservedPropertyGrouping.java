package org.uncertweb.sta.wps.method.grouping;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.uncertweb.intamap.om.Observation;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.ProcessInput;

public class ObservedPropertyGrouping extends GroupingMethod<String> {
	
	public ObservedPropertyGrouping(List<Observation> obs) {
		this.setInputs(obs, null);
	}
	
	@Override
	public Iterator<ObservationMapping<String>> iterator() {
		HashMap<String,LinkedList<Observation>> map = new HashMap<String,LinkedList<Observation>> ();
		for(Observation o : getObservations()) {
			String prop = o.getObservedProperty();
			LinkedList<Observation> obs = map.get(prop);
			if (obs == null) {
				map.put(prop, obs = new LinkedList<Observation>());
			}
			obs.add(o);
		}
		LinkedList<ObservationMapping<String>> mappings = new LinkedList<ObservationMapping<String>>();
		for (String prop : map.keySet()) {
			mappings.add(new ObservationMapping<String>(prop, map.get(prop)));
		}
		return mappings.iterator();
	}

	@Override
	public Set<ProcessInput> getAdditionalInputDeclarations() {
		return Utils.set();
	}

}
