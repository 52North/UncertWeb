package org.uncertweb.viss.core.web.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Utils;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
public class ResourceCollectionProvider implements
		MessageBodyWriter<Iterable<Resource>> {

	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.equals(MediaType.APPLICATION_JSON_TYPE)
				&& (Resource.class.isAssignableFrom(t) || Utils
						.isParameterizedWith(gt, Iterable.class, Resource.class));
	}

	@Override
	public long getSize(Iterable<Resource> t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(Iterable<Resource> o, Class<?> t, Type gt,
			Annotation[] a, MediaType mt, MultivaluedMap<String, Object> h,
			OutputStream es) throws IOException, WebApplicationException {

		try {
			JSONArray aJ = new JSONArray();
			for (Resource r : o) {
				aJ.put(ResourceProvider.toJson(r));
			}
			JSONObject j = new JSONObject().put("resources", aJ);
			ReaderWriter.writeToAsString(Utils.stringifyJson(j), es, mt);
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

}
