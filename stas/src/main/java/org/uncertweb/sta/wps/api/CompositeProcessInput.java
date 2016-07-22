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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.wps.io.data.IData;

/**
 * Represents a process input which consists of other inputs.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class CompositeProcessInput<T> extends AbstractProcessInput<T> {

	/**
	 * The atomic process inputs of this input.
	 */
	private Set<SingleProcessInput<?>> atomicInputs;

	/**
	 * The handler of this input.
	 */
	private ProcessInputHandler<T> handler;

	/**
	 * Constructs a new {@code CompositeProcessInput}.
	 *
	 * @param id the "virtual" id of this input
	 * @param handler the handler which can handle {@code Input}
	 */
	public CompositeProcessInput(String id, ProcessInputHandler<T> handler) {
		super(id);
		this.handler = handler;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<SingleProcessInput<?>> getProcessInputs() {
		if (this.atomicInputs == null) {
			this.atomicInputs = this.handler.getNeededInputs();
		}
		return this.atomicInputs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T handle(Map<String, List<IData>> inputs) {
		return this.handler.process(inputs);
	}
}
