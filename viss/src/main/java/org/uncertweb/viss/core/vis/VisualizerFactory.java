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

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.netcdf.NcUwUncertaintyType;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.utils.UwStringUtils;
import org.uncertweb.viss.core.VissConfig;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.util.VissConstants;

public class VisualizerFactory {

	private static final Logger log = LoggerFactory.getLogger(VisualizerFactory.class);

	private static final Map<String, Class<? extends IVisualizer>> creatorsByShortName = UwCollectionUtils.map();
	private static final Map<Class<? extends IVisualizer>, String> shortNamesByCreator = UwCollectionUtils.map();
	private static final Map<MediaType, Set<Class<? extends IVisualizer>>> creatorsByMediaType = UwCollectionUtils.map();

	static {
		String packages = VissConfig.getInstance().get(
		    VissConstants.SEARCH_PACKAGES_KEY);
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
							creatorsByMediaType.put(mt, set = UwCollectionUtils.set());
						}
						set.add(c);
					}
				}
			} catch (Exception e) {
				log.error("Can not instantiate Visualizer '" + c.getName() + "'", e);
			}
		}
	}

	public static Set<IVisualizer> getVisualizers() {
		Set<IVisualizer> vs = UwCollectionUtils.set();
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
			set = UwCollectionUtils.set();
		return Collections.unmodifiableSet(set);
	}

	public static Set<String> getNames() {
		return Collections.unmodifiableSet(creatorsByShortName.keySet());
	}

	public static Set<Class<? extends IVisualizer>> getClasses() {
		return Collections
		    .unmodifiableSet(UwCollectionUtils.asSet(creatorsByShortName.values()));
	}

	public static IVisualizer getVisualizerForDataSet(IDataSet dataSet,
	    String visualizer) {
		IVisualizer v = getVisualizer(visualizer);
		if (v.getCompatibleUncertaintyTypes().contains(dataSet.getType())
		    && v.getCompatibleMediaTypes().contains(dataSet.getResource().getMediaType())) {
			v.setDataSet(dataSet);
			return v;
		} else {
			throw VissError.invalidParameter("Visualizer " + v.getShortName()
			    + " is not available for this resource");
		}
	}

	public static Set<IVisualizer> getVisualizersForDataSet(IDataSet dataSet) {//TODO
		
		Set<Class<? extends IVisualizer>> visualizerForMediaType = 
				getVisualizerForMediaType(dataSet.getResource().getMediaType());
		
		log.debug("Found {} Visualizers for MediaType {}.",
		    visualizerForMediaType.size(), dataSet.getResource().getMediaType());
		
		Set<IVisualizer> set = UwCollectionUtils.set();
		for (Class<? extends IVisualizer> v : visualizerForMediaType) {
			log.debug("Testing {} for compatibility", shortNamesByCreator.get(v));
			IVisualizer vis = getVisualizer(shortNamesByCreator.get(v));
			log.debug("Testing {} for compatibility", vis);
			Set<NcUwUncertaintyType> comp = vis.getCompatibleUncertaintyTypes();
			log.debug("Compatible Types ({}): {}", comp.size(), UwStringUtils.join(", ", comp));
			log.debug("Dataset type: {}", dataSet.getType());
			if (comp.contains(dataSet.getType())) {
				log.debug("{} seems to be compatible", shortNamesByCreator.get(v));
				vis.setDataSet(dataSet);
				set.add(vis);
			} else {
				log.debug("{} is not compatible", shortNamesByCreator.get(v));
			}
		}
		return set;
	}
}