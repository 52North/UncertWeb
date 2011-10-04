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

import static org.uncertweb.viss.core.util.MediaTypes.JSON_RESOURCE;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_RESOURCE_TYPE;

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
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.IVisualization;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
@Produces({ JSON_RESOURCE, MediaType.TEXT_HTML })
public class ResourceProvider implements MessageBodyWriter<IResource> {

	private UriInfo uriInfo;

	@Context
	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return (mt.equals(JSON_RESOURCE_TYPE) || mt
		    .equals(MediaType.TEXT_HTML_TYPE))
		    && IResource.class.isAssignableFrom(t);
	}

	public void writeTo(IResource r, Class<?> t, Type gt, Annotation[] a,
	    MediaType mt, MultivaluedMap<String, Object> h, OutputStream es)
	    throws IOException, WebApplicationException {
		try {
			if (mt.equals(MediaType.TEXT_HTML_TYPE)) {
				ReaderWriter.writeToAsString(getHTML(r), es, mt);
			} else {
				JSONArray vis = new JSONArray();
				for (IVisualization v : r.getVisualizations()) {
					URI uri = uriInfo.getBaseUriBuilder()
					    .path(RESTServlet.VISUALIZATION_FOR_RESOURCE_WITH_ID)
					    .build(r.getUUID(), v.getVisId());
					vis.put(new JSONObject().put("id", v.getVisId()).put("href", uri));
				}
				JSONObject j = new JSONObject().put("id", r.getUUID())
				    .put("mimeType", r.getMediaType())
				    .put("phenomenon", r.getPhenomenon())
				    .putOpt("temporalExtent", r.getTemporalExtent().toJson())
				    .put("visualizations", vis);

				ReaderWriter.writeToAsString(Utils.stringifyJson(j), es, mt);
			}
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

	public String getHTML(IResource r) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>VISS</title></head><body>");

		sb.append("<ul>");
		for (IVisualization v : r.getVisualizations()) {
			URI uri = uriInfo.getBaseUriBuilder()
			    .path(RESTServlet.VISUALIZATION_FOR_RESOURCE_WITH_ID)
			    .build(r.getUUID(), v.getVisId());
			sb.append("<a href=\"").append(uri.toString()).append("\">")
			    .append(v.getVisId()).append("</a>");
		}
		sb.append("</ul>");

		sb.append("</body></html>");
		return sb.toString();
	}

	@Override
	public long getSize(IResource r, Class<?> t, Type g, Annotation[] a,
	    MediaType m) {
		return -1;
	}

}
