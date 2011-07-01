package org.n52.wps.io.data.binding.complex;

import org.n52.wps.io.data.IComplexData;
import org.uncertweb.StaticInputType;

public class StaticInputDataBinding implements IComplexData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6744995419757903953L;

	private StaticInputType payload;
	
	public StaticInputDataBinding(StaticInputType payload){
		this.payload = payload;
	}
	
	@Override
	public StaticInputType getPayload() {
		return payload;
	}

	@Override
	public Class<?> getSupportedClass() {
		return StaticInputType.class;
	}

}
