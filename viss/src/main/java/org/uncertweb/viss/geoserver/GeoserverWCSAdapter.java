package org.uncertweb.viss.geoserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.visualizer.Visualization;
import org.uncertweb.viss.core.visualizer.VisualizationReference;
import org.uncertweb.viss.core.wcs.WCSAdapter;

public class GeoserverWCSAdapter implements WCSAdapter {

	private static Properties p;
	private Geoserver wcs;
	private static final Logger log = LoggerFactory
			.getLogger(GeoserverWCSAdapter.class);

	protected static String getProp(String key) {
		if (p == null) {
			InputStream is = GeoserverWCSAdapter.class
					.getResourceAsStream("/geoserver.properties");
			if (is == null)
				throw new RuntimeException("Can not find configuration file");
			p = new Properties();
			try {
				p.load(is);
			} catch (IOException e) {
				log.error("Unknown error", e);
				throw new RuntimeException("Can not load configuration file");
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
		return p.getProperty(key);
	}

	protected Geoserver getGeoserver() {
		return this.wcs;
	}

	public GeoserverWCSAdapter() {
		String user = getProp("user");
		String pass = getProp("pass");
		String url = getProp("baseUrl");
		boolean cache = Boolean.valueOf(getProp("cacheWorkspaceList"));
		String path = getProp("path");
		try {
			if (path != null && !path.trim().isEmpty()) {
				wcs = new Geoserver(user, pass, url, cache, new File(path));
			} else {
				wcs = new Geoserver(user, pass, url, cache);
			}
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public VisualizationReference addVisualization(Visualization vis) {
		try {
			String ws = vis.getUuid().toString();
			String cs = vis.getVisId();
			if (!getGeoserver().containsWorkspace(ws)) {
				if (!getGeoserver().createWorkspace(ws)) {
					throw VissError.internal("Could not create Workspace");
				}
			}

			if (!getGeoserver().createCoverageStore(cs, ws, "GeoTIFF", true)) {
				throw VissError.internal("Could not create CoverageStore");
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GeoTiffWriter w = new GeoTiffWriter(out);
			w.write(vis.getCoverages().iterator().next(), null);
			ByteArrayInputStream in = new ByteArrayInputStream(
					out.toByteArray());

			if (!getGeoserver().insertCoverage(ws, cs, Constants.GEOTIFF, in)) {
				throw VissError.internal("Could not insert Coverage");
			}

			return new VisualizationReference(getGeoserver().getUrl(),
					vis.getVisId());
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public boolean deleteResource(Resource resource) {
		try {
			return getGeoserver()
					.deleteWorkspace(resource.getUUID().toString());
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public boolean setSldForVisualization(Visualization vis) {
		try {
			if (getGeoserver().createStyle(vis.getSld(), vis.getVisId())) {
				return getGeoserver().setStyle(vis.getVisId(), vis.getVisId());
			}
			return false;
			
		} catch (IOException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public boolean deleteVisualization(Visualization vis) {
		try {
			return getGeoserver().deleteCoverageStore(vis.getUuid().toString(),
					vis.getVisId());
		} catch (IOException e) {
			throw VissError.internal(e);
		}
	}
}
