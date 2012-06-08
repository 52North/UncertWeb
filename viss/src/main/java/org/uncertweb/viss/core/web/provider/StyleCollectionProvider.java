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
import static org.uncertweb.viss.core.util.JSONConstants.STYLES_KEY;
import static org.uncertweb.viss.core.util.MediaTypes.*;

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
import org.uncertweb.utils.UwReflectionUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.VisualizationStyle;
import org.uncertweb.viss.core.web.RESTServlet;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
@Produces(JSON_VISUALIZATION_STYLE_LIST)
public class StyleCollectionProvider implements
		MessageBodyWriter<Iterable<VisualizationStyle>> {

	private UriInfo uriInfo;

	@Context
	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.isCompatible(JSON_VISUALIZATION_STYLE_LIST_TYPE)
				&& UwReflectionUtils.isParameterizedWith(gt, Iterable.class,
						VisualizationStyle.class);
	}

	@Override
	public long getSize(Iterable<VisualizationStyle> t, Class<?> type,
			Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(Iterable<VisualizationStyle> o, Class<?> t, Type gt,
			Annotation[] a, MediaType mt, MultivaluedMap<String, Object> h,
			OutputStream es) throws IOException, WebApplicationException {
		try {
			
			JSONArray styles = new JSONArray();
			for (VisualizationStyle v : o) {
				URI uri = uriInfo.getBaseUriBuilder()
						.path(RESTServlet.STYLE_FOR_VISUALIZATION)
						.build(v.getVis().getDataSet().getResource().getId(),
								v.getVis().getDataSet().getId(),
								v.getVis().getId(),
								v.getId());
				styles.put(new JSONObject().put(ID_KEY, v.getId()).put(HREF_KEY, uri));
			}
			JSONObject j = new JSONObject().put(STYLES_KEY, styles);
			ReaderWriter.writeToAsString(Utils.stringifyJson(j), es, mt);
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}
}
