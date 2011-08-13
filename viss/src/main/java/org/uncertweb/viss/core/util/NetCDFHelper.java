package org.uncertweb.viss.core.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import org.geotools.coverage.grid.GridCoverageBuilder;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.Envelope;
import org.uncertweb.viss.core.visualizer.WriteableGridCoverage;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import org.uncertweb.viss.core.VissError;

public class NetCDFHelper {

	private static final String MISSING_VALUE_ATTRIBUTE = "missing_value";
	private static final String PRIMARY_VARIABLES_ATTRIBUTE = "primary_variables";
	private static final String UNITS_ATTRIBUTE = "units";
	private static final String REF_ATTRIBUTE = "ref";
	private static final String ANCIALLARY_VARIABLES_ATTRIBUTE = "ancillary_variables";
	private static final String CONVENTIONS_ATTRIBUTE = "Conventions";
	private static final String UW_CONVENTION = "UW-1.0";

	public static void checkForUWConvention(NetcdfFile f) {
		Attribute a = f.findGlobalAttribute(CONVENTIONS_ATTRIBUTE);
		if (a == null) {
			throw VissError.internal("No conventions attribute");
		}
		for (String s : a.getStringValue().split(" ")) {
			if (s.equals(UW_CONVENTION))
				return;
		}
		throw VissError.internal("NetCDF file is not " + UW_CONVENTION
				+ " compliant.");
	}
	
	public static Set<URI> getURIs(NetcdfFile f) {
		Set<URI> set = Utils.set();
		for (Variable v : f.getVariables()) {
			URI uri = getURI(v);
			if (uri != null) {
				set.add(uri);
			}
		}
		return set;
	}

	public static double getMissingValue(Variable v) {
		Attribute a = v.findAttribute(MISSING_VALUE_ATTRIBUTE);
		if (a == null) {
			return Double.NaN;
		} else {
			return a.getNumericValue().doubleValue();
		}
	}

	public static double getMissingValue(NetcdfFile f, URI uri) {
		Variable v = getVariables(f, Utils.set(uri)).get(uri);
		if (v == null) {
			return Double.NaN;
		} else {
			return getMissingValue(v);
		}
	}

	public static Unit<? extends Quantity> getUnit(NetcdfFile f) {
		return getUnit(getPrimaryVariable(f));
	}

	public static Variable getPrimaryVariable(NetcdfFile f) {
		Attribute a = f.findGlobalAttribute(PRIMARY_VARIABLES_ATTRIBUTE);
		if (a.getLength() == 1) {
			return getNotNullVariable(f, a.getStringValue(0));
		} else {
			throw VissError
					.internal("Only a single primary value is currently supported");
		}
	}

	public static Unit<? extends Quantity> getUnit(Variable v) {
		Attribute a = v.findAttribute(UNITS_ATTRIBUTE);
		if (a != null) {
			try {
				return Unit.valueOf(a.getStringValue());
			} catch (IllegalArgumentException e) {
				return Unit.ONE;
			}
		} else {
			return Unit.ONE;
		}
	}

	public static Unit<? extends Quantity> getUnit(NetcdfFile f, URI uri) {
		Variable v = getVariables(f, Utils.set(uri)).get(uri);
		if (v == null) {
			return Unit.ONE;
		}
		return getUnit(v);
	}

	public static Variable getNotNullVariable(NetcdfFile f, String name) {
		Variable v = f.findVariable(name);
		if (v == null) {
			throw VissError.internal("Variable with name \"" + name
					+ "\" could not be found.");
		}
		return v;
	}

	public static Variable getLongitude(NetcdfFile f) {
		return getNotNullVariable(f, "lon");
	}

	public static Variable getLatitude(NetcdfFile f) {
		return getNotNullVariable(f, "lat");
	}

	public static Envelope getEnvelope(NetcdfFile f) {
		Variable lon = getLongitude(f);
		Variable lat = getLatitude(f);
		try {
			int lonSize = lon.getShape()[0];
			int latSize = lat.getShape()[0];

			double lonMin = lon.read(Utils.list(new Range(0, 0))).getDouble(0);
			double lonMax = lon.read(
					Utils.list(new Range(lonSize - 1, lonSize - 1))).getDouble(
					0);

			double latMin = lat.read(Utils.list(new Range(0, 0))).getDouble(0);
			double latMax = lat.read(
					Utils.list(new Range(latSize - 1, latSize - 1))).getDouble(
					0);
			double width = latMax - latMin;
			double height = lonMax - lonMin;

			return new Envelope2D(DefaultGeographicCRS.WGS84, latMin, lonMin,
					width, height);
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	public static WriteableGridCoverage getCoverage(NetcdfFile f,
			String layerName) {
		return getCoverage(f, layerName, getPrimaryVariable(f));
	}

	public static URI getPrimaryURI(NetcdfFile f) {
		return getURI(getPrimaryVariable(f));
	}

	public static WriteableGridCoverage getCoverage(NetcdfFile f,
			String layerName, Variable v) {
		Attribute a = v.findAttribute(ANCIALLARY_VARIABLES_ATTRIBUTE);
		if (a == null)
			throw VissError
					.internal("Can not determine shape of variable: no \""
							+ ANCIALLARY_VARIABLES_ATTRIBUTE + "\" attribute.");
		int[] size = null;
		for (String s : a.getStringValue().split(" ")) {
			int[] shape = getNotNullVariable(f, (String) s).getShape();
			if (size == null)
				size = shape;
			else if (size.length == shape.length) {
				for (int i = 0; i < size.length; i++) {
					if (shape[i] != size[i])
						throw VissError
								.internal("Different shapes in ancillary_variables.");
				}
			} else {
				throw VissError
						.internal("Different dimensions in ancillary_variables.");
			}
		}
		final GridCoverageBuilder b = new GridCoverageBuilder();
		b.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
		b.setImageSize(size[0], size[1]);
		b.setEnvelope(getEnvelope(f));
		b.newVariable(layerName, getUnit(v)).setLinearTransform(1, 0);
		return new WriteableGridCoverage(b.getGridCoverage2D());
	}
	
	public static URI getURI(Variable v) {
		Attribute ref = v.findAttribute(REF_ATTRIBUTE);
		if (ref != null) {
			try {
				return new URI(ref.getStringValue());
			} catch (URISyntaxException e) {
				VissError.internal(e);
			}
		}
		return null;
	}

	public static Map<URI, Variable> getVariables(NetcdfFile f, Set<URI> wanted) {
		if (f == null || wanted == null || wanted.isEmpty()) {
			return Collections.emptyMap();
		} else {
			Map<URI, Variable> variables = Utils.map();
			for (Variable v : f.getVariables()) {
				URI uri = getURI(v);
				if (wanted.contains(uri)) {
					variables.put(uri, v);
				}
			}
			return variables;
		}
	}
}
