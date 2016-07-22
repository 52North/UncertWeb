package org.uncertweb.wps.io.data.binding.complex;

import org.n52.wps.io.data.IComplexData;

public class AlbatrossUInputBinding implements IComplexData{

	private AlbatrossUInput payload;

	public AlbatrossUInputBinding(AlbatrossUInput payload){
		this.payload = payload;
	}

	@Override
	public Object getPayload() {
		return payload;
	}

	@Override
	public Class<AlbatrossUInput> getSupportedClass() {
		return AlbatrossUInput.class;
	}

}
