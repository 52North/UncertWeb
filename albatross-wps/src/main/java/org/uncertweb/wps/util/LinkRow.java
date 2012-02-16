/**
 * 
 */
package org.uncertweb.wps.util;

/**
 * @author s_voss13
 *
 */
public class LinkRow {
	
	private String link;
	private float mean;
	private float sd;
	
	public LinkRow(String link, float mean, float sd ) {
		
		this.link = link;
		this.mean = mean;
		this.sd = sd;
	}
	
	/**
	 * The string representation looks like: link_345 1.9 0.15 
	 */
	@Override
	public String toString() {
		
		return link + " " + mean + " " +sd;
	}

}
