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

import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZATION;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZATION_TYPE;

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
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.vis.IVisualizationReference;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
@Produces(JSON_VISUALIZATION)
public class VisualizationProvider implements MessageBodyWriter<IVisualization> {

	private UriInfo uriInfo;

	@Context
	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type gt, Annotation[] a,
	    MediaType mt) {
		return mt.equals(JSON_VISUALIZATION_TYPE)
		    && IVisualization.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(IVisualization o, Class<?> t, Type gt, Annotation[] a,
	    MediaType mt) {
		return -1;
	}

	@Override
	public void writeTo(IVisualization v, Class<?> t, Type gt, Annotation[] a,
	    MediaType mt, MultivaluedMap<String, Object> hh, OutputStream es)
	    throws IOException {
		try {
			URI uri = uriInfo.getBaseUriBuilder()
			    .path(RESTServlet.VISUALIZER_FOR_RESOURCE)
			    .build(v.getUuid(), v.getCreator().getShortName());
			JSONObject j = new JSONObject()
			    .put("id", v.getVisId())
			    .put(
			        "visualizer",
			        new JSONObject().put("id", v.getCreator().getShortName()).put(
			            "href", uri)).put("params", v.getParameters())
			    .put("minValue", v.getMinValue()).put("maxValue", v.getMaxValue())
			    .put("uom", v.getUom()).put("customSLD", v.getSld() != null);

			IVisualizationReference vr = v.getReference();

			if (vr != null) {
				JSONArray ar = new JSONArray();
				for (String l : vr.getLayers()) {
					ar.put(l);
				}
				j.put("reference",
				    new JSONObject().put("url", vr.getWmsUrl()).put("layers", ar));
			}
			ReaderWriter.writeToAsString(Utils.stringifyJson(j), es, mt);
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}
}
