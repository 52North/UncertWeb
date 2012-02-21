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

import static org.uncertweb.viss.core.util.MediaTypes.GEOTIFF_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_UNCERTAINTY_COLLECTION_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.NETCDF_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.OM_2_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.X_NETCDF_TYPE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.utils.UwIOUtils;
import org.uncertweb.viss.core.VissConfig;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.resource.IResourceStore;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.mongo.MongoDB;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.mapping.Mapper;

public class MongoResourceStore implements IResourceStore {

	private static final Logger log = LoggerFactory
			.getLogger(MongoResourceStore.class);

	@SuppressWarnings("rawtypes")
	private class ResourceDAO extends BasicDAO<AbstractMongoResource, ObjectId> {
		protected ResourceDAO() {
			super(MongoDB.getInstance().getDatastore());
		}
	}

	@SuppressWarnings("rawtypes")
	private class DatasetDao extends BasicDAO<AbstractMongoDataSet, ObjectId> {
		protected DatasetDao() {
			super(MongoDB.getInstance().getDatastore());
		}
	}
	
	private ResourceDAO rDao = new ResourceDAO();
	private DatasetDao dsDao = new DatasetDao();
	private File resourceDir;

	protected ResourceDAO getResourceDao() {
		return this.rDao;
	}
	
	protected DatasetDao getDatasetDao() {
		return this.dsDao;
	}

	@Override
	public IResource get(ObjectId oid) {
		AbstractMongoResource<?> r = getResourceDao().get(oid);
		if (r == null) {
			throw VissError.noSuchResource();
		}
		return r;
	}

	@Override
	public void deleteResource(IResource resource) {
		if (resource == null)
			throw new NullPointerException();
		// does not work... don't know why....
		// getDao().delete((AbstractMongoResource) resource);
		Datastore ds = getResourceDao().getDatastore();
		ObjectId id = resource.getId();
		
		UwIOUtils.deleteRecursively(getResourceDir(id));
		ds.delete(ds.createQuery(AbstractMongoDataSet.class)
				.field("resource").equal(resource));
		ds.delete(ds.createQuery(AbstractMongoResource.class)
				.field(Mapper.ID_KEY).equal(id));
	}

	@Override
	public AbstractMongoResource<?> addResource(InputStream is, MediaType mt) {
		ObjectId oid = new ObjectId();
		try {
			File f = createResourceFile(oid, mt);
			log.debug("Size: {}", f.length());
			long crc = Utils.saveToFileWithChecksum(f, is);
			log.debug("Size: {}", f.length());
			AbstractMongoResource<?> r = getResourceWithChecksum(crc);
			if (r == null) {
				r = getResourceForMediaType(mt, f, oid, crc);
				saveResource(r);
			} else {
				UwIOUtils.deleteRecursively(getResourceDir(oid));
				log.info("Resource allready existent.");
			}
			if (r.getDataSets().isEmpty()) {
				throw VissError.internal("No Datasets.");
			}
			return r;
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}
	
	public AbstractMongoResource<?> getResourceForMediaType(MediaType mt, File f, ObjectId oid, long checksum)
			throws IOException {
		if (mt.equals(GEOTIFF_TYPE)) {
			return new MongoGeoTIFFResource(f, oid, checksum);
		} else if (mt.equals(NETCDF_TYPE) || mt.equals(X_NETCDF_TYPE)) {
			return new MongoNetCDFResource(f, oid, checksum);
		} else if (mt.equals(OM_2_TYPE)) {
			return new MongoOMResource(f, oid, checksum);
		} else if (mt.equals(JSON_UNCERTAINTY_COLLECTION_TYPE)) {
			return new MongoUncertaintyCollectionResource(f, oid, checksum);
		}
		throw VissError.internal("Can not create resource for '" + mt + "'");
	}


	protected AbstractMongoResource<?> getResourceWithChecksum(long crc) {
		return getResourceDao().createQuery()
				.field(AbstractMongoResource.CHECKSUM_PROPERTY).equal(crc)
				.get();
	}

	@Override
	public Set<IResource> getAllResources() {
		return UwCollectionUtils.<IResource> asSet(getResourceDao().find().asList());
	}

	@Override
	public Set<IResource> getResourcesUsedBefore(DateTime dt) {
		return UwCollectionUtils.<IResource> asSet(getResourceDao().createQuery()
				.field(AbstractMongoResource.TIME_PROPERTY).lessThanOrEq(dt)
				.asList());
	}

	@Override
	public void deleteVisualizationForResource(IVisualization v) {
		AbstractMongoDataSet<?> amds = (AbstractMongoDataSet<?>) v.getDataSet();
		amds.removeVisualization(v);
		saveResource(v.getDataSet().getResource());
	}

	public File createResourceFile(ObjectId oid, MediaType mt) throws IOException {
		File dir = getResourceDir(oid);
		if (!dir.exists())
			dir.mkdirs();
		File f = File.createTempFile("RES-", "", dir);
		return f;
	}

	public File getResourceDir(ObjectId oid) {
		if (resourceDir == null) {
			resourceDir = VissConfig.getInstance().getResourcePath();
			if (!resourceDir.exists())
				resourceDir.mkdirs();
		}
		return Utils.to(resourceDir, oid.toString());
	}

	@Override
	public IResource saveResource(IResource r) {
		AbstractMongoResource<?> amr = (AbstractMongoResource<?>) r;
		log.debug("Saving Resource {};", amr.getId());
		for (IDataSet ds : amr.getDataSets()) {
			log.debug("Saving Dataset {};", ds.getId());
			log.debug("{}",ds);
			getDatasetDao().save((AbstractMongoDataSet<?>) ds);
		}
		return get((ObjectId)getResourceDao().save(amr).getId());
	}

}