package org.n52.wps.io.data.binding.complex;

import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.OandMData;

public class OandMDataBinding  implements IComplexData {

	private OandMData payload;
	
	
	@Override
	public Object getPayload() {
		// TODO Auto-generated method stub
		return payload;
	}

	@Override
	public Class getSupportedClass() {
		// TODO Auto-generated method stub
		return OandMData.class;
	}

}
