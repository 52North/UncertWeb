package org.uncertweb.viss.core;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.codehaus.jettison.json.JSONObject;

public class VissError extends WebApplicationException {

	private static final long serialVersionUID = 5597652128019474217L;

	public VissError(Status status, String message) {
		super(Response.status(status).entity(message)
				.type(MediaType.TEXT_PLAIN).build());
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

	public static VissError noSuchVisualization() {
		return notFound("No such Visualization.");
	}
	
	public static VissError invalidParameter(String name) {
		return new VissError(Status.BAD_REQUEST, "Invalid Visualizer Parameter: '" + name + "'");
	}
	
	public static VissError incompatibleVisualizer() {
		return new VissError(Status.BAD_REQUEST, "Visualizer is not compatible");
	}
}
