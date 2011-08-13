package org.uncertweb.viss.mongo;

import java.io.File;

import com.google.code.morphia.converters.SimpleValueConverter;
import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;

@SuppressWarnings("rawtypes")
public class FileConverter extends TypeConverter implements
		SimpleValueConverter {
	public FileConverter() {
		super(File.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null)
			return null;
		File f = (File) value;
		return f.getAbsolutePath();
	}

	@Override
	public Object decode(Class c, Object o, MappedField i)
			throws MappingException {
		if (o == null) {
			return null;
		} else if (o instanceof File) {
			return o;
		} else {
			return new File((String) o);
		}
	}

}
