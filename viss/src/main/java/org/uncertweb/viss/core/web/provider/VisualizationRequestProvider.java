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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.web.VisualizationRequest;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
public class VisualizationRequestProvider implements
		MessageBodyReader<VisualizationRequest> {

	@Override
	public boolean isReadable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.equals(MediaType.APPLICATION_JSON_TYPE)
				&& VisualizationRequest.class.isAssignableFrom(t);
	}

	@Override
	public VisualizationRequest readFrom(Class<VisualizationRequest> t,
			Type gt, Annotation[] a, MediaType mt,
			MultivaluedMap<String, String> hh, InputStream es)
			throws IOException, WebApplicationException {
		try {

			JSONObject j = new JSONObject(ReaderWriter.readFromAsString(es, mt));
			JSONObject param = j.optJSONObject("params");
			if (param != null && !param.keys().hasNext()) {
				param = null;
			}
			return new VisualizationRequest(param, j.getString("visualizer"));
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

}
