package org.uncertweb.viss.core.web.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.Visualizer;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
public class VisualizerCollectionProvider implements
		MessageBodyWriter<Iterable<Visualizer>> {

	@Override
	public boolean isWriteable(Class<?> type, Type gt, Annotation[] a,
			MediaType mt) {
		return mt.equals(MediaType.APPLICATION_JSON_TYPE)
				&& Iterable.class.isAssignableFrom(type)
				&& Utils.isParameterizedWith(gt, Iterable.class,
						Visualizer.class);
	}

	@Override
	public void writeTo(Iterable<Visualizer> o, Class<?> t, Type gt,
			Annotation[] a, MediaType mt, MultivaluedMap<String, Object> hh,
			OutputStream es) throws IOException {
		try {
			JSONArray j = new JSONArray();
			for (Visualizer v : o)
				j.put(VisualizerProvider.toJson(v));

			ReaderWriter
					.writeToAsString(Utils.stringifyJson(new JSONObject().put(
							"visualizers", j)), es, mt);
		} catch (JSONException e) {
			VissError.internal(e);
		}
	}

	@Override
	public long getSize(Iterable<Visualizer> t, Class<?> type,
			Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

}
