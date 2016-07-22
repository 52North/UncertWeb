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
import org.uncertweb.viss.vis.sample.SampleVisualizer;

public class OsloTest extends AbstractVissTest {

	@Test
	public void testOslo() throws JSONException {
		ObjectId r = addResource(MediaTypes.NETCDF_TYPE, AbstractVissTest.class.getResourceAsStream("/data/oslo_conc_20110103_new2.nc"));
		ObjectId ds = getDataSetsForResource(r)[0];
		String visualizer = getNameForVisualizer(SampleVisualizer.class);
		JSONObject visualizerDescription = getVisualizerForDataset(r, ds, visualizer);
		System.err.println(visualizerDescription.toString(4));
		String vis = createVisualization(r, ds, visualizer, new JSONObject()
				.put(SampleVisualizer.REALISATION_PARAMETER, 0)
				.put(SampleVisualizer.SAMPLE_PARAMETER, 3)
				.put(SampleVisualizer.TIME_PARAMETER, "2011-01-03T02:00:00.000+01:00"));
		System.err.println(vis);
	}


}
