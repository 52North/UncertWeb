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

import static org.uncertweb.viss.core.util.JSONConstants.DATASETS_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.HREF_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.ID_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.MIME_TYPE_KEY;
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
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.web.RESTServlet;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
@Produces({ JSON_RESOURCE })
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
			JSONArray dss = new JSONArray();
			for (IDataSet ds : r.getDataSets()) {
				URI uri = uriInfo.getBaseUriBuilder()
					    .path(RESTServlet.DATASET)
					    .build(r.getId(), ds.getId().toString());
					dss.put(new JSONObject()
								.put(ID_KEY, ds.getId())
								.put(HREF_KEY, uri));
			}
			JSONObject j = new JSONObject().put(ID_KEY, r.getId())
			    .put(MIME_TYPE_KEY, r.getMediaType())
			    .put(DATASETS_KEY, dss);

			ReaderWriter.writeToAsString(Utils.stringifyJson(j), es, mt);
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public long getSize(IResource r, Class<?> t, Type g, Annotation[] a,
	    MediaType m) {
		return -1;
	}

}
