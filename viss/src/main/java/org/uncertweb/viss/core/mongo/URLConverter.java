package org.uncertweb.viss.core.mongo;

import java.net.MalformedURLException;
import java.net.URL;

import org.uncertweb.viss.core.VissError;

import com.google.code.morphia.converters.SimpleValueConverter;
import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;

@SuppressWarnings("rawtypes")
public class URLConverter extends TypeConverter implements SimpleValueConverter {
	public URLConverter() {
		super(URL.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null)
			return null;
		return ((URL) value).toString();
	}

	@Override
	public Object decode(Class c, Object o, MappedField i)
			throws MappingException {
		if (o == null)
			return null;
		try {
			return new URL((String) o);
		} catch (MalformedURLException e) {
			throw VissError.internal(e);
		}
	}

}
