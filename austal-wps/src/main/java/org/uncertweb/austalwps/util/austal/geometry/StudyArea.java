package org.uncertweb.austalwps.util.austal.geometry;

public class StudyArea {

	/**
	 * Class to store details about the study area
	 */
	private int gx;	// x coordinate of the study area root in GK3
	private int gy;	// y coordinate of the study area root in GK3
	private int dd;	// cell size
	private int nx;	// number of cells in x direction
	private int ny;	// number of cells in y direction
	private int x0;	// x coordinate of the lower left corner in local coordinates
	private int y0;	// y coordinate of the lower left corner in local coordinates
	
	public StudyArea(){}
	
	// function to calculate GK3 into local coordinates
	public int[] gk3toLocalCoordinates(int[] gk3Coords){
		int[] localCoords = new int[2];
		localCoords[0] = gx - gk3Coords[0];
		localCoords[1] = gy - gk3Coords[1];
		return(localCoords);
	}
	
	// function to calculate local into GK3 coordinates
	public int[] localToGK3Coordinates(int[] localCoords){
		int[] gk3Coords = new int[2];
		gk3Coords[0] = localCoords[0] + gx;
		gk3Coords[1] = localCoords[1] + gy;
		return(gk3Coords);
	}
	
	// *** Getters and Setters ***
	public int getGx() {
		return gx;
	}

	public void setGx(int gx) {
		this.gx = gx;
	}
	
	public int getGy() {
		return gy;
	}

	public void setGy(int gy) {
		this.gy = gy;
	}
	
	public int getDd() {
		return dd;
	}

	public void setDd(int dd) {
		this.dd = dd;
	}
	
	public int getNx() {
		return nx;
	}

	public void setNx(int nx) {
		this.nx = nx;
	}
	
	public int getNy() {
		return ny;
	}

	public void setNy(int ny) {
		this.ny = ny;
	}
	
	public int getX0() {
		return x0;
	}

	public void setX0(int x0) {
		this.x0 = x0;
	}
	
	public int getY0() {
		return y0;
	}

	public void setY0(int y0) {
		this.y0 = y0;
	}
}
