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
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.sta.wps.api.AbstractProcessInput;

/**
 * A {@code GroupingMethod} is method which groups a given collection of
 * {@link Observation}s.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public abstract class GroupingMethod<T> implements
		Iterable<ObservationMapping<T>> {

	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory
			.getLogger(GroupingMethod.class);

	/**
	 * The observation collection input.
	 */
	private List<? extends AbstractObservation> observations;

	/**
	 * Inputs specific for this class.
	 */
	private Map<AbstractProcessInput<?>, Object> inputs;

	/**
	 * Set the input for this grouping.
	 *
	 * @param observations the observations to group
	 * @param inputs inputs needed by this {@code GroupingMethod}
	 */
	public void setInputs(List<? extends AbstractObservation> observations,
			Map<AbstractProcessInput<?>, Object> inputs) {
		this.observations = observations;
		this.inputs = inputs;
	}

	/**
	 * @return the observations to group
	 */
	protected List<? extends AbstractObservation> getObservations() {
		return this.observations;
	}

	/**
	 * @return the inputs needed to group
	 */
	protected Map<AbstractProcessInput<?>, Object> getInputs() {
		return this.inputs;
	}

	/**
	 * @return which inputs are needed to group
	 */
	public abstract Set<AbstractProcessInput<?>> getAdditionalInputDeclarations();

}