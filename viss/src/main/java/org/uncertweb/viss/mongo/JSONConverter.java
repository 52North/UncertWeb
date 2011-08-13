package org.uncertweb.viss.mongo;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.VissError;

import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;
import com.mongodb.util.JSON;

@SuppressWarnings("rawtypes")
public class JSONConverter extends TypeConverter {

	public JSONConverter() {
		super(JSONObject.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null)
			return null;
		return JSON.parse(((JSONObject) value).toString());
	}

	@Override
	public Object decode(Class c, Object o, MappedField i)
			throws MappingException {
		if (o == null)
			return null;
		try {
			return new JSONObject(JSON.serialize(o));
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}
}