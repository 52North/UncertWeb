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

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.method.grouping.GroupingMethod;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.utils.UwCollectionUtils;

/**
 * Groups a {@link Observation} collection by their observed property.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ObservedPropertyGrouping extends GroupingMethod<URI> {

	/**
	 * Creates a new {@code ObservedPropertyGrouping}.
	 *
	 * @param obs
	 *            the observations
	 */
	public ObservedPropertyGrouping(List<? extends AbstractObservation> obs) {
		this.setInputs(obs, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<ObservationMapping<URI>> iterator() {
		Map<URI, LinkedList<AbstractObservation>> map = new HashMap<URI, LinkedList<AbstractObservation>>();
		for (AbstractObservation o : getObservations()) {
			URI prop = o.getObservedProperty();
			LinkedList<AbstractObservation> obs = map.get(prop);
			if (obs == null) {
				map.put(prop, obs = new LinkedList<AbstractObservation>());
			}
			obs.add(o);
		}
		LinkedList<ObservationMapping<URI>> mappings = new LinkedList<ObservationMapping<URI>>();
		for (URI prop : map.keySet()) {
			mappings.add(new ObservationMapping<URI>(prop, map.get(prop)));
		}
		return mappings.iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AbstractProcessInput<?>> getAdditionalInputDeclarations() {
		return UwCollectionUtils.set();
	}

}
