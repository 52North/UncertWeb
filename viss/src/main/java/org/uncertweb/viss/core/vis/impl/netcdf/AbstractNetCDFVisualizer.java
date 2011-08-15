package org.uncertweb.viss.core.vis.impl.netcdf;

import static org.uncertweb.viss.core.util.Constants.NETCDF_TYPE;
import static org.uncertweb.viss.core.util.Constants.X_NETCDF_TYPE;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;
import org.opengis.coverage.grid.GridCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.NetCDFHelper;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.Visualization;
import org.uncertweb.viss.core.vis.Visualizer;
import org.uncertweb.viss.core.vis.WriteableGridCoverage;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public abstract class AbstractNetCDFVisualizer implements Visualizer {

	private static final Logger log = LoggerFactory
			.getLogger(AbstractNetCDFVisualizer.class);

	private JSONObject params;
	private Set<URI> found;

	@Override
	public Visualization visualize(Resource r, JSONObject params) {
		this.params = params;
		try {
			return new Visualization(r.getUUID(), getId(params), this, params, visualize(getNetCDF(r)));
		} catch (IOException e) {
			throw VissError.internal(e);
		}
	}

	@SuppressWarnings("unchecked")
	private GridCoverage visualize(NetcdfFile f) throws IOException {
		NetCDFHelper.checkForUWConvention(f);

		Map<URI, Variable> vars = NetCDFHelper.getVariables(f,
				Utils.combineSets(hasToHaveAll(), hasToHaveOneOf()));
		log.debug("Found {} Variables with relevant URIs.", vars.size());

		this.found = Collections.unmodifiableSet(vars.keySet());

		Map<URI, Array> arrays = Utils.map();
		Map<URI, Index> indexes = Utils.map();
		Map<URI, Double> missingValues = Utils.map();

		for (final Entry<URI, Variable> e : vars.entrySet()) {
			Array a = e.getValue().read();
			arrays.put(e.getKey(), a);
			indexes.put(e.getKey(), a.getIndex());
			missingValues.put(e.getKey(),
					Double.valueOf(NetCDFHelper.getMissingValue(e.getValue())));
		}

		WriteableGridCoverage wgc = NetCDFHelper.getCoverage(f,
				getCoverageName());

		Array latValues = NetCDFHelper.getLongitude(f).read();
		Array lonValues = NetCDFHelper.getLatitude(f).read();

		final int sizeLon = lonValues.getShape()[0];
		final int sizeLat = latValues.getShape()[0];

		for (int i = 0; i < sizeLon; ++i) {
			for (int j = 0; j < sizeLat; ++j) {
				final Map<URI, Double> values = Utils.map();
				for (final URI uri : vars.keySet()) {
					Array a = arrays.get(uri);
					Index x = indexes.get(uri);
					Double val = Double.valueOf(a.getDouble(x.set(i, j)));
					if (!val.equals(missingValues.get(uri))) {
						values.put(uri, val);
					}
				}
				if (!values.isEmpty()) {
					double v = evaluate(values);
					if (!Double.isNaN(v) && Double.isInfinite(v)) {
						double lon = lonValues.getDouble(i);
						double lat = latValues.getDouble(j);
						Point2D p = new Point2D.Double(lon, lat);
						wgc.setValueAtPos(p, v);
					}
				}
			}
		}
		return wgc.getGridCoverage();
	}

	protected Set<URI> getFoundURIs() {
		return this.found;
	}

	protected JSONObject getParams() {
		return this.params;
	}

	@Override
	public boolean isCompatible(Resource r) {
		Set<URI> uris = NetCDFHelper.getURIs(getNetCDF(r));
		Set<URI> all = hasToHaveAll();
		if (!all.isEmpty()) {
			if (!uris.containsAll(all)) {
				return false;
			}
		}
		Set<URI> one = hasToHaveOneOf();
		if (!one.isEmpty()) {
			for (URI uri : one) {
				if (uris.contains(uri)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	private NetcdfFile getNetCDF(Resource r) {
		NetcdfUWFile netCDF = (NetcdfUWFile) r.getResource();
		return netCDF.getNetcdfFile();
	}

	@Override
	public String getId(JSONObject params) {
		return this.getShortName();
	}
	
	@Override
	public String getShortName() {
		return this.getClass().getSimpleName();
	}

	protected abstract String getCoverageName();

	protected abstract Set<URI> hasToHaveOneOf();

	protected abstract Set<URI> hasToHaveAll();

	protected abstract double evaluate(Map<URI, Double> values);

	@Override
	public Set<MediaType> getCompatibleMediaTypes() {
		return Utils.set(NETCDF_TYPE, X_NETCDF_TYPE);
	}

	@Override
	public String getDescription() {
		return null;
	}
}
