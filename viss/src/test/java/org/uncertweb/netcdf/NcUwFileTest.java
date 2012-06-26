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
package org.uncertweb.netcdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.IObservationEncoder;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.utils.UwCollectionUtils;

public class NcUwFileTest {

	private String getEUJune() {
		return getClass().getResource("/data/netcdf/EU_June_4.nc").getPath();
	}

	@Test
	public void testCreation() throws IOException, OMEncodingException, JSONException {
		NcUwFile f = new NcUwFile(getEUJune());
		for (INcUwVariable v : f.getPrimaryVariables()) {
			if (v.isUncertaintyVariable()) {
				System.out.printf("%s: %s", v.getName(), v.getType().getUri());
				System.out.println(v.getEnvelope());
				CoordinateReferenceSystem crs = v.getCRS();
				System.out.println(crs);
				System.out.println(v.getCoverage().getGridCoverage()
						.getCoordinateReferenceSystem().equals(crs));

				List<UncertaintyObservation> list = UwCollectionUtils.list();
				for (NcUwObservation o : v.getTimeLayer(v.getTimes().get(0))) {
					if (o != null) {
						list.add(o);
					}
				}
				UncertaintyObservationCollection col = new UncertaintyObservationCollection(list);

				IObservationEncoder enc;
				File out;
				
				enc = new XBObservationEncoder();
				out = new File("/home/auti/obs.xml");
				enc.encodeObservationCollection(col, out);
				
				enc = new JSONObservationEncoder();
				out = new File("/home/auti/obs.json");
				IOUtils.copy(
					new StringReader(new JSONObject(enc.encodeObservationCollection(col)).toString(4)),
					new FileOutputStream(out));
				

			}
		}
	}

}
