package org.uncertweb.sta.wps.xml.io.enc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.opengis.sos.x10.GetObservationDocument;

import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.uncertweb.sta.wps.xml.binding.GetObservationRequestBinding;
import org.w3c.dom.Node;


/**
 * @author Christian Autermann
 */
public class GetObservationRequestGenerator extends AbstractXMLGenerator implements
		IStreamableGenerator {

	@Override
	public OutputStream generate(IData arg0) {
		LargeBufferStream baos = new LargeBufferStream();
		this.writeToStream(arg0, baos);
		return baos;
	}

	@Override
	public Class<?>[] getSupportedInternalInputDataType() {
		return new Class<?>[] { GetObservationRequestBinding.class };
	}

	@Override
	public Node generateXML(IData data, String arg1) {
		return generateXML(data).getDomNode();
	}

	private GetObservationDocument generateXML(IData data) {
		return ((GetObservationRequestBinding) data).getPayload();
	}

	public void write(IData data, Writer writer) {
		GetObservationDocument xml = generateXML(data);
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

}