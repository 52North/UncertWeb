package org.n52.wps.io.datahandler;

import java.io.InputStream;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.parser.AbstractParser;

public abstract class AbstractUwParser extends AbstractParser {

	public AbstractUwParser(Set<String> supportedSchemas,
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
	public IData parse(InputStream is, String mime, String schema) {
		try {
			return parse(is, mime);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract IData parse(InputStream is, String mimeType)
			throws Exception;

}
