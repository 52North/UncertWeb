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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.joda.time.DateTime;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.utils.UwIOUtils;
import org.uncertweb.utils.UwStringUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.units.DateUnit;

public class NcUwHelper {
	private static final Pattern EPSG_INIT_PARAMETER = Pattern.compile("\\+init=[eE][pP][sS][gG]:[0-9]+");
	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
	private static final Logger log = LoggerFactory.getLogger(NcUwHelper.class);
	private static final String PYTHON_SCRIPT;

	public static final CoordinateReferenceSystem EPSG4326;

	static {
		try {
			EPSG4326 = NcUwHelper.decodeEpsgCode(4326);
		} catch (FactoryException e) {
			throw new NcUwException(e);
		}
	}

	static {
		final URL uri = NcUwHelper.class.getResource("/proj2wkt.py");
		if (uri == null) {
			throw new NcUwException("Can not access pyton script proj2wkt.py");
		}
		InputStream is = null;
		OutputStream os = null;
		File f = null;
		try {
			f = File.createTempFile("proj2wkt", ".py");
			f.createNewFile();
			f.deleteOnExit();
			f.setExecutable(true);
			is = uri.openStream();
			os = new FileOutputStream(f);
			IOUtils.copy(is, os);
		} catch (IOException e) {
			throw new NcUwException(e);
		} finally {
			UwIOUtils.closeQuietly(is);
			UwIOUtils.closeQuietly(os);
		}

		if (f == null || !f.exists() || !f.isFile() || !f.canRead()) {
			throw new NcUwException("Can not access pyton script:" + f);
		}
		PYTHON_SCRIPT = f.toString();
	}

	@SuppressWarnings("unchecked")
	public static CoordinateReferenceSystem decodeProj4(String p4s)
			throws IOException, FactoryException {

		Matcher m = EPSG_INIT_PARAMETER.matcher(p4s);
		if (m.find()) {
			return decodeEpsgCode(m.group().substring("+init=".length()));
		}

		final String[] exec = new String[] { "python", PYTHON_SCRIPT, p4s };
		log.info("executing: {} {} '{}'", (Object[]) exec);
		final Process p = Runtime.getRuntime().exec(exec);
		int status = 0;
		try {
			status = p.waitFor();
		} catch (final InterruptedException e) {
			throw new NcUwException(e);
		}
		if (status > 0) {
			throw new NcUwException("Can not decode Proj4 string:\n\t"
					+ UwStringUtils.join("\n\t",
							IOUtils.readLines(p.getErrorStream())));
		}
		final String wkt = UwStringUtils.join(" ",
				IOUtils.readLines(p.getInputStream()));
		log.debug("Generated WKT: {}", wkt);
		return decodeWkt(wkt);
	}

	public static CoordinateReferenceSystem decodeWkt(String wkt)
			throws FactoryException {
		return CRS.parseWKT(wkt);
	}

	public static CoordinateReferenceSystem decodeEpsgCode(int code)
			throws FactoryException {
		return CRS.decode("EPSG:" + code);
	}

	public static CoordinateReferenceSystem decodeEpsgCode(String code)
			throws FactoryException {
		if (code.matches("\\d+")) {
			return decodeEpsgCode(Integer.valueOf(code));
		}
		return CRS.decode(code);
	}

	public static String getStringAttribute(Variable v, String name) {
		return getStringAttribute(v, name, false);
	}

	public static String getStringAttribute(NetcdfFile f, String name) {
		return getStringAttribute(f, name, false);
	}

	public static String getStringAttribute(Variable v, String name,
			boolean failIfNotExisting) {
		return getStringAttribute(v.findAttributeIgnoreCase(name),
				failIfNotExisting);
	}

	public static String getStringAttribute(NetcdfFile f, String name,
			boolean failIfNotExisting) {
		return getStringAttribute(f.findGlobalAttributeIgnoreCase(name),
				failIfNotExisting);
	}

	public static String getStringAttribute(Attribute a,
			boolean failIfNotExisting) {
		if (a == null) {
			if (failIfNotExisting) {
				throw new NcUwException("Attribute is not present");
			} else {
				return null;
			}
		}
		if (!a.getDataType().isString()) {
			throw new NcUwException(
					"Wrong datatype for string attribute %s: %s", a.getName(),
					a.getDataType());
		} else {
			return a.getStringValue();
		}
	}

	public static Number getNumberAttribute(Variable v, String name) {
		return getNumberAttribute(v, name, false);
	}

	public static Number getNumberAttribute(NetcdfFile f, String name) {
		return getNumberAttribute(f, name, false);
	}

	public static Number getNumberAttribute(Variable v, String name,
			boolean failIfNotExisting) {
		return getNumberAttribute(v.findAttributeIgnoreCase(name),
				failIfNotExisting);
	}

