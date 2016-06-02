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
package org.uncertweb.netcdf.util;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.ViewType;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.uncertweb.netcdf.NcUwException;

public class WriteableGridCoverage {

	private static final int MAX_PENDING_VALUES = 10000;

	private class PendingValue {
		private Point2D pos;
		private Number value;

		public PendingValue(Point2D pos, Number value) {
			this.pos = pos;
			this.value = value;
		}
	}

	private List<PendingValue> pendingValues = new ArrayList<PendingValue>();
	private List<PendingValue> pendingGridValues = new ArrayList<PendingValue>();
	private GridCoverage2D gridCov;
	private MathTransform2D worldToGrid = null;

	public WriteableGridCoverage(GridCoverage2D gridCov) {
		this.gridCov = gridCov.view(ViewType.GEOPHYSICS);
	}

	public GridCoverage2D getGridCoverage() {
		flushCache(true);
		return gridCov;
	}

	public void setValueAtPos(Point2D pos, Number value) {
		pendingValues.add(new PendingValue(new Point2D.Double(pos.getX(), pos
		    .getY()), value));
		flushCache(false);
	}

	public void setValueAtGridPos(int x, int y, Number value) {
		pendingGridValues.add(new PendingValue(new Point(x,y), value));
		flushCache(false);
	}

	private void flushCache(boolean force) {
		int pending = pendingValues.size(), pendingGrid = pendingGridValues.size();
		if (pending + pendingGrid >= MAX_PENDING_VALUES || force) {
			if (pending + pendingGrid > 0) {
				WritableRandomIter writeIter = RandomIterFactory.createWritable(new TiledImage(gridCov.getRenderedImage(), true), null);
				if (pending > 0) {
					if (worldToGrid == null) {
						try {
							worldToGrid = gridCov.getGridGeometry().getGridToCRS2D().inverse();
						} catch (NoninvertibleTransformException e) {
							throw new NcUwException("Could not create geographic to grid coords transform");
						}
					}
					final Point2D.Double gridPos = new Point2D.Double();
					for (PendingValue pv : pendingValues) {
						try {
							worldToGrid.transform(pv.pos, gridPos);
							writeIter.setSample((int) gridPos.x, (int) gridPos.y, 0, pv.value == null ? Double.NaN : pv.value.doubleValue());
						} catch (TransformException e) {
							throw new NcUwException("Could not transform location [" + pv.pos + "] to grid coords");
						}
					}
					pendingValues.clear();
				}
				if (pendingGrid > 0) {
					for (PendingValue pv : pendingGridValues) {
						writeIter.setSample((int) pv.pos.getX(), (int) pv.pos.getY(), 0, pv.value == null ? Double.NaN : pv.value.doubleValue());
					}
					pendingGridValues.clear();
				}
			}
		}
	}
}
