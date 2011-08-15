package org.uncertweb.viss.mongo;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.VissError;

import com.google.code.morphia.converters.SimpleValueConverter;
import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;
import com.mongodb.util.JSON;

@SuppressWarnings("rawtypes")
public class JSONConverter extends TypeConverter implements SimpleValueConverter {
	private static final Logger log = LoggerFactory.getLogger(JSONObject.class);

	public JSONConverter() {
		super(JSONObject.class);
		log.info("Creating JSONConverter");
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null)
			return null;
		String s = ((JSONObject) value).toString();
		log.debug("Encoding JSON: {}", s);
		return JSON.parse(s);
	}

	@Override
	public Object decode(Class c, Object o, MappedField i)
			throws MappingException {
		if (o == null)
			return null;
		try {
			String s = JSON.serialize(o);
			log.debug("Decoded JSON: {}", s);
			return new JSONObject(s);
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}
}