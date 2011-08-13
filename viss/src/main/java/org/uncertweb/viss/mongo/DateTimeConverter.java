package org.uncertweb.viss.mongo;

import java.util.Date;

import org.joda.time.DateTime;

import com.google.code.morphia.converters.SimpleValueConverter;
import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;

@SuppressWarnings("rawtypes")
public class DateTimeConverter extends TypeConverter implements
		SimpleValueConverter {
	public DateTimeConverter() {
		super(DateTime.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null)
			return null;
		DateTime dt = (DateTime) value;
		return dt.getMillis();
	}

	@Override
	public Object decode(Class c, Object o, MappedField i)
			throws MappingException {
		if (o == null) {
			return null;
		} else if (o instanceof DateTime) {
			return o;
		} else if (o instanceof Number) {
			return new DateTime(((Number) o).longValue());
		} else if (o instanceof Date) {
			return new DateTime(((Date) o).getTime());
		} else {
			return new DateTime(o);
		}
	}
}