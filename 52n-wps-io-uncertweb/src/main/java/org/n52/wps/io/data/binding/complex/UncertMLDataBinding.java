package org.n52.wps.io.data.binding.complex;

import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.UncertMLData;

/**
 * data binding for uncertainties encoded as UncertML
 * 
 * @author staschc
 *
 */
public class UncertMLDataBinding implements IComplexData{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * payload is UncertML uncertainties
	 * 
	 */
	private UncertMLData payload;
	
	/**
	 * constructor
	 * 
	 * @param data
	 */
	public UncertMLDataBinding(UncertMLData data){
		this.payload = data;
	}

	@Override
	public Object getPayload() {
		return payload;
	}

	@Override
	public Class<?> getSupportedClass() {
		return UncertMLData.class;
	}

}
