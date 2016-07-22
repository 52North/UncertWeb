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
package org.uncertweb.viss.core.web.provider;

import static org.uncertweb.utils.UwJsonConstants.HREF_KEY;
import static org.uncertweb.utils.UwJsonConstants.ID_KEY;
import static org.uncertweb.utils.UwJsonConstants.SPATIAL_EXTENT_KEY;
import static org.uncertweb.utils.UwJsonConstants.TEMPORAL_EXTENT_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.PHENOMENON_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.UNCERTAINTY_TYPE_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.VISUALIZATIONS_KEY;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_DATASET_TYPE;

import java.net.URI;

import javax.ws.rs.ext.Provider;

import org.apache.xmlbeans.XmlException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.opengis.geometry.Envelope;
import org.uncertweb.api.gml.io.JSONGeometryEncoder;
import org.uncertweb.netcdf.NcUwHelper;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.web.RESTServlet;

import com.vividsolutions.jts.geom.Geometry;

@Provider
public class DataSetProvider extends AbstractJsonSingleWriterProvider<IDataSet> {
	public static final String EPSG_CODE_QUERY_PARAMETER = "srs";

	public DataSetProvider() {
		super(IDataSet.class, JSON_DATASET_TYPE);
	}

	private JSONObject envelopeToJson(Envelope e) {
		String epsg = getUriInfo().getQueryParameters().getFirst(EPSG_CODE_QUERY_PARAMETER);
		Geometry g = NcUwHelper.envelopeToPolygon(e, (epsg != null) ? Integer.valueOf(epsg) : null);
		try {
			log.debug("Geomertry Code: {}", g.getSRID());
			return new JSONObject(new JSONGeometryEncoder().encodeGeometry(g));
		} catch (XmlException x) {
			throw VissError.internal(x);
		} catch (org.json.JSONException x) {
			throw VissError.internal(x);
		} catch (JSONException x) {
			throw VissError.internal(x);
		}
	}

	@Override
	protected JSONObject encode(IDataSet r) throws JSONException {
		JSONArray vis = new JSONArray();
		for (IVisualization v : r.getVisualizations()) {
			URI uri = getUriInfo().getBaseUriBuilder()
					.path(RESTServlet.VISUALIZATION)
					.build(r.getResource().getId(), r.getId(), v.getId());
			vis.put(new JSONObject()
				.put(ID_KEY, v.getId())
				.put(HREF_KEY, uri));
		}
		return new JSONObject()
				.put(ID_KEY, r.getId())
				.put(PHENOMENON_KEY, r.getPhenomenon())
				.put(UNCERTAINTY_TYPE_KEY, r.getType().getUri())
				.put(TEMPORAL_EXTENT_KEY, r.getTemporalExtent().toJson())
				.put(SPATIAL_EXTENT_KEY, envelopeToJson(r.getSpatialExtent()))
				.put(VISUALIZATIONS_KEY, vis);
	}

}
