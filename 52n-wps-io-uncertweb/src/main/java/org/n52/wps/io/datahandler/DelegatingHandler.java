package org.n52.wps.io.datahandler;

import org.uncertweb.utils.UwCollectionUtils;
import java.util.Set;

import org.n52.wps.io.IOHandler;

public abstract class DelegatingHandler implements IOHandler {

	private final Class<?>[] supportedBindings;
	private final String[] supportedFormats;
	private final String[] supportedSchemas;
	private final String[] supportedEncodings;

	public DelegatingHandler(IOHandler... handler) {
		
		Set<String> formats = UwCollectionUtils.set();
		Set<String> schemas = UwCollectionUtils.set();
		Set<String> encodings = UwCollectionUtils.set();
		Set<Class<?>> bindings = UwCollectionUtils.set();
		
		for (IOHandler g : handler) {
			UwCollectionUtils.addAll(formats, g.getSupportedFormats());
			UwCollectionUtils.addAll(schemas, g.getSupportedSchemas());
			UwCollectionUtils.addAll(encodings, g.getSupportedEncodings());
			UwCollectionUtils.addAll(bindings, g.getSupportedDataBindings());
		}
		
		this.supportedFormats = formats.toArray(new String[formats.size()]);
		this.supportedSchemas = schemas.toArray(new String[schemas.size()]);
		this.supportedEncodings = encodings.toArray(new String[encodings.size()]);
		this.supportedBindings = bindings.toArray(new Class<?>[bindings.size()]);
	}

	@Override
	public Class<?>[] getSupportedDataBindings() {
		return supportedBindings;
	}

	@Override
	public String[] getSupportedEncodings() {
		return supportedEncodings;
	}

	@Override
	public String[] getSupportedFormats() {
		return supportedFormats;
	}

	@Override
	public String[] getSupportedSchemas() {
		return supportedSchemas;
	}

	@Override
	public boolean isSupportedDataBinding(Class<?> arg0) {
		for (Class<?> c : getSupportedDataBindings())
			if (c.isAssignableFrom(arg0))
				return true;
		return false;
	}

	@Override
	public boolean isSupportedEncoding(String arg0) {
		for (String s : getSupportedEncodings())
			if (s.equalsIgnoreCase(arg0))
				return true;
		return false;
	}

	@Override
	public boolean isSupportedFormat(String arg0) {
		for (String s : getSupportedFormats())
			if (s.equals(arg0))
				return true;
		return false;
	}

	@Override
	public boolean isSupportedSchema(String arg0) {
		for (String s : getSupportedSchemas())
			if (s.equalsIgnoreCase(arg0))
				return true;
		return false;
	}
}
