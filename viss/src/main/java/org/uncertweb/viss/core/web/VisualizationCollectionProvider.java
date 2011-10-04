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
package org.uncertweb.viss.core.web;

import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZER_LIST;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZER_LIST_TYPE;

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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.IVisualization;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
@Produces(JSON_VISUALIZER_LIST)
public class VisualizationCollectionProvider implements
    MessageBodyWriter<Iterable<IVisualization>> {

	private UriInfo uriInfo;

	@Context
	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.equals(JSON_VISUALIZER_LIST_TYPE)
		    && Utils.isParameterizedWith(gt, Iterable.class, IVisualization.class);
	}

	@Override
	public long getSize(Iterable<IVisualization> t, Class<?> type,
	    Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(Iterable<IVisualization> o, Class<?> t, Type gt,
	    Annotation[] a, MediaType mt, MultivaluedMap<String, Object> h,
	    OutputStream es) throws IOException, WebApplicationException {
		try {
			JSONArray vis = new JSONArray();
			for (IVisualization v : o) {
				URI uri = uriInfo.getBaseUriBuilder()
				    .path(RESTServlet.VISUALIZATION_FOR_RESOURCE_WITH_ID)
				    .build(v.getUuid(), v.getVisId());
				vis.put(new JSONObject().put("id", v.getVisId()).put("href", uri));
			}
			JSONObject j = new JSONObject().put("visualizations", vis);
			ReaderWriter.writeToAsString(Utils.stringifyJson(j), es, mt);
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}
}
