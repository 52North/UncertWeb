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
package org.uncertweb.sta.wps.api;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.AlgorithmParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle {@link AbstractProcessInput}.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public abstract class ProcessInputHandler<T> {

	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory
			.getLogger(ProcessInputHandler.class);

	/**
	 * The atomic inputs this handler needs.
	 */
	private Set<SingleProcessInput<?>> inputs;

	public ProcessInputHandler(AbstractProcessInput<?>... abstractProcessInputs) {
		Set<SingleProcessInput<?>> inputs = new HashSet<SingleProcessInput<?>>();
		for (AbstractProcessInput<?> abi : abstractProcessInputs) {
			inputs.addAll(abi.getProcessInputs());
		}
		setNeededInputs(inputs);
	}

	/**
	 * @param inputs the inputs this handler needs.
	 */
	public void setNeededInputs(Set<SingleProcessInput<?>> inputs) {
		this.inputs = inputs;
	}

	/**
	 * Processes a WPS input collection. The cardinality of every needed input
	 * will be checked and the actual processing will be delegated to
	 * {@link #processInputs(Map)}.
	 *
	 * @param inputs the process inputs (can't be <code>null</code>)
	 * @return the processed final input
	 */
	public T process(Map<String, List<IData>> inputs) {
		if (inputs == null) {
			throw new NullPointerException();
		}
		Map<String, List<IData>> rawInputs = new HashMap<String, List<IData>>();
		for (SingleProcessInput<?> p : this.getNeededInputs()) {
			List<IData> d = inputs.get(p.getId());
			if (d == null || d.isEmpty()) {
				if (p.getMinOccurs().compareTo(BigInteger.ZERO) > 0) {
					throw new AlgorithmParameterException(
							MessageFormat.format("Missing '{1}' parameter.", p
									.getId()));
				}
			} else {
				BigInteger size = BigInteger.valueOf(d.size());
				if (p.getMinOccurs().compareTo(size) > 0) {
					throw new AlgorithmParameterException(
							MessageFormat
									.format("Parameter '{1}' occurs only {2} times.", p
											.getId(), d.size()));
				} else if (p.getMaxOccurs().compareTo(size) < 0) {
					throw new AlgorithmParameterException(
							MessageFormat
									.format("Parameter '{1}' can only occur {2} times.", p
											.getId(), p.getMaxOccurs()));
				} else {
					rawInputs.put(p.getId(), d);
				}
			}
		}

		return processInputs(rawInputs);
	}

	/**
	 * Checks if this handler consists only of one input. If there is more than
	 * one input an {@link RuntimeException} will be thrown and
	 * <code>null</code> will be returned if there is no input.
	 *
	 * @return the single input or <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	protected SingleProcessInput<T> checkForOnlyOneInput() {
		Set<SingleProcessInput<?>> neededInputs = this.getNeededInputs();
		if (neededInputs.size() == 0) {
			return null;
		} else if (neededInputs.size() > 1) {
			throw new RuntimeException(
					MessageFormat
							.format("{1} is not aplicable for more than 1 input.", this
									.getClass()));
		} else {
			return (SingleProcessInput<T>) neededInputs.iterator().next();
		}
	}

	/**
	 * @return the inputs needed for this handler
	 */
	public Set<SingleProcessInput<?>> getNeededInputs() {
		return this.inputs;
	}

	/**
	 * Processes the inputs and generates the final input.
	 *
	 * @param inputs the process inputs (can't be <code>null</code>)
	 * @return the processed final input
	 */
	protected abstract T processInputs(Map<String, List<IData>> inputs);

}
