package org.n52.wps.io.datahandler;

import org.uncertweb.utils.UwCollectionUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.commons.codec.binary.Base64InputStream;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;

public abstract class DelegatingGenerator extends DelegatingHandler implements IGenerator {

	private final Collection<IGenerator> generators;

	public DelegatingGenerator(IGenerator... generators) {
		super(generators);
		List<Class<?>> bindings = UwCollectionUtils.asList(super.supportedBindings);
		bindings.add(UncertWebIODataBinding.class);
		super.supportedBindings = bindings.toArray(new Class<?>[bindings.size()]);
		this.generators = UwCollectionUtils.asSet(generators);
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
	
	public boolean isSupportedSchema(String schema){
		if (schema==null){
			return true;
		}
		else {
			return super.isSupportedSchema(schema);
		}
	}
}
