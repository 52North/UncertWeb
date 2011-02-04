package org.uncertweb.sta.wps.xml.io.dec;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.IStreamableParser;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.uncertweb.sta.wps.parameter.ParametersDocument;
import org.uncertweb.sta.wps.parameter.ParametersDocument.Parameters.Parameter;
import org.uncertweb.sta.wps.xml.binding.ParameterBinding;
import org.uncertweb.sta.wps.xml.binding.ParameterMap;

/**
 * @author Christian Autermann
 */
public class ParameterParser extends AbstractXMLParser implements
		IStreamableParser {

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		return new Class<?>[] { ParameterBinding.class };
	}

	@Override
	public ParameterBinding parse(InputStream is, String mime) {
		if (!isSupportedFormat(mime)) {
			throw new RuntimeException("Not a compatible mime type: " + mime);
		}
		return parseXML(is);
	}

	@Override
	public ParameterBinding parseXML(String xml) {
		try {
			return new ParameterBinding(parse(xml));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ParameterBinding parseXML(InputStream is) {
		try {
			return parseXML(IOUtils.toString(is));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ParameterBinding parseXML(URI uri) {
		try {
			URLConnection connection = uri.toURL().openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(false);
			InputStream stream = connection.getInputStream();
			return parseXML(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ParameterMap parse(String xml) throws XmlException {
		ParameterMap map = new ParameterMap();
		ParametersDocument doc = ParametersDocument.Factory.parse(xml);
		for (Parameter p : doc.getParameters().getParameterList()) {
			map.put(p.getKey(), p.getValue());
		}
		return map;
	}
}
