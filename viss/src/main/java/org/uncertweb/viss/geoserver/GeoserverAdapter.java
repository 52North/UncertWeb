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
package org.uncertweb.viss.geoserver;

import static org.uncertweb.viss.core.util.MediaTypes.GEOTIFF;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import javax.media.jai.JAI;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.codehaus.jettison.json.JSONException;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.utils.UwStringUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.vis.DefaultVisualizationReference;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.vis.IVisualizationReference;
import org.uncertweb.viss.core.vis.VisualizationStyle;
import org.uncertweb.viss.core.wms.WMSAdapter;

public class GeoserverAdapter implements WMSAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(GeoserverAdapter.class);
	private static final String PROPERTIES_FILE = "/geoserver.properties";
	private static final String USER_PROPERTY = "geoserver.user";
	private static final String PASS_PROPERTY = "geoserver.pass";
	private static final String CACHE_PROPERTY = "geoserver.cache";
    private static final String SAME_SERVER = "geoserver.local";
	private static final String URL_PROPERTY = "geoserver.uri";
	private static final String EXTERNAL_URL_PROPERTY = "geoserver.uri.external";

	private static Properties p;
	private Geoserver wms;
    private final URL wmsURL;

    public GeoserverAdapter() {
        String user = getProp(USER_PROPERTY);
        String pass = getProp(PASS_PROPERTY);
        boolean sameServer = Boolean.valueOf(getProp(SAME_SERVER));

        File path = null;
        if (sameServer) {
            path = new File(System.getProperty("java.io.tmpdir"), "geoserver");
        }

        String uri = getProp(URL_PROPERTY);
        boolean cache = Boolean.valueOf(getProp(CACHE_PROPERTY));
        String externalURI;
        if (getProp(EXTERNAL_URL_PROPERTY) == null || getProp(EXTERNAL_URL_PROPERTY).isEmpty()) {
            externalURI = uri;
        } else  {
            externalURI = getProp(EXTERNAL_URL_PROPERTY);
        }

        try {
            this.wmsURL = new URL(externalURI + "/wms");
            this.wms = new Geoserver(user, pass, uri, cache, path);
        } catch (IOException ex) {
            throw VissError.internal(ex);
        } catch (JSONException ex) {
            throw VissError.internal(ex);
        }
    }

	protected Geoserver getGeoserver() {
		return this.wms;
	}


	@Override
	public IVisualizationReference addVisualization(IVisualization vis) {
		try {
			String workSpace = getWorkspaceName(vis);
			String coverageStore = getCoverageStoreName(vis);
			String layerName = getLayerName(vis);
			String[] layerNames = null;
			Set<GridCoverage> coverages = vis.getCoverages();

			if (!getGeoserver().containsWorkspace(workSpace)) {
				if (!getGeoserver().createWorkspace(workSpace)) {
					throw VissError.internal("Could not create Workspace");
				}
			}

			if (coverages.isEmpty()) {
				throw VissError.internal("no coverages to insert");
			} else if (coverages.size() == 1) {
				InputStream is =  toInputStream(vis.getCoverages().iterator().next());
				insertCoverage(workSpace, coverageStore, is);
				layerNames = new String[] { layerName };
			} else {
				ArrayList<String> layers = new ArrayList<String>(vis.getCoverages().size());

				int i = 0;

				for (GridCoverage c : vis.getCoverages()) {
					insertCoverage(workSpace, UwStringUtils.join("-", coverageStore, i), toInputStream(c));
					layers.add(UwStringUtils.join("-", layerName, i));
					++i;
				}

				layerNames = layers.toArray(new String[layers.size()]);
			}

			return new DefaultVisualizationReference(this.wmsURL, UwCollectionUtils.set(layerNames));

		} catch (IOException e) {
			throw VissError.internal(e);
		} catch (JSONException e) {
            throw VissError.internal(e);
        } catch (VissError e) {
            throw VissError.internal(e);
        }
	}

	private void insertCoverage(String ws, String cs, InputStream is)
			throws IOException, JSONException {
		if (!getGeoserver().createCoverageStore(cs, ws, "GeoTIFF", true)) {
			throw VissError.internal("Could not create CoverageStore");
		}
		if (!getGeoserver().insertCoverage(ws, cs, GEOTIFF, is)) {
			throw VissError.internal("Could not insert Coverage");
		}
	}

	private InputStream toInputStream(GridCoverage c) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GeoTiffWriter w = new GeoTiffWriter(out);
		GeoTiffFormat format = new GeoTiffFormat();
		GeoTiffWriteParams wp = new GeoTiffWriteParams();
		wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
		wp.setCompressionType("LZW");
		wp.setTilingMode(GeoTiffWriteParams.MODE_EXPLICIT);
		int width = c.getRenderedImage().getWidth();
		int tileWidth = 1024;
		if (width < 2048) {
			tileWidth = new Double(Math.sqrt(width)).intValue();
		}
		wp.setTiling(tileWidth, tileWidth);
		ParameterValueGroup paramWrite = format.getWriteParameters();
		paramWrite.parameter(
				AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString())
				.setValue(wp);
		JAI.getDefaultInstance().getTileCache()
				.setMemoryCapacity(256 * 1024 * 1024);
		w.write(c, paramWrite.values().toArray(new GeneralParameterValue[1]));
		return new ByteArrayInputStream(out.toByteArray());
	}

	@Override
	public boolean deleteResource(IResource resource) {
		try {

			for (IDataSet ds : resource.getDataSets()) {
				for (IVisualization v : ds.getVisualizations()) {
					for (VisualizationStyle s : v.getStyles()) {
						deleteStyle(s);
					}
				}
			}

			return getGeoserver().deleteWorkspace(resource.getId().toString());
		} catch (IOException e) {
			throw VissError.internal(e);
		} catch (JSONException e) {
            throw VissError.internal(e);
        }
	}

	@Override
	public boolean deleteVisualization(IVisualization vis) {
		try {
			boolean deleted = getGeoserver().deleteCoverageStore(getWorkspaceName(vis), getCoverageStoreName(vis));
			for (VisualizationStyle s : vis.getStyles()) {
				deleteStyle(s);
			}
			return deleted;
		} catch (IOException e) {
			throw VissError.internal(e);
		}
	}

	private String getWorkspaceName(IVisualization vis) {
		return vis.getDataSet().getResource().getId().toString();
	}

	private String getCoverageStoreName(IVisualization vis) {
		return vis.getDataSet().getId() + ":" + vis.getId();
	}

	private String getLayerName(IVisualization vis) {
		return getWorkspaceName(vis) + ":" + getCoverageStoreName(vis);
	}

	@Override
	public StyledLayerDescriptorDocument getStyle(VisualizationStyle vis) {
		StyledLayerDescriptorDocument sld;
		try {
			sld = getGeoserver().getStyle(vis.getId().toString());
		} catch (IOException e) {
			throw VissError.internal(e);
		} catch (XmlException e) {
            throw VissError.internal(e);
        }
		if (sld == null) {
			throw VissError.notFound("No attached SLD");
		}
		return sld;
	}

	@Override
	public boolean deleteStyle(VisualizationStyle s) {
		try {
			return getGeoserver().deleteStyle(s.getId().toString());
		} catch (IOException e) {
			throw VissError.internal(e);
		}

	}

	@Override
	public boolean addStyle(VisualizationStyle s) {
		try {
			if (getGeoserver().createStyle(s.getSld(), s.getId().toString())) {
				return getGeoserver().setStyle(getLayerName(s.getVis()), s.getId().toString());
			}
			return false;
		} catch (IOException e) {
			throw VissError.internal(e);
		}
	}

    protected static String getProp(String key) {
        if (p == null) {
            InputStream is = GeoserverAdapter.class.getResourceAsStream(PROPERTIES_FILE);
            if (is == null) {
                throw new RuntimeException("Can not find configuration file");
            }
            p = new Properties();
            try {
                p.load(is);
            } catch (IOException e) {
                LOG.error("Unknown error", e);
                throw new RuntimeException("Can not load configuration file");
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        return p.getProperty(key);
    }

}
