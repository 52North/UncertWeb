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

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.uncertweb.viss.core.util.MediaTypes;
import org.uncertweb.viss.vis.statistic.SimpleStatisticVisualizer;

public class AggregationResultsTest extends AbstractVissTest {
	
	@Test
	public void testAggregationResults() throws JSONException {
		ObjectId r = addResource(MediaTypes.NETCDF_TYPE, AGGREGATION_RESULT);
		ObjectId ds = getDataSetsForResource(r)[0];
		String vis = createVisualization(r, ds,
				SimpleStatisticVisualizer.MeanStatistic.class.getSimpleName());
		JSONObject res = getVisualization(r, ds, vis);
		System.err.println(res.toString(4));
	}
}
