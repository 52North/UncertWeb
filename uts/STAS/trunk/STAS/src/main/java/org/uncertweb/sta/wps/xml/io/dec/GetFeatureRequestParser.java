package org.uncertweb.sta.wps.xml.io.dec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;

import net.opengis.wfs.GetFeatureDocument;

import org.n52.wps.io.IStreamableParser;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.uncertweb.sta.wps.xml.binding.GetFeatureRequestBinding;

/**
 * @author Christian Autermann
 */
public class GetFeatureRequestParser extends AbstractXMLParser implements IStreamableParser {

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		return new Class<?>[] { GetFeatureRequestBinding.class };
	}

	@Override
	public GetFeatureRequestBinding parse(InputStream is, String mime) {
		if (!isSupportedFormat(mime)) {
			throw new RuntimeException("Not a compatible mime type: " + mime);
		}
		return parseXML(is);
	}

	@Override
	public GetFeatureRequestBinding parseXML(String xml) {
		return parseXML(new ByteArrayInputStream(xml.getBytes()));
	}

	@Override
	public GetFeatureRequestBinding parseXML(InputStream is) {
		try {
			return new GetFeatureRequestBinding(GetFeatureDocument.Factory.parse(is));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public GetFeatureRequestBinding parseXML(URI uri) {
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
