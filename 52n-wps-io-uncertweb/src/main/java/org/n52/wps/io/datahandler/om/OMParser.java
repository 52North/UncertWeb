package org.n52.wps.io.datahandler.om;

import org.n52.wps.io.datahandler.DelegatingParser;

public class OMParser extends DelegatingParser {
	public OMParser() {
		super(new OMXmlParser(), new OMJsonParser());
	}
}