package org.n52.wps.io.data;

import java.util.ArrayList;
import java.util.List;

import org.uncertml.IUncertainty;

/**
 * wrapper for uncertainty encoded in UncertML
 * 
 * @author staschc
 *
 */
public class UncertMLData {
	

	/**
	 * List containing the uncertainties
	 */
	private IUncertainty uncertainty;


	/**
	 * constructor with uncertainty
	 * 
	 * @param uncertainty
	 */
	public UncertMLData(IUncertainty uncertaintyp) {
		this.uncertainty = uncertaintyp;
	}


	/**
	 *
	 * @return Returns the uncertainties contained in this class
	 */
	public IUncertainty getUncertainties(){
		return uncertainty;
	}


}
