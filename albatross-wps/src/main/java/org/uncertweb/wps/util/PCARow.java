/**
 * 
 */
package org.uncertweb.wps.util;

/**
 * @author s_voss13
 *
 */
public class PCARow {
	
	private String PCA4;
	private String activityType;
	private float mean;
	private float sd;
	
	public PCARow(String pCA4, String activityType, float mean, float sd) {
		
		PCA4 = pCA4;
		this.activityType = activityType;
		this.mean = mean;
		this.sd = sd;
	}


	@Override
	public String toString() {

		return PCA4 + " " + activityType + " " + mean + " " + sd;
	}
}
