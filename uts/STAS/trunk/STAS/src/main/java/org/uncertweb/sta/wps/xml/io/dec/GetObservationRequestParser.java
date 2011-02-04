package org.uncertweb.sta.wps.xml.io.dec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;

import net.opengis.sos.x10.GetObservationDocument;

import org.n52.wps.io.IStreamableParser;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.uncertweb.sta.wps.xml.binding.GetObservationRequestBinding;

/**
 * @author Christian Autermann
 */
public class GetObservationRequestParser extends AbstractXMLParser implements IStreamableParser {

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		return new Class<?>[] { GetObservationRequestBinding.class };
	}

	@Override
	public GetObservationRequestBinding parse(InputStream is, String mime) {
		if (!isSupportedFormat(mime)) {
			throw new RuntimeException("Not a compatible mime type: " + mime);
		}
		return parseXML(is);
	}

	@Override
	public GetObservationRequestBinding parseXML(String xml) {
		return parseXML(new ByteArrayInputStream(xml.getBytes()));
	}

	@Override
	public GetObservationRequestBinding parseXML(InputStream is) {
		try {
			return new GetObservationRequestBinding(GetObservationDocument.Factory.parse(is));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public GetObservationRequestBinding parseXML(URI uri) {
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

}
