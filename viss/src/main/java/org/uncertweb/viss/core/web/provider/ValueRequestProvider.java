package org.uncertweb.viss.core.web.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.api.gml.io.JSONGeometryDecoder;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.io.JSONObservationParser;
import org.uncertweb.viss.core.ValueRequest;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.MediaTypes;

import com.vividsolutions.jts.geom.Geometry;

@Provider
@Consumes(MediaTypes.JSON_VALUE_REQUEST)
public class ValueRequestProvider implements MessageBodyReader<ValueRequest> {

	@Override
	public boolean isReadable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return t.isAssignableFrom(ValueRequest.class)
				&& mt.isCompatible(MediaTypes.JSON_VALUE_REQUEST_TYPE);
	}

	@Override
	public ValueRequest readFrom(Class<ValueRequest> c, Type gt,
			Annotation[] a, MediaType mt, MultivaluedMap<String, String> h,
			InputStream in) throws IOException, WebApplicationException {

		try {
			JSONObject j = new JSONObject(IOUtils.toString(in));
			JSONObject jt = j.optJSONObject("time");
			JSONObject jg = j.optJSONObject("location");
			TimeObject t = null;
			Geometry g = null;
			if (jt != null) {
				t = new JSONObservationParser()
						.parseTime(new org.json.JSONObject(jt.toString()));
			}
			if (jg != null) {
				g = new JSONGeometryDecoder().parseUwGeometry(jg.toString());
			}
			return new ValueRequest(g, t);
		} catch (Exception e) {
			throw VissError.badRequest(e);
		}
	}

}
