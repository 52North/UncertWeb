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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.bson.types.ObjectId;
import org.uncertweb.netcdf.NcUwFile;
import org.uncertweb.netcdf.INcUwVariable;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.util.MediaTypes;

import com.google.code.morphia.annotations.Polymorphic;

@Polymorphic
public class MongoNetCDFResource extends AbstractMongoResource<NcUwFile> {

	public MongoNetCDFResource(File f, ObjectId oid, long checksum) {
		super(MediaTypes.NETCDF_TYPE, f, oid, checksum);
	}

	public MongoNetCDFResource() {
		super(MediaTypes.NETCDF_TYPE);
	}

	@Override
	protected Set<IDataSet> createDataSets() {
		log.debug("Loading Datasets.");
		Set<IDataSet> dss = UwCollectionUtils.set();
		for (INcUwVariable v : getContent().getPrimaryVariables()) {
			log.debug("Creating dataset from Variable '{}'", v.getName());
			if (v.isUncertaintyVariable()) {
				dss.add(new MongoNetCDFDataSet(this, v));
			}
		}
		return dss;
	}

	@Override
	protected NcUwFile loadContent() {
		log.debug("Size: {}", getFile().length());
		String path = getFile().getAbsolutePath();
		try {
			return new NcUwFile(path);
		} catch (IOException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public void close() {
		try {
			if (getNullContent() != null)
				getNullContent().close();
		} catch (IOException e) {
		}
	}

}