package org.n52.wps.io.datahandler.uncertml;

import org.n52.wps.io.datahandler.DelegatingGenerator;

public class UncertMLGenerator extends DelegatingGenerator {

	public UncertMLGenerator() {
		super(new UncertMLJsonGenerator(), new UncertMLXmlGenerator());
	}
}
