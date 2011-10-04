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
package org.uncertweb.viss.mongo;

import org.joda.time.Interval;

import com.google.code.morphia.converters.SimpleValueConverter;
import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@SuppressWarnings("rawtypes")
public class IntervalConverter extends TypeConverter implements
    SimpleValueConverter {

	public IntervalConverter() {
		super(Interval.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null)
			return null;
		Interval i = (Interval) value;
		DBObject dbo = new BasicDBObject();
		dbo.put("start", i.getStart().getMillis());
		dbo.put("end", i.getEnd().getMillis());
		return dbo;
	}

	@Override
	public Interval decode(Class c, Object o, MappedField i)
	    throws MappingException {
		if (o == null) {
			return null;
		} else if (o instanceof DBObject) {
			DBObject dbo = (DBObject) o;
			return new Interval(getLong(dbo.get("start")), getLong(dbo.get("end")));
		}
		return new Interval(o);
	}

	private long getLong(Object o) {
		if (o instanceof Number) {
			return ((Number) o).longValue();
		} else if (o instanceof String) {
			return Long.valueOf((String) o);
		} else {
			return ((Long) o).longValue();
		}
	}
}