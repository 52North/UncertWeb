package org.uncertweb.viss.core.web.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public abstract class AbstractWriterProvider<T> implements MessageBodyWriter<T> {
	protected static final Logger log = LoggerFactory
			.getLogger(AbstractWriterProvider.class);

	private final Class<?> classToEncode;
	private final MediaType mediaType;
	@Context private UriInfo uriInfo;

	protected AbstractWriterProvider(Class<?> clazz, MediaType mt) {
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
