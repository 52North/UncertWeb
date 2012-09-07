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

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.api.gml.io.JSONGeometryDecoder;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.io.JSONObservationParser;
import org.uncertweb.viss.core.ValueRequest;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.MediaTypes;

import com.vividsolutions.jts.geom.Geometry;

@Provider
public class ValueRequestProvider implements MessageBodyReader<ValueRequest> {

	@Override
	public boolean isReadable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return t.isAssignableFrom(ValueRequest.class)
				&& mt.isCompatible(MediaTypes.JSON_VALUE_REQUEST_TYPE);
	}

	@Override
	public ValueRequest readFrom(Class<ValueRequest> c, Type gt,
			Annotation[] a, MediaType mt, MultivaluedMap<String, String> h,
			InputStream in) throws IOException, WebApplicationException {

		try {
			JSONObject j = new JSONObject(IOUtils.toString(in));
			JSONObject jt = j.optJSONObject("time");
			JSONObject jg = j.optJSONObject("location");
			TimeObject t = null;
			Geometry g = null;
			if (jt != null) {
				t = new JSONObservationParser()
						.parseTime(new org.json.JSONObject(jt.toString()));
			}
			if (jg != null) {
				g = new JSONGeometryDecoder().parseUwGeometry(jg.toString());
			}
			return new ValueRequest(g, t);
		} catch (Exception e) {
			throw VissError.badRequest(e);
		}
	}

}
