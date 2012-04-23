package org.uncertweb.viss.core;

import java.io.IOException;
import java.net.URI;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonRect;

public class BiotempTest {
	
	private enum Axis {
		X, Y, Z, T, R;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		String f = BiotempTest.class.getResource("/data/netcdf/biotemp.nc").getPath();

		NetcdfDataset ds = NetcdfDataset.openDataset(f);
		String firstPrimary = ds.findGlobalAttribute("primary_variables").getStringValue().split(" ")[0];
		Variable v = ds.findVariable(firstPrimary);
		
		Attribute ancillaryVariables = v.findAttributeIgnoreCase("ancillary_variables");
		if (ancillaryVariables != null && ancillaryVariables.isString()) {
			String[] vars = ancillaryVariables.getStringValue().split(" ");
			for (String s : vars) {
				Variable av = ds.findVariable(s);
				EnumMap<Axis, Integer> index = findDimensionIndexes(ds, av);
				URI uri = getRef(av);
				Variable x = ds.findVariable(av.getDimension(index.get(Axis.X)).getName());
				Variable y = ds.findVariable(av.getDimension(index.get(Axis.Y)).getName());

				Array ax = x.read();
				Array ay = y.read();
				Array avar = av.read();
				Index in = avar.getIndex();
				for (int i = 0; i < ax.getSize(); ++i) {
					for (int j = 0; j < ay.getSize(); ++j) {
						in.setDim(index.get(Axis.X), i);
						in.setDim(index.get(Axis.Y), j);
						double cx = ax.getDouble(i);
						double cy = ay.getDouble(j);
						double d = avar.getDouble(in);
						if (!Double.isNaN(d)) {
							System.out.printf(Locale.US, "(%g,%g) %g%n", cx, cy, d);
						}
					}	
				}
			}
		}
		
		
		
		GridDataset gd = GridDataset.open(f);

		String variableName = gd.findGlobalAttributeIgnoreCase(
				"primary_variables").getStringValue();

		LatLonRect bb = gd.getBoundingBox();

		System.out.printf("(%g,%g),(%g,%g) %n", bb.getLatMin(), bb.getLatMax(),
				bb.getLonMin(), bb.getLonMax());
		
		VariableSimpleIF var = gd.getDataVariable(variableName);
	}
	
	static URI getRef(Variable v) {
		Attribute a = v.findAttributeIgnoreCase("ref");
		if (a != null && a.isString())
			return URI.create(a.getStringValue());
		return null;
	}
	
	static EnumMap<Axis, Integer> findDimensionIndexes(NetcdfDataset ds, Variable v) {
		List<Dimension> dims = v.getDimensions();
		int i = 0;
		EnumMap<Axis, Integer> index = new EnumMap<Axis,Integer>(Axis.class);
		for (Dimension d : dims) {
			CoordinateAxis ca = ds.findCoordinateAxis(d.getName());
			if (ca != null) {
				switch (ca.getAxisType()) {
				case Lat:
				case GeoX:
					index.put(Axis.X, i);
					break;
				case Lon:
				case GeoY:
					index.put(Axis.Y, i);
					break;
				case Height:
				case GeoZ:
					index.put(Axis.Z, i);
					break;
				case Time:
					index.put(Axis.T, i);
					break;
				default:
				}
			} else {
				Attribute a = ds.findVariable(d.getName())
						.findAttributeIgnoreCase("standard_name");
				if (a != null && a.getStringValue().equals("realization")) {
					index.put(Axis.R, i);
				}
			}
			++i;
		}
		return index;
	}

}
