package org.uncertweb.viss.core.vis;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;
import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.uncertweb.viss.core.VissError;

public class WriteableGridCoverage {

	private static final int MAX_PENDING_VALUES = 10000;

	private class PendingValue {
		private Point2D.Double pos;
		private Number value;

		public PendingValue(Point2D.Double pos, Number value) {
			this.pos = pos;
			this.value = value;
		}
	}

	private List<PendingValue> pendingValues = new ArrayList<PendingValue>();
	private GridCoverage2D gridCov;
	private MathTransform2D worldToGrid = null;
	
	public WriteableGridCoverage(int missingValue, GridCoverage2D gridCov) {
		this.gridCov = gridCov;
		this.missingValue = missingValue;
	}

	public GridCoverage2D getGridCoverage() {
		flushCache(true);
		return gridCov;
	}

	public void setValueAtPos(Point2D pos, int value) {
		setValueAtPos(pos, Integer.valueOf(value));
	}

	public void setValueAtPos(Point2D pos, float value) {
		setValueAtPos(pos, Float.valueOf(value));
	}

	public void setValueAtPos(Point2D pos, double value) {
		setValueAtPos(pos, Double.valueOf(value));
	}

	public void setValueAtPos(Point2D pos, Number value) {
		pendingValues.add(new PendingValue(new Point2D.Double(pos.getX(), pos
				.getY()), value));
		flushCache(false);
	}

	private void flushCache(boolean force) {
		if (pendingValues.size() >= MAX_PENDING_VALUES
				|| (force && pendingValues.size() > 0)) {
			if (worldToGrid == null)
				try {
					worldToGrid = gridCov.getGridGeometry().getGridToCRS2D()
							.inverse();
				} catch (NoninvertibleTransformException e) {
					throw VissError
							.internal("Could not create geographic to grid coords transform");
				}
			WritableRandomIter writeIter = RandomIterFactory.createWritable(
					new TiledImage(gridCov.getRenderedImage(), true), null);
			final Point2D.Double gridPos = new Point2D.Double();
			for (PendingValue pv : pendingValues) {
				try {
					worldToGrid.transform(pv.pos, gridPos);
					if (pv.value == null) {
						writeIter.setSample((int) gridPos.x, (int) gridPos.y, 0, getMissingValue());
					} else {
						writeIter.setSample((int) gridPos.x, (int) gridPos.y, 0, pv.value.doubleValue());
					}
				} catch (TransformException e) {
					throw VissError.internal("Could not transform location ["
							+ pv.pos + "] to grid coords");
				}
			}
			pendingValues.clear();
		}
	}
	private int missingValue;
	public int getMissingValue() {
		return this.missingValue;
	}
}
