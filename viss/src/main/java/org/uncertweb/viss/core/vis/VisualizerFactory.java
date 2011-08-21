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
package org.uncertweb.viss.core.vis;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.util.Utils;

public class VisualizerFactory {

	private static final Logger log = LoggerFactory
			.getLogger(VisualizerFactory.class);

	private static final Map<String, Class<? extends Visualizer>> creatorsByShortName = Utils
			.map();
	private static final Map<Class<? extends Visualizer>, String> shortNamesByCreator = Utils
			.map();
	private static final Map<MediaType, Set<Class<? extends Visualizer>>> creatorsByMediaType = Utils
			.map();

	static {
		String packages = Constants.get(Constants.SEARCH_PACKAGES_KEY);
		if (packages != null && (packages = packages.trim()).length() != 0) {
			for (String p : packages.split(",")) {
				searchPackage(p);
			}
		} else {
			searchPackage(VisualizerFactory.class.getPackage().getName());
		}
	}

	private static void searchPackage(String p) {
		log.info("Search for Visualizers in {}", p);
		for (Class<? extends Visualizer> c : new Reflections(p)
				.getSubTypesOf(Visualizer.class)) {
			try {
				if (!Modifier.isAbstract(c.getModifiers()) && !Modifier.isInterface(c.getModifiers())) {
					
					Visualizer v = c.newInstance();
					String sn = v.getShortName();
					log.info("Registered Visualizer: {}", c.getName());

					creatorsByShortName.put(sn, c);
					shortNamesByCreator.put(c, sn);

					for (MediaType mt : v.getCompatibleMediaTypes()) {
						Set<Class<? extends Visualizer>> set = creatorsByMediaType
								.get(mt);
						if (set == null) {
							creatorsByMediaType.put(mt, set = Utils.set());
						}
						set.add(c);
					}
				}
			} catch (Exception e) {
				log.error("Can not instantiate Visualizer '{}'", c.getName());
			}
		}
	}

	public static Set<Visualizer> getVisualizers() {
		Set<Visualizer> vs = Utils.set();
		for (String name : getNames()) {
			vs.add(getVisualizer(name));
		}
		return vs;
	}

	public static Visualizer getVisualizer(String shortname) {
		return fromName(shortname, creatorsByShortName);
	}

	public static Set<Class<? extends Visualizer>> getVisualizerForMediaType(
			MediaType mt) {
		Set<Class<? extends Visualizer>> set = creatorsByMediaType.get(mt);
		if (set == null)
			set = Utils.set();
		return Collections.unmodifiableSet(set);
	}

	private static Visualizer fromName(String name,
			Map<String, Class<? extends Visualizer>> map) {
		Class<? extends Visualizer> vc = map.get(name);
		if (vc == null)
			throw VissError.noSuchVisualizer();
		try {
			return vc.newInstance();
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	public static Set<String> getNames() {
		return Collections.unmodifiableSet(creatorsByShortName.keySet());
	}

	public static Set<Class<? extends Visualizer>> getClasses() {
		return Collections.unmodifiableSet(Utils.asSet(creatorsByShortName
				.values()));
	}

	public static Set<Visualizer> getVisualizerForResource(Resource resource) {
		Set<Class<? extends Visualizer>> visualizerForMediaType = 
				getVisualizerForMediaType(resource.getMediaType());
		log.debug("Found {} Visualizers for MediaType {}.",
				visualizerForMediaType.size(), resource.getMediaType());
		if (!visualizerForMediaType.isEmpty() && resource.isLoaded() == false) {
			try {
				log.debug("Loading resource {}", resource.getUUID());
				resource.load();
			} catch (IOException e) {
				VissError.internal(e);
			}
		}
		Set<Visualizer> set = Utils.set();
		for (Class<? extends Visualizer> v : visualizerForMediaType) {
			Visualizer vis = getVisualizer(shortNamesByCreator.get(v));
			if (vis.isCompatible(resource)) {
				set.add(vis);
			}
		}
		return set;
	}
}