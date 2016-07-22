package org.n52.wps.io.datahandler.uncertml;

import org.n52.wps.FormatDocument.Format;
import org.n52.wps.io.datahandler.DelegatingParser;

public class UncertMLParser extends DelegatingParser {

	public UncertMLParser() {
		super(new UncertMLJsonParser(), new UncertMLXmlParser());
	}

	@Override
	public Format[] getSupportedFullFormats() {
		return null;
	}

}
