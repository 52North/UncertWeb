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
package org.uncertweb.viss.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.util.MediaTypes;
import org.uncertweb.viss.mongo.resource.AbstractMongoResource;
import org.uncertweb.viss.mongo.resource.MongoResourceStore;
import org.uncertweb.viss.vis.sample.SampleVisualizer;

public class OsloTest {
	public static void main(String[] args) throws IOException,
			URISyntaxException, JSONException {

		final AbstractMongoResource<?> r = new MongoResourceStore()
				.getResourceForMediaType(
						MediaTypes.NETCDF_TYPE,
						new File(OsloTest.class.getResource(
								"/data/oslo_conc_20110103_new2.nc").toURI()),
						new ObjectId(), 0);

		final IDataSet ds = r.getDataSets().iterator().next();
		final SampleVisualizer v = new SampleVisualizer();

		System.out.println(new JSONObject(v.getOptionsForDataSet(ds)).toString(4));
		
		v.visualize(ds, new JSONObject()
				.put("realisation", 0)
				.put("sample", 3)
				.put("time", "2011-01-03T02:00:00.000+01:00"));
	}
}
