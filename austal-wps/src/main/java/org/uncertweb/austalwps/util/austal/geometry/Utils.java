package org.uncertweb.austalwps.util.austal.geometry;

import com.vividsolutions.jts.geom.Coordinate;

public class Utils {

	// methods to calculate Gauss-Krueger-Coordinates to local austal coordinates
	public static EmissionSource lineGK3ToLocalCoords(double gx, double gy, Coordinate[] coords){
		double x1 = coords[0].x;
		double y1 = coords[0].y;
		double x2 = coords[coords.length-1].x;	// polylines may have multiple segments, so make sure to pick the last point
		double y2 = coords[coords.length-1].y;
		EmissionSource source = new EmissionSource();
		double xq, yq, wq;
		double bq = Math.sqrt(Math.pow((x1-x2), 2)+Math.pow((y1-y2), 2)); // extension in y direction = length
		double aq=0;					// extension in x direction
		double cq=1;					// extension in z direction
		double hq=0.2;					// height

		// find point to the RIGHT which will stay fixed
		if(x1==x2){ // easiest case
			// convert to local coordinates
			xq = x1 - gx;
			yq = y1 - gy;
			wq = 0;					// angle counterclockwise
		} else if(x1>x2){
			// convert to local coordinates
			xq = x1 - gx;
			yq = y1 - gy;
			if(y1>y2)
				wq = 180 - Math.atan(Math.abs(x1-x2)/Math.abs(y1-y2))*180/Math.PI;
			else
				wq = Math.atan(Math.abs(x1-x2)/Math.abs(y1-y2))*180/Math.PI;

		}else{
			// convert to local coordinates
			xq = x2 - gx;
			yq = y2 - gy;
			if(y2>y1)
				wq = 180 - Math.atan(Math.abs(x1-x2)/Math.abs(y1-y2))*180/Math.PI;
			else
				wq = Math.atan(Math.abs(x1-x2)/Math.abs(y1-y2))*180/Math.PI;
		}

		source.setCoordinates(xq, yq);
		source.setExtent(aq, bq, cq, wq, hq);
		return source;
	}

	public static EmissionSource pointGK3ToLocalCoords(double gx, double gy, Coordinate[] coords){
		double x1 = coords[0].x;
		double y1 = coords[0].y;

		EmissionSource source = new EmissionSource();
		double xq = x1 - gx;
		double yq = y1 - gy;
		double wq = 0;
		double bq = 0; // extension in y direction = length
		double aq = 0;					// extension in x direction
		double cq = 1;					// extension in z direction
		double hq = 0.2;					// height

		source.setCoordinates(xq, yq);
		source.setExtent(aq, bq, cq, wq, hq);
		return source;
	}

	// method to calculate Gauss-Krueger-Coordinates to local austal coordinates
	private static EmissionSource cellPolygonGK3ToLocalCoords(double gx, double gy, Coordinate[] coords){
		double x1 = coords[0].x;
		double y1 = coords[0].y;
		double x2 = coords[2].x;
		double y2 = coords[2].y;

		EmissionSource source = new EmissionSource();
		double xq, yq;
		double bq = Math.abs(y1-y2);
		double aq = Math.abs(x1-x2);	// extension in x direction
		double cq=1;					// extension in z direction
		double hq=0.2;					// height
		double wq = 0;					// This is zero for our case

		// get lower left point which will stay fixed
		if(x1<x2)
			xq = x1 - gx;
		else
			xq = x2 - gx;

		if(y1<y2)
			yq = y1 - gy;
		else
			yq = y2 - gy;

		source.setCoordinates(xq, yq);
		source.setExtent(aq, bq, cq, wq, hq);
		return source;
	}

	// method to calculate Gauss-Krueger-Coordinates to local austal coordinates
	public static EmissionSource polygonGK3ToLocalCoords(double gx, double gy, Coordinate[] coords){
		EmissionSource source = null;
		// for square, not tilted polygons calculation is easier
		if((coords[0].x==coords[1].x||coords[0].y==coords[1].y)&&(coords[2].x==coords[3].x||coords[2].y==coords[3].y))	{
			source = cellPolygonGK3ToLocalCoords(gx, gy, coords);
		}else
		{
			source = new EmissionSource();
			// first sort points
			double xS=coords[0].x, yS=coords[0].y, xN=coords[0].x, yN=coords[0].y,
						xE=coords[0].x, yE=coords[0].y, xW=coords[0].x, yW=coords[0].y;
			for(Coordinate c : coords){
				if(c.y<=yS){
					xS=c.x;
					yS=c.y;
				}
				if(c.y>=yN){
					xN=c.x;
					yN=c.y;
				}
				if(c.x>=xE){
					xE=c.x;
					yE=c.y;
				}
				if(c.x<=xW){
					xW=c.x;
					yW=c.y;
				}
			}


			double xq, yq;
			double aq = Math.sqrt(Math.pow((xE-xS), 2)+Math.pow((yE-yS), 2)); 	// extension in y direction
			double bq = Math.sqrt(Math.pow((xW-xS), 2)+Math.pow((yW-yS), 2));	// extension in x direction
			double cq=1;					// extension in z direction
			double hq=0.2;					// height
			double wq = 0;					// angle

			// xS,yS is lower left point which will stay fixed
			// convert to local coordinates
			xq = xS - gx;
			yq = yS - gy;
			// xE>xN & yE<yN
			wq = Math.atan(Math.abs(xW-xS)/Math.abs(yW-yS))*180/Math.PI;

			source.setCoordinates(xq, yq);
			source.setExtent(aq, bq, cq, wq, hq);
		}
		return source;
	}

}
