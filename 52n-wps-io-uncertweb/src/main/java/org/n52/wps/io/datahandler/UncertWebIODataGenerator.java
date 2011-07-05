package org.n52.wps.io.datahandler;

import java.io.OutputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.NetCDFData;
import org.n52.wps.io.data.OMData;
import org.n52.wps.io.data.UncertMLData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.NetCDFDataBinding;
import org.n52.wps.io.data.binding.complex.OMDataBinding;
import org.n52.wps.io.data.binding.complex.UncertMLDataBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.datahandler.binary.NetCDFGenerator;
import org.n52.wps.io.datahandler.xml.OMXmlGenerator;
import org.n52.wps.io.datahandler.xml.UncertMLXmlGenerator;

public class UncertWebIODataGenerator extends AbstractUncertWebDataGenerator{

	@Override
	public OutputStream generate(IData uwData) {
		OutputStream os=null;
		Object data = ((UncertWebIODataBinding)uwData).getPayload();
		if (data instanceof NetCDFData){
			NetCDFGenerator gen= new NetCDFGenerator();
			os=gen.generate(new NetCDFDataBinding((NetCDFData)data));
		}
		else if (data instanceof UncertMLData){
			if (((UncertMLData)data).getMimeType().equals(UncertWebDataConstants.MIME_TYPE_UNCERTML)){
			UncertMLXmlGenerator gen= new UncertMLXmlGenerator();
			os=gen.generate(new UncertMLDataBinding((UncertMLData)data));
			}
			else {
				//TODO add JSON encoding
			}
		}
		else if (data instanceof OMData){
			if (((OMData)data).getMimeType().equals(UncertWebDataConstants.MIME_TYPE_OMX)){
			OMXmlGenerator gen= new OMXmlGenerator();
			os=gen.generate(new OMDataBinding((OMData)data));
			}
			else {
				//TODO add JSON encoding
			}
		}
		return os;
	}

	@Override
	public Class<?>[] getSupportedInternalInputDataType() {
		Class<?>[] supportedClasses = {UncertWebIODataBinding.class};
		return supportedClasses;
	}

	@Override
	public void writeToStream(IData uwData, OutputStream os) {
		Object data = ((UncertWebIODataBinding)uwData).getPayload();
		if (data instanceof NetCDFDataBinding){
			NetCDFGenerator gen= new NetCDFGenerator();
			gen.writeToStream((NetCDFDataBinding)data,os);
		}
		else if (data instanceof UncertMLDataBinding){
			if (((UncertMLData)data).getMimeType().equals(UncertWebDataConstants.MIME_TYPE_UNCERTML)){
			UncertMLXmlGenerator gen= new UncertMLXmlGenerator();
			gen.writeToStream((UncertMLDataBinding)data,os);
			}
			else {
				//TODO add JSON encoding
			}
		}
		else if (data instanceof OMDataBinding){
			if (((OMData)data).getMimeType().equals(UncertWebDataConstants.MIME_TYPE_OMX)){
			OMXmlGenerator gen= new OMXmlGenerator();
			gen.writeToStream((OMDataBinding)data,os);
			}
			else {
				//TODO add JSON encoding
			}
		}
	}

}
