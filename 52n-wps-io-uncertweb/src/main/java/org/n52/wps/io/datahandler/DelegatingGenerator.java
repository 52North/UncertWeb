package org.n52.wps.io.datahandler;

import static org.uncertweb.utils.UwCollectionUtils.asSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.codec.binary.Base64InputStream;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.data.IData;

public abstract class DelegatingGenerator extends DelegatingHandler implements IGenerator {

	private final Collection<IGenerator> generators;

	public DelegatingGenerator(IGenerator... generators) {
		super(generators);
		this.generators = asSet(generators);
	}

	@Override
	public InputStream generateBase64Stream(IData data, String mimeType, String schema) throws IOException {
		return new Base64InputStream(generateStream(data, mimeType, schema), true);
	}
	
	@Override
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
		return findGenerator(data.getClass(), mimeType, schema).generateStream(data, mimeType, schema);
	}

	private IGenerator findGenerator(Class<? extends IData> data, String mimeType, String schema) {
		for (IGenerator g : generators) {
			if (g.isSupportedDataBinding(data)
			 && g.isSupportedFormat(mimeType)
			 && g.isSupportedSchema(schema)) {
				return g;
			}
		}
		throw new RuntimeException("No applicable generator found.");
	}
}
