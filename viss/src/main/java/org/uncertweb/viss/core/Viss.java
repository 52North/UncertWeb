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
import org.uncertweb.viss.core.visualizer.Visualization;
import org.uncertweb.viss.core.visualizer.VisualizationReference;
import org.uncertweb.viss.core.visualizer.Visualizer;
import org.uncertweb.viss.core.visualizer.VisualizerFactory;
import org.uncertweb.viss.core.wcs.WCSAdapter;

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
	private WCSAdapter wcs = null;
	private ResourceStore store = null;

	private Viss() {
		log.info("Starting up application...");
		this.store = VissConfig.getInstance().getResourceStore();
		this.wcs = VissConfig.getInstance().getWCSAdapter();
		VissConfig.getInstance().scheduleTask(new CleanUpThread(),
				Constants.CLEAN_UP_INTERVAL);
	}

	public static Viss getInstance() {
		return (instance == null) ? instance = new Viss() : instance;
	}

	public void delete(Resource resource) {
		if (lock.deletingResource(resource)) {
			try {
				getWCS().rm(resource);
				getStore().rm(resource);
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
			JSONObject param) {
		Resource resource = this.getResource(uuid);
		if (lock.usingResource(resource, true)) {
			try {
				Visualizer v = VisualizerFactory.getVisualizer(visualizer);
				if (!resource.isLoaded()) {
					resource.load();
				}
				Visualization vis = v.visualize(resource, param);
				if (vis == null)
					throw VissError.internal(new NullPointerException());
				VisualizationReference ref = getWCS().add(vis);
				vis.setReference(ref);
				getStore().saveVisualizationForResource(resource, vis);
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

	public Resource createResource(InputStream is, MediaType mt) {
		return getStore().add(is, mt);
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
		return getStore().getAll();
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

	public StyledLayerDescriptorDocument getSldForVisualization(UUID uuid,
			String vis) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSldForVisualization(UUID uuid, String vis,
			StyledLayerDescriptorDocument sld) {
		// TODO Auto-generated method stub

	}

	protected ResourceStore getStore() {
		return this.store;
	}

	protected WCSAdapter getWCS() {
		return this.wcs;
	}
}
