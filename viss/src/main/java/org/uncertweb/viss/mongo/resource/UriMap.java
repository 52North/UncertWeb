package org.uncertweb.viss.mongo.resource;

import java.net.URI;

public class UriMap {
	private URI uri;
	private Object o;

	public UriMap() {
	}

	public UriMap(URI uri, Object o) {
		setObject(o);
		setUri(uri);
	}

	public Object getObject() {
		return o;
	}

	public void setObject(Object o) {
		this.o = o;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}
}
