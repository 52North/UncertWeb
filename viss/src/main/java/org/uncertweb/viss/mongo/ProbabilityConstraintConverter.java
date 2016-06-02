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

import org.uncertml.statistic.ConstraintType;
import org.uncertml.statistic.ProbabilityConstraint;

import com.github.jmkgreen.morphia.converters.SimpleValueConverter;
import com.github.jmkgreen.morphia.converters.TypeConverter;
import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@SuppressWarnings("rawtypes")
public class ProbabilityConstraintConverter extends TypeConverter implements
		SimpleValueConverter {
	public ProbabilityConstraintConverter() {
		super(ProbabilityConstraint.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null)
			return null;
		ProbabilityConstraint pc = (ProbabilityConstraint) value;
		DBObject dbo = new BasicDBObject();
		dbo.put("type", pc.getType().toString());
		dbo.put("value", pc.getValue());
		return dbo;
	}

	@Override
	public Object decode(Class c, Object o, MappedField i)
			throws MappingException {
		if (o == null)
			return null;
		DBObject dbo = (DBObject) o;
		return new ProbabilityConstraint(ConstraintType.valueOf((String) dbo
				.get("type")), ((Double) dbo.get("value")).doubleValue());
	}
}