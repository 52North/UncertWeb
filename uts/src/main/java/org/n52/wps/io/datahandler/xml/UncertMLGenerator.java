package org.n52.wps.io.datahandler.xml;

import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.uncertml.exception.UncertaintyEncoderException;
import org.uncertml.exception.UnsupportedUncertaintyTypeException;
import org.uncertml.io.XMLEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class UncertMLGenerator extends AbstractXMLGenerator{

	XMLEncoder encoder = new XMLEncoder();
	
	@Override
	public OutputStream generate(IData arg0) {
		
		if(arg0 instanceof UncertWebDataBinding){
			
			UncertWebData theData = ((UncertWebDataBinding)arg0).payload;
			
			if(theData.getUncertaintyType() != null){
				
				LargeBufferStream baos = new LargeBufferStream();
				
				try {
					encoder.encode(theData.getUncertaintyType(), baos);
					
					return baos;
				} catch (UnsupportedUncertaintyTypeException e) {
					e.printStackTrace();
				} catch (UncertaintyEncoderException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		return null;
	}

	@Override
	public Class<?>[] getSupportedInternalInputDataType() {
		Class<?>[] supportedClasses = {UncertWebDataBinding.class};
		return supportedClasses;
	}

	@Override
	public Node generateXML(IData arg0, String arg1) {
		if(arg0 instanceof UncertWebDataBinding){
			
			try{
			
			DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
			
			dbFac.setNamespaceAware(true);
						
//			PipedInputStream pipedin = new PipedInputStream();
//			
//			PipedOutputStream pipedout = new PipedOutputStream(pipedin);
			
			UncertWebData theData = ((UncertWebDataBinding)arg0).payload;
		
			String sb = encoder.encode(theData.getUncertaintyType());
					
			Document d = dbFac.newDocumentBuilder().parse(IOUtils.toInputStream(sb));
					
			return d;
			
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}

}
