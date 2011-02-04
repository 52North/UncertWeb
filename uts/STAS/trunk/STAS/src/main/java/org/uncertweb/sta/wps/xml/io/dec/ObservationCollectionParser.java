package org.uncertweb.sta.wps.xml.io.dec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;

import org.n52.wps.io.IStreamableParser;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.uncertweb.sta.wps.xml.binding.ObservationCollectionBinding;

/**
 * @author Christian Autermann
 */
public class ObservationCollectionParser extends AbstractXMLParser implements IStreamableParser {

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		return new Class<?>[] { ObservationCollectionBinding.class };
	}

	@Override
	public ObservationCollectionBinding parse(InputStream is, String mime) {
		if (!isSupportedFormat(mime)) {
			throw new RuntimeException("Not a compatible mime type: " + mime);
		}
		return parseXML(is);
	}

	@Override
	public ObservationCollectionBinding parseXML(String xml) {
		return parseXML(new ByteArrayInputStream(xml.getBytes()));
	}

	@Override
	public ObservationCollectionBinding parseXML(InputStream is) {
		try {
			return new ObservationCollectionBinding(new org.uncertweb.intamap.om.io.ObservationCollectionParser().parse(is));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ObservationCollectionBinding parseXML(URI uri) {
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
