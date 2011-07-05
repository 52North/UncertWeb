package org.n52.wps.io.data.binding.complex;

import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.OMData;

/**
 * Binding for {@link OMData}
 * @author Kiesow
 *
 */
public class OMDataBinding  implements IComplexData {

	private static final long serialVersionUID = -3033594002991918048L;
	private OMData payload;
	
	/**
	 * Constructor
	 * @param payload
	 */
	public OMDataBinding(OMData payload){
		this.payload = payload;
	}
	
	@Override
	public Object getPayload() {
		// TODO Auto-generated method stub
		return payload;
	}

	@Override
	public Class<?> getSupportedClass() {
		// TODO Auto-generated method stub
		return OMData.class;
	}

}

