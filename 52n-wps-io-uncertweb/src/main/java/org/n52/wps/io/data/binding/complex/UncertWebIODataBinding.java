package org.n52.wps.io.data.binding.complex;

import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.UncertWebIOData;

/**
 * Binding for {@link UncertWebIOData}
 * 
 * @author staschc
 *
 */
public class UncertWebIODataBinding implements IComplexData{
	
	/**
	 *  serial id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * payload
	 */
	private UncertWebIOData payload;
	
	/**
	 * constructor
	 * 
	 * @param payload
	 * 			payload
	 */
	public UncertWebIODataBinding(UncertWebIOData payload){
		this.payload=payload;
	}


	@Override
	public Object getPayload() {
		return payload;
	}

	@Override
	public Class<?> getSupportedClass() {
		return UncertWebIOData.class;
	}

}
