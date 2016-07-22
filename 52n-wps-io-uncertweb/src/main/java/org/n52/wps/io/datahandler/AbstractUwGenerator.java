package org.n52.wps.io.datahandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

public abstract class AbstractUwGenerator extends AbstractGenerator {

	public AbstractUwGenerator(Set<String> supportedSchemas,
			Set<String> supportedEncodings, Set<String> supportedFormats,
			Set<Class<?>> supportedBindings) {
		super();
		if (supportedEncodings != null)
			this.supportedEncodings.addAll(supportedEncodings);
		if (supportedFormats != null)
			this.supportedFormats.addAll(supportedFormats);
		if (supportedBindings != null)
			this.supportedIDataTypes.addAll(supportedBindings);
		if (supportedSchemas != null)
			this.supportedSchemas.addAll(supportedSchemas);
	}

	@Override
	public InputStream generateStream(IData data, String mime, String encoding)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writeToStream(data, out);
		return new ByteArrayInputStream(out.toByteArray());
	}

	protected abstract void writeToStream(IData data, OutputStream out);
}
