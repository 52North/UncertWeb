package org.uncertweb.sta.wps.xml.binding;

import java.io.IOException;
import java.io.StringWriter;

import net.opengis.wfs.GetFeatureDocument;

import org.n52.wps.io.data.IComplexData;
import org.uncertweb.sta.wps.xml.io.dec.GetFeatureRequestParser;
import org.uncertweb.sta.wps.xml.io.enc.GetFeatureRequestGenerator;

/**
 * @author Christian Autermann
 */
public class GetFeatureRequestBinding implements IComplexData {
	private static final long serialVersionUID = 2249930191625226883L;
	private transient GetFeatureDocument doc;

	public GetFeatureRequestBinding(GetFeatureDocument doc) {
		this.doc = doc;
	}

	@Override
	public GetFeatureDocument getPayload() {
		return this.doc;
	}

	@Override
	public Class<?> getSupportedClass() {
		return String.class;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos)
			throws IOException {
		StringWriter buffer = new StringWriter();
		GetFeatureRequestGenerator g = new GetFeatureRequestGenerator();
		g.write(this, buffer);
		oos.writeObject(buffer.toString());
	}

	private synchronized void readObject(java.io.ObjectInputStream oos)
			throws IOException, ClassNotFoundException {
		this.doc = new GetFeatureRequestParser().parseXML((String) oos.readObject())
				.getPayload();
	}

}
