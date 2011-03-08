package org.uncertweb.api.gml.geometry;

import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * factory providing easy creation methods for GmlGeometry Objects
 * 
 * @author staschc
 *
 */
public class GmlGeometryFactory {

	private GeometryFactory geomFac;
	private CoordinateSequenceFactory coordSeqFac;
	
	public GmlGeometryFactory(){
		this.geomFac = new GeometryFactory();
		this. coordSeqFac = this.geomFac.getCoordinateSequenceFactory();
	}
	
	public GmlPoint createGmlPoint(double x, double y, int epsgCode){
		Coordinate[] coords = new Coordinate[1];
		coords[0] = new Coordinate(x,y);
		CoordinateSequence coordSequence = coordSeqFac.create(coords);
		GmlPoint point = new GmlPoint(coordSequence, new GeometryFactory());
		point.setSRID(epsgCode);
		return point;
	}
	
	public GmlPolygon createGmlPolygon(Coordinate[] boundary, List<Coordinate[]> holes, int epsgCode){
		LinearRing lrBoundary = geomFac.createLinearRing(boundary);
		LinearRing[] lrHoles = null;
		if (holes!=null){
			lrHoles = new LinearRing[holes.size()];
			for (int i=0;i<holes.size();i++){
				Coordinate[] hole = holes.get(i);
				lrHoles[i]=geomFac.createLinearRing(hole);
			}
		}
		GmlPolygon polygon = new GmlPolygon(lrBoundary,lrHoles,new GeometryFactory(),"");
		polygon.setSRID(epsgCode);
		return polygon;
	}
	
}
