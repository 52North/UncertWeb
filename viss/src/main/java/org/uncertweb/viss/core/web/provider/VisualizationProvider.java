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
import org.uncertweb.viss.core.visualizer.Visualization;
import org.uncertweb.viss.core.visualizer.VisualizationReference;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
public class VisualizationProvider implements MessageBodyWriter<Visualization> {

	@Override
	public boolean isWriteable(Class<?> type, Type gt, Annotation[] a,
			MediaType mt) {
		return mt.equals(MediaType.APPLICATION_JSON_TYPE)
				&& Visualization.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(Visualization o, Class<?> t, Type gt, Annotation[] a,
			MediaType mt) {
		return -1;
	}

	@Override
	public void writeTo(Visualization o, Class<?> t, Type gt, Annotation[] a,
			MediaType mt, MultivaluedMap<String, Object> hh, OutputStream es)
			throws IOException {
		try {
			ReaderWriter
					.writeToAsString(Utils.stringifyJson(toJson(o)), es, mt);
		} catch (JSONException e) {
			VissError.internal(e);
		}
	}

	static JSONObject toJson(Visualization v) {
		try {
			JSONObject j = new JSONObject().put("id", v.getVisId())
					.put("visualizer", v.getCreator().getShortName())
					.put("params", v.getParameters())
					.put("coverages", v.getCoverages().size())
					.put("customSLD", v.getSld() != null);

			VisualizationReference vr = v.getReference();

			if (vr != null) {
				JSONArray a = new JSONArray();
				for (String l : vr.getLayers())
					a.put(l);
				j.put("reference",
						new JSONObject().putOpt("url", vr.getWcsUrl()).put(
								"layers", a));
			}
			return j;
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

}
