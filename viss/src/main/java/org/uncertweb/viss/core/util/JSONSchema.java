package org.uncertweb.viss.core.util;

public class JSONSchema {

	public static final String BASE_SCHEMA_URL = "http://json-schema.org/draft-03/schema#";

	public static class Key {
		public static final String DESCRIPTION = "description";
		public static final String TYPE = "type";
		public static final String REQUIRED = "required";
		public static final String MINIMUM = "minimum";
		public static final String MAXIMUM = "maximum";
		public static final String EXCLUSIVE_MINIMUM = "exclusiveMinimum";
		public static final String EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
		public static final String ADDITIONAL_PROPERTERTIES = "additionalProperties";
		public static final String PROPERTIES = "properties";
		public static final String FORMAT = "format";
		public static final String ITEMS = "items";
		public static final String REF = "$ref";
		public static final String DEFAULT = "default";
		public static final String ID = "id";
		public static final String UNIQUE_ITEMS = "uniqueItems";
		public static final String PATTERN_PROPERTIES = "patternProperties";
		public static final String ADDITIONAL_ITEMS = "additionalItems";
		public static final String DEPENDENCIES = "dependencies";
		public static final String MIN_ITEMS = "minItems";
		public static final String MAX_ITEMS = "maxItems";
		public static final String MIN_LENGTH = "minLength";
		public static final String MAX_LENGTH = "maxLength";
		public static final String TITLE = "title";
		public static final String DIVISIBLE_BY = "divisibleBy";
		public static final String DISALLOW = "disallow";
		public static final String PATTERN = "pattern";
		public static final String EXTENDS = "extends";
		public static final String SCHEMA = "$schema";
	}

	public static class Type {
		public static final String NUMBER = "number";
		public static final String OBJECT = "object";
		public static final String INTEGER = "integer";
		public static final String ENUM = "enum";
		public static final String ARRAY = "array";
		public static final String STRING = "string";
	}

	public static class Format {
		public static final String DATETIME = "date-time";
		public static final String DATE = "date";
		public static final String TIME = "time";
		public static final String UTC_MILLISEC = "utc-millisec";
		public static final String REGEX = "regex";
		public static final String URI = "uri";
		public static final String URL = "url";
		public static final String URN = "urn";

	}
}
