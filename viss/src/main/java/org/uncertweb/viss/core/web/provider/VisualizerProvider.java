package org.uncertweb.viss.core.web.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.Visualizer;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
public class VisualizerProvider implements MessageBodyWriter<Visualizer> {

	@Override
	public boolean isWriteable(Class<?> type, Type gt, Annotation[] a,
			MediaType mt) {
		return mt.equals(MediaType.APPLICATION_JSON_TYPE)
				&& Visualizer.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(Visualizer o, Class<?> t, Type gt, Annotation[] a,
			MediaType mt, MultivaluedMap<String, Object> hh, OutputStream es)
			throws IOException {
		try {
			ReaderWriter
					.writeToAsString(Utils.stringifyJson(toJson(o)), es, mt);
		} catch (JSONException e) {
			VissError.internal(e);
		}
	}

	static JSONObject toJson(Visualizer v) throws IOException {
		try {
			JSONObject j = new JSONObject()
					.putOpt("description", v.getDescription())
					.put("id", v.getShortName())
					.put("options", v.getOptions());
			return j;
		} catch (JSONException e) {
			throw VissError.internal(e);
		}

	}

	@Override
	public long getSize(Visualizer v, Class<?> t, Type g, Annotation[] a,
			MediaType m) {
		return -1;
	}

}