	public static Number getNumberAttribute(NetcdfFile f, String name,
			boolean failIfNotExisting) {
		return getNumberAttribute(f.findGlobalAttributeIgnoreCase(name),
				failIfNotExisting);
	}

	public static Number getNumberAttribute(Attribute a,
			boolean failIfNotExisting) {
		if (a == null) {
			if (failIfNotExisting) {
				throw new NcUwException("Attribute is not present");
			} else {
				return null;
			}
		}
		if (!a.getDataType().isNumeric()) {
			throw new NcUwException("Wrong datatype for number attribute %s",
					a.getDataType());
		} else {
			return a.getNumericValue();
		}
	}

	public static Variable findVariable(NetcdfFile f, String name,
			boolean failIfNotExisting) {
		final Variable v = f.findVariable(name);
		if (v == null && failIfNotExisting) {
			throw new NcUwException("Variable %s not found.", name);
		}
		return v;
	}

	public static URI getRef(Variable v) {
		final String ref = getStringAttribute(v, NcUwConstants.Attributes.REF,
				false);
		if (ref == null) {
			return null;
		}
		return URI.create(ref);
	}

	public static Number[] getRangeOfOrderedVariable(Variable v) {
		if (!v.isCoordinateVariable() || !v.getDataType().isNumeric()) {
			throw new NcUwException("Incompatible variable.");
		}
		try {
			final int last = v.getShape()[0] - 1;
			final Number d0 = (Number) v.read(UwCollectionUtils.list(new Range(0, 0))).getObject(0);
			final Number dn = (Number) v.read(UwCollectionUtils.list(new Range(last, last))).getObject(0);
			return new Number[] { d0, dn };
		} catch (InvalidRangeException e) {
			throw new NcUwException(e);
		} catch (IOException e) {
            throw new NcUwException(e);
        }
	}

	public static Number min(Number n1, Number n2) {
		return compare(n1, n2) < 0 ? n1 : n2;
	}

	public static Number max(Number n1, Number n2) {
		return compare(n1, n2) > 0 ? n1 : n2;
	}

	public static int compare(Number n1, Number n2) {
		return Double.compare(n1.doubleValue(), n2.doubleValue());
	}

	public static Number min(Number... ns) {
		if (ns.length < 0) {
			return null;
		}
		Number min = Double.POSITIVE_INFINITY;
		for (Number n : ns) {
			min = min(min, n);
		}
		return min;
	}

	public static Number max(Number... ns) {
		if (ns.length < 0) {
			return null;
		}
		Number max = Double.NEGATIVE_INFINITY;
		for (Number n : ns) {
			max = max(max, n);
		}
		return max;
	}

	public static List<TimeObject> parseTimes(Array a, DateUnit unit)
			throws IOException {
		final int size = a.getShape()[0];
		final ArrayList<TimeObject> times = new ArrayList<TimeObject>(size);
		for (int i = 0; i < size; ++i) {
			times.add(new TimeObject(
					new DateTime(unit.makeDate(a.getDouble(i)))));
		}
		return times;
	}

	public static URI toUri(String s, URI defaultUri, URI prefix) {
		if (s != null) {
			try {
				if (prefix == null) {
					return new URI(s);
				} else {
					return new URI(prefix.getPath() + "/" + s);
				}
			} catch (final URISyntaxException e) {
				return defaultUri;
			}
		} else {
			return defaultUri;
		}
	}

	public static Point positionToWgs84Point(DirectPosition dp) {
		try {
			MathTransform mt = CRS.findMathTransform(
					dp.getCoordinateReferenceSystem(),
					DefaultGeographicCRS.WGS84, true);
			DirectPosition transformed = mt.transform(dp, null);
			final double[] coord = transformed.getCoordinate();
			final Point p = GEOMETRY_FACTORY.createPoint(new Coordinate(
					coord[0], coord[1]));
			p.setSRID(4326);
			return p;
		} catch (final FactoryException e) {
			throw new NcUwException(e);
		} catch (final MismatchedDimensionException e) {
			throw new NcUwException(e);
		} catch (final TransformException e) {
			throw new NcUwException(e);
		}
	}

	public static Polygon envelopeToPolygon(Envelope e, Integer srs) {
		try {
			ReferencedEnvelope re = new ReferencedEnvelope(e);

			// no explicit espg code
			if (srs == null || srs < 0) {
				// try to find the code of the envelope
				for (ReferenceIdentifier id : re.getCoordinateReferenceSystem().getIdentifiers()) {
					if (id.getCodeSpace().equalsIgnoreCase("EPSG")) {
						srs = Integer.valueOf(id.getCode());
						break;
					}
				}
				// if no code found, transform to EPSG4326
				if (srs == null || srs < 0) {
					srs = 4326;
				}
			}
			Polygon p = JTS.toGeometry(re.transform(decodeEpsgCode(srs), true, 12));
			p.setSRID(srs);
			return p;
		} catch (final MismatchedDimensionException ex) {
			throw new NcUwException(ex);
		} catch (final TransformException ex) {
			throw new NcUwException(ex);
		} catch (final FactoryException ex) {
			throw new NcUwException(ex);
		}
	}

