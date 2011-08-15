package org.uncertweb.viss.core.web.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
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
import org.uncertweb.viss.core.vis.Visualization;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ResourceProvider implements MessageBodyWriter<Resource> {

	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.equals(MediaType.APPLICATION_JSON_TYPE)
				&& Resource.class.isAssignableFrom(t);
	}

	public void writeTo(Resource o, Class<?> t, Type gt, Annotation[] a,
			MediaType mt, MultivaluedMap<String, Object> h, OutputStream es)
			throws IOException, WebApplicationException {
		try {
			ReaderWriter
					.writeToAsString(Utils.stringifyJson(toJson(o)), es, mt);
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

	static JSONObject toJson(Resource r) throws JSONException {
		JSONArray a = new JSONArray();
		for (Visualization v : r.getVisualizations()) {
			a.put(VisualizationProvider.toJson(v));
		}
		return new JSONObject()
			.put("id", r.getUUID())
			.put("mimeType", r.getMediaType())
			.put("visualizations", a);
	}

	@Override
	public long getSize(Resource r, Class<?> t, Type g, Annotation[] a,
			MediaType m) {
		return -1;
	}

}
