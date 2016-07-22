package org.uncertweb.api.gml.geometry;

import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceComparator;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.Point;

/**
 * Rectified Grid of UncertWeb GML profile; extends JTS geometry;
 * 
 * A RectifiedGrid is a grid for which there is an affine transformation between
 * the grid coordinates and the coordinates of an external coordinate reference
 * system.
 * 
 * It contains several elements:
 * 
 * -limits define the extent of the grid
 * 
 * <gml:limits> <gml:GridEnvelope> <gml:low>1 1</gml:low> <gml:high>5
 * 5</gml:high> </gml:GridEnvelope> </gml:limits>
 * 
 * 
 * -axis labels define the labels of the axis of the grid
 * 
 * <gml:axisLabels>u v</gml:axisLabels>
 * 
 * -origin defines a georeferenced point which is the origin for the affine
 * transformation
 * 
 * <gml:origin> <gml:Point gml:id="IfGI"
 * srsName="urn:x-ogc:def:crs:EPSG:6.6:4326"> <gml:pos>52.77 7.82</gml:pos>
 * </gml:Point> </gml:origin>
 * 
 * 
 * -offset vectors describe the transformation from the grid to the
 * georeferenced grid in consuming applications (e.g. for visualization on the
 * earth's surface)
 * 
 * <gml:offsetVector srsName="urn:x-ogc:def:crs:EPSG:6.6:4329">-0.3
 * 1.25</gml:offsetVector> <gml:offsetVector
 * srsName="urn:x-ogc:def:crs:EPSG:6.6:4329">1.3 0.25</gml:offsetVector>
 * 
 * @author staschc
 * 
 */
public class RectifiedGrid extends Geometry {

	/**
	 * auto-generated serial ID
	 */
	private static final long serialVersionUID = 1L;

	/** GML id of grid */
	private String gmlId;

	/**
	 * envelope of the rectified grid; has to consist of integers indicating the
	 * numbers of rows and columns in the grid
	 */
	private Envelope gridEnvelope;

	/**
	 * labels or names of the axis in the grid; BOTH are kept as same in this implementation
	 */
	private List<String> axisLabels;

	/**
	 * georeferenced origin of the grid
	 * 
	 */
	private Point origin;

	/**
	 * offset vectors of the grid; are currently implemented as JTS points
	 */
	private Collection<Point> offsetVectors;

	/**
	 * constructor
	 * 
	 * @param factory
	 */
	public RectifiedGrid(Envelope gridEnvelope, List<String> axisLabel,
			Point origin, Collection<Point> offsetVectors,
			GeometryFactory factory) {
		super(factory);
		this.gridEnvelope=gridEnvelope;
		this.axisLabels=axisLabel;
		this.origin = origin;
		this.offsetVectors=offsetVectors;
	}

	// ////////////////////////////////////////////////////////////
	// getters and setters
	public Envelope getGridEnvelope() {
		return gridEnvelope;
	}

	public void setGridEnvelope(Envelope gridEnvelope) {
		this.gridEnvelope = gridEnvelope;
	}

	public List<String> getAxisLabels() {
		return axisLabels;
	}

	public void setAxisLabels(List<String> axisLabels) {
		this.axisLabels = axisLabels;
	}

	public Point getOrigin() {
		return origin;
	}

	public void setOrigin(Point origin) {
		this.origin = origin;
	}

	public Collection<Point> getOffsetVectors() {
		return offsetVectors;
	}

	public void setOffsetVectors(Collection<Point> offsetVectors) {
		this.offsetVectors = offsetVectors;
	}

	// /////////////////////////////////////////////////////////////////////
	// methods inherited from JTS Geometry
	public void apply(CoordinateFilter arg0) {
		// TODO Auto-generated method stub

	}

	public void apply(CoordinateSequenceFilter arg0) {
		// TODO Auto-generated method stub

	}

	public void apply(GeometryFilter arg0) {
		// TODO Auto-generated method stub

	}

	public void apply(GeometryComponentFilter arg0) {
		// TODO Auto-generated method stub

	}

	protected int compareToSameClass(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	protected int compareToSameClass(Object arg0,
			CoordinateSequenceComparator arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	protected Envelope computeEnvelopeInternal() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean equalsExact(Geometry arg0, double arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public Geometry getBoundary() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getBoundaryDimension() {
		return 2;
	}

	public Coordinate getCoordinate() {
		// TODO Auto-generated method stub
		return null;
	}

	public Coordinate[] getCoordinates() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getDimension() {
		return 2;
	}

	public String getGeometryType() {
		return "RectifiedGrid";
	}

	public int getNumPoints() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public void normalize() {
		// TODO Auto-generated method stub

	}

	public String getGmlId() {
		return gmlId;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	@Override
	public Geometry reverse() {
		// TODO Auto-generated method stub
		return null;
	}

}