	public static DirectPosition toDirectPosition(Point p, CoordinateReferenceSystem targetCRS) {
		CoordinateReferenceSystem crs = null;
		if (p.getSRID() > 0) {
			try {
				crs = NcUwHelper.decodeEpsgCode(p.getSRID());
			} catch (FactoryException e) {
				throw new NcUwException(e);
			}
		} else {
			crs = EPSG4326;
		}

		if (crs == null) {
			throw new NullPointerException();
		}

		DirectPosition dp = JTS.toDirectPosition(p.getCoordinate(), crs);
		if (targetCRS != null) {
			try {
				dp = CRS.findMathTransform(crs, targetCRS).transform(dp, new GeneralDirectPosition(targetCRS));
			} catch (FactoryException e) {
				throw new NcUwException(e);
			} catch (MismatchedDimensionException e) {
                throw new NcUwException(e);
            } catch (TransformException e) {
                throw new NcUwException(e);
            }
		}
		return dp;
	}

	private static void test(CoordinateReferenceSystem crs, boolean epsgAvailableTest) {
		if (crs == null) {
			throw new NullPointerException();
		}
		System.out.println(crs.toWKT());
		int epsg = getEpsgCodeForCrs(crs);
		if (epsgAvailableTest) {
			if (epsg < 0) {
				throw new NullPointerException();
			}
		}

		System.out.println(epsg);
	}


	public static void main(String[] args) throws FactoryException, IOException {
		final String p4s1 = "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs";
		final String p4s2 = "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs";
		final String p4s3 = "+init=epsg:31467 +proj=tmerc +lat_0=0 +lon_0=9 +k=1 +x_0=3500000 +y_0=0 +ellps=bessel +datum=potsdam +units=m +no_defs +towgs84=606.0,23.0,413.0";
		final String p4s4 = "+proj=tmerc +lat_0=0 +lon_0=9 +k=1 +x_0=3500000 +y_0=0 +ellps=bessel +datum=potsdam +units=m +no_defs";
		final String p4s5 = "+proj=laea +lat_0=52 +lon_0=10 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +units=m +no_defs";
		final String wkt1 = "PROJCS[\"unnamed\",GEOGCS[\"Bessel 1841\",DATUM[\"unknown\",SPHEROID[\"bessel\",6377397.155,299.1528128],TOWGS84[674.374,15.056,405.346,0,0,0,0]],PRIMEM[\"Greenwich\",0],UNIT[\"degree\",0.0174532925199433]],PROJECTION[\"Hotine_Oblique_Mercator\"],PARAMETER[\"latitude_of_center\",46.95240555555556],PARAMETER[\"longitude_of_center\",7.439583333333333],PARAMETER[\"azimuth\",90],PARAMETER[\"rectified_grid_angle\",90],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",2600000],PARAMETER[\"false_northing\",1200000],UNIT[\"Meter\",1]]";
		final String wkt2 = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9108\"]],AUTHORITY[\"EPSG\",\"4326\"]]";

		test(decodeProj4(p4s1), false);
		test(decodeProj4(p4s2), true);
		test(decodeProj4(p4s3), true);
		test(decodeProj4(p4s4), false);
		test(decodeProj4(p4s5), false);
		test(decodeWkt(wkt1), false);
		test(decodeWkt(wkt2), true);
		test(decodeEpsgCode(4326), true);
		test(decodeEpsgCode("4326"), true);
		test(decodeEpsgCode("EPSG:4326"), true);
	}

	public static int getEpsgCodeForCrs(CoordinateReferenceSystem crs) {
		for (ReferenceIdentifier id : crs.getIdentifiers()) {
			if (id.getAuthority() == Citations.EPSG) {
				return Integer.parseInt(id.getCode());
			}
		}
		return -1;
	}

	public static NcUwVariableWithDimensions findGriddedVariable(INcUwVariable v) {
		if (v == null) {
			return null;
		}
		if (!v.hasDimension(NcUwDimension.X, NcUwDimension.Y)) {
			log.debug("X and Y are not present in variable {}. Going deeper...", v.getName());
			for (INcUwVariable av : v.getAncillaryVariables()) {
				INcUwVariable found = findGriddedVariable(av);
				if (found != null) {
					log.debug("{} has X and Y...", found.getName());
					return (NcUwVariableWithDimensions) found;
				}
			}
			return null;
		} else {
			return (NcUwVariableWithDimensions) v;
		}
	}
}
