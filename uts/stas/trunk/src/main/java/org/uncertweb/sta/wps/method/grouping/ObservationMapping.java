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
package org.uncertweb.sta.wps.method.grouping;

import java.util.List;

import org.uncertweb.intamap.om.Observation;

/**
 * A {@link ObservationMapping} maps an {@link Observation} collection with an
 * attribute.
 * 
 * @param <T> the type of the attribute
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ObservationMapping<T> {

	/**
	 * The {@link Observation}s.
	 */
	private List<Observation> observations;

	/**
	 * The attribute.
	 */
	private T t;

	/**
	 * Creates a new {@code ObservationMapping}.
	 * 
	 * @param t the attribute
	 * @param observations the {@code Observation} collection
	 */
	public ObservationMapping(T t, List<Observation> observations) {
		this.observations = observations;
		this.t = t;
	}

	/**
	 * @return the mapped {@code Observation}s
	 */
	public List<Observation> getObservations() {
		return observations;
	}

	/**
	 * @return the attribute to map
	 */
	public T getKey() {
		return t;
	}
}