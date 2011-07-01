/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software 
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24, 
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.sta.wps.method.grouping.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.uncertweb.intamap.om.Observation;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.method.grouping.GroupingMethod;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;

/**
 * Groups a {@link Observation} collection by their observed property.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ObservedPropertyGrouping extends GroupingMethod<String> {

	/**
	 * Creates a new {@code ObservedPropertyGrouping}.
	 * 
	 * @param obs the observations
	 */
	public ObservedPropertyGrouping(List<Observation> obs) {
		this.setInputs(obs, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<ObservationMapping<String>> iterator() {
		HashMap<String, LinkedList<Observation>> map = new HashMap<String, LinkedList<Observation>>();
		for (Observation o : getObservations()) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AbstractProcessInput<?>> getAdditionalInputDeclarations() {
		return Utils.set();
	}

}
