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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.wps.x100.LiteralInputType;

import org.n52.wps.io.data.IData;

/**
 * A single atomic process input.
 * 
 * @param <T>
 *            the Java type which this input produces.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class SingleProcessInput<T> extends AbstractProcessInput<T> {

	/**
	 * A simple {@link ProcessInputHandler} that gets the payload of the
	 * {@link IData} and casts it to the desired type.
	 */
	protected class SingleProcessInputHandler extends ProcessInputHandler<T> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		protected T processInputs(Map<String, List<IData>> inputs) {
			List<IData> data = inputs.get(SingleProcessInput.this.getId());
			if (data == null) {
				return null;
			} else {
				switch (data.size()) {
				case 0:
					return null;
				case 1:
					return (T) data.get(0).getPayload();
				default:
					List<Object> t = new LinkedList<Object>();
					for (IData d : data) {
						t.add(d.getPayload());
					}
					return (T) t;
				}
			}
		}
	}

	/**
	 * The description of this input.
	 */
	private String description;

	/**
	 * The title of this input.
	 */
	private String title;

	/**
	 * The {@link IData} class for this input.
	 */
	private Class<? extends IData> bindingClass;

	/**
	 * The minimal occurrence of this input.
	 */
	private BigInteger minOccurs;

	/**
	 * The maximal occurrence of this input.
	 */
	private BigInteger maxOccurs;

	/**
	 * The allowed values for this input.
	 */
	private Set<String> allowedValues;

	/**
	 * The default value for this input.
	 */
	private T defaultValue;

	/**
	 * The handler for this input.
	 */
	private ProcessInputHandler<T> handler;

	/**
	 * Creates a new {@link SingleProcessInput}.
	 * 
	 * @param id
	 *            the id of this input
	 * @param title
	 *            the title of this input (if <code>null</code>, {@code id} will
	 *            be used)
	 * @param description
	 *            the description of this input
	 * @param bindingClass
	 *            the {@link IData} class for this input
	 * @param min
	 *            the minimal occurrence of this input
	 * @param max
	 *            the maximal occurrence of this input
	 * @param allowedValues
	 *            the allowed values for this input (can be <code>null</code>)
	 * @param defaultValue
	 *            the default value for this input (can be <code>null</code>)
	 * @param handler
	 *            the input handler (if <code>null</code>,
	 *            {@link SingleProcessInputHandler} will be used)
	 */
	public SingleProcessInput(String id, String title, String description,
			Class<? extends IData> bindingClass, int min, int max,
			Set<String> allowedValues, T defaultValue,
			ProcessInputHandler<T> handler) {
		super(id);
		this.description = description;
		this.title = title;
		this.allowedValues = allowedValues;
		this.defaultValue = defaultValue;
		this.bindingClass = bindingClass;
		this.minOccurs = new BigInteger(String.valueOf(min));
		this.maxOccurs = new BigInteger(String.valueOf(max));
		this.handler = (handler == null) ? new SingleProcessInputHandler() : handler;
		this.handler.setNeededInputs(this.getProcessInputs());
	}

	/**
	 * Creates a new {@link SingleProcessInput} with
	 * {@link SingleProcessInputHandler} as handler.
	 * 
	 * @param id
	 *            the id of this input
	 * @param title
	 *            the title of this input (if <code>null</code>, {@code id} will
	 *            be used)
	 * @param description
	 *            the description of this input
	 * @param bindingClass
	 *            the {@link IData} class for this input
	 * @param min
	 *            the minimal occurrence of this input
	 * @param max
	 *            the maximal occurrence of this input
	 * @param allowedValues
	 *            the allowed values for this input (can be <code>null</code>)
	 * @param defaultValue
	 *            the default value for this input (can be <code>null</code>)
	 */
	public SingleProcessInput(String identifier, String title,
			String description, Class<? extends IData> bindingClass, int min,
			int max, Set<String> allowedValues, T defaultValue) {
		this(identifier, title, description, bindingClass, min, max,
				allowedValues, defaultValue, null);
	}

	/**
	 * @return the default value of this input
	 */
	public T getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * @return the minimal occurrence of this input
	 */
	public BigInteger getMinOccurs() {
		return this.minOccurs;
	}

	/**
	 * @return the maximal occurrence of this input
	 */
	public BigInteger getMaxOccurs() {
		return this.maxOccurs;
	}

	/**
	 * @return the title of this input
	 */
	public String getTitle() {
		return this.title == null ? this.getId() : this.title;
	}

	/**
	 * @return the description of this input
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @return the binding class of this input
	 */
	public Class<? extends IData> getBindingClass() {
		return this.bindingClass;
	}

	/**
	 * Generates an {@link AllowedValues} for this input (or <code>null</code>
	 * if no allowed values are given).
	 * 
	 * @return the allowed values
	 */
	public AllowedValues getAllowedValues() {
		if (allowedValues == null || allowedValues.size() == 0) {
			return null;
		} else {
			LiteralInputType lit = LiteralInputType.Factory.newInstance();
			AllowedValues vals = lit.addNewAllowedValues();
			for (String val : allowedValues) {
				vals.addNewValue().setStringValue(val);
			}
			return vals;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<SingleProcessInput<?>> getProcessInputs() {
		Set<SingleProcessInput<?>> set = new HashSet<SingleProcessInput<?>>();
		set.add(this);
		return set;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T handle(Map<String, List<IData>> inputs) {
		T t = handler.process(inputs);
		if (t == null && this.defaultValue != null)
			return this.defaultValue;
		else
			return t;
	}

}