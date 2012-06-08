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
package org.uncertweb.viss.mongo.resource;

import org.uncertweb.netcdf.INcUwVariable;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.core.resource.time.ITemporalExtent;

import com.google.code.morphia.annotations.Polymorphic;
import com.google.code.morphia.annotations.PrePersist;
import com.google.code.morphia.annotations.Property;

@Polymorphic
public class MongoNetCDFDataSet extends AbstractMongoDataSet<INcUwVariable> {

	@Property
	private String variableName;

	public MongoNetCDFDataSet() {
	}

	public MongoNetCDFDataSet(MongoNetCDFResource resource, INcUwVariable v) {
		super(resource);
		setContent(v);
	}

	@Override
	public String getPhenomenon() {
		return getContent().getName();
	}

	@Override
	public UncertaintyType getType() {
		return getContent().getType();
	}

	@Override
	protected ITemporalExtent loadTemporalExtent() {
		return getExtent(getContent().getTimes());
	}

	@PrePersist
	public void saveVariableAsString() {
		this.variableName = getContent().getName();
	}

	@Override
	protected INcUwVariable loadContent() {
		return ((MongoNetCDFResource) getResource()).getContent()
				.getPrimaryVariable(this.variableName);
	}

	@Override
	public String getUom() {
		return getContent().getUnit();
	}

}
