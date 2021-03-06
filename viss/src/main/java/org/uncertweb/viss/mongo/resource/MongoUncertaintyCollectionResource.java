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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.UncertaintyCollection;
import org.uncertweb.viss.core.resource.UncertaintyReference;
import org.uncertweb.viss.core.util.MediaTypes;

public class MongoUncertaintyCollectionResource extends AbstractMongoResource<UncertaintyCollection>{

	public MongoUncertaintyCollectionResource(File f, ObjectId oid, long checksum) {
		super(MediaTypes.JSON_UNCERTAINTY_COLLECTION_TYPE, f, oid, checksum);
	}

	public MongoUncertaintyCollectionResource() {
		super(MediaTypes.JSON_UNCERTAINTY_COLLECTION_TYPE);
	}

	@Override
	public void close() {}

	@Override
	protected UncertaintyCollection loadContent() {
		String path = getFile().getAbsolutePath();
		JSONObject json = null;
		InputStream in = null;
		try {
			in = new FileInputStream(path);
			json = new JSONObject(IOUtils.toString(in));
			JSONArray uncertainties = json
					.getJSONObject("UncertaintyCollection")
					.getJSONArray("uncertainties");
			UncertaintyCollection col = new UncertaintyCollection();
			List<UncertaintyReference> references = UwCollectionUtils.list();
			for (int i = 0; i < uncertainties.length(); i++) {
				JSONObject o = uncertainties.getJSONObject(i);
				UncertaintyReference uref = new UncertaintyReference(o);
				references.add(uref);
			}
			col.setReferences(references);
			return col;
		} catch (JSONException e) {
			throw VissError.badRequest(e);
		} catch (IOException e) {
			throw VissError.badRequest(e);
		} catch (URISyntaxException e) {
			throw VissError.badRequest(e);
		} finally {
			IOUtils.closeQuietly(in);
		}

	}

	@Override
	protected Set<IDataSet> createDataSets() {
		Set<IDataSet> set = UwCollectionUtils.set();
		for (UncertaintyReference ref : getContent().getReferences()) {
			set.add(new MongoUncertaintyCollectionDataSet(this, ref));
		}
		return set;
	}
}
