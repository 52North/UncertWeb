package org.uncertweb.viss.core.mongo;

import java.util.UUID;

import com.google.code.morphia.converters.SimpleValueConverter;
import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;

@SuppressWarnings("rawtypes")
public class UUIDConverter extends TypeConverter implements
		SimpleValueConverter {
	public UUIDConverter() {
		super(UUID.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null)
			return null;
		return value.toString();
	}

	@Override
	public Object decode(Class c, Object o, MappedField i)
			throws MappingException {
		if (o == null)
			return null;
		return UUID.fromString(o.toString());
	}
}