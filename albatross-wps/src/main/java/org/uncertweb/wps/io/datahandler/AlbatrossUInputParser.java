package org.uncertweb.wps.io.datahandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.datahandler.parser.AbstractParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.ExtendedXMLParser;
import org.uncertml.statistic.StandardDeviation;
import org.uncertweb.wps.io.data.binding.complex.AlbatrossUInput;
import org.uncertweb.wps.io.data.binding.complex.AlbatrossUInputBinding;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AlbatrossUInputParser extends AbstractParser {

	private static final Logger log = LoggerFactory
			.getLogger(AlbatrossUInputParser.class);

	@Override
	public IData parse(InputStream input, String mimeType, String schema) {

		XmlObject inputObject = null;
		try {
			inputObject = XmlObject.Factory.parse(input);
		} catch (XmlException e1) {
			log.error(e1.getMessage());
			throw new RuntimeException(e1.getMessage());
		} catch (IOException e1) {
			log.error(e1.getMessage());
			throw new RuntimeException(e1.getMessage());
		}

		Node firstChild = inputObject.getDomNode().getChildNodes().item(0);

		List<String> albatrossIDs = new ArrayList<String>();
		Map<String, String> parameters = new HashMap<String, String>();
		StandardDeviation standardDeviation = null;

		if (firstChild.getLocalName().equals("UncertainAlbatrossInput")) {

			NodeList childNodes = firstChild.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);

				String localName = node.getLocalName();

				if (localName != null) {
					if (localName.equals("albatrossID")) {
						albatrossIDs.add(node.getNodeValue());
					} else if (localName.equals("parameter")) {
						/*
						 * value of attribute "name"
						 */
						String nv1 = node.getAttributes().getNamedItem("name")
								.getNodeValue();
						/*
						 * actual string value of the "parameters" node
						 */
						String nv2 = node.getFirstChild().getNodeValue();
						parameters.put(nv1, nv2);

					} else if (localName.equals("StandardDeviation")) {
						ExtendedXMLParser parser = new ExtendedXMLParser();
						try {
							IUncertainty uncertainty = parser
									.parse(nodeToString(node));

							if (uncertainty instanceof StandardDeviation) {
								standardDeviation = (StandardDeviation) uncertainty;
							} else {
								log.info("Uncertainty not a StandardDeviation!!");
							}

						} catch (UncertaintyParserException e) {
							String message = "Error while parsing UncertML input: "
									+ e.getMessage();
							log.error(message);
							throw new RuntimeException(e.getMessage());
						} catch (TransformerFactoryConfigurationError e) {
							log.error(e.getMessage());
							throw new RuntimeException(e.getMessage());
						} catch (TransformerException e) {
							log.error(e.getMessage());
							throw new RuntimeException(e.getMessage());
						}
					}
				}

			}

			AlbatrossUInput result = new AlbatrossUInput(albatrossIDs,
					parameters, standardDeviation);
			return new AlbatrossUInputBinding(result);
		}
		return null;
	}

	private String nodeToString(Node node)
			throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(
				stringWriter));

		return stringWriter.toString();
	}
	
	@Override
	public Class<?>[] getSupportedDataBindings() {
		return new Class<?>[]{AlbatrossUInputBinding.class};
	}
}
