package org.uncertweb.sta.wps.xml.binding;

import java.io.IOException;
import java.io.StringWriter;

import org.n52.wps.io.data.IComplexData;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.sta.wps.xml.io.dec.ObservationCollectionParser;
import org.uncertweb.sta.wps.xml.io.enc.ObservationCollectionGenerator;

/**
 * @author Christian Autermann
 */
public class ObservationCollectionBinding implements IComplexData {
	private static final long serialVersionUID = 2249930191625226883L;
	private transient ObservationCollection obsColl;
	
	
	public ObservationCollectionBinding(ObservationCollection obsColl) {
		this.obsColl = obsColl;
	}
	
	@Override
	public ObservationCollection getPayload() {
		return obsColl;
	}

	@Override
	public Class<?> getSupportedClass() {
		return ObservationCollection.class;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException {
		StringWriter buffer = new StringWriter();
		ObservationCollectionGenerator omg = new ObservationCollectionGenerator();
		omg.write(this, buffer);
		oos.writeObject(buffer.toString());
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException {
		ObservationCollectionParser omp = new ObservationCollectionParser();
		this.obsColl = omp.parseXML((String) oos.readObject()).getPayload();
	}

}
