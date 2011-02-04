package org.uncertweb.sta.wps.xml.binding;

import java.io.IOException;
import java.io.StringWriter;

import net.opengis.sos.x10.GetObservationDocument;

import org.n52.wps.io.data.IComplexData;
import org.uncertweb.sta.wps.xml.io.dec.GetObservationRequestParser;
import org.uncertweb.sta.wps.xml.io.enc.GetObservationRequestGenerator;

/**
 * @author Christian Autermann
 */
public class GetObservationRequestBinding implements IComplexData {
	private static final long serialVersionUID = 2249930191625226883L;
	private transient GetObservationDocument getObs;

	public GetObservationRequestBinding(GetObservationDocument getObs) {
		this.getObs = getObs;
	}

	@Override
	public GetObservationDocument getPayload() {
		return getObs;
	}

	@Override
	public Class<?> getSupportedClass() {
		return GetObservationDocument.class;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos)
			throws IOException {
		StringWriter buffer = new StringWriter();
		GetObservationRequestGenerator g = new GetObservationRequestGenerator();
		g.write(this, buffer);
		oos.writeObject(buffer.toString());
	}

	private synchronized void readObject(java.io.ObjectInputStream oos)
			throws IOException, ClassNotFoundException {
		this.getObs = new GetObservationRequestParser().parseXML((String) oos.readObject())
				.getPayload();
	}

}
