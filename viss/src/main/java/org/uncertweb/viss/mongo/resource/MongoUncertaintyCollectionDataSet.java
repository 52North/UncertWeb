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
import java.net.URI;
import java.util.List;

import javax.measure.unit.Unit;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.Envelope2D;
import org.opengis.coverage.grid.GridCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.IUncertainty;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.netcdf.NcUwHelper;
import org.uncertweb.netcdf.NcUwObservation;
import org.uncertweb.netcdf.NcUwUncertaintyType;
import org.uncertweb.netcdf.NcUwUriParser;
import org.uncertweb.utils.MultivaluedMap;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.utils.UwIOUtils;
import org.uncertweb.viss.core.VissConfig;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.UncertaintyReference;
import org.uncertweb.viss.core.resource.time.AbstractTemporalExtent;
import org.uncertweb.viss.core.util.MediaTypes;

import com.google.code.morphia.annotations.Embedded;
import com.vividsolutions.jts.geom.Point;

public class MongoUncertaintyCollectionDataSet extends
		AbstractMongoDataSet<UncertaintyReference> {
	
	private static final Logger log = LoggerFactory.getLogger(MongoUncertaintyCollectionDataSet.class);


	@Embedded
	private UncertaintyReference ref;
	private NcUwUncertaintyType type;

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
	public NcUwUncertaintyType getType() {
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
	
	private File downloadReference() throws IOException {
		MongoResourceStore store = (MongoResourceStore) 
				VissConfig.getInstance().getResourceStore();
		File f = store.createResourceFile(getResource().getId(), ref.getMime());
		log.debug("Saving {} to {}.", ref.getRef(), f.getAbsolutePath());
		UwIOUtils.saveToFile(f, ref.getRef().toURL());
		return f;
	}
	
	@Override
	protected AbstractTemporalExtent loadTemporalExtent() {
		return AbstractTemporalExtent.NO_TEMPORAL_EXTENT;
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

	@Override
	public IObservationCollection getValue(Point p, TimeObject t) {
		GridCoverage gc = (GridCoverage) getContent().getContent();
		double[] v = gc.evaluate(NcUwHelper.toDirectPosition(p,
						gc.getCoordinateReferenceSystem()), (double[]) null);
		List<Number> n = UwCollectionUtils.list();
		for (double d : v) {
			n.add(new Double(d));
		}
		MultivaluedMap<URI, Object> uris = getContent().getAdditionalUris();
		uris.add(getContent().getRef(), n);
		IUncertainty u = NcUwUriParser.parse(getContent().getType(), uris);
		IObservationCollection col = new UncertaintyObservationCollection();
		col.addObservation(new NcUwObservation(null, null, null, p, null, u));
		return col;
	}
	
	@Override
	public Envelope2D getSpatialExtent() {
		if (getContent().getContent() instanceof GridCoverage) {
			return (Envelope2D) ((GridCoverage) getContent().getContent())
					.getEnvelope();
		}
		return null;
	}

}
