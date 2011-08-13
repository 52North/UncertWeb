package org.uncertweb.viss.core.web.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Constants;

import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;

@Provider
public class SLDProvider extends
		AbstractMessageReaderWriterProvider<StyledLayerDescriptorDocument> {

	@Override
	public boolean isReadable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.equals(Constants.STYLED_LAYER_DESCRIPTOR_TYPE)
				&& StyledLayerDescriptorDocument.class.isAssignableFrom(t);
	}

	@Override
	public StyledLayerDescriptorDocument readFrom(
			Class<StyledLayerDescriptorDocument> t, Type gt, Annotation[] a,
			MediaType mt, MultivaluedMap<String, String> hh, InputStream es)
			throws IOException, WebApplicationException {
		try {
			return StyledLayerDescriptorDocument.Factory.parse(es, Constants.XML_OPTIONS);
		} catch (XmlException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.equals(Constants.STYLED_LAYER_DESCRIPTOR_TYPE)
				&& XmlObject.class.isAssignableFrom(t);
	}

	@Override
	public void writeTo(StyledLayerDescriptorDocument x, Class<?> t, Type gt,
			Annotation[] a, MediaType mt, MultivaluedMap<String, Object> hh,
			OutputStream es) throws IOException {
		x.save(es, Constants.XML_OPTIONS);
	}
	
}
