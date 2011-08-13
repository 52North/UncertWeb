package org.uncertweb.viss.mongo;

import javax.ws.rs.core.MediaType;

import com.google.code.morphia.converters.SimpleValueConverter;
import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;

@SuppressWarnings("rawtypes")
public class MediaTypeConverter extends TypeConverter implements
		SimpleValueConverter {
	public MediaTypeConverter() {
		super(MediaType.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null)
			return null;
		return ((MediaType) value).toString();
	}

	@Override
	public Object decode(Class c, Object o, MappedField i)
			throws MappingException {
		if (o == null)
			return null;
		return MediaType.valueOf(o.toString());
	}
}