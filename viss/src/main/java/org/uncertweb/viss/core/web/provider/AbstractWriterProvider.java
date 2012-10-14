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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWriterProvider<T> implements MessageBodyWriter<T> {
	protected static final Logger log = LoggerFactory
			.getLogger(AbstractWriterProvider.class);

	private final Class<?> classToEncode;
	private final MediaType mediaType;
	@Context private UriInfo uriInfo;

	public AbstractWriterProvider(Class<?> clazz, MediaType mt) {
		this.classToEncode = clazz;
		this.mediaType = mt;
	}

	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	protected Class<?> getClassToEncode() {
		return this.classToEncode;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	@Override
	public long getSize(T x, Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return -1;
	}
}
