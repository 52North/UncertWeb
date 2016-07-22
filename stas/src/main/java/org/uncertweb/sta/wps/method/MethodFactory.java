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
package org.uncertweb.sta.wps.method;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.STASException;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.grouping.GroupingMethod;
import org.uncertweb.sta.wps.method.grouping.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.TemporalGrouping;

/**
 * Factory class that handles the method configuration.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class MethodFactory {

	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory
			.getLogger(MethodFactory.class);

	/**
	 * The singleton instance of this {@code class}.
	 */
	private static MethodFactory singleton;

	/**
	 * @return the singleton of this {@code class}
	 */
	public static MethodFactory getInstance() {
		if (singleton == null) {
			singleton = new MethodFactory();
		}
		return singleton;
	}

	/**
	 * All registered {@link TemporalGrouping} methods.
	 */
	private Map<String, Class<? extends TemporalGrouping>> temporalMethods = new HashMap<String, Class<? extends TemporalGrouping>>();

	/**
	 * All registered {@link SpatialGrouping} methods.
	 */
	private Map<String, Class<? extends SpatialGrouping>> spatialMethods = new HashMap<String, Class<? extends SpatialGrouping>>();

	/**
	 * All registered {@link AggregationMethod}s.
	 */
	private Map<String, Class<? extends AggregationMethod>> aggregationMethods = new HashMap<String, Class<? extends AggregationMethod>>();

	/**
	 * Creates the singleton factory and loads the method configuration.
	 */
	private MethodFactory() {
		searchPackage("org.uncertweb.sta.wps.method"); // TODO
	}

	private void searchPackage(String p) {
		log.debug("Searching package {} for methods.", p);
		log.debug(new File(".").getAbsoluteFile().getPath());
		Reflections r = new Reflections(p);

		for (Class<? extends TemporalGrouping> c : r
				.getSubTypesOf(TemporalGrouping.class)) {
			this.temporalMethods.put(c.getName(), c);
			log.info("TemporalGrouping registered: {}", c.getName());
		}

		for (Class<? extends SpatialGrouping> c : r
				.getSubTypesOf(SpatialGrouping.class)) {
			this.spatialMethods.put(c.getName(), c);
			log.info("SpatialGrouping registered: {}", c.getName());
		}

		for (Class<? extends AggregationMethod> c : r
				.getSubTypesOf(AggregationMethod.class)) {
			this.aggregationMethods.put(c.getName(), c);
			log.info("AggregationMethod registered: {}", c.getName());
		}
	}

	/**
	 * @return all registered {@link TemporalGrouping} methods
	 */
	public Set<Class<? extends TemporalGrouping>> getTemporalGroupingMethods() {
		return new HashSet<Class<? extends TemporalGrouping>>(
				this.temporalMethods.values());
	}

	/**
	 * @return all registered {@link SpatialGrouping} methods
	 */
	public Set<Class<? extends SpatialGrouping>> getSpatialGroupingMethods() {
		return new HashSet<Class<? extends SpatialGrouping>>(
				this.spatialMethods.values());
	}

	/**
	 * @return all registered {@link AggregationMethod}s
	 */
	public Set<Class<? extends AggregationMethod>> getAggregationMethods() {
		return new HashSet<Class<? extends AggregationMethod>>(
				aggregationMethods.values());
	}

	/**
	 * Creates the class for the given {@link AggregationMethod} class name.
	 *
	 * @param name the class name
	 * @return the class
	 * @throws STASException TODO
	 */
	public Class<? extends AggregationMethod> getMethodForName(String name)
			throws STASException {
		Class<? extends AggregationMethod> c = this.aggregationMethods
				.get(name);
		if (c == null) {
			throw new STASException("The method " + name + " is not registered");
		}
		return c;
	}

	/**
	 * Fetches the description of a {@link GroupingMethod} or
	 * {@link AggregationMethod} from the properties.
	 *
	 * @param gm the {@link Class} of the {@code GroupingMethod} or
	 *            {@link AggregationMethod}.
	 * @return the description
	 */
	public String getMethodDescription(Class<?> c) {
		return Constants.get("process." + c.getName() + ".desc");
	}

}