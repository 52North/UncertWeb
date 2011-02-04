package org.uncertweb.sta.wps.xml.io.enc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.uncertweb.sta.wps.parameter.ParametersDocument;
import org.uncertweb.sta.wps.parameter.ParametersDocument.Parameters;
import org.uncertweb.sta.wps.parameter.ParametersDocument.Parameters.Parameter;
import org.uncertweb.sta.wps.xml.binding.ParameterBinding;
import org.w3c.dom.Node;


/**
 * @author Christian Autermann
 */
public class ParameterGenerator extends AbstractXMLGenerator implements
		IStreamableGenerator {

	@Override
	public OutputStream generate(IData arg0) {
		LargeBufferStream baos = new LargeBufferStream();
		this.writeToStream(arg0, baos);
		return baos;
	}

	@Override
	public Class<?>[] getSupportedInternalInputDataType() {
		return new Class<?>[] { ParameterBinding.class };
	}

	@Override
	public Node generateXML(IData data, String arg1) {
		return generateXML(data).getDomNode();
	}



	public void write(IData data, Writer writer) {
		ParametersDocument xml = generateXML(data);
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			xml.save(bufferedWriter, new XmlOptions().setSavePrettyPrint());
			bufferedWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeToStream(IData coll, OutputStream os) {
		OutputStreamWriter w = new OutputStreamWriter(os);
		write(coll, w);
	}
	
	public ParametersDocument generateXML(IData data) {
		ParametersDocument doc = ParametersDocument.Factory.newInstance();
		Parameters xbParams = doc.addNewParameters();
		
		for (Map.Entry<String, String> entry : (((ParameterBinding) data).getPayload()).entrySet()) {
			Parameter p = xbParams.addNewParameter();
			p.setKey(entry.getKey());
			p.setValue(entry.getValue());
		}
		return doc;
	}

}