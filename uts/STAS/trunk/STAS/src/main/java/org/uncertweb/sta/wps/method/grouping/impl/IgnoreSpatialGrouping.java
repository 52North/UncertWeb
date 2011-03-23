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
package org.uncertweb.sta.wps.method.grouping.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.SamplingSurface;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.RandomStringGenerator;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.method.grouping.SpatialGrouping;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * {@link SpatialGrouping} that maps all {@link Observation}s to one single
 * {@link ISamplingFeature}, which will the transitive closure of the
 * {@code Observation}s {@code ISamplingFeature}s.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class IgnoreSpatialGrouping extends SpatialGrouping {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<ObservationMapping<ISamplingFeature>> iterator() {

		List<Observation> obs = getObservations();
		ISamplingFeature f = null;
		log.info("Calculating Convex Hull for {} Observations.", obs.size());
		switch (obs.size()) {
		case 0:
			return new LinkedList<ObservationMapping<ISamplingFeature>>()
					.iterator();
		case 1:
			f = obs.get(0).getFeatureOfInterest();
		break;
		default:
			ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
			for (Observation o : obs) {
				for (Coordinate c : o.getObservationLocation().getCoordinates()) {
					coordinates.add(c);
				}
			}
			GeometryFactory gf = new GeometryFactory();
			Geometry ch = gf
					.createMultiPoint(coordinates.toArray(new Coordinate[0]))
					.convexHull();
			String id = "foi_"
					+ RandomStringGenerator.getInstance().generate(20);
			f = new SamplingSurface(ch, Constants.NULL_URN, id, id);
		}

		return Utils
				.list(new ObservationMapping<ISamplingFeature>(
						f, obs)).iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AbstractProcessInput<?>> getAdditionalInputDeclarations() {
		return Utils.set();
	}

}
