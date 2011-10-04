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

import java.io.IOException;

import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.time.ITemporalExtent;
import org.uncertweb.viss.core.util.MediaTypes;
import org.uncertweb.viss.vis.UncertaintyNetCDF;

import ucar.nc2.NetcdfFile;

import com.google.code.morphia.annotations.Polymorphic;

@Polymorphic
public class NetCDFResource extends AbstractMongoResource<UncertaintyNetCDF> {

	public NetCDFResource() {
		super(MediaTypes.NETCDF_TYPE);
	}

	private static final boolean LOAD_TO_MEMORY = false;

	@Override
	public void load() throws IOException, VissError {
		log.debug("Size: {}", getFile().length());
		String path = getFile().getAbsolutePath();
		NetcdfFile f = (LOAD_TO_MEMORY) ? NetcdfFile.openInMemory(path)
		    : NetcdfFile.open(path);
		setContent(new UncertaintyNetCDF(f));
	}

	@Override
	protected String getPhenomenonForResource() {
		return getContent().getPrimaryVariable().getName();
	}

	@Override
	protected ITemporalExtent getTemporalExtentForResource() {
		// TODO how is time encoded in NetCDF?
		return ITemporalExtent.NO_TEMPORAL_EXTENT;
	}

	@Override
	public UncertaintyType getType() {
		return UncertaintyType.fromURI(getContent().getPrimaryURI());
	}

}