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

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.Visualizer;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
@Produces(Constants.JSON_VISUALIZER)
public class VisualizerProvider implements MessageBodyWriter<Visualizer> {

	@Override
	public boolean isWriteable(Class<?> type, Type gt, Annotation[] a,
			MediaType mt) {
		return mt.equals(Constants.JSON_VISUALIZER_TYPE)
				&& Visualizer.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(Visualizer o, Class<?> t, Type gt, Annotation[] a,
			MediaType mt, MultivaluedMap<String, Object> hh, OutputStream es)
			throws IOException {
		try {
			ReaderWriter
					.writeToAsString(Utils.stringifyJson(toJson(o)), es, mt);
		} catch (JSONException e) {
			VissError.internal(e);
		}
	}

	static JSONObject toJson(Visualizer v) throws IOException {
		try {
			JSONObject j = new JSONObject()
					.putOpt("description", v.getDescription())
					.put("id", v.getShortName());
			if (v.getResource() != null) {
				j.put("options", v.getOptionsForResource(v.getResource()));
			} else {
				j.put("options", v.getOptions());
			}
			return j;
		} catch (JSONException e) {
			throw VissError.internal(e);
		}

	}

	@Override
	public long getSize(Visualizer v, Class<?> t, Type g, Annotation[] a,
			MediaType m) {
		return -1;
	}

}
