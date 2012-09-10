package org.uncertweb.viss.core.web.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Utils;

import com.sun.jersey.core.util.ReaderWriter;

public abstract class AbstractJsonSingleWriterProvider<T> extends
		AbstractWriterProvider<T> {

	public AbstractJsonSingleWriterProvider(Class<? extends T> clazz,
			MediaType mt) {
		super(clazz, mt);
	}

	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.isCompatible(getMediaType())
				&& getClassToEncode().isAssignableFrom(t);
	}

	@Override
	public void writeTo(T t, Class<?> c, Type gt, Annotation[] a, MediaType mt,
			MultivaluedMap<String, Object> h, OutputStream es)
			throws IOException, WebApplicationException {
		try {
			ReaderWriter
					.writeToAsString(Utils.stringifyJson(encode(t)), es, mt);
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

	protected abstract JSONObject encode(T t) throws JSONException;
}
