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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;

import javax.ws.rs.Produces;
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
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.Visualizer;
import org.uncertweb.viss.core.web.RESTServlet;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
@Produces(Constants.JSON_VISUALIZER_LIST)
public class VisualizerCollectionProvider implements
		MessageBodyWriter<Iterable<Visualizer>> {

	private UriInfo uriInfo;

	@Context
	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type gt, Annotation[] a,
			MediaType mt) {
		return mt.equals(Constants.JSON_VISUALIZER_LIST_TYPE)
				&& Iterable.class.isAssignableFrom(type)
				&& Utils.isParameterizedWith(gt, Iterable.class,
						Visualizer.class);
	}

	@Override
	public void writeTo(Iterable<Visualizer> o, Class<?> t, Type gt,
			Annotation[] a, MediaType mt, MultivaluedMap<String, Object> hh,
			OutputStream es) throws IOException {
		try {

			JSONArray j = new JSONArray();
			for (Visualizer v : o) {
				URI uri = null;
				if (v.getResource() == null) {
					uri = uriInfo.getBaseUriBuilder()
							.path(RESTServlet.VISUALIZER_WITH_ID)
							.build(v.getShortName());
				} else {

					uri = uriInfo.getBaseUriBuilder()
							.path(RESTServlet.VISUALIZER_FOR_RESOURCE)
							.build(v.getResource().getUUID(), v.getShortName());
				}
				j.put(new JSONObject().put("id", v.getShortName()).put("href",
						uri));
			}

			ReaderWriter
					.writeToAsString(Utils.stringifyJson(new JSONObject().put(
							"visualizers", j)), es, mt);
		} catch (JSONException e) {
			VissError.internal(e);
		}
	}

	@Override
	public long getSize(Iterable<Visualizer> t, Class<?> type,
			Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

}
