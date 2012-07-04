/*
 * Copyright (C) 2012 52° North Initiative for Geospatial Open Source Software
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
package org.uncertweb.omcs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.uncertweb.omcs.Constants.*;

@Path("/")
public class ConversionServlet {
	
	private static final Logger log = LoggerFactory.getLogger(ConversionServlet.class);

	private WebApplicationException missingQueryParameter(String name) {
		return new WebApplicationException(Response.status(Status.BAD_REQUEST)
				.type(PLAIN_TYPE).entity(name + " query parameter is missing")
				.build());
	}

	@GET
	@Produces({ JSON, XML, CSV })
	public Response get(@QueryParam("type") MediaType from,
						 @HeaderParam("accept") MediaType to,
						 @QueryParam("to") MediaType qto,
						 @QueryParam("url") URL url) {

		if (url == null) throw missingQueryParameter("url");
		if (from == null) throw missingQueryParameter("type");
		
		if (qto != null) to = qto;
		
		try {
			return post(from, to, url.openStream());
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
	}

	@POST
	@Consumes({ JSON, XML })
	@Produces({ JSON, XML, CSV })
	public Response post(@HeaderParam("content-type") MediaType from,
						  @HeaderParam("accept") MediaType to,
						  InputStream i) {
		
		if (from == null) throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
		if (to == null) to = from;
		if (from.isCompatible(to)) return Response.ok(i, to).build();
		
		log.debug("Converting from {} to {}", from, to);
		
		Conversion c = Conversion.fromMediaTypes(from, to);
		if (c == null) {
			throw new WebApplicationException(Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.type(PLAIN_TYPE)
					.entity("Conversion (" + from + " -> " + to
							+ ") is currently not supported").build());
		}
		return c.conv(i);
	}
}
