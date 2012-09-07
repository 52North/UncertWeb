package org.uncertweb.viss.core.web.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;

public abstract class AbstractSingleWriterProvider<T> extends
		AbstractWriterProvider<T> {

	protected AbstractSingleWriterProvider(Class<? extends T> clazz,
			MediaType mt) {
		super(clazz, mt);
	}

	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.isCompatible(getMediaType())
				&& getClassToEncode().isAssignableFrom(t);
	}
}
