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

import static org.uncertweb.viss.core.util.Constants.GEOTIFF_TYPE;
import static org.uncertweb.viss.core.util.Constants.NETCDF_TYPE;
import static org.uncertweb.viss.core.util.Constants.OM_2_TYPE;
import static org.uncertweb.viss.core.util.Constants.X_NETCDF_TYPE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.VissConfig;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.resource.ResourceStore;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.Visualization;
import org.uncertweb.viss.mongo.MongoDB;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.mapping.Mapper;

public class MongoResourceStore implements ResourceStore {
	
	private static final Logger log = LoggerFactory.getLogger(MongoResourceStore.class);

	@SuppressWarnings("rawtypes")
	private class ResourceDAO extends BasicDAO<AbstractMongoResource, UUID> {
		protected ResourceDAO() {
			super(MongoDB.getInstance().getDatastore());
		}
	}

	private ResourceDAO dao = new ResourceDAO();
	private File resourceDir;

	protected ResourceDAO getDao() {
		return this.dao;
	}

	@Override
	public Resource get(UUID uuid) {
		AbstractMongoResource<?> r = getDao().get(uuid);
		if (r == null) {
			throw VissError.noSuchResource();
		}
		return r;
	}

	@Override
	public void deleteResource(Resource resource) {
		if (resource == null) 
			throw new NullPointerException();
		// does not work... don't know why....
		// getDao().delete((AbstractMongoResource) resource);
		Datastore ds = getDao().getDatastore();
		ds.delete(ds.createQuery(AbstractMongoResource.class)
			.field(Mapper.ID_KEY).equal(resource.getUUID()));
		Utils.deleteRecursively(getResourceDir(resource.getUUID()));
	}

	@Override
	public AbstractMongoResource<?> addResource(InputStream is, MediaType mt) {
		UUID uuid = UUID.randomUUID();
		try {
			File f = createResourceFile(uuid, mt);
			log.debug("Size: {}",f.length());
			long crc = Utils.saveToFileWithChecksum(f, is);
			log.debug("Size: {}",f.length());
			AbstractMongoResource<?> r = getResourceWithChecksum(crc);
			if (r == null) {
				r = getResourceForMediaType(mt);
				r.setFile(f);
				r.setUUID(UUID.randomUUID());
				r.setChecksum(crc);
				saveResource(r);
			} else {
				log.info("Resource allready existent.");
			}
			return r;
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}
	
	protected AbstractMongoResource<?> getResourceWithChecksum(long crc) {
		return getDao().createQuery().field(AbstractMongoResource.CHECKSUM_PROPERTY).equal(crc).get();
	}

	@Override
	public Set<Resource> getAllResources() {
		return Utils.<Resource> asSet(getDao().find().asList());
	}

	@Override
	public Set<Resource> getResourcesUsedBefore(DateTime dt) {
		return Utils.<Resource> asSet(getDao().createQuery()
				.field(AbstractMongoResource.TIME_PROPERTY).lessThanOrEq(dt)
				.asList());
	}

	@Override
	public void deleteVisualizationForResource(Resource r, Visualization v) {
		AbstractMongoResource<?> amr = (AbstractMongoResource<?>) r;
		amr.removeVisualization(v);
		saveResource(r);
	}

	public File createResourceFile(UUID uuid, MediaType mt) throws IOException {
		File dir = getResourceDir(uuid);
		if (!dir.exists())
			dir.mkdirs();
		File f = File.createTempFile("RES-", "", dir);
		return f;
	}

	public File getResourceDir(UUID uuid) {
		if (resourceDir == null) {
			resourceDir = VissConfig.getInstance().getResourcePath();
			if (!resourceDir.exists())
				resourceDir.mkdirs();
		}
		return Utils.to(resourceDir, uuid.toString());
	}

	public AbstractMongoResource<?> getResourceForMediaType(MediaType mt)
			throws IOException {
		if (mt.equals(GEOTIFF_TYPE)) {
			return new GeoTIFFResource();
		} else if (mt.equals(NETCDF_TYPE) || mt.equals(X_NETCDF_TYPE)) {
			return new NetCDFResource();
		} else if (mt.equals(OM_2_TYPE)) {
			return new OMResource();
		}
		throw VissError.internal("Can not create resource for '" + mt + "'");
	}

	@Override
	public void saveResource(Resource r) {
		AbstractMongoResource<?> amr = (AbstractMongoResource<?>) r;
		log.debug("Saving Resource {};",amr.getUUID());
		getDao().save(amr);
	}
}