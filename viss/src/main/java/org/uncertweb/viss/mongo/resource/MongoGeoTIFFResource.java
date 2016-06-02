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
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.util.MediaTypes;

import com.github.jmkgreen.morphia.annotations.Polymorphic;

@Polymorphic
public class MongoGeoTIFFResource extends AbstractMongoResource<GridCoverage2D> {

	public MongoGeoTIFFResource() {
		super(MediaTypes.GEOTIFF_TYPE);
	}

	public MongoGeoTIFFResource(File f, ObjectId oid, long checksum) {
		super(MediaTypes.GEOTIFF_TYPE, f, oid, checksum);
	}

	@Override
	protected Set<IDataSet> createDataSets() {
		return null;
	}

	@Override
	protected GridCoverage2D loadContent() {
		try {
			return new GeoTiffReader(getFile()).read(null);
		} catch (DataSourceException e) {
			throw VissError.internal(e);
		} catch (IOException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
