package org.uncertweb.sta.wps.xml.binding;

import java.io.IOException;
import java.io.StringWriter;

import org.n52.wps.io.data.IComplexData;
import org.uncertweb.sta.wps.xml.io.dec.ParameterParser;
import org.uncertweb.sta.wps.xml.io.enc.ParameterGenerator;

/**
 * @author Christian Autermann
 */
public class ParameterBinding implements IComplexData {
	private static final long serialVersionUID = 2249930191625226883L;
	private transient ParameterMap parameter;

	public ParameterBinding(ParameterMap parameter) {
		this.parameter = parameter;
	}

	@Override
	public ParameterMap getPayload() {
		return this.parameter;
	}

	@Override
	public Class<?> getSupportedClass() {
		return ParameterMap.class;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos)
			throws IOException {
		StringWriter buffer = new StringWriter();
		ParameterGenerator g = new ParameterGenerator();
		g.write(this, buffer);
		oos.writeObject(buffer.toString());
	}

	private synchronized void readObject(java.io.ObjectInputStream oos)
			throws IOException, ClassNotFoundException {
		this.parameter = new ParameterParser().parseXML((String) oos.readObject())
				.getPayload();
	}

}
