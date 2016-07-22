package org.uncertml.statistics;

/**
 * represents an anisotropy parameter of a variogram
 *
 * @author staschc
 *
 */
public class Anisotropy {

	/** principal direction of anisotropy in degrees from y-axis*/
	private double principalDirection;

	/**ratio between ranges at principal direction and perpendicular direction*/
	private double ratio;

	/**
	 * constructor
	 *
	 * @param principalDirection
	 * 			principal direction of anisotropy in degrees from y-axis
	 * @param ratio
	 * 			ratio between ranges at principal direction and perpendicular direction
	 */
	public Anisotropy(double principalDirection,double ratio){
		this.principalDirection=principalDirection;
		this.ratio=ratio;
	}

	/**
	 * @return the principalDirection
	 */
	public double getPrincipalDirection() {
		return principalDirection;
	}

	/**
	 * @return the ratio
	 */
	public double getRatio() {
		return ratio;
	}
}
