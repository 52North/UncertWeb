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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.uncertweb.viss.core.VissError;

import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;

@Provider
public class XmlBeansProvider extends
		AbstractMessageReaderWriterProvider<XmlObject> {

	@Override
	public boolean isReadable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.equals(MediaType.APPLICATION_XML_TYPE)
				&& XmlObject.class.isAssignableFrom(t);
	}

	@Override
	public XmlObject readFrom(Class<XmlObject> t, Type gt, Annotation[] a,
			MediaType mt, MultivaluedMap<String, String> hh, InputStream es)
			throws IOException, WebApplicationException {
		try {
			return XmlObject.Factory.parse(es);
		} catch (XmlException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.equals(MediaType.APPLICATION_XML_TYPE)
				&& XmlObject.class.isAssignableFrom(t);
	}

	@Override
	public void writeTo(XmlObject x, Class<?> t, Type gt, Annotation[] a,
			MediaType mt, MultivaluedMap<String, Object> hh, OutputStream es)
			throws IOException {
		x.save(es, new XmlOptions().setSavePrettyPrintIndent(4));

	}

}
