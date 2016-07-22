package org.n52.wps.io.data.binding.complex;

import org.uncertml.IUncertainty;

/**
 * data binding for uncertainties encoded as UncertML
 * 
 * @author staschc
 * 
 */
public class UncertMLBinding extends UncertWebIODataBinding {
	private static final long serialVersionUID = -4919512307491127757L;

	/**
	 * payload is UncertML uncertainties
	 */
	private IUncertainty payload;

	/**
	 * constructor
	 * 
	 * @param data
	 */
	public UncertMLBinding(IUncertainty data) {
		this.payload = data;
	}

	@Override
	public IUncertainty getPayload() {
		return payload;
	}

	@Override
	public Class<?> getSupportedClass() {
		return IUncertainty.class;
	}

}
