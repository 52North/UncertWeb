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

import javax.ws.rs.core.MediaType;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.util.MediaTypes;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer;
import org.uncertweb.viss.vis.sample.RealisationVisualizer;
import org.uncertweb.viss.vis.statistic.SimpleStatisticVisualizer;

public class MimeTypeParameterTest extends AbstractVissTest {
	@Test 
	public void testMimeTypeParameterForNetCDF() throws JSONException {
		MediaType mt = new MediaType(MediaTypes.NETCDF_TYPE.getType(), 
									 MediaTypes.NETCDF_TYPE.getSubtype(), 
									 UwCollectionUtils.map("encoding","utf-8"));
		ObjectId r = addResource(mt, AGGREGATION_RESULT);
		ObjectId ds = getDataSetsForResource(r)[0];
		String vis = createVisualization(r, ds, getNameForVisualizer(SimpleStatisticVisualizer.MeanStatistic.class));
		JSONObject res = getVisualization(r, ds, vis);
		System.err.println(res.toString(4));
	}
	
	@Test
	public void testMimeTypeParameterForOM() throws JSONException {
		MediaType mt = new MediaType(MediaTypes.OM_2_TYPE.getType(), 
									 MediaTypes.OM_2_TYPE.getSubtype(), 
									 UwCollectionUtils.map("encoding","utf-8"));
		ObjectId oid = addResource(mt, getOMStream());
		ObjectId[] datasets = getDataSetsForResource(oid);
		createVisualization(oid, datasets[0], getNameForVisualizer(NormalDistributionVisualizer.Mean.class));
	}
	
	@Test@Ignore
	public void testMimeTypeParameterForUC() throws JSONException {
		MediaType mt = new MediaType(MediaTypes.JSON_UNCERTAINTY_COLLECTION_TYPE.getType(), 
									 MediaTypes.JSON_UNCERTAINTY_COLLECTION_TYPE.getSubtype(), 
									 UwCollectionUtils.map("encoding","utf-8"));
		ObjectId r = addResource(mt, getUncertaintyCollectionStream());
		int created = 0;
		for (ObjectId ds : getDataSetsForResource(r)) {
			for (String s : getVisualizersForDataset(r, ds))  {
				if (s.equals(getNameForVisualizer(SimpleStatisticVisualizer.MeanStatistic.class))
					|| s.equals(getNameForVisualizer(SimpleStatisticVisualizer.StandardDeviationStatistic.class))
					|| s.equals(getNameForVisualizer(SimpleStatisticVisualizer.ProbabilityStatistic.class))) {
					getVisualizerForDataset(r,ds, s);
					createVisualization(r, ds, s);
					++created;
				}
				if (s.equals(getNameForVisualizer(RealisationVisualizer.class))) {
					getVisualizerForDataset(r, ds, s);
					createVisualization(r, ds, s, new JSONObject().put(
							RealisationVisualizer.REALISATION_PARAMETER, 0));
					++created;
				}
			}
		}
		Assert.assertEquals(5, created);
	}	
}
