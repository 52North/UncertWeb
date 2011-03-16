package org.n52.wps.io.data.binding.complex;

import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.UncertWebData;

public class UncertWebDataBinding implements IComplexData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6716174765886575110L;
	
	public UncertWebData payload;
	
	public UncertWebDataBinding(UncertWebData payload){
		this.payload = payload;
	}
	
	@Override
	public UncertWebData getPayload() {
		return payload;
	}

	@Override
	public Class<?> getSupportedClass() {
		return UncertWebData.class;
	}

}
