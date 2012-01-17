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
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.util.MediaTypes;
import org.uncertweb.viss.vis.netcdf.UncertaintyNetCDF;
import org.uncertweb.viss.vis.netcdf.UncertaintyVariable;

import ucar.nc2.NetcdfFile;

import com.google.code.morphia.annotations.Polymorphic;

@Polymorphic
public class MongoNetCDFResource extends
		AbstractMongoResource<UncertaintyNetCDF> {

	private static final boolean LOAD_TO_MEMORY = false;

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
		for (UncertaintyVariable v : getContent().getVariables()) {
			log.debug("Creating dataset from Variable '{}'", v.getName());
			dss.add(new MongoNetCDFDataSet(this, v));
		}
		return dss;
	}

	@Override
	protected UncertaintyNetCDF loadContent() {
		log.debug("Size: {}", getFile().length());
		String path = getFile().getAbsolutePath();
		NetcdfFile f;
		try {
			f = (LOAD_TO_MEMORY) ? NetcdfFile.openInMemory(path)
					: NetcdfFile.open(path);
		} catch (IOException e) {
			throw VissError.internal(e);
		}
		return new UncertaintyNetCDF(f);
	}

}