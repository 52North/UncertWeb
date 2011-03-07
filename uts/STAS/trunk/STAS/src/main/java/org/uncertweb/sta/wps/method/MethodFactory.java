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

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.grouping.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.TemporalGrouping;

/**
 * Factory class that handles the method configuration.
 * 
 * @see Constants.Files#METHODS_CONFIG
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class MethodFactory {

	/**
	 * The Logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(MethodFactory.class);

	/**
	 * The singleton instance of this {@code class}.
	 */
	private static MethodFactory singleton;

	/**
	 * All registered {@link TemporalGrouping} methods.
	 */
	private HashSet<String> temporalMethods;
	/**
	 * All registered {@link SpatialGrouping} methods.
	 */
	private HashSet<String> spatialMethods;
	/**
	 * All registered {@link AggregationMethod}s.
	 */
	private HashSet<String> aggregationMethods;

	/**
	 * Creates the singleton factory and loads the method configuration.
	 */
	private MethodFactory() {
		temporalMethods = new HashSet<String>();
		spatialMethods = new HashSet<String>();
		aggregationMethods = new HashSet<String>();
		for (Class<?> clazz : parseConfigFile()) {
			if (fitsInterface(TemporalGrouping.class, clazz)) {
				temporalMethods.add(clazz.getName());
				log.info("Method class registered: {}", clazz.getName());
			}
			if (fitsInterface(SpatialGrouping.class, clazz)) {
				spatialMethods.add(clazz.getName());
				log.info("Method class registered: {}", clazz.getName());
			}
			if (fitsInterface(AggregationMethod.class, clazz)) {
				aggregationMethods.add(clazz.getName());
				log.info("Method class registered: {}", clazz.getName());
			}
		}
	}

	/**
	 * @return the singleton of this {@code class}
	 */
	public static MethodFactory getInstance() {
		if (singleton == null)
			singleton = new MethodFactory();
		return singleton;
	}

	/**
	 * @return all registered {@link TemporalGrouping} methods
	 */
	public Set<String> getTemporalGroupingMethods() {
		return temporalMethods;
	}

	/**
	 * @return all registered {@link SpatialGrouping} methods
	 */
	public Set<String> getSpatialGroupingMethods() {
		return spatialMethods;
	}

	/**
	 * @return all registered {@link AggregationMethod}s
	 */
	public Set<String> getAggregationMethods() {
		return aggregationMethods;
	}

	/**
	 * Parses the configuration file and loads all line that are not empty or
	 * begin with '#' as classes.
	 * 
	 * @return all found classes
	 * @see Constants.Files#METHODS_CONFIG
	 */
	private LinkedList<Class<?>> parseConfigFile() {
		try {
			LinkedList<Class<?>> result = new LinkedList<Class<?>>();
			List<String> lines = IOUtils.readLines(getClass()
					.getResourceAsStream(Constants.Files.METHODS_CONFIG));
			for (String line : lines) {
				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("#")) {
					result.add(Class.forName(line));
				}
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(
					"Can not load methods from configuration file.", e);
		}
	}

	/**
	 * Tests if the given {@code class} fits the given {@code interface}.
	 * 
	 * @param interfase
	 *            the {@code interface} which shall be implemented
	 * @param test
	 *            the {@code class} which will be tested
	 * @return <code>true</code> if it fits, <code>false</code> otherwise
	 */
	private boolean fitsInterface(Class<?> interfase, Class<?> test) {
		int modifiers = test.getModifiers();
		return !Modifier.isInterface(modifiers)
				&& !Modifier.isAbstract(modifiers)
				&& interfase.isAssignableFrom(test);
	}
}