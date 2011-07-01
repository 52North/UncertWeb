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
package org.uncertweb.sta.wps.om;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.intamap.om.ObservationTime;
import org.uncertweb.intamap.uncertml.Uncertainty;

/**
 * A {@code Observation} that is aware of the {@code Observation} it is created
 * from.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class OriginAwareObservation extends Observation {

	private static final long serialVersionUID = -6557874377548562307L;

	/**
	 * @return the {@code Observation}s this {@code Observation} is created from
	 */
	private ObservationCollection sourceObservations;

	/**
	 * @return the URL of the SOS from which the {@link #sourceObservations} are
	 *         fetched from.
	 */
	private String url;

	/**
	 * Creates a new {@link OriginAwareObservation}.
	 * 
	 * @param id the id of the {@code Observation}
	 * @param result the result of the {@code Observation}
	 * @param location the location of the {@code Observation}
	 * @param observationError the observationError of the {@code Observation}
	 * @param observedProperty the observedProperty of the {@code Observation}
	 * @param sensor the sensor id of the {@code Observation}
	 * @param observationTime the observationTime of the {@code Observation}
	 * @param uom the unit of measurement of the {@code Observation}
	 * @param obs the {@code Observation}s this {@code Observation} is created
	 *            from
	 * @param sourceUrl the URL from which {@code obs} are fetched
	 */
	public OriginAwareObservation(String id, double result,
			ISamplingFeature location, Uncertainty observationError,
			String observedProperty, String sensor,
			ObservationTime observationTime, String uom,
			Collection<Observation> obs, String sourceUrl) {
		super(id, result, location, observationError, observedProperty, sensor,
				observationTime, uom);
		if (obs instanceof List) {
			this.sourceObservations = new ObservationCollection(
					(List<Observation>) obs);
		} else {
			this.sourceObservations = new ObservationCollection(
					new LinkedList<Observation>(obs));
		}
		this.url = sourceUrl;
	}

	/**
	 * @return the URL of the SOS from which the Source-{@link Observation}s are
	 *         fetched from.
	 */
	public String getSourceUrl() {
		return this.url;
	}

	/**
	 * @return the Sensor URN's this {@code Observation} is created from.
	 */
	public Set<String> getSourceSensors() {
		HashSet<String> p = new HashSet<String>();
		for (Observation o : getSourceObservations()) {
			p.add(o.getSensorModel());
		}
		return p;
	}

	/**
	 * @return the {@code Observation}s this {@code Observation} is created from
	 */
	public ObservationCollection getSourceObservations() {
		return this.sourceObservations;
	}
}
