package org.n52.wps.io.data.binding.complex;

import org.n52.wps.io.data.IComplexData;
import org.uncertweb.UncertainInputType;

public class UncertainInputDataBinding implements IComplexData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7976664155637161333L;

	private UncertainInputType payload;
	
	public UncertainInputDataBinding(UncertainInputType payload){
		this.payload = payload;
	}
	
	@Override
	public UncertainInputType getPayload() {		
		return payload;
	}

	@Override
	public Class<?> getSupportedClass() {
		return UncertainInputType.class;
	}

}
