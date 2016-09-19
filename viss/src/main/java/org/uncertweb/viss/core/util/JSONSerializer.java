/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24,
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.viss.core.util;

import java.util.Iterator;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONString;

/**
 * Heavily influenced by {@link JSONObject} and {@link JSONArray}, but this class does not escape every single forward
 * slash.
 *
 * @author Christian Autermann
 */
public class JSONSerializer {
    private JSONSerializer() {
    }

    public static String toString(JSONObject json) {
        try {
            Iterator<?> keys = json.keys();
            StringBuilder sb = new StringBuilder("{");

            while (keys.hasNext()) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                String o = (String) keys.next();
                sb.append(quote(o));
                sb.append(':');
                sb.append(valueToString(json.get(o)));
            }
            sb.append('}');
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String toString(JSONObject json, int indentFactor) throws JSONException {
        return toString(json, indentFactor, 0);
    }

    private static String toString(JSONObject json, int indentFactor, int indent) throws JSONException {
        int i;
        int n = json.length();
        if (n == 0) {
            return "{}";
        }
        Iterator<?> keys = json.keys();
        StringBuilder sb = new StringBuilder("{");
        int newindent = indent + indentFactor;
        String o;
        if (n == 1) {
            o = (String) keys.next();
            sb.append(quote(o));
            sb.append(": ");
            sb.append(valueToString(json.get(o), indentFactor, indent));
        } else {
            while (keys.hasNext()) {
                o = (String) keys.next();
                if (sb.length() > 1) {
                    sb.append(",\n");
                } else {
                    sb.append('\n');
                }
                for (i = 0; i < newindent; ++i) {
                    sb.append(' ');
                }
                sb.append(quote(o));
                sb.append(": ");
                sb.append(valueToString(json.get(o), indentFactor, newindent));
            }
            if (sb.length() > 1) {
                sb.append('\n');
                for (i = 0; i < indent; ++i) {
                    sb.append(' ');
                }
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private static String valueToString(Object value) throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        try {
            if (value instanceof JSONString) {
                String s = ((JSONString) value).toJSONString();
                return (s == null) ? "null" : s;
            }
        } catch (Exception e) {
            throw new JSONException(e);
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof JSONObject) {
            return toString((JSONObject) value);
        }
        if (value instanceof JSONArray) {
            return toString((JSONArray) value);
        }

        return quote(value.toString());
    }

    private static String valueToString(Object value, int indentFactor, int indent)
            throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        try {
            if (value instanceof JSONString) {
                String s = ((JSONString) value).toJSONString();
                return (s == null) ? "null" : s;
            }
        } catch (Exception e) {
            throw new JSONException(e);
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof JSONObject) {
            return toString((JSONObject) value, indentFactor, indent);
        }
        if (value instanceof JSONArray) {
            return toString((JSONArray) value, indentFactor, indent);
        }
        return quote(value.toString());
    }

    public static String toString(JSONArray json) throws JSONException {
        int len = json.length();
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < len; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(valueToString(json.get(i)));
        }
        sb.append(']');
        return sb.toString();
    }

    public static String toString(JSONArray json, int indentFactor) throws JSONException {
        return toString(json, indentFactor, 0);
    }

    private static String toString(JSONArray json, int indentFactor, int indent) throws JSONException {
        int len = json.length();
        if (len == 0) {
            return "[]";
        }
        int i;
        StringBuilder sb = new StringBuilder("[");
        if (len == 1) {
            sb.append(valueToString(json.get(0), indentFactor, indent));
        } else {
            int newindent = indent + indentFactor;
            sb.append('\n');
            for (i = 0; i < len; ++i) {
                if (i > 0) {
                    sb.append(",\n");
                }
                for (int j = 0; j < newindent; j += 1) {
                    sb.append(' ');
                }
                sb.append(valueToString(json.get(i), indentFactor, newindent));
            }
            sb.append('\n');
            for (i = 0; i < indent; ++i) {
                sb.append(' ');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private static String numberToString(Number n)
            throws JSONException {
        if (n == null) {
            throw new JSONException("Null pointer");
        }
        testValidity(n);

        String s = n.toString();
        if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    static void testValidity(Object o) throws JSONException {
        if (o != null) {
            if (o instanceof Double) {
                if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
                    throw new JSONException("JSON does not allow non-finite numbers");
                }
            } else if (o instanceof Float) {
                if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
                    throw new JSONException("JSON does not allow non-finite numbers.");
                }
            }
        }
    }

    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char c;
        int i;
        int len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; ++i) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
//                case '/':
//                if (b == '<') {
//                    sb.append('\\');
//                }
//                    sb.append(c);
//                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
