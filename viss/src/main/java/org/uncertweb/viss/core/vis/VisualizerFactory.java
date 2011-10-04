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
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.VissConfig;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.util.Utils;

public class VisualizerFactory {

	private static final Logger log = LoggerFactory.getLogger(VisualizerFactory.class);

	private static final Map<String, Class<? extends IVisualizer>> creatorsByShortName = Utils.map();
	private static final Map<Class<? extends IVisualizer>, String> shortNamesByCreator = Utils.map();
	private static final Map<MediaType, Set<Class<? extends IVisualizer>>> creatorsByMediaType = Utils.map();

	static {
		String packages = VissConfig.getInstance().get(
		    Constants.SEARCH_PACKAGES_KEY);
		if (packages != null && (packages = packages.trim()).length() != 0) {
			for (String p : packages.split(",")) {
				searchPackage(p);
			}
		} else {
			searchPackage("org.uncertweb.viss");
		}
	}

	private static void searchPackage(String p) {
		log.info("Search for Visualizers in {}", p);
		for (Class<? extends IVisualizer> c : new Reflections(p,new SubTypesScanner())
		    .getSubTypesOf(IVisualizer.class)) {
			try {
				if (!Modifier.isAbstract(c.getModifiers())
				    && !Modifier.isInterface(c.getModifiers())) {

					IVisualizer v = c.newInstance();
					String sn = v.getShortName();
					log.info("Registered Visualizer: {}", c.getName());

					creatorsByShortName.put(sn, c);
					shortNamesByCreator.put(c, sn);

					for (MediaType mt : v.getCompatibleMediaTypes()) {
						Set<Class<? extends IVisualizer>> set = creatorsByMediaType.get(mt);
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

	public static Set<IVisualizer> getVisualizers() {
		Set<IVisualizer> vs = Utils.set();
		for (String name : getNames()) {
			vs.add(getVisualizer(name));
		}
		return vs;
	}

	public static IVisualizer getVisualizer(String shortname) {
		Class<? extends IVisualizer> vc = creatorsByShortName.get(shortname);
		if (vc == null)
			throw VissError.noSuchVisualizer();
		try {
			return vc.newInstance();
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	public static Set<Class<? extends IVisualizer>> getVisualizerForMediaType(
	    MediaType mt) {
		Set<Class<? extends IVisualizer>> set = creatorsByMediaType.get(mt);
		if (set == null)
			set = Utils.set();
		return Collections.unmodifiableSet(set);
	}

	public static Set<String> getNames() {
		return Collections.unmodifiableSet(creatorsByShortName.keySet());
	}

	public static Set<Class<? extends IVisualizer>> getClasses() {
		return Collections
		    .unmodifiableSet(Utils.asSet(creatorsByShortName.values()));
	}

	public static IVisualizer getVisualizerForResource(IResource resource,
	    String visualizer) {
		IVisualizer v = getVisualizer(visualizer);
		if (!resource.isLoaded()) {
			try {
				log.debug("Loading resource {}", resource.getUUID());
				resource.load();
			} catch (IOException e) {
				VissError.internal(e);
			}
		}
		if (v.getCompatibleUncertaintyTypes().contains(resource.getType())
		    && v.getCompatibleMediaTypes().contains(resource.getMediaType())) {
			v.setResource(resource);
			return v;
		} else {
			throw VissError.invalidParameter("Visualizer " + v
			    + " is not available for this resource");
		}
	}

	public static Set<IVisualizer> getVisualizersForResource(IResource resource) {
		Set<Class<? extends IVisualizer>> visualizerForMediaType = getVisualizerForMediaType(resource
		    .getMediaType());
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
		Set<IVisualizer> set = Utils.set();
		for (Class<? extends IVisualizer> v : visualizerForMediaType) {
			IVisualizer vis = getVisualizer(shortNamesByCreator.get(v));
			if (vis.getCompatibleUncertaintyTypes().contains(resource.getType())) {
				vis.setResource(resource);
				set.add(vis);
			}
		}
		return set;
	}
}