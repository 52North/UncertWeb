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
import static org.uncertweb.utils.UwJsonConstants.SPATIAL_EXTENT_KEX;
import static org.uncertweb.utils.UwJsonConstants.TEMPORAL_EXTENT_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.PHENOMENON_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.UNCERTAINTY_TYPE_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.VISUALIZATIONS_KEY;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_DATASET;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_DATASET_TYPE;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
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
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.web.RESTServlet;

import com.sun.jersey.core.util.ReaderWriter;
import com.vividsolutions.jts.geom.Geometry;

@Provider
@Produces({ JSON_DATASET })
public class DataSetProvider implements MessageBodyWriter<IDataSet> {

	private UriInfo uriInfo;

	@Context
	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.isCompatible(JSON_DATASET_TYPE) && IDataSet.class.isAssignableFrom(t);
	}

	public void writeTo(IDataSet r, Class<?> t, Type gt, Annotation[] a,
	    MediaType mt, MultivaluedMap<String, Object> h, OutputStream es)
	    throws IOException, WebApplicationException {
		try {
			
			JSONArray vis = new JSONArray();
			for (IVisualization v : r.getVisualizations()) {
				URI uri = uriInfo.getBaseUriBuilder()
					    .path(RESTServlet.VISUALIZATION)
					    .build(r.getResource().getId(),r.getId(), v.getId());
					vis.put(new JSONObject().put(ID_KEY, v.getId()).put(HREF_KEY, uri));
			}
			JSONObject j = new JSONObject()
				.put(ID_KEY, r.getId())
				.put(PHENOMENON_KEY, r.getPhenomenon())
				.put(UNCERTAINTY_TYPE_KEY, r.getType().getUri())
				.put(TEMPORAL_EXTENT_KEY, r.getTemporalExtent().toJson())
				.put(SPATIAL_EXTENT_KEX, envelopeToJson(r.getSpatialExtent()))
				.put(VISUALIZATIONS_KEY, vis);

			ReaderWriter.writeToAsString(Utils.stringifyJson(j), es, mt);
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	
	}
	
	private JSONObject envelopeToJson(Envelope e) {
		Geometry g = NcUwHelper.envelopeToPolygon(e, true);
		try {
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
	public long getSize(IDataSet r, Class<?> t, Type g, Annotation[] a, MediaType m) {
		return -1;
	}

}
