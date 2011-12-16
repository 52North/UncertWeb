package org.uncertweb.austalwps.util.austal.geometry;

public class Utils {
	
	// methods to calculate Gauss-Krueger-Coordinates to local austal coordinates
	public static EmissionSource lineGK3ToLocalCoords(double gx, double gy, double x1, double y1, double x2, double y2){
		
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
	
	// method to calculate Gauss-Krueger-Coordinates to local austal coordinates
	public static EmissionSource cellPolygonGK3ToLocalCoords(double gx, double gy, double x1, double y1, double x2, double y2){
			
			EmissionSource source = new EmissionSource();		
			double xq, yq;	
			double bq = Math.abs(y1-y2); 	// extension in y direction
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

}