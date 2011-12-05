package org.n52.sos.uncertainty.decode.impl;

import org.uncertml.statistic.Mean;

/**
 * helper class to attach a uncertainty ID to identify different mean values
 * belonging to a single uncertainty
 */
public class CMean extends Mean {

	public final String uncertaintyID;

	public CMean(String uncID, double value) {
		super(value);
		this.uncertaintyID = uncID;
	}
}