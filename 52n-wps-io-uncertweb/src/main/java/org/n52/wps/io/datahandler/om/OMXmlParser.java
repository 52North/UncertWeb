package org.n52.wps.io.datahandler.om;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_OMX_XML;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_TEXT_XML;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OM_V1;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OM_V2;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OMU;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OMU_52N;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.datahandler.AbstractUwParser;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.StaxObservationParser;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.utils.UwCollectionUtils;

/**
 * Observation parser, handling observations encoded in XML according the
 * UncertWeb O&M profile; wraps {@link XBObservationParser}
 *
 * @author Kiesow, staschc
 *
 */
public class OMXmlParser extends AbstractUwParser {

	private static Logger log = Logger.getLogger(OMXmlParser.class);
	private StaxObservationParser primaryParser = new StaxObservationParser();
	private XBObservationParser secondaryParser = new XBObservationParser();

	public OMXmlParser() {
		super(
			set(SCHEMA_OM_V2, SCHEMA_OM_V1,SCHEMA_OMU,SCHEMA_OMU_52N),
			set(ENCODING_UTF_8),
			set(MIME_TYPE_OMX_XML, MIME_TYPE_TEXT_XML),
			UwCollectionUtils.<Class<?>>set(OMBinding.class)
		);
	}

	@Override
	public IData parse(InputStream primaryFile, String mimeType) {
		IObservationCollection obsCol = null;
		try {
			obsCol = primaryParser.parseObservationCollection(primaryFile);
		} catch (OMParsingException e) {
			//workaround, as WPS response uses XML Beans and sometimes,
			//there may be ns namespace prefixes that cannot be supported by STAXParser!
			try {
				obsCol = secondaryParser.parseObservationCollection(primaryFile);
			} catch (OMParsingException e1) {
				String message = "Error while parsing Observation input: "+e.getMessage();
				log.info(message);
				throw new RuntimeException(message,e);
			}
		}
		if (obsCol.getObservations().size()==1){
			return new OMBinding(obsCol.getObservations().get(0));
		}
		else {
			return new OMBinding(obsCol);
		}
	}

}
