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

import javax.ws.rs.core.MediaType;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.utils.UwIOUtils;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.resource.IResourceStore;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.vis.IVisualizer;
import org.uncertweb.viss.core.vis.VisualizationStyle;
import org.uncertweb.viss.core.vis.VisualizerFactory;
import org.uncertweb.viss.core.wms.WMSAdapter;

import com.vividsolutions.jts.geom.Point;

public class Viss {
	private class CleanUpThread extends TimerTask {
		@Override
		public void run() {
			DateTime dt = new DateTime().minus(VissConfig.getInstance()
			    .getPeriodToDeleteAfterLastUse());
			log.info("CleanUpThread running. Deleting Resources used before {}", dt);
			int count = 0;
			for (IResource r : getStore().getResourcesUsedBefore(dt)) {
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
	private IResourceStore store = null;

	private Viss() {
		log.info("Starting up application...");
		this.store = VissConfig.getInstance().getResourceStore();
		this.wms = VissConfig.getInstance().getWMSAdapter();
		VissConfig.getInstance().scheduleTask(new CleanUpThread(),
		    VissConfig.getInstance().getCleanUpInterval());
	}

	public static Viss getInstance() {
		return (instance == null) ? instance = new Viss() : instance;
	}

	public void delete(IResource resource) {
		if (lock.deletingResource(resource)) {
			try {
				getWMS().deleteResource(resource);
				getStore().deleteResource(resource);
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

	public void delete(ObjectId oid) {
		delete(getResource(oid));
	}

	public IVisualization getVisualization(String visualizer, ObjectId oid,
			ObjectId dataset, final JSONObject param) {
		IResource resource = getResource(oid);
		IDataSet ds = getDataset(resource, dataset);
		if (lock.usingResource(resource, true)) {
			try {
				IVisualization existentVis = getVisualization(resource, dataset, visualizer, param);
				if (existentVis != null) {
					return existentVis;
				}

				IVisualizer v = VisualizerFactory.getVisualizer(visualizer);
				
				if (!v.getCompatibleMediaTypes().contains(resource.getMediaType())) { 
					throw VissError.incompatibleVisualizer("Incompatible mediatype");
				}
				if(!v.getCompatibleUncertaintyTypes().contains(ds.getType())) {
					throw VissError.incompatibleVisualizer("Incompatible UncertaintyType: "+ds.getType());
				}

				IVisualization vis = v.visualize(ds, param);
				

				if (param != null && vis.getParameters() == null)
					throw new NullPointerException();

				vis.setReference(getWMS().addVisualization(vis));

				ds.addVisualization(vis);

				getStore().saveResource(resource);

				return vis;
			} catch (RuntimeException e) {
				log.warn("Error while retrieving Visualization", e);
				throw e;
			} catch (Exception e) {
				throw VissError.internal(e);
			} finally {
				UwIOUtils.closeQuietly(resource);
				lock.usingResource(resource, false);
			}
		} else {
			throw VissError.internal("Resource is marked for deletion.");
		}
	}

	protected IVisualization getVisualization(IResource r, ObjectId dataset, String visualizer,
	    JSONObject param) {
		log.debug("Searching for Visualizer {} in Resource {} with parameters {}",
		    new Object[] { visualizer, r.getId(), param });
		String paramS = null;
		if (param != null) {
			paramS = param.toString();
		}
		for (IVisualization v : getDataset(r, dataset).getVisualizations()) {
			if (v.getCreator().getShortName().equals(visualizer)) {
				log.debug("Found Visualizer {}", visualizer);
				log.debug("ID: {}; Parameters: {}", v.getId(), v.getParameters());
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

	public void deleteVisualization(ObjectId oid, ObjectId dataset, String vis) {
		IResource r = getResource(oid);
		IVisualization v = getVisualization(r, dataset, vis);
		getStore().deleteVisualizationForResource(v);
	}

	public Set<IVisualization> getVisualizations(ObjectId oid, ObjectId dataset) {
		return getDataSet(oid, dataset).getVisualizations();
	}
	
	public IDataSet getDataSet(ObjectId oid, ObjectId dataSet) {
		return getDataset(getResource(oid), dataSet);
	}
	
	public Set<IDataSet> getDataSetsForResource(ObjectId oid) {
		return getResource(oid).getDataSets();
	}
	
	public IDataSet getDataset(IResource r, ObjectId dataset) {
		for (IDataSet ds : r.getDataSets()) {
			if (ds.getId().equals(dataset)) {
				return ds;
			}
		}
		throw VissError.noSuchDataSet();
	}

	public IVisualization getVisualization(ObjectId oid, ObjectId dataset, String vis) {
		return getVisualization(getResource(oid), dataset, vis);
	}

	public IVisualization getVisualization(IResource r, ObjectId dataset, String vis) {
		for (IVisualization v : getDataset(r, dataset).getVisualizations()) {
			if (v.getId().equals(vis))
				return v;
		}
		throw VissError.noSuchVisualization();
	}

	public IResource createResource(InputStream is, MediaType mt) {
		return getStore().addResource(is, mt);
	}

	public IVisualizer getVisualizer(String shortName) {
		try {
			return VisualizerFactory.getVisualizer(shortName);
		} catch (RuntimeException t) {
			log.warn("Error while retrieving VisualizerDescription for " + shortName,
			    t);
			throw t;
		}
	}

	public Set<IVisualizer> getVisualizers() {
		try {
			return VisualizerFactory.getVisualizers();
		} catch (RuntimeException t) {
			log.warn("Error while retrieving VisualizerDescriptions", t);
			throw t;
		}
	}

	public Set<IVisualizer> getVisualizers(ObjectId oid, ObjectId dataSet) {
		return VisualizerFactory.getVisualizersForDataSet(getDataSet(oid, dataSet));
	}
	

	protected VisualizationStyle getStyle(IVisualization vis, ObjectId style) {
		for (VisualizationStyle s : vis.getStyles()) {
			if (s.getId().equals(style)) {
				return s;
			}
		}
		throw VissError.noSuchStyle();
	}
	
	public IVisualizer getVisualizer(ObjectId oid, ObjectId dataset, String visualizer) {
		return VisualizerFactory.getVisualizerForDataSet(getDataSet(oid, dataset), visualizer);
	}

	public Set<VisualizationStyle> getStyles(ObjectId resource, ObjectId dataset, String vis) {
		return getVisualization(getResource(resource), dataset, vis).getStyles();
	}

	public VisualizationStyle getStyle(ObjectId resource, ObjectId dataset, String vis, ObjectId style) {
		return getStyle(getVisualization(getResource(resource), dataset, vis), style);
	}

	
	public void deleteStyle(ObjectId resource, ObjectId dataset, String vis, ObjectId style) {
		IResource r = getResource(resource);
		IVisualization v = getVisualization(r, dataset, vis);
		VisualizationStyle s = getStyle(v, style);
		getWMS().deleteStyle(s);
		v.removeStyle(s);
		getStore().saveResource(r);
	}

	public VisualizationStyle addStyle(ObjectId resource, ObjectId dataset, String vis, StyledLayerDescriptorDocument sld) {
		IResource r = getResource(resource);
		IVisualization v = getVisualization(r, dataset, vis);
		VisualizationStyle s = new VisualizationStyle(v, sld);
		v.addStyle(s);
		getWMS().addStyle(s);
		getStore().saveResource(r);
		return s;
	}
	
	public VisualizationStyle changeStyle(ObjectId resource, ObjectId dataset,
			String vis, ObjectId style, StyledLayerDescriptorDocument sld) {
		IResource r = getResource(resource);
		IVisualization v = getVisualization(r, dataset, vis);
		VisualizationStyle s = getStyle(v, style);
		getWMS().deleteStyle(s);
		s.setSld(sld);
		getWMS().addStyle(s);
		return s;
	}

	public StyledLayerDescriptorDocument getSldForStyle(ObjectId resource,
			ObjectId dataset, String vis, ObjectId style) {
		IResource r = getResource(resource);
		IVisualization v = getVisualization(r, dataset, vis);
		VisualizationStyle s = getStyle(v, style);
		return getWMS().getStyle(s);
	}

	public IResource getResource(ObjectId oid) {
		return getStore().get(oid);
	}

	public Set<IResource> getResources() {
		return getStore().getAllResources();
	}

	protected IResourceStore getStore() {
		return this.store;
	}

	protected WMSAdapter getWMS() {
		return this.wms;
	}

	public IObservationCollection getValue(ObjectId resource, ObjectId dataset, ValueRequest req) {
		IDataSet ds = getDataSet(resource, dataset);
		if (req.getLocation() != null) {
			if (!(req.getLocation() instanceof Point)) {
				throw VissError.badRequest("currently only points are supported.");
			}
		}
		return ds.getValue((Point) req.getLocation(), req.getTime());
	}

}
