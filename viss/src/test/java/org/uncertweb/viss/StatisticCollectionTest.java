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
package org.uncertweb.viss;

import java.net.URI;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.uncertweb.viss.core.util.JSONConstants;
import org.uncertweb.viss.core.util.JSONSchema;
import org.uncertweb.viss.core.util.MediaTypes;
import org.uncertweb.viss.vis.statistic.StatisticCollectionVisualizer;

public class StatisticCollectionTest extends AbstractVissTest {
	private static final URI STATISTIC_COLLECTION =
			URI.create("http://giv-uw.uni-muenster.de/data/netcdf/uts-statistic-collection.nc");

	@Test
	public void testStatisticCollection() throws JSONException {
		ObjectId r = addResource(MediaTypes.NETCDF_TYPE, STATISTIC_COLLECTION);
		ObjectId d = getDataSetsForResource(r)[0];
		JSONObject ds = getDataSetForResource(r, d);
		System.out.println(ds.toString(4));
		String visualizer = getVisualizersForDataset(r, d)[0];
		System.out.println(getVisualizerForDataset(r, d, visualizer).toString(4));
		String stat = getVisualizerForDataset(r, d, visualizer)
				.getJSONObject(JSONConstants.OPTIONS_KEY)
				.getJSONObject(StatisticCollectionVisualizer.STATISTIC_PARAMETER)
				.getJSONArray(JSONSchema.Key.ENUM).getString(0);

		createVisualization(r, d, visualizer, new JSONObject()
				.put(StatisticCollectionVisualizer.STATISTIC_PARAMETER, stat));

	}

}
