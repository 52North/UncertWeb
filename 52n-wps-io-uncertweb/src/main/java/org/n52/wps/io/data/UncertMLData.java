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
	 * mimeType of the UncertMLdata
	 */
	private String mimeType;


	/**
	 * constructor with uncertainty
	 * 
	 * @param uncertainty
	 */
	public UncertMLData(IUncertainty uncertaintyp, String mimeType) {
		this.uncertainty = uncertaintyp;
		this.mimeType=mimeType;
	}


	/**
	 *
	 * @return Returns the uncertainties contained in this class
	 */
	public IUncertainty getUncertainties(){
		return uncertainty;
	}


	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}


}
