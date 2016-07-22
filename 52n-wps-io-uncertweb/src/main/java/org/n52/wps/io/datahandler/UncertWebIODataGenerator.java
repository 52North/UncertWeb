package org.n52.wps.io.datahandler;

import org.n52.wps.FormatDocument.Format;
import org.n52.wps.io.datahandler.netcdf.NetCDFGenerator;
import org.n52.wps.io.datahandler.om.OMGenerator;
import org.n52.wps.io.datahandler.uncertml.UncertMLGenerator;

public class UncertWebIODataGenerator extends DelegatingGenerator {

	public UncertWebIODataGenerator() {
		super(new NetCDFGenerator(), new OMGenerator(), new UncertMLGenerator());
	}

	@Override
	public Format[] getSupportedFullFormats() {
		return null;
	}

}
