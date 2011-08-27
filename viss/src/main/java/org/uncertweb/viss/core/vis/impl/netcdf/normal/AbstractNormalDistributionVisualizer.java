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
package org.uncertweb.viss.core.vis.impl.netcdf.normal;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.util.FastMath;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.util.NetCDFHelper;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.impl.netcdf.AbstractNetCDFVisualizer;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public abstract class AbstractNormalDistributionVisualizer extends
		AbstractNetCDFVisualizer {

	private static final Set<URI> NEEDED = Collections.unmodifiableSet(Utils
			.set(Constants.NORMAL_DISTRIBUTION_MEAN,
					Constants.NORMAL_DISTRIBUTION_VARIANCE));
	
	private static final int TIMES_STANDARD_DEVIATION = 3;

	@Override
	protected Set<URI> hasToHaveOneOf() {
		return Collections.emptySet();
	}

	@Override
	protected Set<URI> hasToHaveAll() {
		return NEEDED;
	}

	@Override
	protected double evaluate(Map<URI, Double> values) {
		double m = values.get(Constants.NORMAL_DISTRIBUTION_MEAN).doubleValue();
		double v = values.get(Constants.NORMAL_DISTRIBUTION_VARIANCE)
				.doubleValue();
		return evaluate(new NormalDistributionImpl(m, FastMath.sqrt(v)));
	}

	protected double[] getRange(Resource r) {
		return getRange(r, TIMES_STANDARD_DEVIATION);
	}
	
	protected double[] getRange(Resource r, int tsd) {
		try {
			NetcdfFile netCDF = getNetCDF(r);
			NetCDFHelper.checkForUWConvention(netCDF);
			Map<URI, Variable> vars = NetCDFHelper.getVariables(netCDF, NEEDED);
			Variable mV = vars.get(Constants.NORMAL_DISTRIBUTION_MEAN);
			Variable vV = vars.get(Constants.NORMAL_DISTRIBUTION_VARIANCE);
			Array mA = mV.read();
			Array vA = vV.read();
			Index mI = mA.getIndex();
			Index vI = vA.getIndex();
			int mMV = NetCDFHelper.getMissingValue(mV).intValue();
			int vMV = NetCDFHelper.getMissingValue(vV).intValue();
			int[] shape = new int[] {
				NetCDFHelper.getLatitude(netCDF).getShape()[0],
				NetCDFHelper.getLongitude(netCDF).getShape()[0]
			};
			double min = Double.POSITIVE_INFINITY; 
			double max = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < shape[0]; ++i) {
				for (int j = 0; j < shape[1]; ++j) {
					Double mVal = Double.valueOf(mA.getDouble(mI.set(i, j)));
					Double vVal = Double.valueOf(vA.getDouble(vI.set(i, j)));
					if (vVal.intValue() == vMV || mVal.intValue() == mMV) 
						continue;
					double m = mVal.doubleValue();
					double v = vVal.doubleValue();
					double sd = FastMath.sqrt(v);
					min = FastMath.min(min, m - tsd * sd);
					max = FastMath.max(max, m + tsd * sd);
				}
			}
			return new double[] { min, max };
		} catch (IOException e) {
			throw VissError.internal(e);
		}
	}
	


	protected abstract double evaluate(NormalDistribution nd);

}
