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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Utils;

import com.sun.jersey.core.util.ReaderWriter;

public abstract class AbstractJsonSingleWriterProvider<T> extends
		AbstractWriterProvider<T> {

	public AbstractJsonSingleWriterProvider(Class<? extends T> clazz,
			MediaType mt) {
		super(clazz, mt);
	}

	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.isCompatible(getMediaType())
				&& getClassToEncode().isAssignableFrom(t);
	}

	@Override
	public void writeTo(T t, Class<?> c, Type gt, Annotation[] a, MediaType mt,
			MultivaluedMap<String, Object> h, OutputStream es)
			throws IOException, WebApplicationException {
		try {
			ReaderWriter
					.writeToAsString(Utils.stringifyJson(encode(t)), es, mt);
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

	protected abstract JSONObject encode(T t) throws JSONException;
}
