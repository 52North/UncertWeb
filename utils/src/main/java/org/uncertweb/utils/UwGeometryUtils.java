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
package org.uncertweb.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

public class UwGeometryUtils extends UwUtils {

	protected static final List<Range> switchedEPSG = new LinkedList<Range>();

	protected static final String[] SRS_TO_SWITCH = { "2044-2045", "2081-2083",
			"2085-2086", "2093", "2096-2098", "2105-2132", "2169-2170",
			"2176-2180", "2193", "2200", "2206-2212", "2319", "2320-2462",
			"2523-2549", "2551-2735", "2738-2758", "2935-2941", "2953",
			"3006-3030", "3034-3035", "3058-3059", "3068", "3114-3118",
			"3126-3138", "3300-3301", "3328-3335", "3346", "3350-3352", "3366",
			"3416", "4001-4999", "20004-20032", "20064-20092", "21413-21423",
			"21473-21483", "21896-21899", "22171", "22181-22187",
			"22191-22197", "25884", "27205-27232", "27391-27398", "27492",
			"28402-28432", "28462-28492", "30161-30179", "30800",
			"31251-31259", "31275-31279", "31281-31290", "31466-31700" };

	static {
		parseEpsgToSwitch();
	}

	private static void parseEpsgToSwitch() {
		for (String entry : SRS_TO_SWITCH) {
			String[] splitted = entry.split("-");
			Range r = null;
			switch (splitted.length) {
			case 1:
				r = new Range(Integer.parseInt(splitted[0]));
				break;
			case 2:
				r = new Range(Integer.parseInt(splitted[0]),
						Integer.parseInt(splitted[1]));
				break;
			default:
				throw new RuntimeException(
						"Invalid format of entry in 'switchCoordinatesForEPSG': "
								+ entry);
			}
			switchedEPSG.add(r);
		}
	}

	public static boolean switchCoordinatesForEPSG(Geometry geom) {
		return switchCoordinatesForEPSG(geom.getSRID());
	}

	public static boolean switchCoordinatesForEPSG(int crs) {
		for (Range r : switchedEPSG)
			if (r.contains(crs))
				return true;
		return false;
	}

	public static Geometry switchCoordinate4Geometry(Geometry sourceGeom) {

		GeometryFactory geomFac = new GeometryFactory();
		Geometry switchedGeom = null;

		if (sourceGeom instanceof MultiPolygon) {
			Polygon[] switchedPolygons = new Polygon[sourceGeom
					.getNumGeometries()];
			for (int i = 0; i < sourceGeom.getNumGeometries(); i++) {
				switchedPolygons[i] = (Polygon) switchCoordinate4Geometry(sourceGeom
						.getGeometryN(i));
			}
			switchedGeom = geomFac.createMultiPolygon(switchedPolygons);
		} else {

			// switch coordinates
			Coordinate[] coordArraySource = sourceGeom.getCoordinates();
			List<Coordinate> coordList = new ArrayList<Coordinate>();
			for (Coordinate coordinate : coordArraySource) {
				if (Double.doubleToLongBits(coordinate.z) == Double
						.doubleToLongBits(Double.NaN)) {
					coordList.add(new Coordinate(coordinate.y, coordinate.x,
							coordinate.z));
				} else {
					coordList.add(new Coordinate(coordinate.y, coordinate.x));
				}
			}
			Coordinate[] coordArraySwitched = coordList
					.toArray(coordArraySource);
			CoordinateArraySequence coordSeqArray = new CoordinateArraySequence(
					coordArraySwitched);

			// create new geometry with switched coordinates.
			if (sourceGeom instanceof Point) {
				Point point = new Point(coordSeqArray, geomFac);
				switchedGeom = point;
			} else if (sourceGeom instanceof LineString) {
				LineString line = new LineString(coordSeqArray, geomFac);
				switchedGeom = line;
			} else if (sourceGeom instanceof Polygon) {
				Polygon polygon = new Polygon(new LinearRing(coordSeqArray,
						geomFac), null, geomFac);
				switchedGeom = polygon;
			} else if (sourceGeom instanceof MultiPoint) {
				MultiPoint multiPoint = geomFac
						.createMultiPoint(coordArraySource);
				switchedGeom = multiPoint;
			}
		}
		if (switchedGeom != null) {
			switchedGeom.setSRID(sourceGeom.getSRID());
		}

		return switchedGeom;
	}
}
