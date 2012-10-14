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

import static org.junit.Assert.assertEquals;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Test;
import org.uncertweb.utils.UwTimeUtils;
import org.uncertweb.viss.core.util.MediaTypes;
import org.uncertweb.viss.vis.AbstractVisualizer;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer;

public class TimeTest extends AbstractVissTest {

	@Test
	public void testTime() throws JSONException {
		ObjectId r = addResource(MediaTypes.NETCDF_TYPE, EU_JUNE);
		ObjectId d = getDataSetsForResource(r)[0];
		String v = getVisualizersForDataset(r, d)[0];
		createVisualization(r, d, v, new JSONObject()
				.put(AbstractVisualizer.TIME_PARAMETER, "2005-11-11T00:00:00.000Z"));
	}
	
	
	@Test
	public void testBiotempTime() throws JSONException {
		ObjectId r = addResource(MediaTypes.NETCDF_TYPE, BIOTEMP_T);
		ObjectId[] datasets = getDataSetsForResource(r);
		assertEquals(1, datasets.length);
		ObjectId d = datasets[0];
		
		String[] visualizers = getVisualizersForDataset(r, d);
		assertEquals(7, visualizers.length);
		
		DateTime begin = UwTimeUtils.parseDateTime("2012-04-01T09:00:00.000Z");
		for (int i = 0; i < 6; ++i) {
			String time = UwTimeUtils.format(begin.plusHours(i));
			JSONObject timep = new JSONObject().put("time", time);
			createVisualization(r, d, getNameForVisualizer(NormalDistributionVisualizer.Mean.class), timep);
			createVisualization(r, d, getNameForVisualizer(NormalDistributionVisualizer.Variance.class), timep);
			createVisualization(r, d, getNameForVisualizer(NormalDistributionVisualizer.StandardDeviation.class), timep);
			createVisualization(r, d, getNameForVisualizer(NormalDistributionVisualizer.Probability.class), new JSONObject()
					.put(NormalDistributionVisualizer.Probability.MAX_PARAMETER, 0.5D)
					.put(NormalDistributionVisualizer.Probability.TIME_PARAMETER, time));
			createVisualization(r, d, getNameForVisualizer(NormalDistributionVisualizer.ProbabilityForInterval.class), new JSONObject()
					.put(NormalDistributionVisualizer.ProbabilityForInterval.MIN_PARAMETER, 0.3D)
					.put(NormalDistributionVisualizer.ProbabilityForInterval.MAX_PARAMETER, 0.6D)
					.put(NormalDistributionVisualizer.ProbabilityForInterval.TIME_PARAMETER, time));
		}
	}
}
