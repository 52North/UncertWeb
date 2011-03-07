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
 * Represents an input to a WPS process. It creates a second abstraction layer
 * to comfortably handle and encapsulate the process inputs. Especially
 * multivariate inputs can be processed without any knowledge about them.
 * 
 * @see SingleProcessInput
 * @see CompositeProcessInput
 * @see ProcessInputHandler
 * @param <T>
 *            the Java type in which this input will be converted
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public abstract class AbstractProcessInput<T> {

	/**
	 * The identifier of this input.
	 */
	private String id;
	
	/**
	 * Creates a new input with the given id.
	 * 
	 * @param id
	 *            the input id
	 */
	protected AbstractProcessInput(String id) {
		this.id = id;
	}
	
	/**
	 * Breaks down this input to its basic components. If this is a input only
	 * consisting of one input it should simply return itself.
	 * 
	 * @return the basic components of this input
	 */
	public abstract Set<SingleProcessInput<?>> getProcessInputs();

	/**
	 * Handles the input set of a WPS process and extracts the input which is
	 * described by this class.
	 * 
	 * @param inputs
	 *            the WPS inputs
	 * @return the final input
	 */
	public abstract T handle(Map<String, List<IData>> inputs);
	
	/**
	 * @return the identifier of this input
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractProcessInput<?> other = (AbstractProcessInput<?>) obj;
		if (getId() == null && other.getId() != null)
			return false;
		if (!getId().equals(other.getId()))
			return false;
		return true;
	}
}
