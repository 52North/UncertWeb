package org.n52.wps.io.data.binding.complex;

import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.data.IComplexData;

public class XMLAnyDataBinding implements IComplexData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3963264069410462216L;

	private XmlObject payload;
	
	public XMLAnyDataBinding(XmlObject payload){
		this.payload = payload;
	}
	
	@Override
	public Object getPayload() {
		return this.payload;
	}

	@Override
	public Class<XmlObject> getSupportedClass() {
		return XmlObject.class;
	}

}
