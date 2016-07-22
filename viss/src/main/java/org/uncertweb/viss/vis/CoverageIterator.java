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
package org.uncertweb.viss.vis;

import java.awt.geom.Point2D;
import java.net.URI;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.InvalidGridGeometryException;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.uncertml.IUncertainty;
import org.uncertweb.netcdf.NcUwObservation;
import org.uncertweb.netcdf.NcUwUriParser;
import org.uncertweb.utils.MultiDimensionalIterator;
import org.uncertweb.utils.MultivaluedHashMap;
import org.uncertweb.utils.MultivaluedMap;
import org.uncertweb.viss.core.VissError;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class CoverageIterator extends MultiDimensionalIterator<NcUwObservation> {
	private final GeometryFactory f = new GeometryFactory();
	private final Point2D temp = new Point2D.Double();
	private final GridGeometry2D gridGeometry;
	private final MathTransform2D transformation;
	private final URI mainUri;
	private final MultivaluedMap<URI, Object> uriMap;
	private final GridCoverage2D coverage;
	private double[] sampleBuffer;

	public CoverageIterator(GridCoverage2D coverage, URI main,
			MultivaluedMap<URI, Object> additionalUris) {
		super(new int[] { coverage.getGridGeometry().getGridRange2D().width,
				coverage.getGridGeometry().getGridRange2D().height });
		this.coverage = coverage;
		this.mainUri = main;
		this.uriMap = (additionalUris != null) ? additionalUris
				: new MultivaluedHashMap<URI, Object>();
		this.gridGeometry = this.coverage.getGridGeometry();
		this.sampleBuffer = new double[this.coverage.getSampleDimensions().length];
		this.transformation = this.gridGeometry.getGridToCRS2D();
	}

	@Override
	protected NcUwObservation value(int[] index) {
		try {
			GridCoordinates2D gp = new GridCoordinates2D(index[0], index[1]);
			for (double d : coverage.evaluate(gp, sampleBuffer)) {
                uriMap.add(mainUri, d);
            }
			IUncertainty u = NcUwUriParser.parse(mainUri, uriMap);
			transformation.transform(gp, temp);
			Point p = f.createPoint(new Coordinate(temp.getY(), temp.getX()));
			return new NcUwObservation(null, null, null, p, gp, u);
		} catch (InvalidGridGeometryException e) {
			throw VissError.internal(e);
		} catch (TransformException e) {
			throw VissError.internal(e);
		}
	}

}