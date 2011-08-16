package org.uncertweb.viss.core.web;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.uncertweb.viss.core.util.HttpMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class CORSFilter implements ContainerResponseFilter {
	private static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	private static final String REQUEST_HEADERS = "Access-Control-Request-Headers";
	private static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";
	private static final String REQUEST_METHODS = "Access-Control-Request-Method";
	private static final String ALLOW_METHODS = "Access-Control-Allow-Methods";

	@Override
	public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
		if (request.getMethod().equals(HttpMethod.OPTIONS.toString())) {
			ResponseBuilder b = Response.ok().header(ALLOW_ORIGIN, "*");
			String headers = request.getHeaderValue(REQUEST_HEADERS);
			String methods = request.getHeaderValue(REQUEST_METHODS);
			if (headers != null) b.header(ALLOW_HEADERS, headers);
			if (methods != null) b.header(ALLOW_METHODS, methods);
			response.setResponse(b.build());
		} else response.getHttpHeaders().add(ALLOW_ORIGIN, "*");
		return response;
	}
}
