package org.uncertweb.viss.core.web.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.utils.UwReflectionUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Utils;

import com.sun.jersey.core.util.ReaderWriter;

public abstract class AbstractJsonCollectionWriterProvider<T> extends
		AbstractWriterProvider<Iterable<T>> {

	private final String collectionName;

	public AbstractJsonCollectionWriterProvider(Class<? extends T> clazz,
			MediaType mt, String collectionName) {
		super(clazz, mt);
		this.collectionName = collectionName;
	}

	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.isCompatible(getMediaType())
				&& UwReflectionUtils.isParameterizedWith(gt, Iterable.class,
						getClassToEncode());
	}

	@Override
	public void writeTo(Iterable<T> iterable, Class<?> t, Type gt,
			Annotation[] a, MediaType mt, MultivaluedMap<String, Object> h,
			OutputStream es) throws IOException, WebApplicationException {
		try {
			JSONArray array = new JSONArray();
			for (T o : iterable) {
				array.put(encode(o));
			}
			JSONObject j = new JSONObject().put(collectionName, array);
			ReaderWriter.writeToAsString(Utils.stringifyJson(j), es, mt);
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

	protected abstract JSONObject encode(T t) throws JSONException;
}
