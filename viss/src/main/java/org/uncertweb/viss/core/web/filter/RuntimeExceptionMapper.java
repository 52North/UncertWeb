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
package org.uncertweb.viss.core.web.filter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<Throwable> {
	private static final Logger log = LoggerFactory
	    .getLogger(RuntimeExceptionMapper.class);

	@Override
	public Response toResponse(Throwable exception) {
		log.info("Mapping Exception", exception);
		if (exception instanceof WebApplicationException) {
			return ((WebApplicationException) exception).getResponse();
		}

		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			exception.printStackTrace(new PrintStream(out));
			return Response.serverError().entity(new String(out.toByteArray()))
			    .type(MediaType.TEXT_PLAIN).build();
		} catch (Throwable t) {
			throw new RuntimeException(exception);
		} finally {
			IOUtils.closeQuietly(out);
		}

	}

}
