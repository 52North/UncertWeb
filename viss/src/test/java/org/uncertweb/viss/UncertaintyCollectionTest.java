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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uncertweb.viss;


import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.uncertweb.viss.core.util.MediaTypes;
import org.uncertweb.viss.vis.sample.RealisationVisualizer;
import org.uncertweb.viss.vis.statistic.SimpleStatisticVisualizer;

@Ignore
public class UncertaintyCollectionTest extends AbstractVissTest {

	@Test
	public void testUncertaintyCollection() throws JSONException {
		_testUncertaintyCollection();
		//_testUncertaintyCollection();
	}

	private void _testUncertaintyCollection() throws JSONException {
		ObjectId r = addResource(MediaTypes.JSON_UNCERTAINTY_COLLECTION_TYPE,
								 getUncertaintyCollectionStream());
		for (ObjectId ds : getDataSetsForResource(r)) {
			getDataSetForResource(r, ds);
			for (String s : getVisualizersForDataset(r, ds)) {
				if (s.equals(getNameForVisualizer(SimpleStatisticVisualizer.MeanStatistic.class))
					|| s.equals(getNameForVisualizer(SimpleStatisticVisualizer.StandardDeviationStatistic.class))
					|| s.equals(getNameForVisualizer(SimpleStatisticVisualizer.ProbabilityStatistic.class))) {
					getVisualizerForDataset(r, ds, s);
					createVisualization(r, ds, s);
				}
				if (s.equals(getNameForVisualizer(RealisationVisualizer.class))) {
					getVisualizerForDataset(r, ds, s);
					createVisualization(r, ds, s, new JSONObject().put(
							RealisationVisualizer.REALISATION_PARAMETER, 0));
				}
			}
		}
	}
}
