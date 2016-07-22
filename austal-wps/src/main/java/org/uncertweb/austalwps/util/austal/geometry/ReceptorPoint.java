package org.uncertweb.austalwps.util.austal.geometry;

import java.io.Serializable;

public class ReceptorPoint implements Serializable{
	/**
	 * Class to store coordinates of the receptor points for Austal
	 */
	
	private static final long serialVersionUID = -7532798837815788388L;
	private int xp; // x coordinate of the point
	private int yp; // y coordinate of the point
	private int hp; // height of the point
	
	public ReceptorPoint(){}
	
	public ReceptorPoint(String xp, String yp, String hp) {
		this.xp = Integer.parseInt(xp);
		this.yp = Integer.parseInt(yp);
		this.hp = Integer.parseInt(hp);
	}

	// *** Getters and Setters ***
	public int getXp() {
		return xp;
	}

	public void setXp(int xp) {
		this.xp = xp;
	}
	
	public void setXp(String xp) {
		this.xp = Integer.parseInt(xp);
	}

	public int getYp() {
		return yp;
	}

	public void setYp(int yp) {
		this.yp = yp;
	}

	public void setYp(String yp) {
		this.yp = Integer.parseInt(yp);
	}
	
	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}
	
	public void setHp(String hp) {
		this.hp = Integer.parseInt(hp);
	}
	
	public ReceptorPoint getCopy() {
		ReceptorPoint p = new ReceptorPoint();
		p.hp = this.hp;
		p.xp = this.xp;
		p.yp = this.yp;
		return p;
	}
	
	
}
