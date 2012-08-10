package org.uncertweb.viss.core.web.filter;

import java.net.URI;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class BaseUriProvider implements ContainerRequestFilter {
	private static final ReentrantLock lock = new ReentrantLock();
	@Context private UriInfo uriInfo;
	private static URI baseUri = null;

	@Override
	public ContainerRequest filter(ContainerRequest request) {
		if (baseUri == null) {
			lock.lock();
			try {
				if (baseUri == null) {
					baseUri = uriInfo.getBaseUri();
				}
			} finally {
				lock.unlock();
			}
		}
		return request;
	}
	
	
	public static URI getBaseURI() {
		return baseUri;
	}
}