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
		this.supportedEncodings.addAll(supportedEncodings);
		this.supportedFormats.addAll(supportedFormats);
		this.supportedIDataTypes.addAll(supportedBindings);
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
