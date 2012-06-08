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
package org.uncertweb.viss.core;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.codehaus.jettison.json.JSONObject;

public class VissError extends WebApplicationException {

	private static final long serialVersionUID = 5597652128019474217L;

	public VissError(Status status, String message) {
		super(Response.status(status).entity(message).type(MediaType.TEXT_PLAIN)
		    .build());
	}

	public VissError(Status status, JSONObject json) {
		super(Response.status(status).entity(json.toString())
		    .type(MediaType.APPLICATION_JSON).build());
	}

	public VissError(Status code, Throwable cause) {
		super(cause, code);
	}

	@Override
	public String getMessage() {
		if (this.getResponse().getEntity() == null) {
			return this.getCause().getLocalizedMessage();
		} else {
			return this.getResponse().getEntity().toString();
		}
	}

	public static VissError internal(String messageFormat, Object... o) {
		return new VissError(Status.INTERNAL_SERVER_ERROR, String.format(messageFormat, o));
	}
	
	public static VissError internal(String message) {
		return new VissError(Status.INTERNAL_SERVER_ERROR, message);
	}

	public static VissError internal(JSONObject message) {
		return new VissError(Status.INTERNAL_SERVER_ERROR, message);
	}

	public static VissError internal(Throwable cause) {
		if (cause != null && cause instanceof VissError)
			return (VissError) cause;
		return new VissError(Status.INTERNAL_SERVER_ERROR, cause);
	}

	public static VissError notFound(String message) {
		return new VissError(Status.NOT_FOUND, message);
	}

	public static VissError notFound(JSONObject message) {
		return new VissError(Status.NOT_FOUND, message);
	}

	public static VissError noSuchResource() {
		return notFound("No such Resource.");
	}

	public static VissError noSuchVisualizer() {
		return notFound("No such Visualizer.");
	}
	
	public static VissError noSuchStyle() {
		return notFound("No such VisualizationStyle.");
	}

	public static VissError noSuchVisualization() {
		return notFound("No such Visualization.");
	}
	
	public static VissError noSuchDataSet() {
		return notFound("No such DataSet.");
	}

	public static VissError invalidParameter(String name) {
		return new VissError(Status.BAD_REQUEST, "Invalid Visualizer Parameter: '"
		    + name + "'");
	}

	public static VissError incompatibleVisualizer() {
		return new VissError(Status.BAD_REQUEST, "Visualizer is not compatible");
	}
	
	public static VissError incompatibleVisualizer(String message) {
		return new VissError(Status.BAD_REQUEST, "Visualizer is not compatible: "+ message);
	}

	public static VissError badRequest(String message) {
		return new VissError(Status.BAD_REQUEST, message);
	}
	public static VissError badRequest(Throwable cause) {
		return new VissError(Status.BAD_REQUEST, cause);
	}
	
}
