package org.n52.wps.io.datahandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.utils.UwCollectionUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.n52.wps.io.IParser;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;

public abstract class DelegatingParser extends DelegatingHandler implements IParser {
	
	private static final Logger log = LoggerFactory.getLogger(DelegatingParser.class);

	private final Set<IParser> parsers;
	
	public DelegatingParser(IParser... parsers) {
		super(parsers);
		
		//add generic UncertWebIODataBinding to supported Formats
		List<Class<?>> bindings = UwCollectionUtils.asList(super.supportedBindings);
		bindings.add(UncertWebIODataBinding.class);
		super.supportedBindings = bindings.toArray(new Class<?>[bindings.size()]);
		this.parsers = UwCollectionUtils.asSet(parsers);
	}

	@Override
	public IData parse(InputStream input, String mimeType, String schema) {
		return findParser(mimeType, schema).parse(input, mimeType, schema);
	}

	@Override
	public IData parseBase64(InputStream input, String mimeType, String schema) {
		return findParser(mimeType, schema).parseBase64(input, mimeType, schema);
	}
	
	private IParser findParser(String mimeType, String schema) {
		log.info("Searching for Parser. Mime-Type: {}, Schema: {}", mimeType, schema);
		for (IParser g : parsers) {
			log.info("Asking Parser: {}", g.getClass().getName());
			if (g.isSupportedFormat(mimeType)
			 && g.isSupportedSchema(schema)) {
				return g;
			}
		}
		throw new RuntimeException("No applicable parser found.");
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
