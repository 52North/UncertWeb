package org.uncertweb.sta.wps.xml.io.enc;

import static org.uncertweb.intamap.utils.Namespace.defaultOptions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationCollectionType;
import net.opengis.om.x10.ObservationDocument;

import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.sta.wps.xml.binding.ObservationCollectionBinding;
import org.w3c.dom.Node;

/**
 * @author Christian Autermann
 */
public class ObservationCollectionGenerator extends AbstractXMLGenerator
		implements IStreamableGenerator {

//	private static final Logger log = LoggerFactory.getLogger(ObservationCollectionGenerator.class);
	
	@Override
	public OutputStream generate(IData arg0) {
		LargeBufferStream baos = new LargeBufferStream();
		this.writeToStream(arg0, baos);
		return baos;
	}

	@Override
	public Class<?>[] getSupportedInternalInputDataType() {
		return new Class<?>[] { ObservationCollectionBinding.class };
	}

	@Override
	public Node generateXML(IData arg0, String arg1) {
		return generateXML(arg0).getDomNode();
	}

	public void write(IData coll, Writer writer) {
		ObservationCollectionDocument xml = generateXML(coll);
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			bufferedWriter
					.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			xml.save(bufferedWriter, defaultOptions());
			bufferedWriter.write("\n");
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

	public ObservationCollectionDocument generateXML(IData om) {
		ObservationCollection oc = ((ObservationCollectionBinding) om).getPayload();
		ObservationCollectionDocument doc = ObservationCollectionDocument.Factory.newInstance();
		ObservationCollectionType xboc = doc.addNewObservationCollection();
		ObservationGenerator og = new ObservationGenerator();
		for (Observation o : oc) {
			ObservationDocument oDoc = og.generateXML(o);
			xboc.addNewMember().addNewObservation().set(oDoc);
		}
		return doc;
	}

}