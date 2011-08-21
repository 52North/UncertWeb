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
package org.uncertweb.viss.core;

import java.io.InputStream;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.resource.ResourceStore;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.vis.Visualization;
import org.uncertweb.viss.core.vis.Visualizer;
import org.uncertweb.viss.core.vis.VisualizerFactory;
import org.uncertweb.viss.core.wms.WMSAdapter;

import com.sun.jersey.spi.resource.Singleton;

@Singleton
public class Viss {
	private class CleanUpThread extends TimerTask {
		@Override
		public void run() {
			DateTime dt = new DateTime()
					.minus(Constants.DELETE_OLDER_THAN_PERIOD);
			log.info(
					"CleanUpThread running. Deleting Resources used before {}",
					dt);
			int count = 0;
			for (Resource r : getStore().getResourcesUsedBefore(dt)) {
				try {
					delete(r);
					count++;
				} catch (Throwable t) {
					log.error("Error while deleting old resources.", t);
				}
			}
			log.info("Deleted {} old resources.", count);
		}
	}

	private static Logger log = LoggerFactory.getLogger(Viss.class);
	private static Viss instance;

	private Lock lock = Lock.getInstance();
	private WMSAdapter wms = null;
	private ResourceStore store = null;

	private Viss() {
		log.info("Starting up application...");
		this.store = VissConfig.getInstance().getResourceStore();
		this.wms = VissConfig.getInstance().getWMSAdapter();
		VissConfig.getInstance().scheduleTask(new CleanUpThread(),
				Constants.CLEAN_UP_INTERVAL);
	}

	public static Viss getInstance() {
		return (instance == null) ? instance = new Viss() : instance;
	}

	public void delete(Resource resource) {
		if (lock.deletingResource(resource)) {
			try {
				getStore().deleteResource(resource);
				getWMS().deleteResource(resource);
			} catch (RuntimeException e) {
				log.warn("Error while deleting resource.", e);
				throw e;
			} finally {
				lock.deleteResources(resource);
			}
		} else {
			throw VissError
					.internal("Resource is already marked for deletion or is in use.");
		}
	}

	public void delete(UUID uuid) {
		delete(getResource(uuid));
	}

	public Visualization getVisualization(String visualizer, UUID uuid,
			final JSONObject param) {
		Resource resource = this.getResource(uuid);
		if (lock.usingResource(resource, true)) {
			try {
				Visualization existentVis = getVisualization(resource, visualizer, param);
				if (existentVis != null) {
					return existentVis;
				}
				
				Visualizer v = VisualizerFactory.getVisualizer(visualizer);
				if (!resource.isLoaded()) {
					resource.load();
				}
				
				if (!v.isCompatible(resource)) {
					throw VissError.incompatibleVisualizer();
				}
				
				Visualization vis = v.visualize(resource, param);
			
				if (param != null && vis.getParameters() == null)
					throw new NullPointerException();
				
				vis.setReference(getWMS().addVisualization(vis));
				
				resource.addVisualization(vis);
				
				getStore().saveResource(resource);
				
				return vis;
			} catch (RuntimeException e) {
				log.warn("Error while retrieving Visualization", e);
				throw e;
			} catch (Exception e) {
				throw VissError.internal(e);
			} finally {
				lock.usingResource(resource, false);
			}
		} else {
			throw VissError.internal("Resource is marked for deletion.");
		}
	}

	protected Visualization getVisualization(Resource r, String visualizer,
			JSONObject param) {
		log.debug(
				"Searching for Visualizer {} in Resource {} with parameters {}",
				new Object[] { visualizer, r.getUUID(), param });
		String paramS = null;
		if (param != null) {
			paramS = param.toString();
		}
		for (Visualization v : r.getVisualizations()) {
			if (v.getCreator().getShortName().equals(visualizer)) {
				log.debug("Found Visualizer {}", visualizer);
				log.debug("ID: {}; Parameters: {}", v.getVisId(),
						v.getParameters());
				if (param == v.getParameters()) {
					return v;
				}
				if (param != null && v.getParameters() != null) {
					if (paramS.equals(v.getParameters().toString())) {
						log.debug("Found matching visualization");
						return v;
					}
				}
			}
		}
		return null;
	}
	
	public void deleteVisualization(UUID uuid, String vis) {
		Resource r = getResource(uuid);
		Visualization v = getVisualization(r, vis);
		getStore().deleteVisualizationForResource(r, v);
	}

	public Set<Visualization> getVisualizations(UUID uuid) {
		return getResource(uuid).getVisualizations();
	}

	public Visualization getVisualization(UUID uuid, String vis) {
		return getVisualization(getResource(uuid), vis);
	}

	public Visualization getVisualization(Resource r, String vis) {
		for (Visualization v : r.getVisualizations()) {
			if (v.getVisId().equals(vis))
				return v;
		}
		throw VissError.noSuchVisualization();
	}

	public Resource createResource(InputStream is, MediaType mt) {
		return getStore().addResource(is, mt);
	}

	public Visualizer getVisualizer(String shortName) {
		try {
			return VisualizerFactory.getVisualizer(shortName);
		} catch (RuntimeException t) {
			log.warn("Error while retrieving VisualizerDescription for "
					+ shortName, t);
			throw t;
		}
	}

	public Set<Visualizer> getVisualizers() {
		try {
			return VisualizerFactory.getVisualizers();
		} catch (RuntimeException t) {
			log.warn("Error while retrieving VisualizerDescriptions", t);
			throw t;
		}
	}

	public Set<Visualizer> getVisualizers(UUID uuid) {
		return VisualizerFactory.getVisualizerForResource(getResource(uuid));
	}

	public Resource getResource(UUID uuid) {
		return getStore().get(uuid);
	}

	public Set<Resource> getResources() {
		return getStore().getAllResources();
	}

	public StyledLayerDescriptorDocument getSldForVisualization(UUID uuid, String vis) {
		return getWMS().getSldForVisualization(getVisualization(uuid, vis));
	}

	public void setSldForVisualization(UUID uuid, String vis,
			StyledLayerDescriptorDocument sld) {
		Resource r = getResource(uuid);
		Visualization v = getVisualization(r, vis);
		v.setSld(sld);
		getWMS().setSldForVisualization(v);
		getStore().saveResource(r);

	}

	protected ResourceStore getStore() {
		return this.store;
	}

	protected WMSAdapter getWMS() {
		return this.wms;
	}
}
