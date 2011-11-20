package org.n52.wps.io.datahandler.om;

import org.n52.wps.io.datahandler.DelegatingGenerator;

public class OMGenerator extends DelegatingGenerator {

	public OMGenerator() {
		super(new OMJsonGenerator(), new OMXmlGenerator());
	}
}
