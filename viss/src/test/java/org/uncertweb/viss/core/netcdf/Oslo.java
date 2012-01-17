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
package org.uncertweb.viss.core.netcdf;

import java.io.IOException;

import org.uncertweb.api.om.TimeObject;
import org.uncertweb.viss.vis.netcdf.UncertaintyNetCDF;
import org.uncertweb.viss.vis.netcdf.UncertaintyValue;
import org.uncertweb.viss.vis.netcdf.UncertaintyVariable;

import ucar.nc2.NetcdfFile;

public class Oslo {

	public static void testFile(String name) throws IOException {
		NetcdfFile f = NetcdfFile.open(Oslo.class.getResource(name).toString());

		UncertaintyNetCDF netcdf = new UncertaintyNetCDF(f);

		for (UncertaintyVariable v : netcdf.getVariables()) {
			System.out.println(v.getType().getURI() + " - " + v.getName()
					+ " in " + v.getUnitAsString());
			int i = 0;

			System.out.println("LatSize:" + v.getLatitudeSize());
			System.out.println("LonSize:" + v.getLongitudeSize());
			if (v.hasTimeComponent())
				System.out.println("TimeSize:" + v.getTimeSize());
			if (v.hasZComponent())
				System.out.println("HeightSize:" + v.getHeightSize());

			for (UncertaintyValue val : v) {
				 System.out.println(++i+": "+val);
			}

			if (v.hasTimeComponent()) {
				TimeObject to = v.getTimes().get(3);
				System.out.println("Printing all for " + to + ": ");
				for (UncertaintyValue vv : v.getTemporalLayer(to)) {
					 System.out.println(vv);
				}
			}
		}

		f.close();
		System.out.println();
	}

	public static void main(String[] args) throws IOException {
		testFile("/biotemp.nc");
		testFile("/oslo_cbg_20110101.nc");
		testFile("/oslo_met_20110102.nc");
	}

}
