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

import javax.measure.unit.Unit;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.coverage.grid.GridCoverage;
import org.slf4j.Logger;
import org.uncertweb.utils.UwIOUtils;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.core.VissConfig;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.UncertaintyReference;
import org.uncertweb.viss.core.resource.time.ITemporalExtent;
import org.uncertweb.viss.core.util.MediaTypes;

import com.google.code.morphia.annotations.Embedded;

public class MongoUncertaintyCollectionDataSet extends
		AbstractMongoDataSet<UncertaintyReference> {

	@Embedded
	private UncertaintyReference ref;
	private UncertaintyType type;

	public MongoUncertaintyCollectionDataSet() {
	}

	public MongoUncertaintyCollectionDataSet(
			MongoUncertaintyCollectionResource resource,
			UncertaintyReference ref) {
		super(resource);
		setContent(this.ref = ref);
		getType();
	}

	@Override
	public String getPhenomenon() {
		return "UNKNOWN";
	}

	@Override
	public UncertaintyType getType() {
		if (type == null) { 
			type = getContent().getType();
		}
		return type;
	}

	@Override
	protected UncertaintyReference loadContent() {
		if (ref.getContent() == null) {
			log.debug("Ref-content is null");
			if (!ref.getMime().equals(MediaTypes.GEOTIFF_TYPE)) {
				throw VissError.internal("Currently only GeoTIFF is supported. (was "+ref.getMime()+")");
			}
			if (ref.getFile() == null) {
				log.debug("Ref-file is null. Downloading reference");
				try {
					ref.setFile(downloadReference());
				} catch (IOException e) {
					throw VissError.internal(e);
				}
			}

			try {
				log.debug("Reading coverage from {}", ref.getFile().getAbsolutePath());
				GridCoverage2D coverage = new GeoTiffReader(ref.getFile()).read(null);
				log.debug("GridCoverage: {}", coverage);
				ref.setContent(coverage);
			} catch (DataSourceException e) {
				throw VissError.internal(e);
			} catch (IOException e) {
				throw VissError.internal(e);
			}
		}
		return ref;
	}
	public static void main(String[] agrs) throws IOException {
		File f = new File("/tmp/viss/resources/4f45fe4a44ae15101b4f6d1e/RES-295738633449198922");
		for (int i = 0; i < 100; ++i) {
			GeoTiffReader r = new GeoTiffReader(f);
			GridCoverage2D gc = r.read(null);
			log.debug("GridCoverage: {}", gc);
		}
	}
	
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(MongoUncertaintyCollectionDataSet.class);
	
	private File downloadReference() throws IOException {
		MongoResourceStore store = (MongoResourceStore) 
				VissConfig.getInstance().getResourceStore();
		File f = store.createResourceFile(getResource().getId(), ref.getMime());
		log.debug("Saving {} to {}.", ref.getRef(), f.getAbsolutePath());
		UwIOUtils.saveToFile(f, ref.getRef().toURL());
		return f;
	}
	
	@Override
	protected ITemporalExtent loadTemporalExtent() {
		return ITemporalExtent.NO_TEMPORAL_EXTENT;
	}

	@Override
	public String getUom() {
		Object gc = getContent();
		if (gc instanceof GridCoverage) {
			Unit<?> u = ((GridCoverage) gc).getSampleDimension(0).getUnits();
			if (u != null)
				return u.toString();
		}
		return Unit.ONE.toString();
	}

}
