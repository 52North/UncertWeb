package org.uncertweb.viss.visualizer;

import static org.uncertweb.viss.core.util.Constants.NETCDF;
import static org.uncertweb.viss.core.util.Constants.NETCDF_TYPE;
import static org.uncertweb.viss.core.util.Constants.NORMAL_DISTRIBUTION;
import static org.uncertweb.viss.core.util.Constants.NORMAL_DISTRIBUTION_MEAN;
import static org.uncertweb.viss.core.util.Constants.OM_2;
import static org.uncertweb.viss.core.util.Constants.OM_2_TYPE;
import static org.uncertweb.viss.core.util.Constants.X_NETCDF;
import static org.uncertweb.viss.core.util.Constants.X_NETCDF_TYPE;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.geotools.coverage.grid.GridCoverage2D;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.NetCDFHelper;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.visualizer.Visualization;
import org.uncertweb.viss.core.visualizer.Visualizer;
import org.uncertweb.viss.core.visualizer.Visualizer.Compatible;
import org.uncertweb.viss.core.visualizer.Visualizer.ShortName;
import org.uncertweb.viss.core.visualizer.WriteableGridCoverage;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

@ShortName("MeanVisualizer")
@Compatible({ NETCDF, X_NETCDF, OM_2 })
public class MeanVisualizer extends Visualizer {

	@Override
	public JSONObject getOptions() {
		try {
			return new JSONObject().put("huhu", "hallo");
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public Visualization visualize(Resource r, JSONObject p) {
		if (r.getMediaType().equals(NETCDF_TYPE)
				|| r.getMediaType().equals(X_NETCDF_TYPE)) {
			NetcdfUWFile f = (NetcdfUWFile) r.getResource();
			GridCoverage2D vis = visualizeNetCDF(f);
			return new Visualization(r.getUUID(), this, p, vis);
		} else if (r.getMediaType().equals(OM_2_TYPE)) {
			// OMResource omr = (OMResource) r;
			// IObservationCollection col = omr.getResource();
		}
		return null;
	}

	private GridCoverage2D visualizeNetCDF(NetcdfUWFile netCDF) {
		try {
			NetcdfFile f = netCDF.getNetcdfFile();
			NetCDFHelper.checkForUWConvention(f);
			final Map<URI, Variable> vars = NetCDFHelper.getVariables(f,
					Utils.set(NORMAL_DISTRIBUTION, NORMAL_DISTRIBUTION_MEAN));
			final Variable distr = vars.get(NORMAL_DISTRIBUTION);
			if (distr == null)
				throw VissError.internal("No ditribution found");
			final Variable mean = vars.get(NORMAL_DISTRIBUTION_MEAN);
			if (mean.getRank() != 2)
				throw VissError
						.internal("Only 2 dimensional arrays are supported");
			int[] size = mean.getShape();
			WriteableGridCoverage wgc = NetCDFHelper.getCoverage(f, "Mean");
			double mM = NetCDFHelper.getMissingValue(mean);
			Array lat = NetCDFHelper.getLongitude(f).read();
			final Array lon = NetCDFHelper.getLatitude(f).read();
			final Array mA = mean.read();
			final Index mI = mA.getIndex();
			for (int i = 0; i < size[0]; ++i) {
				for (int j = 0; j < size[1]; ++j) {
					final double mD = mA.getDouble(mI.set(i, j));
					if (NumberUtils.compare(mD, mM) != 0) {
						wgc.setValueAtPos(new Point2D.Double(lon.getDouble(i),
								lat.getDouble(j)), mD);
					}
				}
			}
			return wgc.getGridCoverage();
		} catch (IOException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public boolean isCompatible(Resource r) {
		return true;
	}

}
