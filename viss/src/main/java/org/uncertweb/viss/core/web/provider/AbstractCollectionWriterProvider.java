package org.uncertweb.viss.core.web.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;

import org.uncertweb.utils.UwReflectionUtils;

public abstract class AbstractCollectionWriterProvider<T> extends
		AbstractWriterProvider<Iterable<T>> {

	protected AbstractCollectionWriterProvider(Class<? extends T> clazz,
			MediaType mt) {
		super(clazz, mt);
	}

	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.isCompatible(getMediaType())
				&& UwReflectionUtils.isParameterizedWith(gt, Iterable.class,
						getClassToEncode());
	}
}
