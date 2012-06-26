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

import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.MediaTypes;

@Provider
@Produces(MediaTypes.JSON_OBSERVATION_COLLECTION)
public class UncertaintyObservationCollectionProvider implements
		MessageBodyWriter<IObservationCollection> {

	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return IObservationCollection.class.isAssignableFrom(t)
				&& mt.isCompatible(MediaTypes.JSON_OBSERVATION_COLLECTION_TYPE);
	}

	@Override
	public long getSize(IObservationCollection t, Class<?> ty, Type gt,
			Annotation[] a, MediaType mt) {
		return -1;
	}

	@Override
	public void writeTo(IObservationCollection t, Class<?> ty, Type gt,
			Annotation[] a, MediaType mt, MultivaluedMap<String, Object> h,
			OutputStream out) throws IOException, WebApplicationException {
		try {
			new JSONObservationEncoder().encodeObservationCollection(t, out);
		} catch (OMEncodingException e) {
			throw VissError.internal(e);
		}
	}

}
