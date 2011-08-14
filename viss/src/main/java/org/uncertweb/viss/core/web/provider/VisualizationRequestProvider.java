package org.uncertweb.viss.core.web.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.web.VisualizationRequest;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
public class VisualizationRequestProvider implements
		MessageBodyReader<VisualizationRequest> {

	@Override
	public boolean isReadable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.equals(MediaType.APPLICATION_JSON_TYPE)
				&& VisualizationRequest.class.isAssignableFrom(t);
	}

	@Override
	public VisualizationRequest readFrom(Class<VisualizationRequest> t,
			Type gt, Annotation[] a, MediaType mt,
			MultivaluedMap<String, String> hh, InputStream es)
			throws IOException, WebApplicationException {
		try {

			JSONObject j = new JSONObject(ReaderWriter.readFromAsString(es, mt));
			JSONObject param = j.optJSONObject("params");
			if (param != null && !param.keys().hasNext()) {
				param = null;
			}
			return new VisualizationRequest(param, j.getString("visualizer"));
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

}
