package org.uncertweb.api.gml.geometry;

import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * factory providing easy creation methods for GmlGeometry Objects
 * 
 * @author staschc
 *
 */
public class GmlGeometryFactory {

	/**
	 * geometry factory used for creating JTS geometries
	 */
	private GeometryFactory geomFac;
	
	/**
	 * coordinate sequence factory used to create JTS coordinate sequences
	 */
	private CoordinateSequenceFactory coordSeqFac;
	
	/**
	 * constructor initializes factories
	 * 
	 */
	public GmlGeometryFactory(){
		this.geomFac = new GeometryFactory();
		this. coordSeqFac = this.geomFac.getCoordinateSequenceFactory();
	}
	
	/**
	 * Factory method for creating JTS Points
	 * 
	 * @param x 
	 * 			x coordinate of point
	 * @param y
	 * 			y coordinate of point
	 * @param epsgCode
	 * 			epsgCode of spatial reference system
	 * @return Returns JTS point
	 */
	public Point createPoint(double x, double y, int epsgCode){
		Coordinate[] coords = new Coordinate[1];
		coords[0] = new Coordinate(x,y);
		CoordinateSequence coordSequence = coordSeqFac.create(coords);
		Point point = new GeometryFactory().createPoint(coordSequence);
		point.setSRID(epsgCode);
		return point;
	}
	
	/**
	 * factory method for creating JTS polygon
	 * 
	 * @param boundary
	 * 			exterior boundary of polygon
	 * @param holes
	 * 			interior boundaries of polygon; can also be null, if no interior boundaries are contained in polygon
	 * @param epsgCode
	 * 			code of spatial reference system
	 * @return Returns JTS polygon
	 */
	public Polygon createPolygon(Coordinate[] boundary, List<Coordinate[]> holes, int epsgCode){
		LinearRing lrBoundary = geomFac.createLinearRing(boundary);
		LinearRing[] lrHoles = null;
		if (holes!=null){
			lrHoles = new LinearRing[holes.size()];
			for (int i=0;i<holes.size();i++){
				Coordinate[] hole = holes.get(i);
				lrHoles[i]=geomFac.createLinearRing(hole);
			}
		}
		Polygon polygon = new Polygon(lrBoundary,lrHoles,new GeometryFactory());
		polygon.setSRID(epsgCode);
		return polygon;
	}
	
	/**
	 * factory method for creating JTS line string
	 * 
	 * @param coordinateSequence
	 * 			set of Coordinates representing the anchor points defining the line string
	 * @param srid
	 * 			code of the spatial reference system
	 * @return Returns JTS line string
	 */
	public LineString createLineString(Coordinate[] coordinateSequence, int srid){
		LineString lineString = geomFac.createLineString(coordSeqFac.create(coordinateSequence));
		lineString.setSRID(srid);
		return lineString;
	}
	
	/**
	 * factory method for creating a grid
	 * 
	 * @param gridEnvelope
	 * 			envelope of the grid
	 * @param axisLabel
	 * 			labels of the axis
	 * @param origin
	 * 			origin of the grid
	 * @param offsetVectors
	 * 			offset vectors of the grid
	 * @return Returns the rectified grid
	 */
	public RectifiedGrid createRectifiedGrid(Envelope gridEnvelope, List<String> axisLabel,
			Point origin, Collection<Point> offsetVectors){
		RectifiedGrid grid = new RectifiedGrid(gridEnvelope,axisLabel,origin,offsetVectors,new GeometryFactory());
		return grid;
	}
	
	public MultiPoint createMultiPoint(Point[] points, int srid){
		MultiPoint mp = geomFac.createMultiPoint(points);
		mp.setSRID(srid);
		return mp;
	}
	
	public MultiPolygon createMultiPolygon(Polygon[] polygons, int srid){
		MultiPolygon mp = geomFac.createMultiPolygon(polygons);
		mp.setSRID(srid);
		return mp;
	}
	
	public MultiLineString createMultiLineString(LineString[] lineStrings, int srid){
		MultiLineString mls = geomFac.createMultiLineString(lineStrings);
		mls.setSRID(srid);
		return mls;
	}
}
